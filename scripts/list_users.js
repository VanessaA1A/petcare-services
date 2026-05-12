const db = require('../src/db');

(async () => {
  try {
    const res = await db.query('SELECT id, username, email, rol, created_at FROM usuarios ORDER BY id DESC LIMIT 50');
    console.log(JSON.stringify(res.rows, null, 2));
  } catch (err) {
    console.error('DB error:', err.message || err);
    process.exit(2);
  }
  process.exit(0);
})();
