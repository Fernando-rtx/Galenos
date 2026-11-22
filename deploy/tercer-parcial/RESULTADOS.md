# Pruebas locales del tercer parcial

Entorno de prueba del 21 de noviembre de 2026. Mac Apple Silicon, Docker Desktop 29.8.0, Buildx y Chromium automatizado. Las dos imágenes se construyeron localmente para cada plataforma. El backend usó una copia local del ZIP oficial de Open Liberty, cotejada con Maven Central y validada por SHA-256 durante el build.

## Resultados por plataforma

| Comprobación | ARM64 | AMD64 |
| --- | --- | --- |
| Backend y frontend construidos y cargados en Docker | Sí | Sí |
| Arquitectura en los contenedores | aarch64 | x86_64 |
| Usuario en ambos servicios | usuario, UID 1000 | usuario, UID 1000 |
| Debian, Java, Open Liberty, Node.js | 13.7, 21.0.13.1, 26.0.0.8, 22.23.5 | 13.7, 21.0.13.1, 26.0.0.8, 22.23.5 |
| Montajes de solo lectura, red ppi115net y puertos 80/9080 | Sí | Sí |
| WAR desplegado una vez desde dropins, JPA, JDBC y JSF/PrimeFaces | Sí | Sí |
| Catálogo desde el frontend por el FQDN interno del backend | 200, dos registros | 200, dos registros con otros UUID |
| Preflight de origen admitido y rechazo de origen ajeno | 204 / 403 | 204 / 403 |
| Chromium con paciente, empleado, clínica, citas, dashboard, bandeja, atención y descarga de ticket | Sí | Sí |
| Escritura verificada directamente en PostgreSQL | galeno_eval_2026, EN_CURSO | galeno_eval_amd64, EN_CURSO |
| Shell offline y ausencia de REST o tickets en Cache Storage | Sí | Sí |
| Publicada en Docker Hub | Pendiente | Pendiente |

Maven ejecutó 847 pruebas y produjo un WAR nuevo. El frontend aprobó 427 unitarias, 9 de transporte y 3 del servidor Node. Se validaron por separado las cinco variables obligatorias y el rechazo de puertos inválidos. El mecanismo de ZIP se ensayó con repositorios ficticios y produjo tres entradas en el ZIP principal y solo `src/` en el interno.

Chromium accedió por la IP LAN bajo el nombre de frontend y, con una resolución DNS de ensayo, también usó `http://galeno.backend:9080/galeno/resources/` como URL efectiva. La segunda prueba cambió la URL pública en la misma sesión con el service worker activo. Un cliente en una red Docker distinta alcanzó los puertos publicados. El DNS que suministre el entorno del docente y una segunda computadora física quedan por comprobar.

## Incidencias

- El PDF fallaba con `UnsatisfiedLinkError: libharfbuzz.so.0` en la primera imagen Debian. Se resolvió incluyendo fontconfig, fuentes DejaVu y HarfBuzz. Las pruebas de descarga y apertura del PDF pasaron en ambas plataformas.
- Un clic automatizado antes de concluir la apertura del selector PrimeFaces lanzaba un error en el widget. Se reprodujo con el WAR original y con el WAR de la rama. La prueba espera que termine la apertura y luego verifica la petición AJAX y el selector dependiente.
- Liberty emitió avisos de MyFaces en modo de desarrollo y de EclipseLink sobre MBean Server. El ensayo funcional corrigió los errores de la primera imagen y completó las operaciones probadas.

PostgreSQL de prueba usó dos bases ficticias en un contenedor aislado. El Docker Hub y el ZIP definitivo quedan fuera de este ensayo local.
