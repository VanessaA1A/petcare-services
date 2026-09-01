--
-- PostgreSQL database dump
--

\restrict 8RRowgK8ub1wThUZs1WDTZcqpwDANYKQ6WAWLFwuxHogKUgELKDHZwyUEAH0g94

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.usuarios DROP CONSTRAINT IF EXISTS usuarios_pkey;
ALTER TABLE IF EXISTS ONLY public.usuarios DROP CONSTRAINT IF EXISTS uk_m2dvbwfge291euvmk6vkkocao;
ALTER TABLE IF EXISTS ONLY public.usuarios DROP CONSTRAINT IF EXISTS uk_kfsp0s1tflm1cwlj8idhqsad0;
ALTER TABLE IF EXISTS ONLY public.sesiones DROP CONSTRAINT IF EXISTS uk_jvfinfmkxx3ng0gv41sdjnv5p;
ALTER TABLE IF EXISTS ONLY public.ratings DROP CONSTRAINT IF EXISTS uk9ej9s35yp2yuj3xqu1r9r40ac;
ALTER TABLE IF EXISTS ONLY public.service_applications DROP CONSTRAINT IF EXISTS uk2dge2g8e2xeb6pwr277b4jv8y;
ALTER TABLE IF EXISTS ONLY public.sesiones DROP CONSTRAINT IF EXISTS sesiones_pkey;
ALTER TABLE IF EXISTS ONLY public.service_requests DROP CONSTRAINT IF EXISTS service_requests_pkey;
ALTER TABLE IF EXISTS ONLY public.service_applications DROP CONSTRAINT IF EXISTS service_applications_pkey;
ALTER TABLE IF EXISTS ONLY public.ratings DROP CONSTRAINT IF EXISTS ratings_pkey;
ALTER TABLE IF EXISTS ONLY public.pets DROP CONSTRAINT IF EXISTS pets_pkey;
ALTER TABLE IF EXISTS ONLY public.offered_services DROP CONSTRAINT IF EXISTS offered_services_pkey;
ALTER TABLE IF EXISTS ONLY public.actividades DROP CONSTRAINT IF EXISTS actividades_pkey;
ALTER TABLE IF EXISTS public.usuarios ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.sesiones ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.service_applications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.ratings ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.pets ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.offered_services ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.actividades ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.usuarios_id_seq;
DROP TABLE IF EXISTS public.usuarios;
DROP SEQUENCE IF EXISTS public.sesiones_id_seq;
DROP TABLE IF EXISTS public.sesiones;
DROP TABLE IF EXISTS public.service_requests;
DROP SEQUENCE IF EXISTS public.service_applications_id_seq;
DROP TABLE IF EXISTS public.service_applications;
DROP SEQUENCE IF EXISTS public.ratings_id_seq;
DROP TABLE IF EXISTS public.ratings;
DROP SEQUENCE IF EXISTS public.pets_id_seq;
DROP TABLE IF EXISTS public.pets;
DROP SEQUENCE IF EXISTS public.offered_services_id_seq;
DROP TABLE IF EXISTS public.offered_services;
DROP SEQUENCE IF EXISTS public.actividades_id_seq;
DROP TABLE IF EXISTS public.actividades;
DROP TYPE IF EXISTS public.rol_usuario;
--
-- Name: rol_usuario; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.rol_usuario AS ENUM (
    'administrador',
    'propietario',
    'gestor'
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: actividades; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.actividades (
    id integer NOT NULL,
    descripcion character varying(255),
    fecha_hora timestamp(6) with time zone,
    ip_address character varying(255),
    sesion_id integer,
    tipo_actividad character varying(255),
    usuario_id integer
);


--
-- Name: actividades_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.actividades_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: actividades_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.actividades_id_seq OWNED BY public.actividades.id;


--
-- Name: offered_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.offered_services (
    id integer NOT NULL,
    caregiver_id integer NOT NULL,
    created_at timestamp(6) with time zone,
    description character varying(255),
    is_available boolean NOT NULL,
    latitude double precision,
    longitude double precision,
    price double precision NOT NULL,
    service_type_id integer NOT NULL,
    title character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone
);


--
-- Name: offered_services_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.offered_services_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: offered_services_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.offered_services_id_seq OWNED BY public.offered_services.id;


--
-- Name: pets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pets (
    id integer NOT NULL,
    age integer,
    breed character varying(255),
    created_at timestamp(6) with time zone,
    description character varying(255),
    name character varying(255) NOT NULL,
    owner_id integer NOT NULL,
    size character varying(255),
    species character varying(255),
    updated_at timestamp(6) with time zone,
    weight numeric(38,2)
);


--
-- Name: pets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pets_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: pets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pets_id_seq OWNED BY public.pets.id;


--
-- Name: ratings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ratings (
    id integer NOT NULL,
    caregiver_id integer NOT NULL,
    comment text,
    created_at timestamp(6) with time zone,
    owner_id integer NOT NULL,
    rated_by_role character varying(255) NOT NULL,
    score double precision NOT NULL,
    service_request_id integer NOT NULL
);


--
-- Name: ratings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ratings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ratings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ratings_id_seq OWNED BY public.ratings.id;


--
-- Name: service_applications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_applications (
    id integer NOT NULL,
    caregiver_id integer NOT NULL,
    created_at timestamp(6) with time zone,
    initiated_by character varying(255) NOT NULL,
    offered_service_id integer,
    service_request_id integer NOT NULL,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone
);


--
-- Name: service_applications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_applications_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_applications_id_seq OWNED BY public.service_applications.id;


--
-- Name: service_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_requests (
    id integer NOT NULL,
    created_at timestamp(6) with time zone,
    description text,
    end_time character varying(255),
    latitude double precision,
    longitude double precision,
    offered_service_id integer,
    owner_id integer NOT NULL,
    pet_id integer NOT NULL,
    pet_ids character varying(255),
    requested_date character varying(255),
    service_type_id integer NOT NULL,
    source_type character varying(255) NOT NULL,
    start_time character varying(255),
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone
);


--
-- Name: sesiones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sesiones (
    id integer NOT NULL,
    fecha_fin timestamp(6) with time zone,
    fecha_inicio timestamp(6) with time zone,
    ip_address character varying(255),
    logout_explicito boolean,
    token_sesion character varying(255) NOT NULL,
    user_agent character varying(255),
    usuario_id integer NOT NULL
);


--
-- Name: sesiones_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sesiones_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sesiones_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sesiones_id_seq OWNED BY public.sesiones.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id integer NOT NULL,
    apellido character varying(255),
    created_at timestamp(6) with time zone,
    email character varying(255) NOT NULL,
    foto_perfil_filename character varying(255),
    foto_perfil_url character varying(255),
    is_active boolean,
    last_login timestamp(6) with time zone,
    nombre character varying(255),
    password_hash character varying(255) NOT NULL,
    reset_token character varying(255),
    reset_token_expires timestamp(6) with time zone,
    rol public.rol_usuario,
    telefono character varying(255),
    username character varying(255) NOT NULL
);


--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: actividades id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.actividades ALTER COLUMN id SET DEFAULT nextval('public.actividades_id_seq'::regclass);


--
-- Name: offered_services id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offered_services ALTER COLUMN id SET DEFAULT nextval('public.offered_services_id_seq'::regclass);


--
-- Name: pets id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pets ALTER COLUMN id SET DEFAULT nextval('public.pets_id_seq'::regclass);


--
-- Name: ratings id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ratings ALTER COLUMN id SET DEFAULT nextval('public.ratings_id_seq'::regclass);


--
-- Name: service_applications id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_applications ALTER COLUMN id SET DEFAULT nextval('public.service_applications_id_seq'::regclass);


--
-- Name: sesiones id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sesiones ALTER COLUMN id SET DEFAULT nextval('public.sesiones_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Data for Name: offered_services; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.offered_services (id, caregiver_id, created_at, description, is_available, latitude, longitude, price, service_type_id, title, updated_at) FROM stdin;
\.


--
-- Data for Name: pets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pets (id, age, breed, created_at, description, name, owner_id, size, species, updated_at, weight) FROM stdin;
4	\N	pastor	2026-06-29 23:33:44.418622-06	\N	mati	1	S (5-10 kg)	Dog	2026-06-29 23:33:44.418622-06	\N
2	\N	labrador	2026-06-29 23:24:35.227481-06	\N	bandi	1	M (10-20 kg)	Dog	2026-06-29 23:24:35.227481-06	\N
3	\N	hibrido	2026-06-29 23:26:37.018535-06	\N	lobo	1	L (20-40 kg)	Dog	2026-06-29 23:26:37.018535-06	\N
\.


--
-- Data for Name: ratings; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.ratings (id, caregiver_id, comment, created_at, owner_id, rated_by_role, score, service_request_id) FROM stdin;
1	2	Muy bueno	2026-06-30 11:18:10.749651-06	1	CAREGIVER	4	1001
2	2	\N	\N	1	OWNER	5	1001
\.


--
-- Data for Name: service_applications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.service_applications (id, caregiver_id, created_at, initiated_by, offered_service_id, service_request_id, status, updated_at) FROM stdin;
2	2	2026-06-30 09:51:55.854995-06	CAREGIVER	\N	421122079	ACCEPTED	2026-06-30 10:36:46.317466-06
4	2	2026-06-30 10:42:39.797112-06	CAREGIVER	\N	388904027	ACCEPTED	2026-06-30 11:17:10.461802-06
6	2	2026-06-30 11:33:56.939302-06	CAREGIVER	\N	429299624	ACCEPTED	2026-06-30 11:35:20.601828-06
3	2	2026-06-30 10:42:34.033834-06	CAREGIVER	\N	389246867	REJECTED	2026-06-30 11:35:22.030233-06
5	2	2026-06-30 10:59:52.572421-06	CAREGIVER	\N	427279718	REJECTED	2026-06-30 11:35:24.493633-06
1	2	\N	CAREGIVER	\N	1001	COMPLETED	2026-06-30 12:06:30.17627-06
\.


--
-- Data for Name: service_requests; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.service_requests (id, created_at, description, end_time, latitude, longitude, offered_service_id, owner_id, pet_id, pet_ids, requested_date, service_type_id, source_type, start_time, status, title, updated_at) FROM stdin;
387391761	2026-06-29 23:53:42.020267-06	\nUbicación: Parque Luis Alfonso\nPrecio: C$200	04:00	\N	\N	\N	1	4	4	01/07/2026	3	OPEN	02:00	PENDING	Paseo	2026-06-29 23:53:42.020267-06
389246867	2026-06-30 00:24:36.959313-06	Necesita transporte: No\nPrecio: C$100	01:05	\N	\N	\N	1	4	4	30/06/2026	2	OPEN	03:30	PENDING	Guardería	2026-06-30 00:24:36.959313-06
421122079	2026-06-30 09:15:54.305455-06	Fecha de salida: 10/07/2026\nNecesita transporte: No\nPrecio: C$150	\N	\N	\N	\N	1	3	3	01/07/2026	1	OPEN	\N	ACCEPTED	Alojamiento	2026-06-30 10:36:46.318348-06
427279718	2026-06-30 10:58:31.968406-06	Tipo de peluquería: Baño\nUbicaciÃ³n: Metrocentro, Paseo de la Unión Europea, Lomas De Guadalupe, Los Robles, Distrito I, Managua, 14101, Nicaragua\nPrecio: C$100	\N	12.127962	-86.2648812	\N	1	3	3	30/06/2026	496973443	OPEN	06:00	PENDING	Peluquería	2026-06-30 10:58:31.968406-06
428274947	2026-06-30 11:15:07.127012-06	Quiero que lo cuiden, es miedoso\nNecesita transporte: No\nPrecio: C$110	02:00	\N	\N	\N	1	3	3	30/06/2026	1817843334	OPEN	01:00	PENDING	Guardería	2026-06-30 11:15:07.127012-06
388904027	2026-06-30 00:18:54.127708-06	Fecha de salida: 30/06/2026\nNecesita transporte: No\nPrecio: C$150	\N	\N	\N	\N	1	4	4	30/06/2026	1	OPEN	\N	ACCEPTED	Alojamiento	2026-06-30 11:17:10.462811-06
429299624	2026-06-30 11:32:11.833583-06	Hola\nDirección de recogida: Plaza Inter, 8a Calle S.O., Explanada Loma de Tiscapa, Sector Gobierno, Distrito I, Managua, 11118, Nicaragua\nDirección de destino: Metrocentro, Paseo de la Unión Europea, Lomas De Guadalupe, Los Robles, Distrito I, Managua, 14101, Nicaragua\nIda y vuelta: No\nUbicaciÃ³n: Plaza Inter, 8a Calle S.O., Explanada Loma de Tiscapa, Sector Gobierno, Distrito I, Managua, 11118, Nicaragua\nPrecio: C$150	\N	12.1447482	-86.2739888	\N	1	4	4	30/06/2026	4	OPEN	06:00	ACCEPTED	Taxi	2026-06-30 11:35:20.601828-06
1001	2026-06-29 21:56:25.638753-06	Necesito paseo por la tarde	16:00	\N	\N	\N	1	1	1	30/06/2026	1	OPEN	15:00	COMPLETED	Paseo para Max	2026-06-30 12:06:30.231045-06
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.usuarios (id, apellido, created_at, email, foto_perfil_filename, foto_perfil_url, is_active, last_login, nombre, password_hash, reset_token, reset_token_expires, rol, telefono, username) FROM stdin;
2	\N	2026-06-29 21:51:23.621327-06	cuidador@petcare.com	\N	\N	t	2026-06-30 11:33:31.371939-06	\N	$2a$10$WRiIDQkosdR8Wfe5Ipo1QeZjCFy1ays5jtVDK3a4C54gdhIrwwX7W	\N	\N	gestor	\N	cuidador
1	\N	2026-06-29 21:26:01.819396-06	dueno@petscare.com	\N	\N	t	2026-06-30 11:34:37.784764-06	\N	$2a$10$upO9Nt.deZ/gPG4iyOBlmedPeswtoAPp4NfqTjb2nldrTgHGhTBLG	\N	\N	propietario	\N	dueno
\.


--
-- Name: actividades_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.actividades_id_seq', 23, true);


--
-- Name: offered_services_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.offered_services_id_seq', 1, false);


--
-- Name: pets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pets_id_seq', 4, true);


--
-- Name: ratings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ratings_id_seq', 2, true);


--
-- Name: service_applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.service_applications_id_seq', 6, true);


--
-- Name: sesiones_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sesiones_id_seq', 23, true);


--
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 2, true);


--
-- Name: actividades actividades_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.actividades
    ADD CONSTRAINT actividades_pkey PRIMARY KEY (id);


--
-- Name: offered_services offered_services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offered_services
    ADD CONSTRAINT offered_services_pkey PRIMARY KEY (id);


--
-- Name: pets pets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pets
    ADD CONSTRAINT pets_pkey PRIMARY KEY (id);


--
-- Name: ratings ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_pkey PRIMARY KEY (id);


--
-- Name: service_applications service_applications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_applications
    ADD CONSTRAINT service_applications_pkey PRIMARY KEY (id);


--
-- Name: service_requests service_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_requests
    ADD CONSTRAINT service_requests_pkey PRIMARY KEY (id);


--
-- Name: sesiones sesiones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sesiones
    ADD CONSTRAINT sesiones_pkey PRIMARY KEY (id);


--
-- Name: service_applications uk2dge2g8e2xeb6pwr277b4jv8y; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_applications
    ADD CONSTRAINT uk2dge2g8e2xeb6pwr277b4jv8y UNIQUE (service_request_id, caregiver_id);


--
-- Name: ratings uk9ej9s35yp2yuj3xqu1r9r40ac; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT uk9ej9s35yp2yuj3xqu1r9r40ac UNIQUE (service_request_id, rated_by_role);


--
-- Name: sesiones uk_jvfinfmkxx3ng0gv41sdjnv5p; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sesiones
    ADD CONSTRAINT uk_jvfinfmkxx3ng0gv41sdjnv5p UNIQUE (token_sesion);


--
-- Name: usuarios uk_kfsp0s1tflm1cwlj8idhqsad0; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT uk_kfsp0s1tflm1cwlj8idhqsad0 UNIQUE (email);


--
-- Name: usuarios uk_m2dvbwfge291euvmk6vkkocao; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT uk_m2dvbwfge291euvmk6vkkocao UNIQUE (username);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Regla de negocio: un usuario no puede tener a la vez rol de propietario y de
-- cuidador. Los usuarios de este respaldo ya tienen un rol en uso, se marcan
-- confirmados; las cuentas nuevas nacen sin confirmar hasta elegir su rol.
--

ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS rol_confirmado boolean NOT NULL DEFAULT true;
ALTER TABLE public.usuarios ALTER COLUMN rol_confirmado SET DEFAULT false;

--
-- Ubicacion registrada del usuario, para calcular cercania con solicitudes/ofertas.
--

ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS latitud double precision;
ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS longitud double precision;
ALTER TABLE public.usuarios ADD COLUMN IF NOT EXISTS direccion_texto text;
CREATE INDEX IF NOT EXISTS idx_usuarios_ubicacion ON public.usuarios(latitud, longitud);

--
-- Chat interno entre propietario y cuidador, ligado a una solicitud de servicio.
--

CREATE TABLE IF NOT EXISTS public.chat_messages (
  id serial PRIMARY KEY,
  service_request_id integer NOT NULL REFERENCES public.service_requests(id) ON DELETE CASCADE,
  sender_id integer NOT NULL REFERENCES public.usuarios(id) ON DELETE CASCADE,
  receiver_id integer NOT NULL REFERENCES public.usuarios(id) ON DELETE CASCADE,
  message text NOT NULL,
  is_read boolean NOT NULL DEFAULT false,
  created_at timestamptz DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_chat_messages_service_request_id ON public.chat_messages(service_request_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_receiver_id ON public.chat_messages(receiver_id);

--
-- Verificacion de identidad por OTP (codigo de un solo uso enviado al correo).
--

CREATE TABLE IF NOT EXISTS public.verificaciones (
  id serial PRIMARY KEY,
  email text NOT NULL,
  otp text NOT NULL,
  fecha_expiracion timestamptz NOT NULL,
  usado boolean NOT NULL DEFAULT false,
  creado_en timestamptz DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_verificaciones_email ON public.verificaciones(email);

--
-- PostgreSQL database dump complete
--

\unrestrict 8RRowgK8ub1wThUZs1WDTZcqpwDANYKQ6WAWLFwuxHogKUgELKDHZwyUEAH0g94

