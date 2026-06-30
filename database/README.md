# Base de datos PetCare

Esta carpeta contiene el respaldo portable para que otra computadora pueda levantar la misma base de datos de desarrollo.

## Opcion recomendada: pgAdmin

1. Abrir pgAdmin y conectarse al servidor local de PostgreSQL.
2. Abrir Query Tool sobre la base `postgres`.
3. Ejecutar `database/00_create_database.sql`. Si `PetCareBD` ya existe, omitir este paso.
4. Cambiarse a la base `PetCareBD`.
5. Abrir y ejecutar `database/petcare_restore.sql`.
6. Ejecutar la API.

## Opcion con terminal

Desde la raiz del repo:

```powershell
psql -U postgres -d postgres -f database/00_create_database.sql
psql -U postgres -d PetCareBD -f database/petcare_restore.sql
```

Si PostgreSQL pide otra clave, no hay que cambiar el codigo. Se puede configurar la API con variables de entorno:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/PetCareBD"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="TU_CLAVE"
.\gradlew.bat bootRun
```

## Que trae el respaldo

- Usuarios de prueba.
- Mascotas.
- Servicios ofrecidos.
- Solicitudes de servicio.
- Postulaciones.
- Calificaciones.

No se exportan datos de sesiones ni actividad de login, porque no son necesarios para probar la app y pueden generar ruido.

## Actualizar el respaldo

Cuando la base local tenga cambios importantes, ejecutar:

```powershell
.\scripts\export-db.ps1
```

Eso vuelve a generar `database/petcare_restore.sql`.
