const mysql = require("mysql2");
const { OkPacket, RowDataPacket } = require("mysql2");
const { v4: uuidv4 } = require("uuid");
const dotenv = require("dotenv");
dotenv.config();

console.log("[SQL] Connecting to MySQL server...");
console.log(`[SQL] Host: ${process.env.MYSQL_HOST}`);
console.log(`[SQL] Database: ${process.env.MYSQL_DB}`);

const pool = mysql.createPool({
    connectionLimit: 10,
    host: process.env.MYSQL_HOST,
    database: process.env.MYSQL_DB,
    user: process.env.MYSQL_USER,
    password: process.env.MYSQL_PASSWORD,
    port: parseInt(process.env.MYSQL_PORT || '3306')
});

pool.on('connection', (connection) => {
    console.log('[SQL] DB connected');
});



function query(query, values) {
    const queryId = uuidv4();
    console.log(`[SQL] (${queryId}): ${query} => ${values}`);
    return new Promise((resolve, reject) => {
        pool.getConnection((err, connection) => {
            if (err) {
                if (connection) connection.release();
                reject(err);
            } else {
                connection.execute(query, values, (err, results) => {
                    connection.release();
                    if (err) {
                        reject(err);
                    } else {
                        console.log(`[SQL] (${queryId}): ` + JSON.stringify(results));
                        resolve(results);
                    }
                });
            }
        });
    });
}

query("SHOW GLOBAL STATUS LIKE 'Uptime';").then((results) => {
    console.log(`[SQL] Server uptime: ${results[0].Value}`);
});







module.exports = { query, OkPacket, RowDataPacket };

