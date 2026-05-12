const http = require('http');

const user = { username: 'debug_user', email: 'debug_user@example.com', password: 'Pass1234' };

const data = JSON.stringify(user);
const req = http.request({
  hostname: 'localhost',
  port: 3000,
  path: '/api/users',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(data)
  },
  timeout: 5000
}, res => {
  console.log('STATUS', res.statusCode);
  console.log('HEADERS', res.headers);
  let d = '';
  res.on('data', c => d += c);
  res.on('end', () => {
    console.log('BODY:', d);
  });
});

req.on('error', err => {
  console.error('REQUEST ERROR:', err);
});
req.on('timeout', () => {
  console.error('REQUEST TIMEOUT');
  req.destroy();
});

req.write(data);
req.end();
