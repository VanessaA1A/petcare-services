$ErrorActionPreference = "Stop"

$pgDump = $env:PG_DUMP
if (-not $pgDump) {
    $candidate = "C:\Program Files\PostgreSQL\18\bin\pg_dump.exe"
    if (Test-Path $candidate) {
        $pgDump = $candidate
    } else {
        $pgDump = "pg_dump"
    }
}

$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$dbName = if ($env:DB_NAME) { $env:DB_NAME } else { "PetCareBD" }
$dbUser = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "postgres" }

New-Item -ItemType Directory -Force -Path "database" | Out-Null

& $pgDump `
    -h $dbHost `
    -U $dbUser `
    -d $dbName `
    --no-owner `
    --no-privileges `
    --clean `
    --if-exists `
    --exclude-table-data=actividades `
    --exclude-table-data=sesiones `
    -f "database\petcare_restore.sql"

Write-Host "Respaldo actualizado en database\petcare_restore.sql"
