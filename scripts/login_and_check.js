const http = require('http');
const db = require('../src/db');

const USERNAME = process.argv[2] || 'test1';
const PASSWORD = process.argv[3] || 'Pass1234';
const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;

function postLogin(username, password) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify({ username, password });
    const req = http.request({ hostname: 'localhost', port: PORT, path: '/api/auth/login', method: 'POST', headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data) } }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve({ statusCode: res.statusCode, body: d }));
    });
    req.on('error', err => reject(err));
    req.write(data);
    req.end();
  });
}

(async () => {
  try {
    console.log('Attempting login for', USERNAME);
    const r = await postLogin(USERNAME, PASSWORD);
    console.log('Login HTTP status:', r.statusCode);
    console.log('Login response body:', r.body);

    // find usuario id
    const u = await db.query('SELECT id FROM usuarios WHERE username = $1', [USERNAME]);
    if (u.rowCount === 0) {
      console.error('User not found in DB:', USERNAME);
      process.exit(2);
    }
    const usuarioId = u.rows[0].id;

    // find latest session(s) for this usuario
    const ses = await db.query('SELECT id, token_sesion, fecha_inicio, fecha_fin FROM sesiones WHERE usuario_id = $1 ORDER BY fecha_inicio DESC LIMIT 5', [usuarioId]);
    console.log('Recent sesiones for user:', JSON.stringify(ses.rows, null, 2));

    if (ses.rowCount > 0) {
      const sesionId = ses.rows[0].id;
      const acts = await db.query('SELECT id, tipo_actividad, descripcion, fecha_hora, ip_address FROM actividades WHERE sesion_id = $1 ORDER BY fecha_hora DESC LIMIT 20', [sesionId]);
      console.log('Actividades for session', sesionId, JSON.stringify(acts.rows, null, 2));
    } else {
      console.log('No sesiones found for user yet.');
    }
  } catch (err) {
    console.error('Error in login_and_check:', err && err.stack ? err.stack : err);
  } finally {
    process.exit(0);
  }
})();
