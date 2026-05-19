import { Pool, QueryResult, QueryResultRow } from 'pg';

const pool = new Pool({
  host: process.env.PGHOST || 'localhost',
  user: process.env.PGUSER || 'postgres',
  password: process.env.PGPASSWORD || '2006',
  database: process.env.PGDATABASE || 'PetCareBd',
  port: process.env.PGPORT ? parseInt(process.env.PGPORT, 10) : 5432,
});

export async function query<T extends QueryResultRow = any>(text: string, params?: unknown[]): Promise<QueryResult<T>> {
  return pool.query<T>(text, params as any);
}

export { pool };
