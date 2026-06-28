"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.findUserByUsername = findUserByUsername;
exports.verifyPassword = verifyPassword;
exports.loginUser = loginUser;
const db_1 = require("../db");
const hash_1 = require("../utils/hash");
const activity_1 = require("../utils/activity");
async function findUserByUsername(username) {
    const text = "SELECT id, username, email, password_hash, rol FROM usuarios WHERE username = $1";
    const result = await (0, db_1.query)(text, [username]);
    return result.rowCount === 0 ? null : result.rows[0];
}
function verifyPassword(user, plainPassword) {
    if (!user || !user.password_hash)
        return false;
    return user.password_hash === (0, hash_1.hashPassword)(plainPassword);
}
async function loginUser(username, password, opts = {}) {
    const user = await findUserByUsername(username);
    if (!verifyPassword(user, password))
        return null;
    if (!user)
        return null;
    const session = await (0, activity_1.createSession)(user.id, {
        ipAddress: opts.ipAddress || null,
        userAgent: opts.userAgent || null,
    });
    await (0, activity_1.logActivity)({
        sesionId: session.id,
        usuarioId: user.id,
        tipoActividad: "login",
        descripcion: JSON.stringify({ username }),
        ipAddress: opts.ipAddress || null,
    });
    return { user, session };
}
