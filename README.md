# SistemaVetApi

API REST (Ktor + Kotlin) entre la app Android y Azure SQL.

## Variables de entorno necesarias (NO subir a Git)
- DB_SERVER   ej: sistemavet-tec.database.windows.net
- DB_NAME     ej: sistemavet
- DB_USER
- DB_PASSWORD

En Android Studio: Run > Edit Configurations > Environment variables.
Formato: DB_SERVER=...;DB_NAME=...;DB_USER=...;DB_PASSWORD=...

## Probar
Ejecutar `Application.kt` y abrir http://localhost:8080/health
