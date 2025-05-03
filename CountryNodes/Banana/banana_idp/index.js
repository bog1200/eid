const app = require("./app");
const https = require("https");
const dotenv = require("dotenv");
dotenv.config();

const fs = require('fs');

const privateKey = fs.readFileSync('ssl/idp.banana.key');
const certificate = fs.readFileSync('ssl/idp.banana.crt');
const ca = fs.readFileSync('ssl/idp.banana-chain.crt');
const credentials = { key: privateKey, cert: certificate, ca: ca };
const httpsServer = https.createServer(credentials,app);
httpsServer.listen(process.env.SERVER_PORT, () => {
  console.info(`Banana IDP listening on port ${process.env.SERVER_PORT}`);
});
