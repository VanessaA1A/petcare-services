"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.pool = void 0;
exports.query = query;
const pg_1 = require("pg");
const pool = new pg_1.Pool({
    host: process.env.PGHOST || 'localhost',
    user: process.env.PGUSER || 'postgres',
    password: process.env.PGPASSWORD || '2006',
    database: process.env.PGDATABASE || 'PetCareBd',
    port: process.env.PGPORT ? parseInt(process.env.PGPORT, 10) : 5432,
});
exports.pool = pool;
async function query(text, params) {
    return pool.query(text, params);
}
