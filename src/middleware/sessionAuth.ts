import { Request, Response, NextFunction } from 'express';
import { query } from '../db';
import { mapDbRoleToApi } from '../utils/roles';

type SessionRow = {
  sesion_id: number;
  token_sesion: string;
  usuario_id: number;
  username: string;
  email: string;
  rol: string;
};

export default async function sessionAuth(req: Request, res: Response, next: NextFunction) {
  try {
    const auth = req.get('Authorization') || req.get('authorization');
    if (!auth || !auth.startsWith('Bearer ')) return res.status(401).json({ error: 'Missing Authorization header' });
    const token = auth.slice('Bearer '.length).trim();
    const text = `SELECT s.id as sesion_id, s.token_sesion, s.usuario_id, u.username, u.email, u.rol FROM sesiones s JOIN usuarios u ON s.usuario_id = u.id WHERE s.token_sesion = $1 AND (s.fecha_fin IS NULL OR s.logout_explicito = false) LIMIT 1`;
    const result = await query<SessionRow>(text, [token]);
    if (result.rowCount === 0) return res.status(401).json({ error: 'Invalid or expired session' });
    const row = result.rows[0];
    const role = mapDbRoleToApi(row.rol);
    req.user = { id: row.usuario_id, username: row.username, email: row.email, rol: role, role } as any;
    req.session = { id: row.sesion_id, token: row.token_sesion };
    next();
  } catch (err) {
    console.error('sessionAuth error', err);
    res.status(500).json({ error: 'Auth error' });
  }
}
