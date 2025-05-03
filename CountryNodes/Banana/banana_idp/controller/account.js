const { off } = require('../app');
const db = require('../misc/database');
const { logAction, logTypes } = require('../misc/log');

const getHistory = async (req, res) => {
    const { user } = req.session;
    const offset = req.query.offset || 0;
    if (user == null) {
        return res.redirect('/sso/login');
    };
    const history = await db.query('SELECT ip, action, data, timestamp FROM audit_log WHERE uuid = ? ORDER BY id DESC LIMIT 10 OFFSET ? ', [user, offset]);
    // replace history.action with string
    for (let i = 0; i < history.length; i++) {
        // get action string from logTypes
        history[i].action = Object.keys(logTypes).find(key => logTypes[key] === history[i].action);
    }

    res.render('accountHistory', {
        history: history,
        offset: offset,
        title: 'Banana IDP | History',
        user: req.session.account,
        support_id: req.session.account.uuid.substring(24),
    });
}

const getApps = async (req, res) => {
    const offset = req.query.offset || 0;
    const { user } = req.session;
    if (user == null) {
        return res.redirect('/sso/login');
    };
    const apps = await db.query('SELECT * FROM clients WHERE owner = ? ORDER BY name ASC LIMIT 5 OFFSET ? ', [user, offset]);
    res.render('accountApps', {
        apps: apps,
        title: 'Banana IDP | Apps',
        offset: offset,
        user: req.session.account,
        support_id: req.session.account.uuid.substring(24),
    });
}

const getPasskeys = async (req, res) => {
    const { user } = req.session;
    if (user == null) {
        return res.redirect('/sso/login');
    };
    res.render('accountPasskeys', {
        title: 'Banana IDP | Passkeys',
        user: req.session.account,
        support_id: req.session.account.uuid.substring(24),
    });
}


const changeUsername = async (req, res) => {
    const { username } = req.body;
    const { user } = req.session;
    if (username.match(/[a-zA-z0-9]{4,32}/) == null) {
        return res.redirect('/account?error=Invalid username');
    }

    const checkUsername = await db.query('SELECT uuid FROM users WHERE username = ?', [username]);
    if (checkUsername.length > 0) {
        return res.redirect('/account?error=Username already exists');
    }
    if (req.session.username != "demo") {
        db.query('UPDATE users SET username = ? WHERE uuid = ?', [username, user]);
    }
    logAction(logTypes.ACCOUNT_USERNAME_CHANGED, user, req.headers["x-real-ip"] || req.connection.remoteAddress, JSON.stringify({ old: req.session.username, new: username }));
    res.redirect('/account?message=Username changed successfully! Please re-login');
}

const changePassword = async (req, res) => {
    const { new_password, new_password_confirm } = req.body;
    const { user } = req.session;
    if (new_password != new_password_confirm) {
        return res.redirect('/account?error=Passwords do not match');
    }
    if (new_password.length < 8) {
        return res.redirect('/account?error=Password must be at least 8 characters long');
    }
    if (req.session.username != "demo") {
        db.query('UPDATE users SET password = ? WHERE uuid = ?', [new_password, user]);
    }
    logAction(logTypes.ACCOUNT_PASS_CHANGED, user, req.headers["x-real-ip"] || req.connection.remoteAddress);
    res.redirect('/account?message=Password changed successfully! Please re-login');
}


module.exports = Object.assign({}, { getHistory, getPasskeys, changeUsername, changePassword });