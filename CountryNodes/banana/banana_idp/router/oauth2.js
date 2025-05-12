const express = require("express");

const router = express.Router();
const controller = require("../controller/sso");

router.get("/authorize", controller.doLogin);
router.post("/token", controller.verifySsoToken);
router.get("/userinfo", controller.getUserInfo);


module.exports = router;