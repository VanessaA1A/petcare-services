# petcare-spring

Minimal Spring Boot skeleton for the PetCare API migration. Includes:

- `Pet` entity, `PetRepository`, `PetController`
- Reads `PORT` env to set server port (default 3000)
- Flyway configured (place migrations in `src/main/resources/db/migration`)

Build & run:

```bash
cd spring-backend
mvn -U clean package
java -jar target/petcare-spring-0.1.0.jar
```

Set DB connection via `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD` or via `spring.datasource.*` vars.
