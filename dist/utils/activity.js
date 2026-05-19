"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createSession = createSession;
exports.logActivity = logActivity;
const uuid_1 = require("uuid");
const db_1 = require("../db");
async function createSession(usuarioId, opts = {}) {
    const token = (0, uuid_1.v4)();
    const { ipAddress = null, userAgent = null } = opts;
    const text = 'INSERT INTO sesiones(usuario_id, token_sesion, fecha_inicio, ip_address, user_agent) VALUES($1, $2, NOW(), $3, $4) RETURNING id, token_sesion, fecha_inicio';
    const res = await (0, db_1.query)(text, [usuarioId, token, ipAddress, userAgent]);
    return res.rows[0];
}
async function logActivity(params) {
    const text = 'INSERT INTO actividades(sesion_id, usuario_id, tipo_actividad, descripcion, ip_address, fecha_hora) VALUES($1, $2, $3, $4, $5, NOW()) RETURNING id';
    const res = await (0, db_1.query)(text, [params.sesionId, params.usuarioId, params.tipoActividad, params.descripcion || null, params.ipAddress || null]);
    return res.rows[0];
}
