# PetCare Services

![Backend CI](https://github.com/VanessaA1A/petcare-services/actions/workflows/build.yml/badge.svg)

Este repositorio ahora contiene un backend completo en Kotlin con Spring Boot.

## Estructura del proyecto
- `build.gradle` / `settings.gradle`: configuración del proyecto Spring Boot
- `gradlew`, `gradlew.bat`, `gradle/wrapper`: Gradle Wrapper para ejecutar el proyecto sin instalar Gradle globalmente
- `src/main/kotlin/com/petcare`: controladores, servicios, repositorios, modelos y utilidades
- `src/main/resources/application.yml`: configuración de conexión a PostgreSQL y JSON
- `src/main/resources/static`: páginas estáticas de frontend (`login.html`, `register.html`, `owner-home.html`, `caregiver-home.html`)
- `migrations/schema.sql`: esquema de base de datos

## Endpoints principales

La lista completa y siempre actualizada está en Swagger (`/swagger-ui.html`) — esta sección
agrupa las áreas principales por funcionalidad.

**Auth y usuarios**
- `POST /api/auth/registro`, `POST /api/auth/login`, `POST /api/auth/recover`, `GET /api/auth/me`
- `POST /api/auth/send-otp`, `POST /api/auth/verify-otp`
- `GET/PUT/DELETE /api/users/{id}`, `POST /api/users/{id}/roles`
- `GET/POST/PUT/DELETE /api/usuarios/...` (perfil, foto de perfil, `GET /api/usuarios/{id}/badge`)

**Mascotas**
- `GET/POST/PUT/DELETE /api/pets/...`
- `GET/POST/PUT/DELETE /api/pets/{id}/expediente`, `/api/pets/expediente/{entradaId}` — expediente médico (Bloque 11)

**Solicitudes y servicios ofrecidos**
- `GET/POST/PUT /api/service-requests/...`, `/api/solicitudes/...` (edición, extensión, reasignación, historial, búsqueda)
- `GET/POST/PUT/DELETE /api/offered-services/...`
- `POST/GET /api/solicitudes/{id}/ubicacion` — ubicación en vivo (taxi/paseo)
- `POST /api/solicitudes/{id}/valorar-durante` — reacción rápida en tiempo real (Bloque 3)
- `POST /api/solicitudes/{id}/evidencia`, `GET /api/solicitudes/{id}/evidencias` — foto antes/después (Bloque 8)

**Chat**
- `POST /api/chat/mensajes`, `GET /api/chat/mensajes/{serviceRequestId}`, `PUT /api/chat/mensajes/leidos`
- `POST /api/chat/{serviceRequestId}/imagen`, `GET /api/chat/{serviceRequestId}/imagenes` — chat con fotos (Bloque 3)

**Calificaciones y emergencias**
- `GET/POST /api/ratings/...`
- `POST/GET /api/emergencias` — botón de emergencia (Bloque 3)

**Geolocalización**
- `GET /api/geo/geocode`, `/api/geo/solicitudes-cercanas`, `/api/geo/ofertas-cercanas`, `/api/geo/cuidadores-cercanos`

**Calendario**
- `GET /api/calendario?usuario_id=&mes=&anio=` — servicios programados del usuario (Bloque 9)

**Mascota perdida**
- `POST /api/alertas-perdida`, `POST /api/alertas-perdida/{id}/avistamiento`, `GET /api/alertas-perdida/{id}/avistamientos`, `PUT /api/alertas-perdida/{id}/encontrada`, `GET /api/alertas-perdida/cercanas` (Bloque 12)

## Ejecutar el proyecto
1. Configura tu base de datos PostgreSQL.
2. Actualiza `src/main/resources/application.yml` con los datos de conexión.
3. En la raíz del proyecto:
```powershell
./gradlew bootRun
```

## Ejecutar con Docker
1. Copia `.env.example` a `.env` y ajusta las contraseñas/secretos.
2. Levanta todo el stack (Postgres + backend + pgAdmin):
```powershell
docker compose up --build
```
3. La API queda en `http://localhost:8080`, pgAdmin en `http://localhost:5050`.
4. La base de datos se inicializa automáticamente con `database/petcare_restore.sql` (esquema + datos de prueba) en el primer arranque del contenedor `db`.

## Pruebas
```powershell
./gradlew test
```
Genera un reporte de cobertura con JaCoCo en `build/reports/jacoco/test/html/index.html`.

## Tests de integración

Además de las pruebas unitarias, `src/test/kotlin/com/petcare/IntegrationTest.kt` levanta el
contexto completo de Spring Boot (`@SpringBootTest`, puerto aleatorio) y ejercita el flujo real de
la API con `TestRestTemplate` contra una base de datos **PostgreSQL local real** (`petcare_test`):
registro de un propietario y un cuidador, login (JWT), publicación de una solicitud de servicio,
postulación del cuidador, aceptación, intercambio de mensajes de chat, marcado del servicio como
completado y envío de una calificación — verificando en cada paso el código HTTP esperado (incluye
también un par de casos negativos: 400/401/404).

**Nota importante:** este proyecto **no usa TestContainers** para estas pruebas porque en este
entorno no hay Docker disponible. En su lugar, las pruebas corren contra una instancia de Postgres
local de verdad (sin mocks ni base de datos en memoria).

### 1. Preparar la base de datos `petcare_test`

Con PostgreSQL instalado y corriendo localmente:

```powershell
# Crear la base de datos de pruebas (una sola vez)
psql -h localhost -U postgres -c "CREATE DATABASE petcare_test;"

# Aplicar el esquema actual
psql -h localhost -U postgres -d petcare_test -f src/main/resources/schema.sql
```

Cada vez que `src/main/resources/schema.sql` cambie, hay que volver a aplicarlo sobre
`petcare_test` de la misma forma.

### 2. Ejecutar las pruebas de integración

`src/test/resources/application-test.yml` apunta a `jdbc:postgresql://localhost:5432/petcare_test`
con el usuario `postgres`. La contraseña **nunca** se hardcodea en ese archivo: se pasa como
variable de entorno `DB_PASSWORD` al invocar Gradle.

```powershell
# PowerShell
$env:DB_PASSWORD='tu_password_local'; ./gradlew test --tests "*IntegrationTest*"
```

```bash
# Git Bash / bash
DB_PASSWORD=tu_password_local ./gradlew test --tests "*IntegrationTest*"
```

Cada corrida genera emails/usuarios únicos (con un sufijo de timestamp), así que se puede volver a
ejecutar la suite varias veces sobre la misma base sin chocar con las restricciones UNIQUE de
email/username.

## Documentación de la API (Swagger)
Con el backend corriendo (local o en Docker), la documentación interactiva está disponible en:
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/api-docs` (spec OpenAPI en JSON)

## Notas
- Este proyecto ya no depende del backend JavaScript/TypeScript previo.
- Si quieres, puedo limpiar los archivos legacy restantes y dejar solo el backend Kotlin/Spring Boot.
