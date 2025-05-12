const express = require("express");

const router = express.Router();
const controller = require("../controller/sso");

router.post("*", (req, res, next) => {
   // whitelist localhost
    if (req.host === "localhost") {
        return next();
    }
    //check captcha
  const captcha = req.body["g-recaptcha-response"];
  if ( typeof captcha === "undefined" || captcha === "") {
    return res.status(403);
  }
  const params = new URLSearchParams();
  params.append("secret", process.env.CAPTCHA_SECRET_KEY);
  params.append("response", captcha);
  // Verify URL
  const query = `https://api.hcaptcha.com/siteverify`;
  // Make a request to verifyURL
  fetch(query, { method: "POST", body: params })
    .then((response) => response.json())
    .then((body) => {
      // If not successful
      console.log(`[Captcha]: [IP: ${req.ip} | Status: ${body.success} | Score: ${body.score} | Error-codes: ${body["error-codes"]}]`)
      if (body.success !== undefined && !body.success) {
        return res.status(400).json({ message: "Failed captcha verification" });
      }
      //If successful
      return next();
    })
    .catch((err) => {
      console.error(err);
      return res.status(500).json({ message: "Captcha error" });
    });
});

router
  .route("/login")
  .get(controller.login)
  .post(controller.doLogin);

router.route("/register")
  .get(controller.register)
  .post(controller.doRegister);

router.post("/verifytoken", (req, res) => {
  res.redirect(308, "/oauth2/token?" + req._parsedUrl.query);
});

router.get("/verifyDID", controller.verifyDID);

router.get("/logout", controller.logout);

module.exports = router;
