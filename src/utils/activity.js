const db = require('../db');
const { v4: uuidv4 } = require('uuid');

// createSession: insert into `sesiones` and return the inserted row (id, token_sesion, fecha_inicio)
async function createSession(usuarioId, opts = {}) {
  const token = uuidv4();
  const { ipAddress = null, userAgent = null } = opts;
  const text = 'INSERT INTO sesiones(usuario_id, token_sesion, fecha_inicio, ip_address, user_agent) VALUES($1, $2, NOW(), $3, $4) RETURNING id, token_sesion, fecha_inicio';
  const res = await db.query(text, [usuarioId, token, ipAddress, userAgent]);
  return res.rows[0];
}

// logActivity: insert into `actividades` table
async function logActivity({ sesionId, usuarioId, tipoActividad, descripcion, ipAddress }) {
  const text = 'INSERT INTO actividades(sesion_id, usuario_id, tipo_actividad, descripcion, ip_address, fecha_hora) VALUES($1, $2, $3, $4, $5, NOW()) RETURNING id';
  const res = await db.query(text, [sesionId, usuarioId, tipoActividad, descripcion || null, ipAddress || null]);
  return res.rows[0];
}

module.exports = { createSession, logActivity };
