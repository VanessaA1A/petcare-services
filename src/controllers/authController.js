const { loginUser } = require("../services/authService");
const { mapDbRoleToApi } = require("../utils/roles");

function formatUser(user) {
  const role = mapDbRoleToApi(user.rol);
  return {
    id: user.id,
    username: user.username,
    email: user.email,
    rol: role,
    role,
  };
}

async function login(req, res) {
  try {
    const username = req.body.username || req.body.userName;
    const { password } = req.body;
    if (!username || !password)
      return res.status(400).json({ error: "username and password required" });

    const result = await loginUser(username, password, {
      ipAddress: req.ip,
      userAgent: req.get("User-Agent"),
    });
    if (!result)
      return res.status(401).json({ error: "Invalid username or password" });

    const { user, session } = result;
    res.json({ user: formatUser(user), session });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Login error" });
  }
}

async function recoverPassword(req, res) {
  try {
    const { email } = req.body;
    const text = "SELECT id, username FROM usuarios WHERE email = $1";
    const result = await db.query(text, [email]);
    if (result.rowCount === 0)
      return res.status(404).json({ error: "Email not found" });
    const user = result.rows[0];
    // create token and store in usuarios.reset_token and reset_token_expires
    const token = uuidv4();
    const expires = new Date(Date.now() + 1000 * 60 * 60); // 1 hour
    await db.query(
      "UPDATE usuarios SET reset_token = $1, reset_token_expires = $2 WHERE id = $3",
      [token, expires, user.id],
    );
    // In production send token via email. Here we return the token for testing.
    res.json({ message: "Recovery token created", token });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Recover error" });
  }
}

async function me(req, res) {
  // sessionAuth middleware sets req.user
  if (!req.user) return res.status(401).json({ error: "Not authenticated" });
  res.json({ user: req.user, session: req.session });
}

module.exports = { login, recoverPassword, me };
