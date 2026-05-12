const { Pool } = require('pg');

const pool = new Pool({
  host: process.env.PGHOST || 'localhost',
  user: process.env.PGUSER || 'postgres',
  password: process.env.PGPASSWORD || '2006',
  database: process.env.PGDATABASE || 'PetCareBd',
  port: process.env.PGPORT ? parseInt(process.env.PGPORT, 10) : 5432,
});

module.exports = {
  query: (text, params) => pool.query(text, params),
  pool,
};
