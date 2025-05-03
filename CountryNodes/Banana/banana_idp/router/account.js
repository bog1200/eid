const express = require("express");
const router = express.Router();
const controller = require("../controller/account");
const passkeys = require("../controller/passkeys");

router.get("/", async (req, res, next) => {
    console.log(req.session);
    if (req.session.user == null) {
        return res.redirect("/sso/login");
    }
    const account = req.session.account;
    let passkeys_count = 0;
    await fetch(`${process.env.SERVER_URL}/passkeys/count?userId=${account.uuid}`).then((response) => response.json()).then((data) => {
        passkeys_count = data.count;
    });
    console.log(account);
    res.render("account", {
        error: req.query.error,
        success: req.query.message,
        modify: req.query.modify,
        photo: req.session.photo,
        user: account,
        support_id: account.uuid.substring(24),
        passkeys_count: passkeys_count,
        title: "Banana IDP | Home",
    });
});

router.get("/history", controller.getHistory);
//router.get("/apps", controller.getApps);
router.get("/passkeys", controller.getPasskeys);

router.post("/change_password", controller.changePassword);
//router.post("/change_email", controller.changeEmail);
// router.post("/change_name", controller.changeName);
router.post("/change_username", controller.changeUsername);


module.exports = router;
