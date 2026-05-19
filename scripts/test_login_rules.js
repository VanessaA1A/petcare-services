const db = require('../src/db');
const { hashPassword } = require('../src/utils/hash');
const http = require('http');

const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;
const TEST_EMAIL = 'login_test@example.com';
const TEST_PASSWORD = 'TestPass123';
const TEST_USERNAME = 'login_test_user';

function postLogin(email, password) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify({ email, password });
    const req = http.request(
      { hostname: 'localhost', port: PORT, path: '/api/auth/login', method: 'POST', headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data) } },
      res => {
        let d = '';
        res.on('data', c => (d += c));
        res.on('end', () => resolve({ statusCode: res.statusCode, body: d }));
      }
    );
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

(async () => {
  try {
    console.log('\n========== TEST DE LOGIN ==========\n');

    // Paso 1: Limpiar usuario de prueba si existe
    console.log('✓ Paso 1: Limpiando usuario anterior si existe...');
    try {
      await db.query('DELETE FROM usuarios WHERE email = $1', [TEST_EMAIL]);
      console.log(`  Usuario "${TEST_EMAIL}" eliminado (si existía).\n`);
    } catch (e) {
      console.log('  (Sin usuarios previos)\n');
    }

    // Paso 2: Crear usuario de prueba
    console.log('✓ Paso 2: Creando usuario de prueba...');
    const hashed = hashPassword(TEST_PASSWORD);
    const insertRes = await db.query(
      "INSERT INTO usuarios(username, email, password_hash, rol, created_at) VALUES($1,$2,$3,'propietario'::rol_usuario, NOW()) RETURNING id, email, rol",
      [TEST_USERNAME, TEST_EMAIL, hashed]
    );
    const userId = insertRes.rows[0].id;
    console.log(`  Usuario creado: ${TEST_EMAIL}`);
    console.log(`  ID: ${userId}`);
    console.log(`  Rol en BD: ${insertRes.rows[0].rol}\n`);

    // Paso 3: Intentar login con email correcto pero contraseña incorrecta
    console.log('✓ Paso 3: Login con email correcto pero contraseña INCORRECTA...');
    const wrongPassRes = await postLogin(TEST_EMAIL, 'WrongPassword123');
    console.log(`  Status: ${wrongPassRes.statusCode}`);
    console.log(`  Respuesta: ${wrongPassRes.body}`);
    const wrongPassBody = JSON.parse(wrongPassRes.body);
    if (wrongPassRes.statusCode === 401 && wrongPassBody.error === 'Invalid password') {
      console.log('  ✓ CORRECTO: Rechaza contraseña incorrecta\n');
    } else {
      console.log('  ✗ ERROR: Debería rechazar contraseña incorrecta\n');
    }

    // Paso 4: Intentar login con email que no existe
    console.log('✓ Paso 4: Login con email que NO EXISTE...');
    const noUserRes = await postLogin('nonexistent@example.com', TEST_PASSWORD);
    console.log(`  Status: ${noUserRes.statusCode}`);
    console.log(`  Respuesta: ${noUserRes.body}`);
    const noUserBody = JSON.parse(noUserRes.body);
    if (noUserRes.statusCode === 401 && noUserBody.error === 'Email not found') {
      console.log('  ✓ CORRECTO: Rechaza email no registrado\n');
    } else {
      console.log('  ✗ ERROR: Debería rechazar email no registrado\n');
    }

    // Paso 5: Login EXITOSO con credenciales correctas
    console.log('✓ Paso 5: Login con credenciales CORRECTAS...');
    const successRes = await postLogin(TEST_EMAIL, TEST_PASSWORD);
    console.log(`  Status: ${successRes.statusCode}`);
    const successBody = JSON.parse(successRes.body);
    console.log(`  Respuesta JSON:\n${JSON.stringify(successBody, null, 2)}\n`);

    if (successRes.statusCode === 200) {
      console.log('  ✓ CORRECTO: Status 200 OK\n');

      // Verificar estructura de respuesta
      if (successBody.user && successBody.session) {
        console.log('  ✓ CORRECTO: Respuesta tiene "user" y "session"\n');

        // Verificar que el rol está normalizado
        const user = successBody.user;
        console.log(`  Usuario retornado:`);
        console.log(`    - id: ${user.id}`);
        console.log(`    - email: ${user.email}`);
        console.log(`    - username: ${user.username}`);
        console.log(`    - rol: ${user.rol}`);
        console.log(`    - role: ${user.role}\n`);

        if (user.rol === 'OWNER' || user.rol === 'CAREGIVER') {
          console.log(`  ✓ CORRECTO: rol está normalizado a "${user.rol}"\n`);
        } else {
          console.log(`  ✗ ERROR: rol debe ser "OWNER" o "CAREGIVER", recibido: "${user.rol}"\n`);
        }

        if (user.role === user.rol) {
          console.log(`  ✓ CORRECTO: role también devuelto con valor "${user.role}"\n`);
        } else {
          console.log(`  ✗ ERROR: role no coincide con rol\n`);
        }

        // Verificar sesión
        const session = successBody.session;
        console.log(`  Sesión retornada:`);
        console.log(`    - id: ${session.id}`);
        console.log(`    - token_sesion: ${session.token_sesion}\n`);

        if (session.token_sesion) {
          console.log('  ✓ CORRECTO: Sesión creada con token\n');
        }
      } else {
        console.log('  ✗ ERROR: Respuesta no tiene estructura esperada\n');
      }
    } else {
      console.log(`  ✗ ERROR: Debería ser 200, recibido ${successRes.statusCode}\n`);
    }

    // Paso 6: Verificar que el mismo usuario puede loguearse múltiples veces
    console.log('✓ Paso 6: Intentar login NUEVAMENTE con mismo usuario...');
    const secondLoginRes = await postLogin(TEST_EMAIL, TEST_PASSWORD);
    console.log(`  Status: ${secondLoginRes.statusCode}`);
    if (secondLoginRes.statusCode === 200) {
      console.log('  ✓ CORRECTO: Usuario puede loguearse múltiples veces sin crear cuenta nueva\n');
      const secondBody = JSON.parse(secondLoginRes.body);
      if (secondBody.user.role === secondBody.user.rol && (secondBody.user.role === 'OWNER' || secondBody.user.role === 'CAREGIVER')) {
        console.log(`  ✓ CORRECTO: Segundo login también devuelve role normalizado "${secondBody.user.role}"\n`);
      }
    } else {
      console.log('  ✗ ERROR: Segundo login falló\n');
    }

    console.log('========== TEST COMPLETADO ==========\n');
  } catch (err) {
    console.error('Error:', err && err.stack ? err.stack : err);
    process.exit(1);
  } finally {
    process.exit(0);
  }
})();
