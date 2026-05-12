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

(async () => {
  try {
    console.log('Logging in test1...');
    const login = await postLogin('test1', 'Pass1234');
    console.log('Login status', login.statusCode);
    const body = JSON.parse(login.body);
    const token = body.session && body.session.token_sesion;
    console.log('Token:', token);
    if (!token) { console.error('No token returned'); process.exit(2); }
    const me = await getMe(token);
    console.log('ME status', me.statusCode);
    console.log('ME body', me.body);
  } catch (err) {
    console.error('Error', err.message || err);
    process.exit(2);
  }
})();
