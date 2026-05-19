import { Request, Response } from 'express';
import { query } from '../db';
import { hashPassword } from '../utils/hash';
import { createSession, logActivity } from '../utils/activity';
import { v4 as uuidv4 } from 'uuid';
import { ApiRole, mapDbRoleToApi } from '../utils/roles';

type UserRow = { id: number; username: string; email: string; password_hash: string; rol?: string | null };

type UserLoginResponse = { id: number; username: string; email: string; rol: ApiRole; role: ApiRole };

function formatUserResponse(user: { id: number; username: string; email: string; rol?: string | null }): UserLoginResponse {
  const role = mapDbRoleToApi(user.rol);
  return { id: user.id, username: user.username, email: user.email, rol: role, role };
}

export async function login(req: Request, res: Response) {
  try {
    const { email, password } = req.body as { email?: string; password?: string };
    if (!email || !password) return res.status(400).json({ error: 'email and password required' });

    const text = 'SELECT id, username, email, password_hash, rol FROM usuarios WHERE email = $1';
    const result = await query<UserRow>(text, [email]);
    if (result.rowCount === 0) {
      return res.status(401).json({ error: 'Email not found' });
    }

    const user = result.rows[0];
    const hashed = hashPassword(password);
    if (user.password_hash !== hashed) {
      return res.status(401).json({ error: 'Invalid password' });
    }

    const session = await createSession(user.id, { ipAddress: req.ip, userAgent: req.get('User-Agent') || null });
    await logActivity({ sesionId: session.id, usuarioId: user.id, tipoActividad: 'login', descripcion: JSON.stringify({ email }), ipAddress: req.ip });
    await query('UPDATE usuarios SET last_login = NOW() WHERE id = $1', [user.id]);

    const userResponse = formatUserResponse(user);
    res.json({ user: userResponse, session });
  } catch (err: unknown) {
    console.error(err);
    res.status(500).json({ error: 'Login error' });
  }
}

export async function recoverPassword(req: Request, res: Response) {
  try {
    const { email } = req.body as { email?: string };
    if (!email) return res.status(400).json({ error: 'email required' });
    const text = 'SELECT id, username FROM usuarios WHERE email = $1';
    const result = await query<UserRow>(text, [email]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'Email not found' });
    const user = result.rows[0];
    const token = uuidv4();
    const expires = new Date(Date.now() + 1000 * 60 * 60);
    await query('UPDATE usuarios SET reset_token = $1, reset_token_expires = $2 WHERE id = $3', [token, expires, user.id]);
    res.json({ message: 'Recovery token created', token });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Recover error' });
  }
}

export async function me(req: Request, res: Response) {
  if (!req.user) return res.status(401).json({ error: 'Not authenticated' });
  res.json({ user: req.user, session: req.session });
}
