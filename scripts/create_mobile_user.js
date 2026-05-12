const http = require('http');

const payload = {
  username: process.argv[2] || 'mobile_user2',
  email: process.argv[3] || 'mobile2@example.com',
  password: process.argv[4] || 'Pass1234',
  rol: process.argv[5] || 'cliente'
};

const data = JSON.stringify(payload);

const opts = {
  hostname: 'localhost',
  port: process.env.PORT ? parseInt(process.env.PORT, 10) : 3000,
  path: '/api/users',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(data)
  }
};

const req = http.request(opts, res => {
  let d = '';
  res.on('data', c => d += c);
  res.on('end', () => {
    console.log('status', res.statusCode);
    try {
      console.log(JSON.stringify(JSON.parse(d), null, 2));
    } catch (e) {
      console.log(d);
    }
  });
});

req.on('error', err => {
  console.error('request error', err);
});

req.write(data);
req.end();
