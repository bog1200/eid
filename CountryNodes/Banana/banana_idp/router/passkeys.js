const express = require("express");

const router = express.Router();
const controller = require("../controller/passkeys");
const sso_controller = require("../controller/sso");

router.get("/create", controller.createPasskey);
router.get("/list", controller.viewPasskey);
router.get("/count", controller.countPasskeys);
router.get("/verify", sso_controller.verifyPasskey);

module.exports = router;
