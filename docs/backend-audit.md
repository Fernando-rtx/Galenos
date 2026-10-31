# Auditoría para el primer recurso REST

Fecha: 2026-10-23. Backend `Fernando-rtx/Galenos`.

## Base de trabajo

Se encontró limpio el checkout `Cortez` (`480b325`). Después de `git fetch
origin`, `origin/main` avanzó a `97b9c2f`. Se creó
`feat/rest-tipo-documento` desde esa referencia, sin modificar `main` ni
sobrescribir trabajo local. El frontend vive en `Fernando-rtx/GalenoSVFE`,
carpeta local `GalenoFE`; la semilla y los contratos se consolidan en `main`.

Antes de implementar no se encontraron recursos Jakarta REST, ApplicationPath
ni filtros CORS en `origin/main`. `TipoDocumentoDAO` ya ofrecía búsqueda por
UUID, consulta paginada con orden y conteo; no fue necesario duplicar SQL ni
modificar el esquema existente. La entidad contiene una relación hacia
documentos que el DTO REST excluye explícitamente.

También se buscaron recursos en las referencias remotas descargadas. La rama
`origin/Itzep` contiene únicamente `JakartaEE11Resource`, un ejemplo
`@Path("jakartaee11")` que responde texto `ping Jakarta EE`; no es un recurso
de TipoDocumento o Consulta y no está integrado en `origin/main`. No se encontró
otro recurso REST de dominio que hubiera que recuperar antes de este lote.

## Entorno observado

| Elemento | Evidencia efectiva |
|---|---|
| Java instalado | Inicialmente OpenJDK 25.0.4.1; posteriormente se agregó Temurin 21.0.13+9 y se verificó la demo con ese JDK |
| Objetivo de compilación | release 21 del POM, conservado |
| Maven | 3.9.16 |
| Open Liberty instalado y usado | 26.0.0.8 |
| Jakarta EE | 11.0.0; webProfile-11.0 en Liberty |
| PrimeFaces | 15.0.17 Jakarta |
| JDBC PostgreSQL | 42.7.4 |
| PostgreSQL efectivo | 12.22 en contenedor local `postgres-local` |
| Node/npm observados | 24.17.0 / 11.13.0; tooling FE todavía no fijado |

El anterior documento de entorno mencionaba PostgreSQL 18.6; la instancia local
usada en este ensayo responde 12.22. `server.xml` define `clinica` como nombre
de base por defecto, pero el ejemplo de entorno decía `universidad`. Se alineó
el ejemplo con `server.xml`; no se cambió ninguna base existente.

## Cambios y límites

Se añadió REST de lectura para `TipoDocumento` con DTO, validación, errores JSON
y CORS configurable. No se añadieron dependencias Maven/npm, escrituras REST,
endpoints clínicos ni cambios de tablas de la aplicación. El WAR sigue
conteniendo JSF/PrimeFaces y se ejecutaron las pruebas Java existentes.

La integración se hizo contra una nueva base `galenos_rest_test_*` con solo dos
tipos de documento sintéticos, usando un servidor Liberty separado en 19080.
No se leyó el contenido de la base clínica ni se copiaron credenciales al repo.

## Pendiente para Consulta

La aplicación contiene selección de rol y clínica en la sesión JSF. Eso no
demuestra autenticación ni autorización HTTP de una API para datos clínicos.
Antes del recurso `Consulta` deben acordarse identidad verificable, reglas de
acceso por actor/clínica y campos autorizados del DTO. No se asumen esos
permisos a partir de un selector visual.

Resultados y reproducción: `docs/rest-validation.md`. Contrato compartido:
`docs/api-contract.md`. Las invitaciones de los tres integrantes fueron
confirmadas por Fernando en la conversación. Fernando autorizó después la
publicación directa de esta entrega en `main`, sin PR. La protección de ramas
queda como política por acordar para futuros cambios.

## Segunda entrega: Consulta académica

Fernando confirmó que la entrega usará exclusivamente datos ficticios. La rama
`feat/consulta-read-model` agrega un modo demo explícito, desactivado por
defecto, que solo acepta nombres de base con prefijo de demostración/prueba.
No se interpreta la sesión JSF como autenticación. La API demo de Consulta
requiere una clínica y la aplica también al detalle; no publica información
clínica libre, documentos o colecciones.

`GET /v1/clinica` permite obtener las opciones del selector por HTTP, sin IDs
quemados en componentes. Se conservan los DAO y el esquema existentes.
Fixtures de siete consultas y relaciones sintéticas se cargaron únicamente en
la base aislada de la entrega anterior. Contrato 0.2 y reproducción:
`docs/consulta-demo.md`. El uso con datos reales sigue pendiente de un contrato
de autenticación y autorización.

## Preparación reproducible para la integración

Se agregó el comando backend `./demo` y un perfil Maven `demo` que fija el
runtime y lo aísla de la instalación habitual. JDK 21 se instaló en una carpeta
del usuario, verificando su checksum oficial y conservando el Java del sistema.
Se comprobó compilación limpia, suite Java, HTTP y Chromium con ese JDK.
El contenedor y volumen de demostración tienen propiedad verificada; claves y
logs se conservan solo en `.galenos-demo/`, ignorado por Git. No se accedió a
tablas de la base clínica ni se modificó el contenedor `postgres-local`.

`./demo info` entrega URL, CORS, rutas y modo de credenciales para el cliente.
Requisitos y reproducción: `docs/demo-setup.md`. El siguiente paso depende de
los DAOs/dashboard de Itzep y componentes de Rodrigo; no se implementan en
esta preparación de Fernando.

## Revisión del ingeniero — 2026-10-30

Base: `origin/main d7ff967`, actualizado con fetch; checkout limpio aislado en
`/private/tmp/galenos-correcciones-20261030`, rama `fix/observaciones-ingeniero`.
Las copias anteriores del escritorio se conservaron: Integrado tenía cambios
locales, Main_Final estaba atrasado y Anterior seguía en Itzep. Autoría comprobada
por Git: Fernando entregó REST, contratos y demo; Rodrigo las vistas y componentes;
Itzep los DAO/modelos de contactos y el acceso HTTP/dashboard del frontend.
Los cambios compartidos quedan sujetos a revisión de Fernando.

| Archivo / método | Defecto comprobado y corrección mínima | Riesgo / prueba |
|---|---|---|
| `control/MedioContactoDAO.existeContacto` | Null era false; ahora UUID obligatorios y valor no vacío producen IllegalArgumentException. Una consulta válida sin coincidencias sigue siendo false. | Cambia el contrato de argumentos inválidos. Único consumidor productivo: MedioContactoModel, que valida antes. Ocho casos unitarios ejecutan DAO y Criteria de producción con EntityManager simulado, incluyendo exclusión y causa original de SQLException. |
| `control/DefaultDAO.convertirValor` | Boolean.valueOf convertía cualquier texto desconocido en false. Se rechaza ese texto y se convierte Date desde un instante ISO-8601. | Entradas antes aceptadas incorrectamente ahora fallan antes de ejecutar consulta; pruebas de UUID, enum, enteros, booleanos y fechas. |
| `control/DefaultDAO.resolverPath/construirPredicados` | La navegación usaba el metamodelo del último From incluso tras bajar por Path; un join sobre una relación final eliminaba sus null. Se navega por el tipo de cada Path y se rechazan colecciones y operadores incompatibles. | Consumidores actuales usan atributos singulares. Pruebas unitarias y lecturas HTTP reales sobre relaciones, fechas, filtros, conteos y paginación. |
| `model/MedioContactoModel.getContactosPorPersona` | Construía FiltroDAO y usaba Integer.MAX_VALUE. Delega en buscarPorPersona del DAO con JPQL y orden UUID. | Se conserva la lista completa del tab; no se impone un corte silencioso. Pruebas de delegación, persona no persistida y propagación de fallos. No equivale a paginación nueva. |
| `control/ConsultaFlujoService`, `model/ConsultaModel`, `rest/ConsultaResource` | Dos constructores del mismo criterio de fechas/clínica. Se comparte filtrosConsulta y se coordinan las lecturas REST desde el servicio existente. | Conserva zona, límites diarios y DTO; pruebas JSF de fechas, Resource con servicio real y DAO simulados, más nueve casos HTTP reales. No se traslada a REST la restricción de escritura al rol Paciente. |
| `model/AbstractModel.getRowData`, `MedioContactoModel.buscarPersona/buscarTipo` | El catch de UUID incluía también la llamada al DAO y podía ocultar IllegalArgumentException de JPA. La captura ahora cubre únicamente la conversión. | UUID mal formado continúa retornando null; errores del DAO se propagan con su instancia original. |
| `model/MedioContactoModel.guardar` | Un fallo JPA/EJB podía escapar al formulario. Se registra con causa y se muestra mensaje seguro en cinco bundles, manteniendo selección y estado. | Prueba de FacesMessage, validationFailed y ausencia de escritura cuando falla la consulta de duplicados. |

Se conservaron SUPPORTS para lecturas y REQUIRED para escrituras de DefaultDAO.
Las nuevas lecturas de servicio y contactos usan SUPPORTS; los flujos de escritura
y ProcedimientoFlujoService permanecen con sus transacciones anteriores.
No se añadió un máximo interno arbitrario al DAO: el límite REST sigue en
RestParameters (422), mientras existen consumidores internos de colecciones
completas. El resto de llamadas Integer.MAX_VALUE no se sustituyó globalmente.

Inventario y depuración de pruebas: 64 suites Java más un fixture auxiliar;
frontend: ocho archivos unitarios, dos de transporte, runner REST y suite de
navegador. El análisis detallado se concentró en los métodos modificados.

| Clasificación | Decisión basada en evidencia |
|---|---|
| A — útil | Conservar validaciones de Models, converters, contratos REST, arquitectura, XHTML, traducciones y esquema; son unitarias/estructurales, no acreditan PostgreSQL. Conservar los casos HTTP y navegador reales. |
| B — incompleta | Ampliar DefaultDAO y MedioContactoDAO con errores y conversiones; Resource usa servicio de producción con DAO simulados. ConcreteDAOTest ahora instancia el DAO y comprueba getEntityClass, en lugar de inferir el constructor desde el genérico. |
| C — redundante | Frontend consolida test_real_card_model_event_and_update dentro de test_real_card_contract y conserva la actualización con null, el evento compuesto, sus campos y las fechas. |
| D — reproduce defecto | Sustituir MedioContactoDAOTest.valorNoExistenteYArgumentosNulosNoSonDuplicados: afirmaba false ante persona null. Reemplazo exige excepción y cero interacción JPA. |
| E — vacía/engañosa | Eliminar ConcreteDAOTest.testNoLegAcabanLosDaoSinHerencia: su bucle podía no ejecutar ninguna aserción y no demostraba herencia. testHeredanDeDefaultDAO ya verifica esa regla directamente. |
| F — integración aparente | Los casos Mockito, Sinon y transporte sintético se registran como unitarios/sintéticos. No se encontró en el bloque intervenido una falsa integración que justificara eliminar otra suite; los HTTP reales se contabilizan aparte. |

Validación nueva de esta revisión, Java 21.0.12.1, Maven 3.9.16 y Liberty 26.0.0.8:

| Ejecución | Aprobadas | Fallidas / omitidas |
|---|---:|---:|
| `mvn -q -Pdemo clean package`: JUnit, JaCoCo y WAR | 757 | 0 / 0 |
| Protecciones del orquestador Python, sin persistencia | 10 | 0 / 0 |
| HTTP real TipoDocumento | 12 | 0 / 0 |
| HTTP real Consulta/Clínica | 9 | 0 / 0 |
| Navegador JSF Consulta: clínica y filtro diario | 1 | 0 / 0 |
| Comprobaciones reales de infraestructura HTTP 500 (tres recursos) | 3 | 0 / 0 |
| Frontend `npm test` (unitarias sintéticas) | 402 | 0 / 0 |
| Frontend `npm run test:transport` (Fetch real, servidor sintético) | 9 | 0 / 0 |
| Frontend `npm run test:integration` (REST real) | 20 | 0 / 0 |
| Chromium 140.0.7339.16: dashboard/tarjetas, mezcla explícita de REST real y fallos interceptados | 19 | 0 / 0 |

`npm ci` completó; `npm run check` validó 57 archivos. JaCoCo: líneas
3735/5174 (72.19%), ramas 1800/3374 (53.35%), instrucciones 14226/20214
(70.38%). Es cobertura de la suite Java, no cobertura de integración ni prueba
de rollback. Reportes locales: target/surefire-reports y target/site/jacoco;
WAR: target/GalenosSV.war. Los primeros intentos restringidos fallaron por
permisos de adjunción de Mockito o sockets; las ejecuciones indicadas arriba
se repitieron fuera del sandbox. El primer intento de Chromium no encontró el
binario; la ejecución aprobada usó la instalación ya existente mediante
PLAYWRIGHT_BROWSERS_PATH, sin instalar dependencias.

Solo se utilizó el contenedor existente galenos-demo-itzep-final-20261028,
con etiqueta de propietario verificada, puerto 15433 y base galenos_demo_local.
La revisión de fixture coincidió con SHA-256
b83692af0510933844c7573f29b920d8362f8cf09e47bf1938f1ea72192438a1.
El fallo real se provocó en otro Liberty temporal con datasource a un puerto
loopback cerrado, sin modificar tablas ni detener PostgreSQL. Se reutilizaron
credenciales privadas existentes; no se generaron claves ni se tocó postgres_db.

Estado: PARCIALMENTE COMPLETO. Pendiente para Fernando: acordar un fixture oficial
con tablas de contactos/documentos/procedimientos si se requiere probar sus
consultas, integridad y transacciones en PostgreSQL. No se alteró el esquema.
No se afirma rollback real ni CRUD JSF completo. La lectura contextual de contactos
sigue trayendo la colección de esa persona; una paginación real del tab necesita
ajustar la vista y su contrato, sin ocultar registros. No se agregan servicios
por entidad, endpoints ni dependencias. Los 17 DAO frontend pendientes conservan
CONTRACT_PENDING y Rodrigo no necesita modificar sus componentes.

La prueba reproducible `test/integration/consulta_jsf_browser.py` ejecuta la
página JSF real: selección de clínica A, cinco filas, filtro 22/10/2026 y dos
filas, sin errores JavaScript. Se ejecutó con la misma instalación de Playwright
externa y el WAR probado por REST; no simula el servicio ni EntityManager.
Los primeros intentos de automatización fallaron por selección/espera del DOM;
no se contabilizan como escenarios aprobados adicionales.

Archivos modificados: control/{DefaultDAO,MedioContactoDAO,ConsultaFlujoService}.java;
model/{AbstractModel,MedioContactoModel,ConsultaModel}.java;
rest/ConsultaResource.java; los cinco bundles messages*.properties;
pruebas Java {ConcreteDAOTest,DefaultDAOTest,MedioContactoDAOTest,
AbstractModelTest,MedioContactoModelTest,ConsultaResourceTest};
test/integration/{tipo_documento_storage_failure_http,consulta_jsf_browser}.py;
y este informe. Frontend: test/unit/DefaultDAO.test.js,
test/browser/dashboard.py y docs/revision-ingeniero.md.

Proveniencia del despliegue: el puerto inicial 19082 tenía otro Liberty activo;
esas primeras ejecuciones no se usan como evidencia del WAR actual. Las suites
HTTP, DAO frontend, Chromium y JSF se repitieron con éxito en GalenosSVReview,
servidor exclusivo http://localhost:19184/GalenosSV con el mismo datasource demo
15433. SHA-256 del WAR empaquetado y desplegado, contrastados antes del arranque:
`f28260ab0cb7e97e3a7d703cdeedda92ae4a7a5e1c678683640501f5a94fd6ff`.
La prueba de infraestructura usa GalenosSVFailure en 19183 y el mismo WAR.
Los dos environment.local.js frontend se respaldaron para apuntar temporalmente
al puerto de revisión y se restauraron al terminar. No forman parte del commit.
