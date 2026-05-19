const { createSession } = require('../src/utils/activity');
const db = require('../src/db');
const { mapDbRoleToApi } = require('../src/utils/roles');

(async ()=>{
  try {
    // find test1 id
    const r = await db.query('SELECT id FROM usuarios WHERE username = $1 LIMIT 1', ['test1']);
    if (r.rowCount === 0) throw new Error('test1 not found');
    const id = r.rows[0].id;
    const session = await createSession(id, { ipAddress: '127.0.0.1', userAgent: 'script' });
    console.log('created session', session);
    const q = `SELECT s.id as session_id, s.token_sesion, s.usuario_id, u.username, u.email, u.rol FROM sesiones s JOIN usuarios u ON u.id = s.usuario_id WHERE s.token_sesion = $1 LIMIT 1`;
    const me = await db.query(q, [session.token_sesion]);
    if (me.rowCount > 0) {
      const row = { ...me.rows[0], rol: mapDbRoleToApi(me.rows[0].rol) };
      console.log('me lookup', JSON.stringify(row, null, 2));
    } else {
      console.log('me lookup: no row');
    }
  } catch (err) {
    console.error('Error', err.message || err);
    process.exit(2);
  }
})();
