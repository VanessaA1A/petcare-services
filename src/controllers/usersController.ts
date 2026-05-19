import { Request, Response } from 'express';
import { query } from '../db';
import { hashPassword } from '../utils/hash';
import { createSession, logActivity } from '../utils/activity';
import { UserModel } from '../types/models';
import { ApiRole } from '../types/viewmodels';
import { mapDbRoleToApi } from '../utils/roles';

type UserWithRole = UserModel & { role?: ApiRole };

function formatUser(user: UserModel): UserWithRole {
  const role = mapDbRoleToApi((user as any).rol);
  return { ...user, rol: role, role };
}

async function getAllowedRoles(): Promise<string[]> {
  try {
    const r = await query<{ enumlabel: string }>(`SELECT e.enumlabel FROM pg_type t JOIN pg_enum e ON t.oid = e.enumtypid WHERE t.typname = 'rol_usuario' ORDER BY e.enumsortorder`);
    return r.rows.map((r) => r.enumlabel);
  } catch (err) {
    console.warn('Could not read rol_usuario enum from DB, will fallback to defaults', (err as Error).message);
    return [];
  }
}

export async function createUser(req: Request, res: Response) {
  try {
    const { username, email, password, rol } = req.body as { username?: string; email?: string; password?: string; rol?: string };
    if (!username || !email || !password) return res.status(400).json({ error: 'username, email and password are required' });
    const hashed = hashPassword(password);
    let roleToPass: string | null = null;
    if (rol) {
      const allowed = await getAllowedRoles();
      if (allowed.length === 0) {
        roleToPass = rol;
      } else if (allowed.includes(rol)) {
        roleToPass = rol;
      } else {
        console.warn(`Provided role '${rol}' not in allowed list, will use default role`);
        roleToPass = null;
      }
    }
    const text = "INSERT INTO usuarios(username, email, password_hash, rol, created_at) VALUES($1,$2,$3, COALESCE($4::rol_usuario, 'gestor'::rol_usuario), NOW()) RETURNING id, username, email, rol";
    const result = await query<UserModel>(text, [username, email, hashed, roleToPass]);
    const user = result.rows[0];
    if (user) Object.assign(user, formatUser(user));
    try {
      const sess = await createSession(user.id, { ipAddress: req.ip, userAgent: req.get('User-Agent') || null });
      await logActivity({ sesionId: sess.id, usuarioId: user.id, tipoActividad: 'register', descripcion: JSON.stringify({ username, email }), ipAddress: req.ip });
      return res.status(201).json({ user, session: sess });
    } catch (innerErr) {
      console.error('Failed to create session or activity for new user', innerErr);
      return res.status(201).json({ user, warning: 'user created but session/activity failed' });
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error creating user' });
  }
}

export async function getAllUsers(req: Request, res: Response) {
  try {
    const text = "SELECT id, username, email, rol, created_at, last_login, is_active FROM usuarios ORDER BY username";
    const result = await query<UserModel>(text);
    const rows = result.rows.map((r) => formatUser(r));
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error fetching users' });
  }
}

export async function getUserById(req: Request, res: Response) {
  try {
    const { id } = req.params;
    const text = "SELECT id, username, email, rol, created_at, last_login, is_active FROM usuarios WHERE id = $1";
    const result = await query<UserModel>(text, [id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'User not found' });
    const row = result.rows[0];
    res.json(formatUser(row));
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error fetching user' });
  }
}

export async function updateUser(req: Request, res: Response) {
  try {
    const { id } = req.params;
    const { username, email, password, rol, is_active } = req.body as { username?: string | null; email?: string | null; password?: string | null; rol?: string | null; is_active?: boolean | null };
    const hashed = password ? hashPassword(password) : null;
    let roleToPass = rol ?? null;
    if (rol != null) {
      const allowedRoles = await getAllowedRoles();
      if (allowedRoles.length > 0 && !allowedRoles.includes(rol)) {
        return res.status(400).json({ error: `Invalid role '${rol}'. Allowed roles: ${allowedRoles.join(', ')}` });
      }
    }
    const text = `UPDATE usuarios SET username = COALESCE($1, username), email = COALESCE($2, email), password_hash = COALESCE($3, password_hash), rol = COALESCE($4::rol_usuario, rol), is_active = COALESCE($5, is_active) WHERE id = $6 RETURNING id, username, email, rol, is_active`;
    const result = await query<UserModel>(text, [username, email, hashed, roleToPass, is_active, id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'User not found' });
    const updated = result.rows[0];
    res.json(formatUser(updated));
  } catch (err: any) {
    console.error(err);
    if (err.code === '23505') {
      const message = err.constraint === 'usuarios_username_key'
        ? 'username already exists'
        : err.constraint === 'usuarios_email_key'
          ? 'email already exists'
          : 'Duplicate value';
      return res.status(400).json({ error: message });
    }
    if (err.code === '22P02' && (err.message as string).includes('rol_usuario')) {
      const providedRole = (req.body as { rol?: string }).rol;
      return res.status(400).json({ error: `Invalid role '${providedRole ?? ''}'` });
    }
    res.status(500).json({ error: 'Error updating user' });
  }
}

export async function deleteUser(req: Request, res: Response) {
  try {
    const { id } = req.params;
    const text = "DELETE FROM usuarios WHERE id = $1";
    const result = await query(text, [id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'User not found' });
    res.status(204).send();
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error deleting user' });
  }
}

export async function assignRoles(req: Request, res: Response) {
  try {
    const { id } = req.params;
    const { role } = req.body as { role?: string };
    if (!role) return res.status(400).json({ error: 'role is required' });
    const text = "UPDATE usuarios SET rol = $1::rol_usuario WHERE id = $2 RETURNING id, username, email, rol";
    const result = await query<UserModel>(text, [role, id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'User not found' });
    const updated = result.rows[0];
    res.json(formatUser(updated));
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error assigning role' });
  }
}
