"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = sessionAuth;
const db_1 = require("../db");
const roles_1 = require("../utils/roles");
async function sessionAuth(req, res, next) {
    try {
        const auth = req.get('Authorization') || req.get('authorization');
        if (!auth || !auth.startsWith('Bearer '))
            return res.status(401).json({ error: 'Missing Authorization header' });
        const token = auth.slice('Bearer '.length).trim();
        const text = `SELECT s.id as sesion_id, s.token_sesion, s.usuario_id, u.username, u.email, u.rol FROM sesiones s JOIN usuarios u ON s.usuario_id = u.id WHERE s.token_sesion = $1 AND (s.fecha_fin IS NULL OR s.logout_explicito = false) LIMIT 1`;
        const result = await (0, db_1.query)(text, [token]);
        if (result.rowCount === 0)
            return res.status(401).json({ error: 'Invalid or expired session' });
        const row = result.rows[0];
        const role = (0, roles_1.mapDbRoleToApi)(row.rol);
        req.user = { id: row.usuario_id, username: row.username, email: row.email, rol: role, role };
        req.session = { id: row.sesion_id, token: row.token_sesion };
        next();
    }
    catch (err) {
        console.error('sessionAuth error', err);
        res.status(500).json({ error: 'Auth error' });
    }
}
