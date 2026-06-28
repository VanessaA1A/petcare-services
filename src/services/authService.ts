import { query } from "../db";
import { hashPassword } from "../utils/hash";
import { createSession, logActivity } from "../utils/activity";
import { UserModel, SessionModel } from "../types/models";

type UserWithPasswordHash = UserModel & { password_hash: string };

type LoginOptions = {
  ipAddress?: string | null;
  userAgent?: string | null;
};

export async function findUserByUsername(
  username: string,
): Promise<UserWithPasswordHash | null> {
  const text =
    "SELECT id, username, email, password_hash, rol FROM usuarios WHERE username = $1";
  const result = await query<UserWithPasswordHash>(text, [username]);
  return result.rowCount === 0 ? null : result.rows[0];
}

export function verifyPassword(
  user: UserWithPasswordHash | null,
  plainPassword: string,
): boolean {
  if (!user || !user.password_hash) return false;
  return user.password_hash === hashPassword(plainPassword);
}

export async function loginUser(
  username: string,
  password: string,
  opts: LoginOptions = {},
): Promise<{ user: UserWithPasswordHash; session: SessionModel } | null> {
  const user = await findUserByUsername(username);
  if (!verifyPassword(user, password)) return null;
  if (!user) return null;

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
