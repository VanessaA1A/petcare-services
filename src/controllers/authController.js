const db = require('../db');
const { hashPassword } = require('../utils/hash');
const { createSession, logActivity } = require('../utils/activity');
const { v4: uuidv4 } = require('uuid');
const { mapDbRoleToApi } = require('../utils/roles');

function formatUser(user) {
  const role = mapDbRoleToApi(user.rol);
  return { id: user.id, username: user.username, email: user.email, rol: role, role };
}

async function login(req, res) {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ error: 'email and password required' });

    const text = 'SELECT id, username, email, password_hash, rol FROM usuarios WHERE email = $1';
    const result = await db.query(text, [email]);
    if (result.rowCount === 0) return res.status(401).json({ error: 'Email not found' });

    const user = result.rows[0];
    const hashed = hashPassword(password);
    if (user.password_hash !== hashed) return res.status(401).json({ error: 'Invalid password' });

    const session = await createSession(user.id, { ipAddress: req.ip, userAgent: req.get('User-Agent') });
    await logActivity({ sesionId: session.id, usuarioId: user.id, tipoActividad: 'login', descripcion: JSON.stringify({ email }), ipAddress: req.ip });
    await db.query('UPDATE usuarios SET last_login = NOW() WHERE id = $1', [user.id]);
    res.json({ user: formatUser(user), session });
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

