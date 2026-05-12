const db = require('../src/db');
(async () => {
  try {
    const r = await db.query(`SELECT e.enumlabel FROM pg_type t JOIN pg_enum e ON t.oid = e.enumtypid WHERE t.typname = 'rol_usuario' ORDER BY e.enumsortorder`);
    const roles = r.rows.map(x => x.enumlabel);
    console.log('current roles:', roles);
    if (!roles.includes('cliente')) {
      console.log('Adding role cliente to enum rol_usuario...');
      await db.query(`ALTER TYPE rol_usuario ADD VALUE 'cliente'`);
      console.log('Added cliente');
    } else {
      console.log('cliente already present');
    }
  } catch (err) {
    console.error('error altering enum:', err && err.stack ? err.stack : err);
  } finally {
    process.exit(0);
  }
})();
