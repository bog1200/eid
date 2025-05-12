const jwt = require("jsonwebtoken");
const { publicCert, privateCert } = require("../config").keys;

const ISSUER = "romail-sso";

const genJwtToken = payload =>
  new Promise((resolve, reject) => {
    // some of the libraries and libraries written in other language,
    // expect base64 encoded secrets, so sign using the base64 to make
    // jwt useable across all platform and langauage.
    jwt.sign(
      { ...payload },
      privateCert,
      {
        algorithm: "RS256",
        expiresIn: "1h",
        issuer: ISSUER
      },
      (err, token) => {
        if (err) return reject(err);
        return resolve(token);
      }
    );
  });

const verifyJwtToken = token =>
  new Promise((resolve, reject) => {
    jwt.verify(token, publicCert, { issuer: ISSUER }, (err, decoded) => {
      if (err) return reject(err);
      return resolve(decoded);
    });
  });


module.exports = Object.assign({}, { genJwtToken, verifyJwtToken });
