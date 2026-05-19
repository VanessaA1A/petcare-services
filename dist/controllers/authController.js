"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.login = login;
exports.recoverPassword = recoverPassword;
exports.me = me;
const db_1 = require("../db");
const hash_1 = require("../utils/hash");
const activity_1 = require("../utils/activity");
const uuid_1 = require("uuid");
const roles_1 = require("../utils/roles");
function formatUserResponse(user) {
    const role = (0, roles_1.mapDbRoleToApi)(user.rol);
    return { id: user.id, username: user.username, email: user.email, rol: role, role };
}
async function login(req, res) {
    try {
        const { email, password } = req.body;
        if (!email || !password)
            return res.status(400).json({ error: 'email and password required' });
        const text = 'SELECT id, username, email, password_hash, rol FROM usuarios WHERE email = $1';
        const result = await (0, db_1.query)(text, [email]);
        if (result.rowCount === 0) {
            return res.status(401).json({ error: 'Email not found' });
        }
        const user = result.rows[0];
        const hashed = (0, hash_1.hashPassword)(password);
        if (user.password_hash !== hashed) {
            return res.status(401).json({ error: 'Invalid password' });
        }
        const session = await (0, activity_1.createSession)(user.id, { ipAddress: req.ip, userAgent: req.get('User-Agent') || null });
        await (0, activity_1.logActivity)({ sesionId: session.id, usuarioId: user.id, tipoActividad: 'login', descripcion: JSON.stringify({ email }), ipAddress: req.ip });
        await (0, db_1.query)('UPDATE usuarios SET last_login = NOW() WHERE id = $1', [user.id]);
        const userResponse = formatUserResponse(user);
        res.json({ user: userResponse, session });
    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Login error' });
    }
}
async function recoverPassword(req, res) {
    try {
        const { email } = req.body;
        if (!email)
            return res.status(400).json({ error: 'email required' });
        const text = 'SELECT id, username FROM usuarios WHERE email = $1';
        const result = await (0, db_1.query)(text, [email]);
        if (result.rowCount === 0)
            return res.status(404).json({ error: 'Email not found' });
        const user = result.rows[0];
        const token = (0, uuid_1.v4)();
        const expires = new Date(Date.now() + 1000 * 60 * 60);
        await (0, db_1.query)('UPDATE usuarios SET reset_token = $1, reset_token_expires = $2 WHERE id = $3', [token, expires, user.id]);
        res.json({ message: 'Recovery token created', token });
    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Recover error' });
    }
}
async function me(req, res) {
    if (!req.user)
        return res.status(401).json({ error: 'Not authenticated' });
    res.json({ user: req.user, session: req.session });
}
