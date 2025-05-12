const { v4: uuidv4 } = require("uuid");
const Hashids = require("hashids");
const URL = require("url").URL;
const hashids = new Hashids();
const crypto = require('crypto');
const { genJwtToken, verifyJwtToken } = require("./jwt_helper");
const { logAction, logTypes } = require("../misc/log");
const db = require("../misc/database");
const bcrypt = require("bcrypt");
const md5 = require('md5');
const re = /(\S+)\s+(\S+)/;

// Note: express http converts all headers
// to lower case.

const AUTH_HEADER = "authorization";
const BEARER_AUTH_SCHEME = "bearer";

function parseAuthHeader(hdrValue) {
  if (typeof hdrValue !== "string") {
    return null;
  }
  const matches = hdrValue.match(re);
  return matches && { scheme: matches[1], value: matches[2] };
}

const fromAuthHeaderWithScheme = function (authScheme) {
  const authSchemeLower = authScheme.toLowerCase();
  return function (request) {
    let token = null;
    if (request.headers[AUTH_HEADER]) {
      const authParams = parseAuthHeader(request.headers[AUTH_HEADER]);
      if (authParams && authSchemeLower === authParams.scheme.toLowerCase()) {
        token = authParams.value;
      }
    }
    return token;
  };
};

const fromAuthHeaderAsBearerToken = function () {
  return fromAuthHeaderWithScheme(BEARER_AUTH_SCHEME);
};

// const appTokenFromRequest = fromAuthHeaderAsBearerToken();


const deHyphenatedUUID = () => uuidv4().replace(/-/gi, "");
const encodedId = () => hashids.encodeHex(deHyphenatedUUID());

// A temporary cahce to store all the application that has login using the current session.
// It can be useful for variuos audit purpose
const sessionUser = {};
const sessionApp = {};
const sessionVerifiers = {};


// these token are for the validation purpose
const intrmTokenCache = {};

const fillIntrmTokenCache = (origin, id, intrmToken) => {
  intrmTokenCache[intrmToken] = [id, [origin]];
};

const storeApplicationInCache = async (origin, id, intrmToken) => {
  if (sessionApp[id] == null) {
    sessionApp[id] = {
      [origin]: true
    };
    fillIntrmTokenCache(origin, id, intrmToken);
  } else {
    sessionApp[id][[origin]] = true;
    fillIntrmTokenCache(origin, id, intrmToken);
  }
  //console.log({ ...sessionApp }, { ...sessionUser }, { intrmTokenCache });
};

const generatePayload = async (ssoToken) => {
  const globalSessionToken = intrmTokenCache[ssoToken][0];
  const appName = intrmTokenCache[ssoToken][1];
  const userEmail = sessionUser[globalSessionToken];
  let user = await db.query("SELECT * FROM users WHERE username = ?", [userEmail]);

  user = user[0];
  //const appPolicy = user.appPolicy[appName];
  //const email = appPolicy.shareEmail === true ? userEmail : undefined;
  // const email = userEmail;
  return {
    sub: user.uuid,
    scope: appName,
    pin: user.pin,
    name: `${user.first_name} ${user.last_name}`,
    given_name: user.first_name,
    family_name: user.last_name,
    username: user.username,
    email: user.email,
    gender: user.gender,
    address: user.address,
    dob: user.dob,
    age: user.age,

    // email,
    // global SessionID for the logout functionality.
    sid: globalSessionToken
  };
};

const verifySsoToken = async (req, res, next) => {
  console.log("SID: ", req.session.id);
  // const appToken = appTokenFromRequest(req);
  let code_verifier_passed = false;
  let { code, client_id, client_secret, code_verifier } = req.body;
  let auth_header = fromAuthHeaderWithScheme("basic")(req)
  if (auth_header){
    let buff = Buffer.from(auth_header,"base64");
    auth_header = buff.toString("ascii");
    auth_header = auth_header.split(':');
    client_id = auth_header[0];
    client_secret = auth_header[1];

  }
  else if (code_verifier){
    const code_challenge = sessionVerifiers[code];
    const verifier = code_verifier;
    const hash = crypto.createHash('sha256').update(verifier).digest("base64url");
    if (hash !== code_challenge) {
      return res.status(400).json({ message: "badRequest" });
    }
    console.log("code_verifier is correct");
    code_verifier_passed = true;


  }


  // if the application token is not present or code request is invalid
  // if the code is not present in the cache some is
  // smart.
  else if (
    client_secret == undefined ||
    code == undefined ||
    intrmTokenCache[code] == undefined
  ) {
    return res.status(400).json({ message: "badRequest" });
  }

  // if the appToken is present and check if it's valid for the application
  const appName = intrmTokenCache[code][1];
  const globalSessionToken = intrmTokenCache[code][0];
  const client_secret_db = await db.query("SELECT client_secret FROM clients WHERE client_id = ?", [client_id]);
  // If the appToken is not equal to token given during the sso app registraion or later stage than invalid
  if (
    code_verifier_passed == false &&
    client_secret !== client_secret_db[0].client_secret ||
    sessionApp[globalSessionToken][appName] !== true
  ) {
    return res.status(403).json({ message: "Unauthorized" });
  }
  // checking if the token passed has been generated
  const payload = await generatePayload(code);

  const access_token = await genJwtToken(payload);
  // delete the itremCache key for no futher use,
  delete intrmTokenCache[code];
  const token_type = "Bearer";
  return res.status(200).json({ token_type, access_token });
};
const doLogin = async (req, res, next) => {
  console.log("SID: ", req.session.id);
  const { username, password } = req.body;
  const { client_id, redirect_uri, code_challenge, login_hint } = req.query;
  if (username === undefined || password === undefined) {
    if (login_hint !== undefined) {
      return res.redirect(`/sso/login?response_type=${req.query.response_type}&redirect_uri=${redirect_uri}&client_id=${client_id}&state=${req.query.state}&code_challenge=${code_challenge}&login_hint=${login_hint}`);
    }
    return res.redirect(`/sso/login?response_type=${req.query.response_type}&redirect_uri=${redirect_uri}&client_id=${client_id}&state=${req.query.state}&code_challenge=${code_challenge}`);
  }


  if (client_id != null && redirect_uri != null) {

    const url = new URL(redirect_uri);
    const app = await db.query("SELECT name FROM clients WHERE client_id = ? AND client_url = ?", [client_id, url.href]);
    if (app.length == 0) {
      return res.redirect(`/sso/login?response_type=${req.query.response_type}&redirect_uri=${redirect_uri}&client_id=${client_id}`);
    }
  }
  let user = await db.query("SELECT uuid,password FROM users WHERE username = ?", [username]);
  if (user.length == 0 || !bcrypt.compareSync(password, user[0].password)) {
    logAction(logTypes.ACCOUNT_SIGNIN_FAIL, user[0].uuid, req.headers["x-real-ip"] || req.connection.remoteAddress, client_id);
    if (req.originalUrl.includes("?error=invalid_credentials")) {
      return res.redirect(`${req.originalUrl}`);
    }
    else {
      return res.redirect(`${req.originalUrl}?error=invalid_credentials`);
    }
  }

  let full_account = await db.query("SELECT * FROM users WHERE username = ?", [username]);
  full_account = full_account[0];
  // remove the password from the account
  delete full_account.password;
  const id = user[0].uuid;
  req.session.account = full_account;
  req.session.user = id;
  req.session.username = username;
  req.session.email = full_account.email;
  req.session.photo = `https://www.gravatar.com/avatar/${md5(full_account.email)}`;
  sessionUser[id] = username;
  logAction(logTypes.ACCOUNT_SIGNIN, id, req.headers["x-real-ip"] || req.connection.remoteAddress, client_id);
  if (redirect_uri == null) {
    return res.redirect("/");
  }
  const intrmid = encodedId();
  storeApplicationInCache(client_id, id, intrmid);
  if (code_challenge != null) {
    sessionVerifiers[intrmid] = code_challenge;
  }
  return res.type("application/json")
    .redirect(`${redirect_uri}?code=${intrmid}&state=${req.query.state}`);
};

const verifyPasskey = async (req, res, next) => {
  // Fetch the authentication token from your frontend.
const token = { token: req.query.token };
const { client_id, redirect_uri, code_challenge } = req.query;

// POST the authentication token to the Passwordless.dev API using your API private secret.
const apiUrl = 'https://v4.passwordless.dev';
const response = await fetch(apiUrl + '/signin/verify', {
method: 'POST',
body: JSON.stringify(token),
headers: {
  'ApiSecret': process.env.PASSWORDLESS_SECRET,
  'Content-Type': 'application/json'
}
});

// Cache the API response (see below) to a variable.
const body = await response.json();

// Check the API response for successful verification.
// To see all properties returned by this endpoint, checkout the Backend API Reference for /signin/verify.
if (body.success) {
console.log('Successfully verified sign-in for user.', body);
let full_account = await db.query("SELECT * FROM users WHERE uuid = ?", [body.userId]);
full_account = full_account[0];
// remove the password from the account
delete full_account.password;
const id = body.userId;
req.session.account = full_account;
req.session.user = id;
req.session.username = full_account.username;
req.session.email = full_account.email;
req.session.photo = `https://www.gravatar.com/avatar/${md5(full_account.email)}`;
sessionUser[id] = full_account.username;
logAction(logTypes.ACCOUNT_PASSKEY_SIGNIN, id, req.headers["x-real-ip"] || req.connection.remoteAddress, client_id);
if (redirect_uri == null) {
  return res.redirect("/");
}
const intrmid = encodedId();
storeApplicationInCache(client_id, id, intrmid);
if (code_challenge != null) {
  sessionVerifiers[intrmid] = code_challenge;
}
return res.type("application/json")
  .redirect(`${redirect_uri}?code=${intrmid}&state=${req.query.state}`);
} else {
console.warn('Sign in failed.', body);
}
}

const register = (req, res, next) => {
  if (req.query.build == "20230802") {
    return res.render("register-new", {
      title: "Banana IDP | Register"
    });
  }
  return res.render("register", {
    title: "Banana IDP | Register"
  });
};

const doRegister = async (req, res, next) => {
  // do the validation with email and password
  // return res.redirect("/sso/register");
  const { username, password, confirm_password, fname, lname, email, invite_code } = req.body;
  if (username == undefined || password == undefined || confirm_password == undefined || fname == undefined || lname == undefined || email == undefined || invite_code == undefined || invite_code != process.env.INVITE_CODE) {
    return res.redirect(400, "/sso/register?error=invalid_request");
  }
  if (password !== confirm_password) {
    return res.redirect(400, "/sso/register?error=password_mismatch");
  };
  let user = await db.query("SELECT * FROM users WHERE username = ?", [username]);
  if (user.length > 0) {
    return res.redirect(400, "/sso/register?error=user_already_exists");
  }
  const mailRegex = new RegExp("([!#-'*+/-9=?A-Z^-~-]+(\.[!#-'*+/-9=?A-Z^-~-]+)*|\"\(\[\]!#-[^-~ \t]|(\\[\t -~]))+\")@([!#-'*+/-9=?A-Z^-~-]+(\.[!#-'*+/-9=?A-Z^-~-]+)*|\[[\t -Z^-~]*])");
  const validateMail = email.match(mailRegex);
  if (validateMail == null) {
    return res.redirect(400, "/sso/register?error=invalid_email");
  }
  const uuid = uuidv4();
  const hashedPassword = bcrypt.hashSync(password, 10);
  const addUser = await db.query("INSERT INTO users (uuid, username, password, first_name, last_name, email) VALUES (?, ?, ?, ?, ?, ?)", [uuid, username, hashedPassword, fname, lname, email]);
  if (addUser.affectedRows > 0) {
    return res.redirect(201, "/sso/login");
  }
  return res.redirect(500, "/sso/register?error=unknown_error");
};

const login = async (req, res, next) => {
  // The req.query will have the redirect url where we need to redirect after successful
  // login and with sso token.
  // This can also be used to verify the origin from where the request has came in
  // for the redirection
  const { client_id, redirect_uri, login_hint } = req.query;
  let photo = null;
  if (req.session.user != null) {
    photo = req.session.photo;
  }
  let originDB = null;
  let originName = "Account Panel";
  //sanity check for the client_id  and redirect_uri
  // direct access will give the error inside new URL.
  if (redirect_uri != null) {
    const url = new URL(redirect_uri);
    originDB = await db.query("SELECT name FROM clients WHERE client_id = ? AND client_url = ?", [client_id, url.href]);
    if (originDB.length == 0) {
      return res
        .status(400)
        .json({ message: "Your are not allowed to access the Banana IDP" });
    }
    originName = originDB[0].name;

  }
  const logoName = client_id ? client_id.includes("romailapp") ? originName : "" : "";
  if (req.session.user != null && redirect_uri == null) {
    return res.redirect("/");
  }
  const { confirm } = req.query;
  // if global session already has the user directly redirect with the token
  if (req.session.user != null && redirect_uri != null && confirm != null) {
    const intrmid = encodedId();
    storeApplicationInCache(client_id, req.session.user, intrmid);
    logAction(logTypes.ACCOUNT_SIGNIN, req.session.user, req.headers["x-real-ip"] || req.connection.remoteAddress, client_id);
    return res.type("application/json")
    .redirect(`${redirect_uri}?code=${intrmid}&state=${req.query.state}`);
  }


  return res.render("login", {
    title: "Banana IDP | Login",
    client_name: originName,
    logo_name: logoName,
    login_hint: login_hint,
    error: req.query.error,
    loggedIn: req.session.user != null,
    user: req.session.user,
    username: req.session.username,
    photo: photo
  });
};

const logout = (req, res, next) => {
  const globalSessionID = req.sid;
  // delete the session from the sessionUser
  delete sessionUser[globalSessionID];
  // delete the session from the sessionApp
  delete sessionApp[globalSessionID];
  // delete the session from the intrmTokenCache
  Object.keys(intrmTokenCache).forEach(key => {
    if (intrmTokenCache[key][0] === globalSessionID) {
      delete intrmTokenCache[key];
    }
  });
  // delete the session from the session
  req.session.destroy(err => {
    if (err) {
      return res.status(500).json({ message: "Internal Server Error" });
    }
    const url = new URL(process.env.SERVER_URL + req.originalUrl);
    return res.redirect("/sso/login" + url.search);
  });
  // redirect to the login page

};

const getUserInfo = async (req, res, next) => {
  const access_token = fromAuthHeaderAsBearerToken()(req);
  if (access_token == null) {
    return res.status(401).json({ message: "Unauthorized" });
  }
  let userInfo = await verifyJwtToken(access_token);
  if (userInfo == null) {
    return res.status(403).json({ message: "Invalid Token" });
  };
  res.status(200).json(userInfo);
}

const verifyDID = async (req, res, next) => {
  const { did } = req.query;
  if (did === undefined) {
    return res.status(400).json({});
  }
  let user = await db.query("SELECT * FROM users WHERE username = ?", [did]);
  if (user.length === 0) {
    return res.status(404).json({});
  }
  return res.status(204).json({});
};





module.exports = Object.assign({}, { doLogin, login, register, doRegister, logout, getUserInfo, verifySsoToken, verifyPasskey, verifyDID });
