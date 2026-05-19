const http = require('http');

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

function getMe(token) {
  return new Promise((resolve, reject) => {
    const req = http.request({ hostname: 'localhost', port: 3000, path: '/api/auth/me', method: 'GET', headers: { 'Authorization': 'Bearer ' + token } }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve({ statusCode: res.statusCode, body: d }));
    });
    req.on('error', reject);
    req.end();
  });
}

(async ()=>{
  try {
    const login = await postLogin('test1','Pass1234');
    console.log('login', login.statusCode, login.body);
    const parsed = JSON.parse(login.body || '{}');
    const token = parsed.session && parsed.session.token_sesion;
    if (!token) { console.error('No token returned'); process.exit(2); }
  // Instead of calling HTTP /api/auth/me (which may fail if server restarted),
  // query DB directly to simulate sessionAuth lookup
  const db = require('../src/db');
  const { mapDbRoleToApi } = require('../src/utils/roles');
  const r = await db.query(`SELECT s.id as session_id, s.token_sesion, s.usuario_id, u.username, u.email, u.rol FROM sesiones s JOIN usuarios u ON u.id = s.usuario_id WHERE s.token_sesion = $1 LIMIT 1`, [token]);
  if (r.rowCount > 0) {
    const row = { ...r.rows[0], rol: mapDbRoleToApi(r.rows[0].rol) };
    console.log('DB /me lookup:', JSON.stringify(row, null, 2));
  } else {
    console.log('DB /me lookup: no row');
  }
  } catch (err) { console.error('Error', err); process.exit(2); }
})();
