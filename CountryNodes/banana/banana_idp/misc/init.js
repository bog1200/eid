function initializeDatabase() {
    const { query } = require('./database');
    const fs = require('fs');

    query("SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'banana_db'")
        .then(result => {
            const count = result[0].count;

            if (count === 0) {
                console.log("[SQL] Database is empty. Loading SQL file...");
                fs.readFile('banana.sql',"utf8", (err, sql) => {
                    if (err) {
                        console.error("[SQL] Error reading SQL file:", err);
                        return;
                    }

                    // Remove any leading characters/BOM from the SQL content
                    // sql = sql.replace(/^\uFEFF/, '');

                    const queries = sql.split(';').filter(query => query.trim() !== '');

                    let promiseChain = Promise.resolve();
                    queries.forEach(sqlQuery => {
                        console.log("[SQL Init]"+sqlQuery)
                        promiseChain = promiseChain.then(() => query(sqlQuery));
                    });

                    promiseChain.then(() => {
                        console.log("[SQL] Database initialized from SQL file.");
                    }).catch(error => {
                        console.error("[SQL] Error initializing database:", error);
                    });
                });
            } else {
                console.log("[SQL] Database is not empty. Skipping initialization.");
            }
        })
        .catch(error => {
            console.error("[SQL] Error checking database:", error);
        });
}

module.exports = { initializeDatabase };