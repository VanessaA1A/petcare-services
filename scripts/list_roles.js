const db = require('../src/db');
(async () => {
  try {
    const r = await db.query(`SELECT e.enumlabel FROM pg_type t JOIN pg_enum e ON t.oid = e.enumtypid WHERE t.typname = 'rol_usuario' ORDER BY e.enumsortorder`);
    console.log('roles:', JSON.stringify(r.rows.map(r => r.enumlabel), null, 2));
  } catch (err) {
    console.error('error querying enum roles:', err && err.stack ? err.stack : err);
  } finally {
    process.exit(0);
  }
})();
