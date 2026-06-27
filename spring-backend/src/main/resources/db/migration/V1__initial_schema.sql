CREATE TYPE IF NOT EXISTS rol_usuario AS ENUM ('gestor', 'cliente', 'OWNER', 'CAREGIVER');

CREATE TABLE IF NOT EXISTS usuarios (
  id uuid PRIMARY KEY,
  username text UNIQUE NOT NULL,
  email text UNIQUE NOT NULL,
  password_hash text NOT NULL,
  rol rol_usuario NOT NULL DEFAULT 'gestor',
  created_at timestamptz DEFAULT NOW(),
  last_login timestamptz,
  is_active boolean DEFAULT true,
  reset_token text,
  reset_token_expires timestamptz
);

CREATE TABLE IF NOT EXISTS sesiones (
  id uuid PRIMARY KEY,
  usuario_id uuid NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  token_sesion text UNIQUE NOT NULL,
  fecha_inicio timestamptz DEFAULT NOW(),
  ip_address text,
  user_agent text,
  fecha_fin timestamptz,
  logout_explicito boolean DEFAULT false
);

CREATE TABLE IF NOT EXISTS actividades (
  id uuid PRIMARY KEY,
  sesion_id uuid REFERENCES sesiones(id) ON DELETE CASCADE,
  usuario_id uuid REFERENCES usuarios(id) ON DELETE CASCADE,
  tipo_actividad text,
  descripcion text,
  ip_address text,
  fecha_hora timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS password_recovery (
  id uuid PRIMARY KEY,
  user_id uuid REFERENCES usuarios(id) ON DELETE CASCADE,
  token text,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pets (
  id uuid PRIMARY KEY,
  owner_id uuid NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  name text NOT NULL,
  species text,
  breed text NOT NULL,
  size text NOT NULL,
  age integer,
  weight numeric(5,2),
  description text,
  created_at timestamptz DEFAULT NOW(),
  updated_at timestamptz DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pets_owner_id ON pets(owner_id);
