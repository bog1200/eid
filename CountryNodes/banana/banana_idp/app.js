const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const app = express();
app.use(cors());
const engine = require("ejs-mate");
const session = require("express-session");
const account_router = require("./router/account");

const sso_router = require("./router/sso");
const oauth2router = require("./router/oauth2");
const dotenv = require("dotenv");
const passkey_router = require("./router/passkeys");
dotenv.config();

app.use((req, res, next) => {
  //console.log(req.session);
  res.setHeader("Access-Control-Allow-Origin", "*");
  next();
});
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

app.use((req, res, next) => {
  const originalSetHeader = res.setHeader;

  // Override setHeader method to strip charset from Content-Type
  res.setHeader = function (name, value) {
      if (name.toLowerCase() === 'content-type' && typeof value === 'string') {
          // Remove charset from Content-Type
          value = value.replace(/;\s*charset=utf-8/i, '');
      }
      originalSetHeader.call(this, name, value);
  };

  next();
});

app.use(morgan("dev"));
app.engine("ejs", engine);
app.set("views", __dirname + "/views");
app.set("view engine", "ejs");
app.use("/static", express.static(__dirname + "/static"));
app.use("/.well-known", (req, res, next) => {
  res.setHeader("Content-Type", "application/json");
  next();
});
app.set('trust proxy', 1) 
app.use(session({
  secret: '1234567890123456789012345678909876543210987654321',
  resave: false,
  saveUninitialized: true,
  proxy: true,
  cookie: { secure: true, maxAge: 10800000 }
}))

app.use("/.well-known", express.static(__dirname + "/.well-known"));


app.use("/sso", sso_router);
app.use("/oauth2", oauth2router);
app.use("/account", account_router);
app.use("/passkeys", passkey_router);


app.use((req, res, next) => {
  console.log("SS:"+req.session);
  next();
});

app.get("/", (req, res) => {
  res.redirect("/account");
});

app.use((req, res, next) => {
  // catch 404 and forward to error handler
  const err = new Error("Resource Not Found");
  err.status = 404;
  next(err);
});

app.use((err, req, res, next) => {
  console.error({
    message: err.message,
    error: err,
  });
  const statusCode = err.status || 500;
  let message = err.message || "Internal Server Error";

  if (statusCode === 500) {
    message = "Internal Server Error";
  }
  res.status(statusCode).json({ message });
});



module.exports = app;
