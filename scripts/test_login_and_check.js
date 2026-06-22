const http = require('http');
const db = require('../src/db');

function postLogin(username, password) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify({ username, password });
    const req = http.request({ hostname: 'localhost', port: 3000, path: '/api/auth/login', method: 'POST', headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data) } }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve({ statusCode: res.statusCode, body: d }));
    });
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

(async () => {
  try {
    console.log('Logging in as test1...');
    const r = await postLogin('test1', 'Pass1234');
    console.log('Login response status:', r.statusCode);
    console.log('Login response body:', r.body);

    const s = await db.query('SELECT id, usuario_id, token_sesion, fecha_inicio, fecha_fin, ip_address FROM sesiones WHERE usuario_id = (SELECT id FROM usuarios WHERE username = $1) ORDER BY id DESC LIMIT 5', ['test1']);
    console.log('Sesiones for test1:', JSON.stringify(s.rows, null, 2));

    const a = await db.query('SELECT id, sesion_id, usuario_id, tipo_actividad, descripcion, fecha_hora FROM actividades WHERE usuario_id = (SELECT id FROM usuarios WHERE username = $1) ORDER BY id DESC LIMIT 10', ['test1']);
    console.log('Actividades for test1:', JSON.stringify(a.rows, null, 2));
  } catch (err) {
    console.error('Error during test:', err.message || err);
    process.exit(2);
  }
})();
