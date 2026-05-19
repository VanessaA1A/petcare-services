"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createUser = createUser;
exports.getAllUsers = getAllUsers;
exports.getUserById = getUserById;
exports.updateUser = updateUser;
exports.deleteUser = deleteUser;
exports.assignRoles = assignRoles;
const db_1 = require("../db");
const hash_1 = require("../utils/hash");
const activity_1 = require("../utils/activity");
const roles_1 = require("../utils/roles");
function formatUser(user) {
    const role = (0, roles_1.mapDbRoleToApi)(user.rol);
    return { ...user, rol: role, role };
}
async function getAllowedRoles() {
    try {
        const r = await (0, db_1.query)(`SELECT e.enumlabel FROM pg_type t JOIN pg_enum e ON t.oid = e.enumtypid WHERE t.typname = 'rol_usuario' ORDER BY e.enumsortorder`);
        return r.rows.map((r) => r.enumlabel);
    }
    catch (err) {
        console.warn('Could not read rol_usuario enum from DB, will fallback to defaults', err.message);
        return [];
    }
}
async function createUser(req, res) {
    try {
        const { username, email, password, rol } = req.body;
        if (!username || !email || !password)
            return res.status(400).json({ error: 'username, email and password are required' });
        const hashed = (0, hash_1.hashPassword)(password);
        let roleToPass = null;
        if (rol) {
            const allowed = await getAllowedRoles();
            if (allowed.length === 0) {
                roleToPass = rol;
            }
            else if (allowed.includes(rol)) {
                roleToPass = rol;
            }
            else {
                console.warn(`Provided role '${rol}' not in allowed list, will use default role`);
                roleToPass = null;
            }
        }
        const text = "INSERT INTO usuarios(username, email, password_hash, rol, created_at) VALUES($1,$2,$3, COALESCE($4::rol_usuario, 'gestor'::rol_usuario), NOW()) RETURNING id, username, email, rol";
        const result = await (0, db_1.query)(text, [username, email, hashed, roleToPass]);
        const user = result.rows[0];
        if (user)
            Object.assign(user, formatUser(user));
        try {
            const sess = await (0, activity_1.createSession)(user.id, { ipAddress: req.ip, userAgent: req.get('User-Agent') || null });
            await (0, activity_1.logActivity)({ sesionId: sess.id, usuarioId: user.id, tipoActividad: 'register', descripcion: JSON.stringify({ username, email }), ipAddress: req.ip });
            return res.status(201).json({ user, session: sess });
        }
        catch (innerErr) {
            console.error('Failed to create session or activity for new user', innerErr);
            return res.status(201).json({ user, warning: 'user created but session/activity failed' });
        }
    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error creating user' });
    }
}
async function getAllUsers(req, res) {
    try {
        const text = "SELECT id, username, email, rol, created_at, last_login, is_active FROM usuarios ORDER BY username";
        const result = await (0, db_1.query)(text);
        const rows = result.rows.map((r) => formatUser(r));
        res.json(rows);
    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error fetching users' });
    }
}
async function getUserById(req, res) {
    try {
        const { id } = req.params;
        const text = "SELECT id, username, email, rol, created_at, last_login, is_active FROM usuarios WHERE id = $1";
        const result = await (0, db_1.query)(text, [id]);
        if (result.rowCount === 0)
            return res.status(404).json({ error: 'User not found' });
        const row = result.rows[0];
        res.json(formatUser(row));
    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error fetching user' });
    }
}
async function updateUser(req, res) {
    try {
        const { id } = req.params;
        const { username, email, password, rol, is_active } = req.body;
        const hashed = password ? (0, hash_1.hashPassword)(password) : null;
        let roleToPass = rol ?? null;
        if (rol != null) {
            const allowedRoles = await getAllowedRoles();
            if (allowedRoles.length > 0 && !allowedRoles.includes(rol)) {
                return res.status(400).json({ error: `Invalid role '${rol}'. Allowed roles: ${allowedRoles.join(', ')}` });
            }
        }
        const text = `UPDATE usuarios SET username = COALESCE($1, username), email = COALESCE($2, email), password_hash = COALESCE($3, password_hash), rol = COALESCE($4::rol_usuario, rol), is_active = COALESCE($5, is_active) WHERE id = $6 RETURNING id, username, email, rol, is_active`;
        const result = await (0, db_1.query)(text, [username, email, hashed, roleToPass, is_active, id]);
        if (result.rowCount === 0)
            return res.status(404).json({ error: 'User not found' });
        const updated = result.rows[0];
        res.json(formatUser(updated));
    }
    catch (err) {
        console.error(err);
        if (err.code === '23505') {
            const message = err.constraint === 'usuarios_username_key'
                ? 'username already exists'
                : err.constraint === 'usuarios_email_key'
                    ? 'email already exists'
                    : 'Duplicate value';
            return res.status(400).json({ error: message });
        }
        if (err.code === '22P02' && err.message.includes('rol_usuario')) {
            const providedRole = req.body.rol;
            return res.status(400).json({ error: `Invalid role '${providedRole ?? ''}'` });
        }
        res.status(500).json({ error: 'Error updating user' });
    }
}
async function deleteUser(req, res) {
    try {
        const { id } = req.params;
        const text = "DELETE FROM usuarios WHERE id = $1";
        const result = await (0, db_1.query)(text, [id]);
        if (result.rowCount === 0)
            return res.status(404).json({ error: 'User not found' });
        res.status(204).send();
    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error deleting user' });
    }
}
async function assignRoles(req, res) {
    try {
        const { id } = req.params;
        const { role } = req.body;
        if (!role)
            return res.status(400).json({ error: 'role is required' });
        const text = "UPDATE usuarios SET rol = $1::rol_usuario WHERE id = $2 RETURNING id, username, email, rol";
        const result = await (0, db_1.query)(text, [role, id]);
        if (result.rowCount === 0)
            return res.status(404).json({ error: 'User not found' });
        const updated = result.rows[0];
        res.json(formatUser(updated));
    }
    catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error assigning role' });
    }
}
