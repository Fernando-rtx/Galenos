# Preparación de entrega GT02

Repositorios: https://github.com/Fernando-rtx/GalenoSVFE y https://github.com/Fernando-rtx/Galenos.
Bases auditadas: frontend `ebfa0c3220bca477dba1f0ca5967526390e97ff8`, backend `ce23fd3c14c93cb9036551a8c7ca0179d618f8f0`. Correcciones en ramas `audit/gt02-final-2026`; consultar `git log -1` para commits finales. Esta auditoría no integra ni publica main.

## Requisitos y clonación

Git, JDK 21, Maven, Node >=22 y npm, Open Liberty 26.0.0.8, PostgreSQL local con `clinica` y sus 20 tablas oficiales. Playwright/Chromium son herramientas opcionales de desarrollo, no recursos de la SPA.

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

Asignar responsables para instalar/abrir PWA, VoiceOver/teclado/contraste, datos sintéticos y escritura clínica controlada. Corregir o sustituir el runner Python PWA antiguo que depende de `componentes.html`. Revisar las cuatro alertas npm de desarrollo (una alta), con actualización compatible y pruebas. Adjuntar [auditoría](auditoria-final-gt02.md), evidencias, commits y [manual](manual-pruebas-finales.md); no presentar 10/10 mientras haya observables NO VERIFICADO o PARCIAL.
