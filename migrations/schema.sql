-- Assumed schema used by the Node services. Run in PetCareBd if needed.

CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY,
  username text UNIQUE NOT NULL,
  email text UNIQUE,
  password text
);

CREATE TABLE IF NOT EXISTS roles (
  name text PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id uuid REFERENCES users(id) ON DELETE CASCADE,
  role_name text REFERENCES roles(name) ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_name)
);

CREATE TABLE IF NOT EXISTS sessions (
  id uuid PRIMARY KEY,
  user_id uuid REFERENCES users(id) ON DELETE CASCADE,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS activities (
  id uuid PRIMARY KEY,
  session_id uuid REFERENCES sessions(id) ON DELETE CASCADE,
  user_id uuid REFERENCES users(id) ON DELETE CASCADE,
  action text,
  data jsonb,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS password_recovery (
  id uuid PRIMARY KEY,
  user_id uuid REFERENCES users(id) ON DELETE CASCADE,
  token text,
  created_at timestamptz DEFAULT NOW()
);
