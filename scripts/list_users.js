const db = require('../src/db');
const { mapDbRoleToApi } = require('../src/utils/roles');

(async () => {
  try {
    const res = await db.query('SELECT id, username, email, rol, created_at FROM usuarios ORDER BY id DESC LIMIT 50');
    const rows = res.rows.map(r => ({ ...r, rol: mapDbRoleToApi(r.rol) }));
    console.log(JSON.stringify(rows, null, 2));
  } catch (err) {
    console.error('DB error:', err.message || err);
    process.exit(2);
  }
  process.exit(0);
})();
