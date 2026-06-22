const db = require('../src/db');
const { hashPassword } = require('../src/utils/hash');

const users = [
  { username: 'test1', email: 'test1@example.com', password: 'Pass1234', rol: 'gestor' },
  { username: 'test2', email: 'test2@example.com', password: 'Pass1234', rol: 'gestor' },
  { username: 'test3', email: 'test3@example.com', password: 'Pass1234', rol: 'gestor' },
  { username: 'test4', email: 'test4@example.com', password: 'Pass1234', rol: 'gestor' },
  { username: 'test5', email: 'test5@example.com', password: 'Pass1234', rol: 'gestor' }
];

async function insertUsers() {
  try {
    const inserted = [];
    for (const u of users) {
      const pw = hashPassword(u.password);
      try {
        const res = await db.query(
          'INSERT INTO usuarios(username, email, password_hash, rol, created_at) VALUES($1,$2,$3,$4,NOW()) RETURNING id, username, email, rol, created_at',
          [u.username, u.email, pw, u.rol]
        );
        inserted.push(res.rows[0]);
      } catch (err) {
        if (err.code === '23505') {
          const r = await db.query('SELECT id, username, email, rol, created_at FROM usuarios WHERE username = $1 OR email = $2 LIMIT 1', [u.username, u.email]);
          if (r.rowCount) inserted.push({ existing: true, ...r.rows[0] });
          else inserted.push({ error: err.message });
        } else {
          inserted.push({ error: err.message });
        }
      }
    }

    console.log(JSON.stringify(inserted, null, 2));
    const names = users.map(u => u.username);
    const sel = await db.query('SELECT id, username, email, rol, created_at FROM usuarios WHERE username = ANY($1)', [names]);
    console.log('--- Confirm in DB ---');
    console.log(JSON.stringify(sel.rows, null, 2));
    process.exit(0);
  } catch (err) {
    console.error('Fatal error:', err.message || err);
    process.exit(2);
  }
}

insertUsers();
