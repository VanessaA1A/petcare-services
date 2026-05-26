const { Pool } = require('pg');
const pool = new Pool({
  host: process.env.PGHOST || 'localhost',
  user: process.env.PGUSER || 'postgres',
  password: process.env.PGPASSWORD || '2006',
  database: process.env.PGDATABASE || 'PetCareBd',
  port: process.env.PGPORT ? parseInt(process.env.PGPORT, 10) : 5432,
});
(async () => {
  try {
    const pets = await pool.query('SELECT * FROM pets ORDER BY created_at DESC LIMIT 20');
    console.log('pets count', pets.rowCount);
    console.log(pets.rows);
    const owners = await pool.query('SELECT id, username, email FROM usuarios ORDER BY id LIMIT 20');
    console.log('owners count', owners.rowCount);
    console.log(owners.rows);
  } catch (err) {
    console.error('ERR', err);
  } finally {
    await pool.end();
  }
})();
