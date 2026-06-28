const { Pool } = require("pg");
const pool = new Pool({
  host: "localhost",
  user: "postgres",
  password: "2006",
  database: "PetCareBd",
  port: 5432,
});
(async () => {
  try {
    const res = await pool.query(
      `SELECT column_name, data_type, udt_name FROM information_schema.columns WHERE table_name='usuarios' ORDER BY ordinal_position`,
    );
    console.log("USUARIOS SCHEMA:");
    console.log(JSON.stringify(res.rows, null, 2));
    const res2 = await pool.query(
      `SELECT column_name, data_type, udt_name FROM information_schema.columns WHERE table_name='sesiones' ORDER BY ordinal_position`,
    );
    console.log("SESIONES SCHEMA:");
    console.log(JSON.stringify(res2.rows, null, 2));
    const res3 = await pool.query(
      `SELECT column_name, data_type, udt_name FROM information_schema.columns WHERE table_name='actividades' ORDER BY ordinal_position`,
    );
    console.log("ACTIVIDADES SCHEMA:");
    console.log(JSON.stringify(res3.rows, null, 2));
    const res4 = await pool.query(
      `SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_name IN ('flyway_schema_history')`,
    );
    console.log("FLYWAY HISTORY TABLE:");
    console.log(JSON.stringify(res4.rows, null, 2));
  } catch (err) {
    console.error("ERROR", err.message);
    process.exit(1);
  } finally {
    await pool.end();
  }
})();
