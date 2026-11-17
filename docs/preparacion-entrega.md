# Preparación de entrega GT02

Repositorios: https://github.com/Fernando-rtx/GalenoSVFE y https://github.com/Fernando-rtx/Galenos.
Bases auditadas: frontend `ebfa0c3220bca477dba1f0ca5967526390e97ff8`, backend `ce23fd3c14c93cb9036551a8c7ca0179d618f8f0`. Correcciones en ramas `audit/gt02-final-2026`; consultar `git log -1` para commits finales. La auditoría inicial no integró main; la publicación posterior sí dejó los commits indicados más abajo accesibles en GitHub.

## Requisitos y clonación

Git, JDK 21, Maven, Node >=22.12.0 y npm, Open Liberty 26.0.0.8, PostgreSQL local con `clinica` y sus 20 tablas oficiales. Playwright/Chromium son herramientas opcionales de desarrollo, no recursos de la SPA.

```sh
git clone https://github.com/Fernando-rtx/GalenoSVFE.git
git clone https://github.com/Fernando-rtx/Galenos.git
```

Después de publicar las ramas, el equipo debe indicar los commits exactos evaluables. No asumir que las correcciones locales ya existen en GitHub.

## Backend

En el repositorio Galenos: copiar `src/main/liberty/config/server.env.example` a `src/main/liberty/config/server.env`; configurar usuario/clave PostgreSQL local y mantener DB `clinica`, host localhost, puerto5432, `GALENOS_API_MODE=academic`, demos false y orígenes exactos CORS localhost/127.0.0.1:3000. No publicar el archivo. No se necesita una lista de UUID de roles; el servicio deriva asignaciones vigentes de JPA.

```sh
mvn clean test
mvn package
./run.sh
```

El pom usa una instalación existente por defecto en `$HOME/openliberty/wlp`; si está en otra ruta, indicar `-Dliberty.install.dir=/ruta/a/wlp` al invocar Maven. `package` copia PrimeFaces a esa instalación y genera `target/GalenosSV.war`. Liberty copia el driver PostgreSQL a jdbc. Puerto9080/contexto `/GalenosSV`. Revisar log de aplicación iniciada; un WAR construido por sí solo no demuestra conexión a la base.

## Frontend

```sh
npm ci
npm test
npm run check
npm run test:transport
npm run test:coverage
npm run dev
```

Abrir http://localhost:3000; default REST http://127.0.0.1:9080/GalenosSV/resources/. Si cambia backend, crear `src/js/control/environment.local.js` con `export const gestionConfig = { baseUrl: 'http://127.0.0.1:PUERTO/GalenosSV/resources/' };`. No incluir credenciales. Si cambia el origen del frontend, ajustar CORS de forma explícita. Verificar GET y OPTIONS con navegador; no usar comodines para ocultar errores.

Para navegadores de prueba usar Playwright instalado externamente. Los scripts .mjs aceptan `PLAYWRIGHT_MODULE`, `CHROMIUM_EXECUTABLE` (offline-shell usa `CHROMIUM_PATH`). HU2/HU3/HU4 readonly requieren `GALENOS_BACKEND_URL` apuntando al runtime19084 y `GALENOS_BACKEND_REPO`. Prepararlo con `scripts/gt02_readonly_runtime.py stage/run` únicamente después de revisar su configuración y con `clinica` existente. No ejecutar los scripts de flujos antiguos sin revisar si escriben.

## Datos y reproducibilidad

La clonación, instalación npm, tests, empaquetado y arranque BrowserSync fueron comprobados desde copias nuevas. El despliegue Liberty se verificó contra una `clinica` local preexistente, no contra una base creada por el clon.

Los repositorios no entregan por sí solos los datos académicos empleados en la demostración. `scripts/academic/` estaba sin seguimiento; su SQL contiene INSERT sobre UUID y catálogos existentes. No es un inicializador validado de un clon vacío, no se publicó ni ejecutó. El equipo debe preparar y revisar un conjunto exclusivamente sintético compatible con el esquema oficial, sus procedimientos/secuencias y asignaciones, y proporcionar instrucciones autorizadas de importación fuera de esta auditoría. No crear otra base como atajo. No compartir volcados con datos privados ni credenciales.

Antes de la revisión, confirmar al menos un paciente con citas, empleados por paso y clínica, rutas válidas con responsables activos, exámenes permitidos, y rangos de hoy/pasado/futuro. Las fechas fijas de noviembre no son “hoy” para siempre. La ejecución sin plantilla y las asignaciones insuficientes son bloqueos de datos que no se arreglan relajando permisos.

## Lista del equipo

Asignar responsables para instalar/abrir PWA, VoiceOver/teclado/contraste, datos sintéticos y escritura clínica controlada. El runner Python PWA delega ahora al runner Node vigente, con fixtures corregidos. Las cuatro alertas npm quedaron corregidas mediante lockfile y overrides limitados a Mocha, sin cambiar herramientas principales. Adjuntar [auditoría](auditoria-final-gt02.md), evidencias, commits y [manual](manual-pruebas-finales.md); no presentar 10/10 mientras haya observables NO VERIFICADO o PARCIAL.

## Correcciones entregables de esta revisión

Frontend: `6be1edc` (limpieza/fixtures/modularidad/pruebas), `2aae94e` (dependencias), `a773144` (contraste), `5859e14` (catálogo sintético), `dca5798` (placeholder Shadow DOM y configuración portátil). Usar `git log -1` para incluir también el último commit de documentación/test offline. Backend: reglas/DAO/JPA no modificadas; la documentación actualiza `582b3a2`. No se han publicado estas ramas; antes de la revisión de pares el equipo debe entregar los commits aprobados que sí estén accesibles en GitHub.

### Navegador sin conocimientos de la Mac del autor

Herramienta externa de pruebas validada: Playwright1.62.1. Mantenerla fuera de las dependencias de ejecución de la SPA:

```sh
npm install --prefix /tmp/galenos-browser-tools --no-save playwright@1.62.1
/tmp/galenos-browser-tools/node_modules/.bin/playwright install chromium
```

En el frontend:

```sh
PLAYWRIGHT_MODULE=/tmp/galenos-browser-tools/node_modules/playwright npm run test:browser
PLAYWRIGHT_MODULE=/tmp/galenos-browser-tools/node_modules/playwright node test/browser/offline-shell.mjs
```

El primero arranca su propio servidor temporal, prueba los seis fixtures y publica `coverage/browser/resultado.json` y capturas. No necesita Liberty ni PostgreSQL. El segundo verifica instalación nueva y detiene realmente su servidor antes de recargar. Para un Chromium instalado externamente, indicar CHROMIUM_EXECUTABLE en el runner principal y CHROMIUM_PATH en el test offline. El comando antiguo `python3 test/browser/webcomponents_pwa.py` sigue funcionando como delegación a Node.

Ejecutar después `npm run test:coverage`: genera informes temporales y distingue las fuentes de navegador cuyo hash coincide con el actual. No sumar las cifras V8 de unitarias con las del navegador. Los 19 archivos fuera de unitarias están enumerados y clasificados en el informe de auditoría; no faltan del repositorio.

### Preparación mínima de datos de demostración

1. Confirmar `clinica` existente con sus veinte tablas y esquema oficial, JDBC y orígenes CORS exactos. Configurar credenciales propias fuera de Git.
2. El equipo entrega por un canal autorizado los documentos **ficticios** del paciente y responsables, clínica/rol, UUID de consulta y rango de fechas. No hay contraseñas de paciente ni roles inventados por el frontend.
3. Para el caso ya usado de Luis, buscar15–16/11/2026 como Doctor asignado y contrastar el UUID de consulta del manual. La orden histórica es de una ejecución distinta de la actual.
4. No ejecutar automáticamente `scripts/academic/poblar_20261116.sql`: tiene105INSERT sin control de duplicados y depende de los catálogos locales. Mantenerlo sin seguimiento hasta preparar un plan idempotente, revisado y autorizado.
5. El residual `e0da3669-f37f-4890-ae80-41d5541d0c18` tiene dos plantillas candidatas; no completar su FK por conjetura. No bloquear ni relajar reglas para utilizarlo en la demostración. Preparar un caso válido con autorización si se necesitan datos adicionales.
6. Antes de mostrar hoy/futuro y múltiples asignaciones, verificar que existan esos registros. La base actual no tiene personas con varias asignaciones activas de empleado y los rangos del responsable comprobado no tenían consultas hoy/futuras. La creación autorizada de esos datos queda fuera de las verificaciones automáticas de solo lectura.

La instalación técnica desde clon fue verificada; la demostración clínica requiere estos datos explícitos. Esto se declara para evitar depender de archivos ignorados o de conocer el equipo del autor.

## Archivo de entrega en Classroom

El PDF oficial pide un archivo de texto con los nombres de los tres integrantes, identificador del commit y URL del repositorio que contiene ese commit. Completar los datos del equipo y los dos commits finales aprobados; no entregar referencias a ramas locales ni afirmar que los commits están publicados antes de subirlos con autorización.

Verificación final del clon local sin archivos ignorados: frontend código `dca5798`, 415 unitarias y122archivos de sintaxis válidos (124en el árbol local, que incluye dos configuraciones ignoradas). El commit frontend final con documentación es `7c48cc5`. Los archivos ignorados de configuración son opcionales/privados y no se requieren para instalar las dependencias ni ejecutar estas pruebas.

## Verificación estricta de lo publicado

Los SHA de main comprobados desde clones remotos son FE `7c48cc528575c911a18933440df13cea044ff4ad` y BE `4406204f04b5020465f16f5c4a2750a58f19c573`. Hacer checkout de esos SHA para evaluar exactamente el código del informe. Las frases sobre ramas todavía locales en apartados anteriores son históricas; la verificación nueva se entrega en `audit/gt02-verificacion-publicada`, sin actualizar main.

Para repetir el probe clínico de solo lectura con el archivo de verificación disponible en esa rama, configurar primero `src/main/liberty/config/server.env` con credenciales propias, localhost:5432/clinica y los prerrequisitos de Liberty:

```sh
mvn package
GT02_CONFIG="$PWD/src/main/liberty/config" python3 scripts/gt02_readonly_runtime.py stage
python3 scripts/gt02_readonly_runtime.py run
```

El último comando mantiene la instancia temporal19084. En otra terminal del backend:

```sh
python3 test/integration/gt02_hu_readonly.py --consulta e5bd6458-4469-5543-98b0-1a5b595559a3
python3 scripts/gt02_readonly_runtime.py stop
```

El helper SQL reutilizado requiere Docker y el contenedor existente `postgres_db` con clinica; no lo crea ni importa datos. No usar el comando de demo que crea otra base. El probe valida runtime readonly, tipo de consulta ACAD, sesiones de memoria y hashes de20tablas. No ejecuta DELETE ni registrar atención. Un cambio concurrente de otro usuario puede invalidar la igualdad de hashes y exige revisar la causa, no repetir escrituras. El runtime usa una instalación de Liberty existente; si está en otra ubicación indicar GT02_LIBERTY y ajustar la ruta Maven de forma coherente. No publicar server.env ni copiar datos privados para satisfacer el fixture.

La reproducción automática del navegador se hace con las dependencias ya documentadas y sin backend; GET clínicos reales y escrituras simuladas se reportan por separado. Para la regla de revisión secuencial, resolver primero el acta de instalación y la excepción del diálogo antes de confiar en una suma de puntos.
