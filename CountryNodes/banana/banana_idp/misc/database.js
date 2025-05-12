const mysql = require("mysql2");
const { OkPacket, RowDataPacket } = require("mysql2");
const { v4: uuidv4 } = require("uuid");
const dotenv = require("dotenv");
dotenv.config();

const maxRetries = 5;
let retryCount = 0;

function createPool() {
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

    return pool;
}

let pool = createPool();

function handlePoolError() {
    if (retryCount < maxRetries) {
        retryCount++;

        setTimeout(() => {
            console.log(`[SQL] Connection failed, retrying... (${retryCount}/${maxRetries})`);
            pool = createPool(); // Recreate the pool
        }, 10000); // Retry after 10 seconds
    } else {
        console.error("[SQL] Max retries reached. Failed to connect to the database.");
        process.exit(1); // Exit the application or handle it differently
    }
}

pool.on('error', (err) => {
    console.error("[SQL] Pool error:", err);
    handlePoolError();
});

function query(query, values) {
    const queryId = uuidv4();
    console.log(`[SQL] (${queryId}): ${query} => ${values}`);
    return new Promise((resolve, reject) => {
        const getConnection = () => {
            pool.getConnection((err, connection) => {
                if (err) {
                    console.error("[SQL] Error getting connection from pool:", err);
                    handlePoolError();
                    if (retryCount >= maxRetries) {
                        reject(err);
                    } else {
                        setTimeout(getConnection, 10000); // Retry getting connection after 10 seconds
                    }
                    return;
                }
                connection.execute(query, values, (err, results) => {
                    connection.release();
                    if (err) {
                        reject(err);
                    } else {
                        console.log(`[SQL] (${queryId}): ` + JSON.stringify(results));
                        resolve(results);
                    }
                });
            });
        };

        getConnection();
    });
}

query("SHOW GLOBAL STATUS LIKE 'Uptime';").then((results) => {
    console.log(`[SQL] Server uptime: ${results[0].Value}`);
}).catch(err => {
    console.error("[SQL] Error fetching uptime:", err);
    handlePoolError();
});

module.exports = { query, OkPacket, RowDataPacket };