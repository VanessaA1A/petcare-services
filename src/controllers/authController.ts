import { Request, Response } from "express";
import { v4 as uuidv4 } from "uuid";
import { loginUser } from "../services/authService";
import { ApiRole, mapDbRoleToApi } from "../utils/roles";
import { query } from "../db";

type UserRow = {
  id: number;
  username: string;
  email: string;
  password_hash: string;
  rol?: string | null;
};

type UserLoginResponse = {
  id: number;
  username: string;
  email: string;
  rol: ApiRole;
  role: ApiRole;
};

function formatUserResponse(user: {
  id: number;
  username: string;
  email: string;
  rol?: string | null;
}): UserLoginResponse {
  const role = mapDbRoleToApi(user.rol);
  return {
    id: user.id,
    username: user.username,
    email: user.email,
    rol: role,
    role,
  };
}

export async function login(req: Request, res: Response) {
  try {
    const username =
      (req.body as { username?: string; userName?: string }).username ||
      (req.body as { username?: string; userName?: string }).userName;
    const { password } = req.body as { password?: string };
    if (!username || !password)
      return res.status(400).json({ error: "username and password required" });

    const result = await loginUser(username, password, {
      ipAddress: req.ip,
      userAgent: req.get("User-Agent") || null,
    });
    if (!result)
      return res.status(401).json({ error: "Invalid username or password" });

    const { user, session } = result;
    res.json({ user: formatUserResponse(user), session });
  } catch (err: unknown) {
    console.error(err);
    res.status(500).json({ error: "Login error" });
  }
}

export async function recoverPassword(req: Request, res: Response) {
  try {
    const { email } = req.body as { email?: string };
    if (!email) return res.status(400).json({ error: "email required" });
    const text = "SELECT id, username FROM usuarios WHERE email = $1";
    const result = await query<UserRow>(text, [email]);
    if (result.rowCount === 0)
      return res.status(404).json({ error: "Email not found" });
    const user = result.rows[0];
    const token = uuidv4();
    const expires = new Date(Date.now() + 1000 * 60 * 60);
    await query(
      "UPDATE usuarios SET reset_token = $1, reset_token_expires = $2 WHERE id = $3",
      [token, expires, user.id],
    );
    res.json({ message: "Recovery token created", token });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Recover error" });
  }
}

export async function me(req: Request, res: Response) {
  if (!req.user) return res.status(401).json({ error: "Not authenticated" });
  res.json({ user: req.user, session: req.session });
}

export async function logout(req: Request, res: Response) {
  try {
    const auth = req.get("Authorization") || req.get("authorization");
    if (!auth || !auth.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing Authorization header" });
    }

    const token = auth.slice("Bearer ".length).trim();
    const text = `
      UPDATE sesiones
      SET fecha_fin = NOW(), logout_explicito = true
      WHERE token_sesion = $1 AND (fecha_fin IS NULL OR logout_explicito = false)
      RETURNING id, token_sesion
    `;
    const result = await query(text, [token]);

    if (result.rowCount === 0) {
      return res.status(404).json({ error: "Active session not found" });
    }

    res.json({
      message: "Session closed successfully",
      sessionId: result.rows[0].id,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Logout error" });
  }
}
