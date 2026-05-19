import { v4 as uuidv4 } from 'uuid';
import { query } from '../db';
import { SessionModel, ActivityModel } from '../types/models';

export async function createSession(usuarioId: number, opts: { ipAddress?: string | null; userAgent?: string | null } = {}): Promise<SessionModel> {
  const token = uuidv4();
  const { ipAddress = null, userAgent = null } = opts;
  const text = 'INSERT INTO sesiones(usuario_id, token_sesion, fecha_inicio, ip_address, user_agent) VALUES($1, $2, NOW(), $3, $4) RETURNING id, token_sesion, fecha_inicio';
  const res = await query<SessionModel>(text, [usuarioId, token, ipAddress, userAgent]);
  return res.rows[0];
}

export async function logActivity(params: { sesionId?: number; usuarioId?: number; tipoActividad?: string; descripcion?: string | null; ipAddress?: string | null }): Promise<ActivityModel> {
  const text = 'INSERT INTO actividades(sesion_id, usuario_id, tipo_actividad, descripcion, ip_address, fecha_hora) VALUES($1, $2, $3, $4, $5, NOW()) RETURNING id';
  const res = await query<ActivityModel>(text, [params.sesionId, params.usuarioId, params.tipoActividad, params.descripcion || null, params.ipAddress || null]);
  return res.rows[0];
}
