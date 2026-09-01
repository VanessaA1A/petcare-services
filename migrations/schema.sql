-- PetCare Services schema init

DROP TABLE IF EXISTS password_recovery CASCADE;
DROP TABLE IF EXISTS actividades CASCADE;
DROP TABLE IF EXISTS sesiones CASCADE;
DROP TABLE IF EXISTS pets CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TYPE IF EXISTS rol_usuario;

CREATE TYPE rol_usuario AS ENUM ('administrador', 'propietario', 'gestor');

CREATE TABLE usuarios (
  id serial PRIMARY KEY,
  username text UNIQUE NOT NULL,
  email text UNIQUE NOT NULL,
  password_hash text NOT NULL,
  rol rol_usuario NOT NULL DEFAULT 'gestor',
  rol_confirmado boolean NOT NULL DEFAULT false,
  latitud double precision,
  longitud double precision,
  direccion_texto text,
  nombre text,
  apellido text,
  telefono text,
  foto_perfil_filename text,
  foto_perfil_url text,
  created_at timestamptz DEFAULT NOW(),
  last_login timestamptz,
  is_active boolean DEFAULT true,
  reset_token text,
  reset_token_expires timestamptz
);

CREATE TABLE sesiones (
  id serial PRIMARY KEY,
  usuario_id integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  token_sesion text UNIQUE NOT NULL,
  fecha_inicio timestamptz DEFAULT NOW(),
  ip_address text,
  user_agent text,
  fecha_fin timestamptz,
  logout_explicito boolean DEFAULT false
);

CREATE TABLE actividades (
  id serial PRIMARY KEY,
  sesion_id integer REFERENCES sesiones(id) ON DELETE CASCADE,
  usuario_id integer REFERENCES usuarios(id) ON DELETE CASCADE,
  tipo_actividad text,
  descripcion text,
  ip_address text,
  fecha_hora timestamptz DEFAULT NOW()
);

CREATE TABLE password_recovery (
  id serial PRIMARY KEY,
  user_id integer REFERENCES usuarios(id) ON DELETE CASCADE,
  token text,
  created_at timestamptz DEFAULT NOW()
);

CREATE TABLE pets (
  id serial PRIMARY KEY,
  owner_id integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
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


CREATE TABLE offered_services (
  id serial PRIMARY KEY,
  caregiver_id integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  service_type_id integer NOT NULL,
  title text NOT NULL,
  description text,
  price numeric(10,2) NOT NULL,
  is_available boolean NOT NULL DEFAULT true,
  latitude double precision,
  longitude double precision,
  created_at timestamptz DEFAULT NOW(),
  updated_at timestamptz DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_offered_services_caregiver_id ON offered_services(caregiver_id);
CREATE INDEX IF NOT EXISTS idx_offered_services_available ON offered_services(is_available);

CREATE TABLE service_requests (
  id integer PRIMARY KEY,
  owner_id integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  pet_id integer NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
  pet_ids text,
  service_type_id integer NOT NULL,
  title text NOT NULL,
  description text,
  requested_date text,
  start_time text,
  end_time text,
  status text NOT NULL DEFAULT 'PENDING',
  offered_service_id integer REFERENCES offered_services(id) ON DELETE SET NULL,
  source_type text NOT NULL DEFAULT 'OPEN',
  latitude double precision,
  longitude double precision,
  created_at timestamptz DEFAULT NOW(),
  updated_at timestamptz DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_service_requests_owner_id ON service_requests(owner_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_status ON service_requests(status);

CREATE TABLE service_applications (
  id serial PRIMARY KEY,
  service_request_id integer NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
  caregiver_id integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  offered_service_id integer REFERENCES offered_services(id) ON DELETE SET NULL,
  initiated_by text NOT NULL DEFAULT 'CAREGIVER',
  status text NOT NULL DEFAULT 'PENDING',
  created_at timestamptz DEFAULT NOW(),
  updated_at timestamptz DEFAULT NOW(),
  UNIQUE(service_request_id, caregiver_id)
);

CREATE INDEX IF NOT EXISTS idx_service_applications_caregiver_id ON service_applications(caregiver_id);
CREATE INDEX IF NOT EXISTS idx_service_applications_request_id ON service_applications(service_request_id);


CREATE TABLE ratings (
  id serial PRIMARY KEY,
  service_request_id integer NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
  caregiver_id integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  owner_id integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  rated_by_role text NOT NULL DEFAULT 'OWNER',
  score numeric(2,1) NOT NULL,
  comment text,
  created_at timestamptz DEFAULT NOW(),
  UNIQUE(service_request_id, rated_by_role)
);

CREATE INDEX IF NOT EXISTS idx_ratings_caregiver_id ON ratings(caregiver_id);
CREATE INDEX IF NOT EXISTS idx_ratings_owner_id ON ratings(owner_id);
