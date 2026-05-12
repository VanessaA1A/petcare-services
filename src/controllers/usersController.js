const db = require('../db');
const { hashPassword } = require('../utils/hash');

async function createUser(req, res) {
  try {
    const { username, email, password, rol } = req.body;
    const hashed = password ? hashPassword(password) : null;
  const text = "INSERT INTO usuarios(username, email, password_hash, rol, created_at) VALUES($1,$2,$3, COALESCE($4, 'gestor'), NOW()) RETURNING id, username, email, rol";
    const result = await db.query(text, [username, email, hashed, rol]);
    res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error creating user' });
  }
}

async function getAllUsers(req, res) {
  try {
    const text = 'SELECT id, username, email, rol, created_at, last_login, is_active FROM usuarios ORDER BY username';
    const result = await db.query(text);
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error fetching users' });
  }
}

async function getUserById(req, res) {
  try {
    const { id } = req.params;
    const text = 'SELECT id, username, email, rol, created_at, last_login, is_active FROM usuarios WHERE id = $1';
    const result = await db.query(text, [id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'User not found' });
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error fetching user' });
  }
}

async function updateUser(req, res) {
  try {
    const { id } = req.params;
    const { username, email, password, rol, is_active } = req.body;
    const hashed = password ? hashPassword(password) : null;
    const text = `UPDATE usuarios SET username = COALESCE($1, username), email = COALESCE($2, email), password_hash = COALESCE($3, password_hash), rol = COALESCE($4, rol), is_active = COALESCE($5, is_active) WHERE id = $6 RETURNING id, username, email, rol, is_active`;
    const result = await db.query(text, [username, email, hashed, rol, is_active, id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'User not found' });
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error updating user' });
  }
}

async function deleteUser(req, res) {
  try {
    const { id } = req.params;
    const text = 'DELETE FROM usuarios WHERE id = $1';
    const result = await db.query(text, [id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'User not found' });
    res.status(204).send();
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error deleting user' });
  }
}

async function assignRoles(req, res) {
  try {
    const { id } = req.params; // user id
    const { role } = req.body; // expect single role string to match enum
    if (!role) return res.status(400).json({ error: 'role is required' });
    const text = 'UPDATE usuarios SET rol = $1 WHERE id = $2 RETURNING id, username, email, rol';
    const result = await db.query(text, [role, id]);
    if (result.rowCount === 0) return res.status(404).json({ error: 'User not found' });
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Error assigning role' });
  }
}

module.exports = { createUser, getAllUsers, getUserById, updateUser, deleteUser, assignRoles };
