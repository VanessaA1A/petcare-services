const db = require('../db');
const { hashPassword } = require('../utils/hash');
const { createSession, logActivity } = require('../utils/activity');
const { v4: uuidv4 } = require('uuid');

async function login(req, res) {
  try {
    const { username, password } = req.body;
    const hashed = hashPassword(password);
    const text = 'SELECT id, username, email FROM usuarios WHERE username = $1 AND password_hash = $2';
    const result = await db.query(text, [username, hashed]);
    if (result.rowCount === 0) return res.status(401).json({ error: 'Invalid credentials' });
    const user = result.rows[0];
    // create session (sesiones) using activity util
    const session = await createSession(user.id, { ipAddress: req.ip, userAgent: req.get('User-Agent') });
    // log activity in actividades
    await logActivity({ sesionId: session.id, usuarioId: user.id, tipoActividad: 'login', descripcion: JSON.stringify({ username }) , ipAddress: req.ip });
    // update last_login on usuarios
    await db.query('UPDATE usuarios SET last_login = NOW() WHERE id = $1', [user.id]);
    res.json({ user, session });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Login error' });
  }
}

async function recoverPassword(req, res) {
  try {
    const { email } = req.body;
    const text = 'SELECT id, username FROM usuarios WHERE email = $1';
    const result = await db.query(text, [email]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'Email not found' });
    const user = result.rows[0];
    // create token and store in usuarios.reset_token and reset_token_expires
    const token = uuidv4();
    const expires = new Date(Date.now() + 1000 * 60 * 60); // 1 hour
    await db.query('UPDATE usuarios SET reset_token = $1, reset_token_expires = $2 WHERE id = $3', [token, expires, user.id]);
    // In production send token via email. Here we return the token for testing.
    res.json({ message: 'Recovery token created', token });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Recover error' });
  }
}

async function me(req, res) {
  // sessionAuth middleware sets req.user
  if (!req.user) return res.status(401).json({ error: 'Not authenticated' });
  res.json({ user: req.user, session: req.session });
}

module.exports = { login, recoverPassword, me };

