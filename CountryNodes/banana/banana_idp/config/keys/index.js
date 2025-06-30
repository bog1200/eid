const fs = require("fs");
const path = require("path");

const privateKeyFilePath =
  process.env.JWT_SSO_PRIVATE_KEY_FILE ||
  path.resolve(__dirname, "./jwtPrivate.key");

const privateCert = fs.readFileSync(privateKeyFilePath);

const publicKeyFilePath =
  process.env.JWT_SSO_PUBLIC_KEY_FILE ||
  path.resolve(__dirname, "./jwtPublic.key");

const publicCert = fs.readFileSync(publicKeyFilePath);

const jwtValidatityKey = "banana-sso-jwt-validatity";

module.exports = Object.assign(
  {},
  {
    privateCert,
    publicCert,

    jwtValidatityKey
  }
);
