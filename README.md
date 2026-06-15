# PetCare Services

Este repositorio ahora contiene un backend completo en Kotlin con Spring Boot.

## Estructura del proyecto
- `build.gradle` / `settings.gradle`: configuración del proyecto Spring Boot
- `gradlew`, `gradlew.bat`, `gradle/wrapper`: Gradle Wrapper para ejecutar el proyecto sin instalar Gradle globalmente
- `src/main/kotlin/com/petcare`: controladores, servicios, repositorios, modelos y utilidades
- `src/main/resources/application.yml`: configuración de conexión a PostgreSQL y JSON
- `src/main/resources/static`: páginas estáticas de frontend (`login.html`, `register.html`, `owner-home.html`, `caregiver-home.html`)
- `migrations/schema.sql`: esquema de base de datos

## Endpoints principales
- `POST /api/auth/login`
- `POST /api/auth/recover`
- `GET /profile`
- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`
- `POST /api/users/{id}/roles`
- `GET /api/pets/owner/{owner_id}`
- `GET /api/pets/{id}`
- `POST /api/pets`
- `POST /api/pets/bulk`
- `PUT /api/pets/{id}`
- `DELETE /api/pets/{id}`
- `GET /api/pets/all`

## Ejecutar el proyecto
1. Configura tu base de datos PostgreSQL.
2. Actualiza `src/main/resources/application.yml` con los datos de conexión.
3. En la raíz del proyecto:
```powershell
./gradlew bootRun
```

## Notas
- Este proyecto ya no depende del backend JavaScript/TypeScript previo.
- Si quieres, puedo limpiar los archivos legacy restantes y dejar solo el backend Kotlin/Spring Boot.
