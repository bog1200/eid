const db = require("../misc/database");

const logTypes = {
    ACCOUNT_SIGNIN: 2,
    ACCOUNT_PASSKEY_CREATED: 9,
    ACCOUNT_PASSKEY_SIGNIN: 8,
    ACCOUNT_SIGNIN_FAIL: 6,
    ACCOUNT_CREATED: 1,
    ACCOUNT_PASS_CHANGED: 3,
    ACCOUNT_EMAIL_CHANGED: 4,
    ACCOUNT_NAME_CHANGED: 5,
    ACCOUNT_USERNAME_CHANGED: 7,
}




const logAction = async (type, user_id, ip, data = null) => {
    //check if user exists in db
    const user = db.query('SELECT uuid FROM users WHERE uuid = ?', [user_id]);
    if (user.length == 0) {
        return false;
    }
    //insert log
    if (data == null) {
        const result = await db.query('INSERT INTO audit_log (uuid, ip, action) VALUES (?, ?, ?)', [user_id, ip, type]);
    } else {
        const result = await db.query('INSERT INTO audit_log (uuid, ip, action, data) VALUES (?, ?, ?, ?)', [user_id, ip, type, data]);
    }
}


module.exports = { logAction, logTypes };




