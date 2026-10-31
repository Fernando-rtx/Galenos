# Estación local con una sola base

JSF y REST se ejecutan en el mismo WAR, puerto9080, datasource jdbc/pgdb conectado a postgres_db:5432/clinica.
La revisión compilada está en /private/tmp/galenos-crud-backend, rama feat/aplicacion-rest-completa.
El override privado de Liberty galenos-local-api.xml selecciona ese WAR y configura las identidades locales.
El archivo server.env privado original se conserva; ambos flags de demo están desactivados.

Roles: administrador-local tiene lectura/escritura/administración, lector-local solo lectura clínica.
Ambos tienen asignada explícitamente la única clínica existente. Las contraseñas están fuera de Git en
~/Library/Application Support/GalenosSV/credentials.json, con permisos600. No se usan credenciales PostgreSQL para autenticación web.

Enlaces:
- http://localhost:9080/GalenosSV/
- http://localhost:9080/GalenosSV/paginas/Consulta.xhtml
- http://localhost:3000 (menú Personas y catálogos; volver a Consultas tras iniciar sesión)
- http://localhost:9080/GalenosSV/resources/v2/contexto (BASIC requerido)
- http://localhost:5050

Encendido:
1. docker start postgres_db pgadmin4 (solo si están detenidos).
2. En el worktree backend: mvn package. Liberty utiliza target/GalenosSV.war.
3. ~/openliberty/wlp/bin/server run GalenosSV (solo si9080 está libre).
4. En GalenoSVFE: npm run dev (solo si3000 está libre).

Apagado: Ctrl+C en BrowserSync y Liberty, o ~/openliberty/wlp/bin/server stop GalenosSV.
No detener PostgreSQL compartido ni borrar volúmenes. El demo anterior permanece conservado; el frontend ya no lo utiliza.

Verificación de esta integración: JUnit, npm test/check, HTTP401/403 y lectura autenticada.
Prueba explícita de escritura test/browser/clinica_desarrollo.py: crea únicamente registros DESARROLLO,
no limpia tablas; verifica navegador real Chromium, recuperación HTTP y SELECT independiente en clinica.
No ejecutar suites test/crud sintéticas contra clinica: pertenecen al perfil anterior y sus fixtures.
