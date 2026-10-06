# Plan de implementación del parcial — Fernando

Fecha de revisión: 1 de octubre de 2026, zona America/El_Salvador.
Base revisada: `3004b20d1b5c892115c31629a51b094d22bed200`.
Este documento conserva la comparación inicial y el plan. La implementación se
realizó en `feature/fernando-parcial`; el estado y la evidencia están en la
sección 10 y en [validacion-parcial-fernando.md](validacion-parcial-fernando.md).

## 1. Resultado de la verificación del remoto

Se ejecutó `git fetch origin` y se compararon las referencias descargadas con las locales.

| Referencia | Estado verificado |
| --- | --- |
| `feature/fernando`, rama local actual | `3004b20` |
| `origin/main` | `3004b20`; sin commits adicionales respecto a la rama local actual |
| `origin/feature/rodrigo-completar-procedimientos` | `3004b20`; coincide con `origin/main` |
| `main` local | `c06741e`; tres commits detrás de `origin/main` |
| `origin/feature/fernando` | `ed458e6`; cuatro commits detrás de la rama local actual |

El último commit de `origin/main` es del 30 de septiembre de 2026 a las 12:48:16, UTC−06:00. Es exactamente el corte de la auditoría PDF. No hay cambios posteriores a esa auditoría en `origin/main`.

Los tres commits que faltan en `main` local ya están en `feature/fernando`:

- `9afad0c`: nombres de pasos, combos filtrados y mensajes compactos.
- `5cc9b83`: integración de main.
- `3004b20`: corrección del responsable médico usado en una prueba.

Hay historia exclusiva en ramas antiguas: `origin/Itzep` (18 commits), `origin/feature/rodrigo` (un merge) y `origin/copilot/revisar-test-rodrigo` (dos commits). Sus últimos commits preceden al corte auditado; su existencia no representa avance nuevo en las tareas de Fernando. Las otras ramas de trabajo revisadas no tienen commits exclusivos respecto a `origin/main`.

Se conservaron la rama actual y los archivos locales previos sin seguimiento, `anahata.md` y `db/`. No se hizo merge, commit ni push durante la revisión.

## 2. Documentos y responsabilidades

Fuentes locales:

- `/home/fernando/Descargas/Plan_Trabajo_Colaborativo_GalenosSV_PPI115_GT02_FINAL.pdf`: secciones 3.3, 3.4, 7, 8, 9 y 11.
- `/home/fernando/Descargas/Plan_Maestro_GalenosSV_Parcial_PPI115_GT02.pdf`: observables 5, 9 y 10, dependencias y simulacro.
- `/home/fernando/Descargas/auditoria_galenos_sv_ppi115_gt02.pdf`: corte `3004b20`, hallazgos y puerta de salida.

El Plan Maestro y la auditoría asignan el catálogo TipoExamen a Itzep. El Plan Colaborativo FINAL lo reparte temporalmente a Fernando en la primera fase; después devuelve a cada integrante a su módulo principal. Para este plan se adopta ese reparto operativo:

- **Apoyo inicial:** TipoExamen, observable 5 (5%), y revisión cruzada de TipoMedioContacto de Rodrigo.
- **Responsabilidad principal:** clínica actual, observable 9 (5%), y Consulta, observable 10 (20%).
- **Itzep mantiene:** asociaciones Examen–TipoExamen y reglas de Persona/PersonaRol.
- **Rodrigo mantiene:** plantillas de procedimientos, pasos, secuencias y roles requeridos.

La revisión del docente es secuencial. Terminar los puntos 9 y 10 depende de que los compañeros preparen correctamente los puntos anteriores.

## 3. Comparación con el código actual

Los archivos Java citados están bajo `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/`.

| Requisito | Evidencia actual | Trabajo pendiente |
| --- | --- | --- |
| TipoExamen: crear, editar y conservar estado | `model/TipoExamenModel.java`, `paginas/TipoExamen.xhtml` y pruebas existentes; nuevo inicia activo, edición permite cambiar estado | Comprobar desde UI dos activos y uno inactivo, recarga y edición. Corregir solamente defectos observados |
| Clínica actual compartida | `general.xhtml` tiene selector de idioma; no hay bean de clínica de sesión | Crear contexto y selector persistente al navegar |
| Paciente de la clínica actual | `ConsultaModel.buscarPacientes()` filtra por nombre del rol Paciente y busca por nombre/DUI, tras cargar todos los PersonaRol | Consultar por clínica, exigir rol activo y validar de nuevo al guardar |
| Listado de consultas por clínica | `ConsultaModel` hereda el listado genérico sin filtro propio de clínica | Aplicar el mismo filtro en carga, conteo y resolución de selección |
| Procedimiento activo | `ConsultaProcedimientoModel.inicializar()` excluye `activo=false`, pero acepta null; `guardar()` no revalida el catálogo | Exigir activo=true y volver a verificar existencia/estado al guardar |
| Primer paso automático | `ConsultaProcedimientoModel.guardar()` solamente persiste la asociación | Obtener raíz y rol; persistir asociación y primer paso juntos |
| Responsable del rol requerido y misma clínica | `ConsultaProcedimientoPasoModel` carga todos los PersonaRol y busca solo médicos por nombres/observaciones del rol | Resolver por UUID del rol exigido y clínica; recepción/enfermería también pueden ser responsables |
| Rango de fechas | Hay campos de fechas y validación fin ≥ inicio, pero no controles de rango en el listado | Agregar Desde/Hasta y filtros en BD, coherentes con paginación |
| Relaciones bloqueadas en edición | Paciente, procedimiento y responsable siguen editables en `Consulta.xhtml`; los guardar no defienden esas relaciones | Bloquear UI y comprobar invariantes en servidor |
| Contrato PersonaRol por clínica | `PersonaRolDAO` solo hereda CRUD genérico | Exponer o documentar el contrato con Itzep |
| Contrato de paso inicial | `ProcedimientoPasoDAO` tiene búsqueda de pasos por procedimiento, pero no método para obtener raíz única | Definirlo con Rodrigo; rechazar cero o varias raíces |

La auditoría sigue siendo válida en sus faltantes principales de los observables 9 y 10. El código confirma además que el responsable actual está restringido a médicos, una regla incompatible con un primer paso que requiera Atención al público o Enfermera.

## 4. Decisiones técnicas para implementar

1. Mantener XHTML → Model → EJB/DAO → JPA → PostgreSQL, `getDao()`, `Serializable` y los ámbitos de vista existentes. Inyectar DAO/EJB con `@EJB`; el contexto CDI de sesión puede inyectarse con `@Inject`.
2. Usar un único `ClinicaActualBean`, `@Named` y `@SessionScoped`, serializable. Conservar el UUID de la clínica seleccionada; revalidar su existencia y estado en operaciones de escritura. No elegir una clínica silenciosamente cuando no haya selección.
3. Derivar la clínica de una Consulta mediante `idPersonaRol.idClinica.idClinica`. La tabla consulta no tiene columna directa de clínica; no agregarla.
4. Consultar pacientes/responsables por clínica en el DAO. No duplicar en varios Models la interpretación de PersonaRol ni depender de listas cargadas una vez en `@PostConstruct`.
5. Resolver al responsable mediante el UUID del rol del paso inicial. Para el rol Paciente, centralizar la regla existente de nombre en el contrato de búsqueda; no fijar UUID de datos demo ni crear otro significado de paciente.
6. Usar un EJB `ConsultaFlujoService` con transacción `REQUIRED` para guardar la asociación y el primer paso. Los guardar de los DAO participan en esa transacción; dos llamadas independientes desde un Model no garantizan atomicidad.
7. Persistir en el primer paso solamente las columnas existentes: asociación, PersonaRol responsable, fechaInicio, fechaFin y estado. Estado inicial propuesto: `PENDIENTE`, con fechaFin nula. Confirmar su coherencia con los valores usados en la aplicación.
8. No hay FK de `consulta_procedimiento_paso` a `procedimiento_paso`. La plantilla sirve para decidir raíz y rol durante la creación; no se almacena un identificador ficticio ni se codifica en observaciones/estado. La ejecución posterior y el remapeo corresponden al siguiente entregable.
9. Para fechas de calendario, usar el intervalo fechaInicio ≥ inicio del día Desde y fechaInicio < inicio del día siguiente a Hasta, en la zona acordada del sistema. Ambos límites son opcionales; rechazar Desde > Hasta.

## 5. Contratos que se deben cerrar con los compañeros

Los contratos siguientes se implementaron de forma aditiva en la rama de Fernando.
Sus firmas, reglas y límites están en
[contratos-parcial-fernando.md](contratos-parcial-fernando.md); la revisión de los
dueños sigue siendo un paso de integración del equipo.

| Dueño | Contrato propuesto | Resultado requerido |
| --- | --- | --- |
| Itzep | `PersonaRolDAO.buscarPacientesPorClinica(UUID idClinica, String criterio, int limite)` | Solo asociaciones persistidas de esa clínica con Persona y rol Paciente activo; conservar búsqueda por nombre/documento |
| Itzep | `PersonaRolDAO.buscarResponsablesPorClinicaYRol(UUID idClinica, UUID idRol)` | Solo asociaciones con persona existente, clínica coincidente y el rol exacto activo |
| Rodrigo | `ProcedimientoPasoDAO.obtenerPasoInicial(UUID idProcedimiento)` | Una única raíz persistida perteneciente al procedimiento, con rol requerido existente y activo; error claro si la plantilla no es válida |
| Fernando | `ClinicaActualBean.getIdClinicaActual()` | UUID de una selección compartida en sesión o null si no hay contexto |

El esquema no tiene bandera de paso inicial. La propuesta es derivar la raíz del grafo de secuencias, considerando origen → referencia como origen → siguiente, tal como funciona el Model actual. Debe acordarse con Rodrigo cómo intervienen los tipos de secuencia antes de codificar; la existencia del texto `INICIAL` como etiqueta no demuestra una regla implementada.

El contrato de Rodrigo debe probar una cadena A1 → A2 → A3, obtener A1 y rechazar múltiples raíces, ciclos o referencias incoherentes. Fernando consume ese contrato; no implementa una segunda interpretación del grafo en Consulta.

Si hay varios responsables válidos, elegir uno de forma determinista mediante un orden documentado, por ejemplo UUID. Este parcial no incluye balanceo de carga.

## 6. Orden de implementación y entregas

### Paso 0 — Preparar la rama y confirmar contratos

- Antes de empezar código, repetir fetch y comparar por si los compañeros publicaron avances.
- Crear `feature/fernando-parcial` desde la base actual actualizada, conforme al Plan Colaborativo FINAL. No es necesario pasar primero por el main local desactualizado.
- Mantener fuera de los commits los archivos locales ajenos a estas tareas y agregar archivos concretos.
- Documentar entrada, salida, errores y dueño de los contratos de PersonaRol y paso inicial.
- Verificar el arranque de PostgreSQL y del servidor Liberty existente descrito en la sección 9, para poder ensayar TipoExamen desde la UI.

Salida: rama del parcial basada en el último main y contratos que eviten trabajo duplicado.

### Paso 1 — Cerrar el apoyo de TipoExamen

Archivos: `TipoExamenModel`, `TipoExamen.xhtml`, `TipoExamenModelTest` si se encuentra un defecto.

- Ensayar creación de dos tipos activos y uno inactivo; recargar, editar y comprobar conservación de estados.
- Comprobar nombre obligatorio y duplicados usando las validaciones existentes.
- Corregir fallos concretos; no reescribir el catálogo por cumplir el reparto.
- Entregar a Itzep el tipo inactivo para probar el rechazo en Examen. Ese rechazo pertenece a su asociación, no a la creación del catálogo.
- Ejecutar la revisión cruzada de TipoMedioContacto de Rodrigo: dos activos con regex, uno inactivo y conservación al recargar; reportar defectos al dueño.

Commit si hay corrección: `fix: completar tipos de examen para parcial`.

### Paso 2 — Implementar clínica actual, observable 9

Archivos nuevos/probables: `boundary/ClinicaActualBean.java`, `ClinicaActualBeanTest`, plantilla `WEB-INF/plantillas/general.xhtml` y bundles `messages*.properties`.

- Listar clínicas activas y permitir una selección explícita con etiqueta visible.
- Mantener selección al navegar y aislarla por sesión de usuario.
- Mostrar un mensaje útil cuando no hay clínicas o no se ha seleccionado una.
- Rechazar UUID inexistente o clínica inactiva también en servidor.
- Al cambiar clínica en Consulta, reiniciar o redirigir la vista para limpiar selecciones, resultados y formularios de los tres Models. Usar solo el contexto de sesión como fuente de verdad.
- Las escrituras deben comprobar el contexto actual incluso si otra pestaña cambió la clínica mientras el formulario estaba abierto.

Aceptación: seleccionar A, navegar y conservar A; cambiar a B y no poder guardar un formulario pendiente de A; clínica inactiva no disponible.

Commit: `feat: agregar contexto de clinica actual`.

### Paso 3 — Restringir Consulta por clínica y paciente

Archivos: `ConsultaModel`, `Consulta.xhtml`, pruebas de Consulta; contrato `PersonaRolDAO` acordado con Itzep.

- Sustituir carga global de PersonaRol por búsqueda contextual del contrato.
- Conservar búsqueda por nombre/DUI y ampliar identificación con fecha de nacimiento cuando corresponda.
- En guardar, volver a cargar el PersonaRol por ID y exigir persona existente, rol Paciente activo y clínica actual. Rechazar null, persona sin rol y roles de otra clínica.
- Filtrar el listado, conteo y selección por `idPersonaRol.idClinica.idClinica`. Sin contexto, devolver lista/conteo vacíos y pedir selección.
- Hacer coherentes los filtros del listado lazy. `AbstractModel` hoy tiene conversión privada de filtros: elegir una extensión pequeña y compatible o sobrescribir en Consulta con DAO específico, sin reescribir todos los catálogos.
- Bloquear paciente en edición y defender su UUID original en guardar. Contrastar con el registro persistido o una copia del ID original; no confiar en una misma entidad mutable enlazada a la vista.

Aceptación: paciente A aparece solo con A seleccionada; persona sin rol y paciente B se rechazan incluso con ID enviado directamente; edición conserva paciente.

Commit: `feat: filtrar consultas y pacientes por clinica`.

### Paso 4 — Crear asociación y primer paso de forma atómica

Archivos: `control/ConsultaFlujoService.java` y pruebas nuevas, `ConsultaProcedimientoModel`, `ConsultaProcedimientoPasoModel`, `Consulta.xhtml`; consumo de contratos de los compañeros.

En la creación del procedimiento de consulta, el servicio realiza dentro de una transacción:

1. Recargar Consulta y comprobar que pertenece a la clínica actual válida.
2. Recargar Procedimiento y exigir `activo=true`.
3. Obtener del contrato la raíz única y su rol requerido activo.
4. Buscar un PersonaRol del mismo rol y clínica; si no existe, emitir error antes de persistir.
5. Guardar ConsultaProcedimiento.
6. Guardar exactamente un ConsultaProcedimientoPaso inicial con responsable automático, fechas y estado válidos.
7. Permitir commit solo si ambas escrituras terminan correctamente. Para errores de negocio dentro de la transacción usar una excepción con rollback explícito; no capturar y ocultar fallos de persistencia en el servicio.

Actualizar las pestañas para mostrar inmediatamente asociación y paso. Mantener la vista en creación y marcar fallo de validación si ocurre un error, con mensaje claro; no cerrar el formulario como si se hubiera guardado.

La edición del procedimiento no genera otro paso. Proteger la relación con Consulta y el UUID del procedimiento en UI y servidor. Adaptar el formulario de pasos existente para no exigir siempre médico ni permitir sustituir el responsable automático o la asociación al editar. Evitar que el alta manual permita eludir las reglas del flujo; no construir la ejecución/avance de pasos de este entregable.

Aceptación: primer paso de recepción asigna recepción; uno de enfermería asigna enfermería; nadie de otra clínica se asigna. Sin responsable o con fallo al insertar el paso, no queda una asociación nueva. Editar no crea otro paso.

Commit: `feat: crear primer paso y responsable automaticos en consulta`.

### Paso 5 — Filtrar por rango de fechas

Archivos: `ConsultaModel`, `Consulta.xhtml`, DAO si se elige consulta específica y pruebas de listado.

- Agregar Desde, Hasta, Buscar y Limpiar al listado de consultas.
- Componer siempre clínica + rango + filtros existentes, tanto en consulta paginada como en conteo.
- Reutilizar `FiltroDAO` y los operadores de comparación existentes; no cargar todas las consultas para filtrarlas en Java.
- Validar rango invertido y reiniciar página al aplicar/limpiar.
- Comprobar límites de día, rango abierto, resultados vacíos y coincidencia del total del paginador.

Commit: `feat: filtrar consultas por rango de fechas`.

### Paso 6 — Validar e integrar

- Ejecutar pruebas del bloque antes de cada commit y `git diff --check`.
- Antes de entregar rama, ejecutar `mvn clean test` y `mvn package`.
- El POM copia PrimeFaces a una instalación de Liberty fuera del proyecto durante package: usar el entorno permitido o solicitar el permiso de escritura necesario en ese momento.
- Probar en Liberty/PostgreSQL con datos creados desde las pantallas. Para el simulacro usar una BD de ensayo vacía; no borrar la BD local por iniciativa propia ni depender de `db/datos_demo.sql` para demostrar el requisito.
- Itzep prueba TipoExamen; Fernando prueba TipoMedioContacto; comprobar con Rodrigo el contrato del primer paso.
- Publicar la rama del integrante cuando esté validada e integrar mediante `feature/parcial-integracion`, según el plan del equipo. Repetir pruebas y simulacro secuencial 1 → 10 antes del merge final a main.

## 7. Pruebas que demuestran los requisitos

| Caso | Resultado esperado |
| --- | --- |
| Dos sesiones seleccionan clínicas diferentes | Contextos independientes |
| Cambiar clínica con formulario abierto o desde otra pestaña | Los datos viejos se limpian o el guardar se rechaza; no mezcla clínicas |
| Buscar/guardar persona sin rol, rol Paciente inactivo o paciente de otra clínica | Rechazo, sin escritura |
| Seleccionar paciente activo de A con A actual | Consulta creada y visible solamente en el contexto de A |
| Enviar directamente ID de procedimiento inexistente, inactivo o con activo=null | Rechazo |
| Plantilla sin raíz única o con rol inválido | Rechazo, sin nueva asociación ni paso |
| Raíz exige recepción/enfermería y existe empleado adecuado en A | Asignación automática del rol exacto |
| Solo hay responsable en B o con rol distinto | Rechazo, sin nueva asociación ni paso |
| Fallo de BD al persistir el paso | Rollback real de asociación y paso |
| Guardar edición o repetir evento de edición | No genera un segundo paso |
| Intentar cambiar paciente, procedimiento, asociación o responsable durante edición | UI bloqueada y defensa de servidor |
| Rango Desde/Hasta, extremos del día, un límite vacío e inversión | Resultados correctos o mensaje de rango inválido; conteo coherente |

Las pruebas unitarias con mocks pueden verificar reglas y coordinación. El rollback necesita una prueba de integración con EJB/JPA real o una comprobación controlada en BD de ensayo: Mockito no demuestra atomicidad real.

## 8. Verificación de la base y límites de esta revisión

- Fetch y comparaciones de referencias completados; la rama actual coincide con `origin/main` y el corte auditado.
- La primera ejecución de `mvn -o test` dentro del sandbox descubrió 544 pruebas, con 499 errores por imposibilidad de adjuntar el agente Mockito/Byte Buddy. No se interpreta como 499 defectos funcionales.
- La repetición de `mvn -o test` fuera del sandbox terminó con **544 pruebas, 0 fallos, 0 errores, BUILD SUCCESS**, a las 22:34:32 del 1 de octubre de 2026. Esta es la base verificada, no una certificación de los requisitos aún ausentes. Log: `/tmp/galenos-verificacion-fernando-maven-fuera-sandbox.log`.
- No se reauditaron datos actuales de PostgreSQL ni se ejecutó el simulacro en Liberty durante esta revisión. Los conteos de BD del PDF son históricos y no se dan por actuales.

## 9. Entorno local proporcionado por Fernando

Las rutas `/home/fernando/servidores` y `/home/fernando/openliberty` se revisaron mediante lectura de configuración y listado de archivos.

| Componente | Ubicación y evidencia |
| --- | --- |
| Instalación Liberty usada por Maven | `/home/fernando/openliberty/wlp`, coincide con `liberty.install.dir` del POM |
| Servidor del proyecto | `/home/fernando/openliberty/wlp/usr/servers/GalenosSV`, coincide con `liberty.server.name` |
| Configuración del servidor | `server.xml` con `webProfile-11.0`, contexto `/GalenosSV`; server.env efectivo y el del proyecto usan HTTP 9080, HTTPS 9443 y BD `clinica` en localhost:5432 |
| PrimeFaces | Biblioteca compartida existente en `wlp/usr/shared/resources/primefaces/primefaces-15.0.17-jakarta.jar` |
| JDBC PostgreSQL | Driver existente en `usr/servers/GalenosSV/jdbc/postgresql-42.7.4.jar` |
| PostgreSQL Docker | `/home/fernando/servidores/postgres/docker-compose.yml`, contenedor `postgres-local`, imagen declarada `postgres:12`, puerto `5432:5432` |

El compose declara `POSTGRES_DB: universidad`, mientras los server.env efectivo y del proyecto usan `clinica`. Esa variable de Docker define la BD inicial y no prueba que la BD clinica falte: confirmar que el datasource acceda a clinica antes del ensayo. No cambiar credenciales ni recrear volúmenes para resolverlo.

La configuración real difiere de algunos datos antiguos de `docs/ENTORNO.md` (instalación en target, versión PostgreSQL y base por defecto). Para esta implementación usar el POM, el server.xml efectivo y el contenedor real como referencia.

Se comprobó `http://localhost:9080/GalenosSV/paginas/Consulta.xhtml` dentro y fuera del sandbox: no hubo conexión (`HTTP 000`). Los logs incluyen un despliegue del 30 de septiembre, pero eso no acredita que el servidor siga activo ahora.

Antes de pruebas manuales:

1. Comprobar estado del contenedor `postgres-local` y acceso del datasource a la BD de ensayo.
2. Usar el servidor GalenosSV existente. El descriptor `apps/GalenosSV.war.xml` configura un despliegue loose que referencia `src/main/webapp` y `target/classes` del workspace; no necesita un WAR empaquetado en ese directorio. Verificar que las clases y recursos referenciados estén preparados.
3. Preparar el artefacto y arrancar/desplegar mediante la configuración Maven actual; verificar logs y respuesta HTTP.
4. Tener en cuenta que arrancar o desplegar escribe bajo `/home/fernando/openliberty`, fuera del workspace permitido; solicitar el permiso correspondiente al ejecutar ese paso.
5. Ensayar TipoExamen y después los observables 9/10 con datos de interfaz. No modificar otros servidores de la misma instalación.

Fuera del alcance: parche DDL, nuevas columnas/FK, login completo, dashboards, rediseño visual, finalización/avance real de pasos, telemetría, balanceo de carga y cambio de URLs a `.jsf`.

## 10. Estado de implementación — 2 de octubre de 2026

| Bloque de Fernando | Estado |
| --- | --- |
| Rama propia desde `origin/main` | `feature/fernando-parcial`, base `3004b20`; commits por bloque |
| TipoExamen | Catálogo existente validado desde UI: dos activos, uno inactivo; edición y recarga conservan estado |
| Revisión cruzada TipoMedioContacto | Dos activos con regex y uno inactivo; edición y recarga conservan estado |
| Clínica actual | Contexto de sesión, clínicas activas, selección conservada al navegar, revisión del contexto al guardar |
| Consulta por clínica | Búsqueda por nombre/DUI en DAO, fecha de nacimiento en sugerencias; filtros de carga/conteo/selección y validación de escritura |
| Primer paso automático | Raíz única de plantilla, rol exacto y responsable de la misma clínica; transacción EJB para asociación y paso |
| Relaciones en edición | Bloqueo en UI y defensa en servicio para paciente, procedimiento, asociación y responsable |
| Rango de fechas | Desde/Hasta opcionales, días completos de El Salvador; búsqueda y limpieza coherentes con paginación |
| Ensayo real | WAR en Open Liberty `GalenosSVParcial`, puerto 9081, PostgreSQL `clinica_fernando_parcial`; datos desde UI |
| Rollback real | Fallo controlado al insertar paso; las dos asociaciones y los dos pasos previos permanecen sin nuevas filas; disparador de ensayo retirado |
| Esquema original | SQL y entidades sin cambios; DDL del repositorio equivalente al SQL del Escritorio, diferencias solo en comentarios/espacios |
| PrimeFaces | `provided`, biblioteca compartida de Open Liberty; WAR sin JARs en `WEB-INF/lib` |

La implementación de Fernando está preparada para revisión e integración.
Restan al equipo la revisión de contratos con Itzep/Rodrigo, el ensayo completo
de observables 1 → 10 y la integración en `feature/parcial-integracion` antes de
un merge final a main. Los resultados de esta rama no certifican el parcial
completo de los tres integrantes.
