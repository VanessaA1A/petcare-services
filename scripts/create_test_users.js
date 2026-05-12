const http = require('http');
const fs = require('fs');

const users = [
  { username: 'test1', email: 'test1@example.com', password: 'Pass1234' },
  { username: 'test2', email: 'test2@example.com', password: 'Pass1234' },
  { username: 'test3', email: 'test3@example.com', password: 'Pass1234' },
  { username: 'test4', email: 'test4@example.com', password: 'Pass1234' },
  { username: 'test5', email: 'test5@example.com', password: 'Pass1234' }
];

async function postUser(user) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(user);
    const port = process.env.PORT ? parseInt(process.env.PORT,10) : 3000;
    const req = http.request({
        hostname: 'localhost',
        port: port,
      path: '/api/users',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data)
      }
    }, res => {
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
  const results = [];
  for (const u of users) {
    try {
      const r = await postUser(u);
      console.log('Created:', u.username, 'status', r.statusCode);
      results.push({ user: u, status: r.statusCode, body: r.body });
    } catch (err) {
      console.error('Error creating', u.username, err.message);
      results.push({ user: u, error: err.message });
    }
  }
  fs.writeFileSync('out_users.json', JSON.stringify(results, null, 2));
  console.log('Wrote out_users.json');
})();
