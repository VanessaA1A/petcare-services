const db = require('../src/db');
const { mapDbRoleToApi } = require('../src/utils/roles');
(async () => {
  try {
    const r = await db.query("SELECT id, username, email, rol, created_at FROM usuarios WHERE username LIKE 'mobile_%' ORDER BY id DESC LIMIT 20");
    const rows = r.rows.map(rr => ({ ...rr, rol: mapDbRoleToApi(rr.rol) }));
    console.log('found', r.rowCount, 'rows');
    console.log(JSON.stringify(rows, null, 2));

    if (r.rowCount > 0) {
      const uid = r.rows[0].id;
      const s = await db.query('SELECT id, token_sesion, fecha_inicio, fecha_fin FROM sesiones WHERE usuario_id = $1 ORDER BY id DESC LIMIT 10', [uid]);
      console.log('sesiones for', uid, JSON.stringify(s.rows, null, 2));
      const a = await db.query('SELECT id, tipo_actividad, descripcion, fecha_hora, ip_address FROM actividades WHERE usuario_id = $1 ORDER BY id DESC LIMIT 20', [uid]);
      console.log('actividades for', uid, JSON.stringify(a.rows, null, 2));
    }
  } catch (err) {
    console.error('err', err && err.stack ? err.stack : err);
  } finally {
    process.exit(0);
  }
})();
