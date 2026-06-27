const { query } = require("../db");
const { hashPassword } = require("../utils/hash");
const { createSession, logActivity } = require("../utils/activity");

async function findUserByUsername(username) {
  const text =
    "SELECT id, username, email, password_hash, rol FROM usuarios WHERE username = $1";
  const result = await query(text, [username]);
  return result.rowCount === 0 ? null : result.rows[0];
}

function verifyPassword(user, plainPassword) {
  if (!user || !user.password_hash) return false;
  return user.password_hash === hashPassword(plainPassword);
}

async function loginUser(username, password, opts = {}) {
  const user = await findUserByUsername(username);
  if (!verifyPassword(user, password)) return null;

  const session = await createSession(user.id, {
    ipAddress: opts.ipAddress || null,
    userAgent: opts.userAgent || null,
  });

  await logActivity({
    sesionId: session.id,
    usuarioId: user.id,
    tipoActividad: "login",
    descripcion: JSON.stringify({ username }),
    ipAddress: opts.ipAddress || null,
  });

  return { user, session };
}

module.exports = { findUserByUsername, verifyPassword, loginUser };
