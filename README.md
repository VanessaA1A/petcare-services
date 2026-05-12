# PetCare Services


Proyecto simple en Node.js que expone servicios web para gestión de usuarios, roles, autenticación, recuperación de contraseña, sesiones y registro de actividades.

Contexto persistente (especificación proporcionada):

1) Algoritmo de hashing
- El requisito original pedía "sha128". No existe un SHA-128 estándar. Tú pediste usar un polyfill; el enfoque correcto es usar una implementación que entregue un hash de 128 bits compatible con lo esperado.
- En esta versión de ejemplo se incluyó inicialmente MD5 para obtener 128 bits. Si prefieres explícitamente un "sha128" como polyfill, propongo usar una implementación JS que emule un SHA-2 truncado o una librería que implemente SHA-3 con truncado a 128 bits. Esto puede instalarse como dependencia y reemplazar `src/utils/hash.js`.

Recomendación de producción: usar bcrypt o argon2 con salt (no MD5). Si necesitas interoperabilidad con un sistema legado que usa un hash 128-bit específico, añado aquí cómo integrar un polyfill.

2) Esquema de base de datos existente (exacto, proporcionado por ti)

-- Opcional: crear tipo ENUM para roles (recomendado)
CREATE TYPE rol_usuario AS ENUM ('administrador', 'propietario', 'gestor');

CREATE TABLE usuarios (
		id SERIAL PRIMARY KEY,
		username VARCHAR(50) NOT NULL UNIQUE,
		email VARCHAR(100) NOT NULL UNIQUE,
		password_hash VARCHAR(255) NOT NULL,
		rol rol_usuario NOT NULL DEFAULT 'gestor',
		reset_token VARCHAR(255) NULL,
		reset_token_expires TIMESTAMP NULL,
		created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		last_login TIMESTAMP NULL,
		is_active BOOLEAN DEFAULT TRUE
);

-- Trigger para actualizar updated_at (opcional pero útil)
CREATE OR REPLACE FUNCTION actualizar_updated_at()
RETURNS TRIGGER AS $$
BEGIN
		NEW.updated_at = CURRENT_TIMESTAMP;
		RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_actualizar_usuarios_updated_at
BEFORE UPDATE ON usuarios
FOR EACH ROW EXECUTE FUNCTION actualizar_updated_at();

CREATE TABLE sesiones (
		id SERIAL PRIMARY KEY,
		usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
		token_sesion VARCHAR(255) NOT NULL UNIQUE,
		fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
		fecha_fin TIMESTAMP NULL,
		ip_address VARCHAR(45) NULL,
		user_agent TEXT NULL,
		logout_explicito BOOLEAN DEFAULT FALSE
);

CREATE TABLE actividades (
		id SERIAL PRIMARY KEY,
		sesion_id INT NOT NULL REFERENCES sesiones(id) ON DELETE CASCADE,
		usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
		fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
		tipo_actividad VARCHAR(50) NOT NULL,
		descripcion TEXT NULL,
		ip_address VARCHAR(45) NULL
);

CREATE TABLE roles (
		id SERIAL PRIMARY KEY,
		nombre VARCHAR(50) NOT NULL UNIQUE,
		descripcion TEXT NULL,
		created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

3) Mapeo entre el código actual y tus tablas
- En el código actual (src/controllers/*) usé tablas con nombres en inglés (`users`, `sessions`, `activities`, `user_roles`, `password_recovery`). Para que funcionen con tu esquema debes:
	- sustituir `users` -> `usuarios`
	- sustituir `sessions` -> `sesiones`
	- sustituir `activities` -> `actividades`
	- sustituir `user_roles`/`roles` -> `roles` (en tu esquema `roles` ya existe; la relación de usuario-roles puede implementarse con una tabla puente si la necesitas)
	- renombrar columnas: `password` -> `password_hash`, `user_id` -> `usuario_id`, etc.

4) Polyfill/implementación propuesta para "sha128"
- Opción A (compatibilidad con 128-bit): usar MD5 (ya implementado como placeholder en `src/utils/hash.js`). Fácil, pero inseguro.
- Opción B (polyfill solicitado): instalar una librería que permita SHA-3/Shake o un truncado de SHA-512 a 128 bits. Ejemplo con dependency `js-sha3` y truncado:

	1. Instalar: `npm install js-sha3`
	2. En `src/utils/hash.js`:
		 - importar `const { sha3_256, shake128 } = require('js-sha3');`
		 - usar `shake128(password, 16)` para obtener 128-bit (16 bytes) en hex.

	Esto emularía un "sha128" produciendo 128 bits con una función de la familia SHA-3. Es más seguro que MD5, pero aún así recomiendo bcrypt para contraseñas.

5) Pasos para adaptar el código a tu esquema
- Reemplazar consultas SQL en `src/controllers/usersController.js` y `src/controllers/authController.js` para usar nombres de tabla y columnas en español.
- Ajustar la generación y persistencia de token de sesión (`token_sesion`) y la relación con la tabla `sesiones`.
- Actualizar `src/utils/activity.js` para insertar en `sesiones` y `actividades` usando `SERIAL` ids (no UUIDs) o adaptar a UUID si prefieres.

6) Cómo ejecutar (PowerShell)
```powershell
cd C:\proyectos\petcare-services
# permitir ejecución de scripts si es necesario (ejecutar como Admin si no funciona)
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned -Force
npm install
npm start
```

7) Notas finales
- He persistido este contexto en el README para que el equipo lo tenga como fuente de verdad: algoritmo de hashing (polyfill), esquema SQL exactamente como me indicaste, y pasos para adaptar el código.
- Si quieres, puedo:
	- actualizar el código fuente automáticamente para usar tus tablas/columnas (haría cambios en `src/controllers/*`, `src/utils/activity.js`, y `migrations/schema.sql`), o
	- implementar el polyfill real con `js-sha3` y reemplazar el hashing MD5 actual por shake128 truncado a 128 bits.

Indícame cuál de las dos acciones prefieres y procedo a implementarla.
