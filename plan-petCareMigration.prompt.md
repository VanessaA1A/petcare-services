## Plan: Migración a Spring Boot

TL;DR - Qué, por qué y cómo:
Migrar solo la capa de servicios (API) de Express/TypeScript a Spring Boot manteniendo la base de datos y los modelos actuales. Usaremos Spring Web, Spring Data JPA, Flyway para migraciones (scripts), y Spring Security con JWT para autenticación. Mantendremos la funcionalidad existente (endpoints, validaciones, transacciones, logging de actividades) y adaptaremos tipos para UUIDs nativos en Java.

**Steps**

1. Discovery: revisar código y esquema SQL, identificar endpoints, modelos, validaciones y flujos de auth (_completado_).
2. Decisiones ya tomadas por el equipo (aplicar durante la migración):
   - Mantener los mismos modelos y la misma base de datos sin cambios estructurales en los tipos de ID (usar UUID como están hoy).
   - Usar UUID nativo de Java (`java.util.UUID`) — no se requiere librería externa para UUIDs en Spring Boot.
   - Autenticación: migrar a JWT (stateless) en lugar de sesiones en BD.
   - Passwords: usar `BCryptPasswordEncoder` para nuevos/actualizados; si fuera necesario un fallback temporal, soportar contraseñas en texto plano durante transición (no recomendado) y documentar la ventana de riesgo.
3. Diseño del esqueleto Spring Boot:
   - Crear proyecto Spring Boot (Java 17+ o 21) con dependencias: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `flyway-core`, `postgresql`, `spring-boot-starter-validation`, `jjwt` o `spring-security-oauth2-jose` para JWT, `lombok` (opcional), testing (JUnit, MockMvc, Testcontainers).
   - Estructura de paquetes: `controller`, `service`, `repository`, `model`/`entity`, `dto`, `mapper`, `config`, `exception`, `util`.
4. Preparación del esquema y scripts Flyway:
   - Revisar y limpiar `migrations/schema.sql` para eliminar inconsistencias detectadas.
   - Crear scripts Flyway basados en el esquema final (sin cambiar tipos de ID).
5. Mapear modelos a JPA entities y DTOs:
   - Entidades: `Usuario`, `Sesion` (solo si se mantiene histórico), `Actividad`, `Pet`, `PasswordRecovery`.
   - Usar `UUID` como tipo de campo `@Id` y `@Column(columnDefinition = "uuid")` cuando sea necesario; Hibernate y Postgres trabajan bien con `java.util.UUID`.
   - Implementar DTOs y mappers (MapStruct o mapeo manual). Mantener `PetViewModel` equivalente.
6. Implementar repositorios y servicios:
   - Repositorios `JpaRepository<Entidad, UUID>`.
   - Servicios con lógica: usuario (registro, login), auth (generación/validación JWT), pets (create, bulk @Transactional), actividades (log en BD).
7. Implementar controladores REST:
   - Reproducir rutas existentes usando `@RestController` y validaciones (`@Valid`, `@NotBlank`, `@Size`).
   - Mantener estatus HTTP y mensajes equivalentes; centralizar manejo de excepciones con `@ControllerAdvice`.
8. Seguridad y JWT:
   - Implementar `UserDetailsService` que cargue usuario desde DB.
   - Generar JWT firmado (HS256 o RS256 según preferencia) con claims mínimos (user id, roles, exp).
   - Añadir filtro que valide Authorization: Bearer <token> y cargue `SecurityContext`.
   - Implementar logout del lado cliente (eliminar token); si se requiere invalidación inmediata, añadir lista negra en DB o Redis (opcional).
9. Passwords y migración:
   - Usar `BCryptPasswordEncoder` para nuevos usuarios y cambios de contraseña.
   - Para usuarios existentes con MD5 u otro esquema, ofrecer autenticación que detecte esquema antiguo y re-hasheé a bcrypt en primer login.
   - Documentar ventana temporal si aceptamos contraseñas en claro como fallback.
10. Migraciones y datos:

- Ejecutar Flyway en entorno staging y validar tablas/constraints.
- No cambiar tipo de IDs ni migrar datos masivos (evitar conversiones UUID↔int).

11. Pruebas y verificación:

- Unit tests para servicios y mappers.
- Integration tests (Spring Boot Test + Testcontainers Postgres) para endpoints críticos (auth, pets bulk, create user).
- Validación manual con la colección Postman existente.

12. CI/CD y despliegue:

- Pipeline Maven/Gradle que ejecute Flyway, tests y empaquete artifact.
- Empaquetado en Docker image recomendada para despliegue.

13. Cutover y roll-back:

- Plan de coexistencia: desplegar API Spring Boot en paralelo, dirigir tráfico en entorno de staging, luego en producción usar mantenimiento breve para swap.
- Rollback: restaurar servicio previo y/o DB snapshot si fuera necesario.

**Relevant files**

- [src/index.ts](src/index.ts#L1) — entrada y static serving.
- [src/routes/pets.ts](src/routes/pets.ts#L1) — rutas pets.
- [src/controllers/petsController.ts](src/controllers/petsController.ts#L1) — lógica pets, bulk transaction.
- [src/controllers/authController.ts](src/controllers/authController.ts#L1) — login/me/logout/session handling.
- [migrations/schema.sql](migrations/schema.sql#L1) — esquema actual (limpiar inconsistencias antes de usar como base para Flyway).
- [src/db.ts](src/db.ts#L1) — pool Postgres; útil para entender queries y transacciones.
- [src/types/models.ts](src/types/models.ts#L1) — tipos TS que guían los DTOs/entidades.

**Verification**

1. Ejecutar Flyway migrations en staging y validar tablas/constraints.
2. Ejecutar tests unitarios e integración: `mvn test` o `gradle test`.
3. Probar endpoints críticos con Postman (importar [UAM Ari.postman_collection.json](UAM%20Ari.postman_collection.json)).
4. Verificar que JWT auth funciona y que roles/protecciones replican las reglas actuales.
5. Probar `POST /api/pets/bulk` en staging para asegurar transaccionalidad y performance.

**Decisions aplicadas**

- Modelos: se mantienen exactamente como están en la DB y en el código.
- IDs: usar `UUID` nativo de Java; no se cambia la DB.
- Auth: migración a JWT stateless.
- Passwords: implementar `BCryptPasswordEncoder`; si durante la implementación se detecta un bloqueo temporal, podremos habilitar soporte temporal para contraseñas en texto plano documentado (pero será una medida provisional).
- UUID library: no requerida; usar `java.util.UUID`.

**Further Considerations**

1. Implementar migración de hashes antiguos: detectar esquema antiguo y re-hashear a bcrypt en primer login.
2. Si se requiere invalidación inmediata de JWT, añadir mecanismo de blacklist con TTL (Redis recomendado).
3. Añadir pruebas de integración con Testcontainers para asegurar paridad con el comportamiento de Postgres.

---

He actualizado el plan con tus decisiones. ¿Quieres que genere el esqueleto del proyecto Spring Boot ahora (pom/gradle, estructura de paquetes y entidades iniciales) para empezar la implementación?
