# GT02 2026 — Contrato compartido HU1/HU2 y contexto HU3/HU4

Estado: primer entregable contractual, pendiente de confirmación antes de implementar.
Las rutas marcadas **nueva** y las medidas de sesión/autorización aquí descritas
son decisiones de implementación; **todavía no existen en el SHA auditado**.
Este documento no constituye evidencia de HU1/HU2 funcionando.

## 1. Procedencia y auditoría

Auditoría del 11 de noviembre de 2026, exclusivamente del código versionado:

- Backend: https://github.com/Fernando-rtx/Galenos.git,
  `origin/main` = `22ef3cd9e09eec1ca7fd9367d0009ff572526b0b`.
- Frontend: https://github.com/Fernando-rtx/GalenoSVFE.git,
  `origin/main` = `a103fb71ac2afcb049f270f25b2ec54ce75fcd41`.
- Ambas referencias se actualizaron con `git fetch origin --prune`.
- Orígenes de los worktrees: `GalenosSV_Actualizado` y `GalenoSVFE`, limpios
  antes de crear la rama `feat/gt02-hu1-hu2` en ambos repositorios.
- Worktrees: `/private/tmp/gt02-hu1-hu2-backend` y
  `/private/tmp/gt02-hu1-hu2-frontend`.
- `GalenosSV_Integrado` tiene cambios ajenos y no se utiliza ni modifica.
  `GalenosSV_Anterior` apunta al mismo remoto, pero no se toma su rama como base.

Fuentes bajo `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/`:

| Fuente auditada | Hecho comprobado |
| --- | --- |
| `entity/Persona.java` | UUID `idPersona`, nombres, apellidos, fechas y relaciones inversas. Sin credenciales ni estado activo. |
| `entity/Documento.java` | UUID `idDocumento`, `valor`, `rutaFisica`, relaciones `idPersona` e `idTipoDocumento`. |
| `entity/TipoDocumento.java` | UUID, nombre, indicaciones, `expresionRegular`, Boolean `activo`. |
| `entity/PersonaRol.java` | UUID `idPersonaRol`, fechaCreacion y relaciones persona/rol/clínica. **No tiene activo ni fechas de vigencia**. |
| `entity/Rol.java`, `entity/Clinica.java` | UUID, nombre y Boolean `activo`; no hay categoría persistida «empleado». |
| `entity/Consulta.java` | UUID, fechaInicio/fechaFin, referenciaExterna, observaciones y `idPersonaRol` del paciente. Sin médico ni estado clínico propios. |
| `entity/ConsultaProcedimiento.java` | Pertenece a Consulta; `idProcedimiento` es UUID simple, no relación JPA. Resolver su nombre con ProcedimientoDAO. |
| `entity/ConsultaProcedimientoPaso.java` | Relaciona asociación, responsable PersonaRol, fechas y estado del paso. Ese responsable no es el paciente de Consulta. |
| `rest/PersonaResource.java` | CRUD de persona/documento/persona_rol; no identifica por documento ni crea sesión. |
| `rest/ConsultaGestionResource.java` | Consulta por clínica y opcional PersonaRol, asociaciones/pasos. Comprueba modo de gestión; no vincula al solicitante. |
| `control/ConsultaFlujoService.java` | Filtros por clínica, fechas y asignación; valida rol paciente y clínica/rol activos en operaciones clínicas. No implementa sesión. |
| `control/PersonaFlujoService.java` | Valida documentos mediante regex Java y duplicados; mantiene asociaciones inmutables al editar. No implementa identificación. |
| `rest/RestApplication.java` | Base `/resources`, registro explícito de clases v1/v2. |
| `rest/ContextoResource.java` | Contexto de despliegue `academico-local`, `puedeEscribir: true`, todas las clínicas; no es una identidad. |
| `rest/ApiError.java`, `rest/ApiExceptionMapper.java` | Error público `{"mensaje":"…"}`; no publicar trazas ni SQL. |
| `rest/ApiSettings.java`, `rest/CorsFilter.java` | Paginación 0/20, máximo100 por defecto; zona configurable America/El_Salvador; CORS exacto sin cookies. Academic exige actualmente base clinica. |

Referencias anteriores: [api-contract.md](api-contract.md),
[api-gestion-v2.md](api-gestion-v2.md). Se conservan DTO existentes. Para
identidad y acceso de GT02 prevalece este documento sobre el acceso académico
anónimo descrito allí. No se han auditado datos de producción ni cargado fixtures.
No se encontró una rúbrica adicional versionada; se aplican las restricciones
académicas de la solicitud y AGENTS.md, sin añadir dependencias.

## 2. Convenciones y catálogo público reutilizado

Base del WAR actual: `/GalenosSV/resources`; no fijar host/puerto en componentes.
UUID como strings canónicos. JSON UTF-8 con claves camelCase. `null` se conserva
para fechas/nombres opcionales. Listas vacías son `[]` con HTTP200.

Se reutiliza `GET /v1/tipo_documento?first=0&max=100`, y su detalle `/{id}`.
DTO existente: `idTipoDocumento`, `nombre`, `indicaciones`, `expresionRegular`,
`activo`. La lista incluye tipos inactivos: ofrecer solamente `activo === true`
y recorrer todas las páginas usando `Total-registros`. El backend vuelve a
validar tipo activo al identificar; filtrar el catálogo no autoriza nada.

Cada POST de identificación recibe **solamente**:

```json
{"idTipoDocumento":"00000000-0000-4000-8000-000000000001","valor":"ABC"}
```

Ejemplo exclusivamente sintético. No mandar `idPersona`, rol, clínica ni
asignación para establecer identidad. Tipo obligatorio, existente y activo;
valor String obligatorio y no vacío tras trim. Comparación exacta con
Documento.valor para el tipo elegido, sin cambiar guiones, mayúsculas o ceros.
Si hay expresión configurada, aplicar coincidencia completa con Pattern Java.
El cliente puede anticipar validación compatible con ECMAScript, pero no debe
reinterpretar ni rechazar una regex Java no compatible: decide el servidor.
Regex configurada mal formada es fallo de configuración500 con mensaje seguro.
Sin regex solamente se exige valor no vacío. No introducir algoritmos nuevos de DUI.
Cero personas:404; documentos ambiguos de distintas personas:409, sin elegir una.

## 3. Sesión académica nueva

No hay usuarios, contraseñas, BASIC, LTPA ni administrador-local. Conocer un
documento permite simular identidad para el examen; **no es autenticación fuerte**.

El servidor emite token opaco de 32 bytes aleatorios mediante SecureRandom,
Base64 URL sin padding. Conserva sólo su hash y contexto mínimo en memoria del
backend, con propósito PACIENTE o EMPLEADO, UUID de persona y vencimiento absoluto
de 30 minutos. Las sesiones expiran también al reiniciar el backend; limpiar
entradas vencidas. No persistir documentos ni tokens en base de datos.

Enviar `Authorization: Bearer <token>` en consultas posteriores. El frontend
mantiene token/contexto sólo en memoria; recargar requiere identificar de nuevo.
No usar localStorage, sessionStorage, URL, registros, Cache Storage ni precarga
PWA para documentos, tokens, respuestas, contexto o PDF. Usar fetch con
`credentials: 'omit'` y `cache: 'no-store'`. Sesiones paciente y empleado son
independientes; un token de paciente no sirve para gestión.

No confiar en asignaciones contenidas en la respuesta anterior: cada operación
vuelve a leer la persona, asignación, rol y clínica. Asignación utilizable:
existe, pertenece a esa persona y tiene rol y clínica existentes con activo=true.
`null` no equivale a activo. Eliminación de asignación o desactivación de rol o
clínica revoca inmediatamente su uso. No inventar `PersonaRol.activo`.

## 4. Identificación y clínicas de pacientes (HU1)

**Nuevas**, rutas relativas a la base:

| Método y ruta | Entrada / salida |
| --- | --- |
| `POST /v2/identidad_academica/paciente` | Body de sección2; 200 SesionAcademica con contexto PACIENTE. Sin asignación paciente utilizable:403, sin emitir token. |
| `GET /v2/identidad_academica/paciente` | Bearer PACIENTE; 200 ContextoAcademico actualizado. |
| `DELETE /v2/identidad_academica/paciente` | Revoca token PACIENTE, 204 sin cuerpo. |

Paciente se reconoce por `Rol.nombre.trim().equalsIgnoreCase("paciente")`,
criterio ya utilizado en ConsultaFlujoService. No crear ni fijar UUID de catálogo.
Contexto PACIENTE contiene únicamente asignaciones con ese rol y sus clínicas.
Para varias asignaciones en una clínica conservar los idPersonaRol distintos;
el selector muestra la clínica una vez. No descargar todas las clínicas para
filtrarlas en JavaScript.

## 5. Identificación y contexto de empleados (HU2)

**Nuevas**:

| Método y ruta | Entrada / salida |
| --- | --- |
| `POST /v2/identidad_academica/empleado` | Body de sección2; 200 SesionAcademica con contexto EMPLEADO. Sin asignación de empleado utilizable:403. |
| `GET /v2/identidad_academica/empleado` | Bearer EMPLEADO; 200 ContextoAcademico actualizado. Sin asignaciones válidas restantes:403. |
| `DELETE /v2/identidad_academica/empleado` | Revoca token EMPLEADO, 204 sin cuerpo. |

Como el esquema no distingue empleados, se define configuración nueva del
servidor `GALENOS_EMPLEADO_ROLES`: lista de UUID de Rol separados por comas.
Debe resolverse contra roles reales activos; nunca se toma del navegador.
Vacío significa ningún rol habilitado, sin apertura automática. Un UUID
inexistente o del rol Paciente es error de configuración, no acceso permitido.
Los nombres Recepción y Médico existen en el fixture sintético
`test/fixtures/crud_demo.sql`; no se asumen sus UUID en datos existentes.
Configurar IDs comprobados en la base sintética al desplegar pruebas.
No clasificar automáticamente todo rol distinto de paciente como empleado.

Un empleado obtiene todas sus asignaciones utilizables de los roles configurados,
incluidas varias clínicas/roles. Sus asignaciones paciente no conceden permisos
de empleado y no se incluyen en este contexto. La identidad no implica facultad
de administrar catálogos ni información de otras clínicas.

DTO nuevos, iguales para ambos propósitos:

```text
SesionAcademica {
  token: string, expiraEn: string ISO-8601 UTC,
  contexto: ContextoAcademico
}
ContextoAcademico {
  tipo: "PACIENTE" | "EMPLEADO",
  idPersona: UUID string,
  personaNombre: string,
  asignaciones: AsignacionAcademica[],
  zonaHoraria: string IANA
}
AsignacionAcademica {
  idPersonaRol: UUID string,
  idRol: UUID string, rolNombre: string,
  idClinica: UUID string, clinicaNombre: string
}
```

`personaNombre` combina nombres y apellidos no vacíos con un espacio. Si faltan
ambos, usar etiqueta de presentación «Persona sin nombre», sin reemplazar datos
persistidos. El nombre del empleado es ese mismo campo, no un atributo JPA nuevo.
GET devuelve sólo ContextoAcademico, nunca token. Orden de asignaciones estable
por idPersonaRol. No se devuelve documento, fecha de nacimiento ni rutaFisica.
Cerrar sesión borra también el contexto, las selecciones y resultados en memoria.
DELETE es idempotente para token desconocido/expirado (204); si el token pertenece
al otro propósito responde403 sin revocarlo. Las lecturas requieren token válido.

## 6. Búsqueda de citas y PDF (HU1)

**Nueva** `GET /v2/citas_paciente` con Bearer PACIENTE. Query obligatorio:
`idClinica`, `fechaDesde`, `fechaHasta`; opcional `first`, `max` con paginación
existente (por defecto0/20, máximo configurable100). No acepta idPersona ni
idPersonaRol proporcionados por el cliente. Propiedades/query desconocidas:422.

El servidor verifica pertenencia paciente a clínica antes de consultar. Filtra
por todas las asignaciones paciente utilizables de la persona en esa clínica,
no sólo por clínica ni sólo por el primer rol. Usa DAO/JPA para la restricción,
conteo y paginación. Orden fechaInicio ascendente e idConsulta ascendente como
desempate. `Total-registros` cuenta exclusivamente ese filtro. Sin resultados:200
con `[]`, total0. No publica creación, modificación ni eliminación de citas.

DTO **nuevo** CitaPaciente, sin cambiar ConsultaDatos/ConsultaDto existentes:

```text
CitaPaciente {
  idConsulta: UUID string,
  idClinica: UUID string, clinicaNombre: string,
  fechaInicio: string UTC, fechaFin: string UTC | null,
  procedimientos: [{idConsultaProcedimiento: UUID string,
                    idProcedimiento: UUID string, procedimientoNombre: string}]
}
```

Procedimientos obtenidos de ConsultaProcedimiento de esa consulta, nombre por
ProcedimientoDAO, orden por idConsultaProcedimiento. Sin asociaciones: `[]`.
Procedimiento referenciado ausente/nombre vacío: etiqueta «Procedimiento sin
nombre» conservando su ID, sin inventar descripción. No ocultar asociaciones
históricas sólo porque Procedimiento.activo sea false. No exponer observaciones,
resultados clínicos, responsables ni documentos en este portal.

**Nueva** `GET /v2/citas_paciente/{idConsulta}/ticket` con Bearer PACIENTE;
no requiere rango ni clínica: el backend resuelve consulta y verifica persona,
rol paciente y clínica utilizable. Inexistente o ajena:404 indistinguible. No
basta con conocer idConsulta. Paciente con ninguna asignación válida:403.

Éxito: HTTP200, `Content-Type: application/pdf`,
`Content-Disposition: attachment; filename="cita-<UUID>.pdf"`,
`Cache-Control: no-store`. Errores JSON según sección8, nunca un PDF de error.
Contenido: marca visual GalenosSV, personaNombre, clínica, fecha y hora local,
procedimientos (o «Sin procedimientos asociados»), UUID de cita como referencia
y fecha/hora de emisión. No es necesario revelar referenciaExterna.

Generación binaria en Java, con cabecera PDF, objetos, streams, tabla xref y
trailer válidos. Decisión inicial: escritor pequeño con JDK, sin dependencia
adicional ni licencia externa que aprobar. Debe escapar texto, calcular offsets
sobre bytes, ajustar líneas/páginas y probar acentos y nombres. Evaluar fuente y
cobertura de caracteres antes de implementarlo: no truncar ni corromper nombres.
Si este enfoque no cumple la fidelidad, documentar alternativa/licencia/versión
y acordarla antes de añadir una biblioteca; este commit no autoriza dependencias.
La aceptación exige un lector PDF real y extracción de contenido, además de
cabeceras HTTP; cambiar la extensión de texto/HTML no cumple.

## 7. Autorización compartida y consumo por Responsable 2

`GET /v2/contexto` existente queda como información heredada de despliegue;
`puedeEscribir` y su lista global **no autorizan operaciones GT02**. El frontend
nuevo usa `GET /v2/identidad_academica/empleado`. No restaurar el administrador
local. Evitar acceso alternativo a los mismos datos desde recursos antiguos.

La implementación debe incorporar un filtro/guardia de servidor para gestión
v2, excluyendo únicamente catálogo público e identificación. Por defecto denegar
operaciones sin política explícita. CORS/OPTIONS no reemplaza autorización.
Catálogos públicos: sólo lectura de tipos de documento v1; mantener recursos de
demostración v1 bajo su condición de base sintética, sin abrir datos reales.

Contrato interno nuevo para HU3/HU4:
`IdentidadAcademicaService.exigirEmpleado(token, idPersonaRol, idClinica)` devuelve
la PersonaRol revalidada o error401/403. Resuelve persona desde sesión, verifica
propósito EMPLEADO, pertenencia, rol permitido y clínica activa. No acepta UUID
de persona como prueba de identidad. Se implementará antes de integrar HU3/HU4.

El Responsable 2 debe:

1. Obtener contexto por el GET de empleado y mostrar sólo sus asignaciones.
2. Seleccionar idPersonaRol y clínica de la misma entrada, sin mezclar rol de una
   asignación y clínica de otra. Mantener token en el control compartido en memoria.
3. Enviar Bearer en Fetch; para operaciones HU3/HU4 se reserva
   `X-Persona-Rol: <idPersonaRol>` como selector de asignación, nunca como identidad.
   Conservar idClinica en query cuando ya sea parte de la ruta existente.
4. Llamar a exigirEmpleado desde el backend antes de ejecutar casos de uso;
   comprobar además política de la operación, recurso y relación con clínica.
   Si el paso requiere rol/responsable, verificar su correspondencia real con
   esa asignación. Ser empleado no autoriza modificar pasos ajenos.
5. Definir explícitamente la matriz de acciones HU3/HU4 en su contrato y pruebas;
   mientras no exista política para una acción, rechazar403. Los nuevos endpoints
   HU1 no habilitan ninguna escritura; no cambiar reglas clínicas ni sus DTO.
6. Ante401 limpiar sesión y pedir identificación; ante403 actualizar contexto y
   selección sin intentar con UUID de otra persona. Cierre exitoso invalida token.

La matriz mínima de este entregable es:

| Actor | Operación | Resultado permitido |
| --- | --- | --- |
| Sin sesión | Catálogo público, POST identificación | Sólo catálogo o identificación validada |
| PACIENTE | Contexto propio, citas/PDF propios, cerrar sesión propia | Lecturas de asignaciones paciente/clínicas válidas |
| PACIENTE | Gestión de empleados o escritura | Denegado403 |
| EMPLEADO | Contexto propio, cerrar sesión propia | Asignaciones empleadas válidas de esa persona |
| EMPLEADO | Citas del portal paciente | Denegado403; requiere sesión PACIENTE separada |
| EMPLEADO | Gestión HU3/HU4 | Sólo con guardia y política explícita de acción/recurso/rol/clínica |
| Cualquier sesión | UUID de asignación ajena | Denegado403 |

Adaptar CORS existente para admitir Authorization y X-Persona-Rol, manteniendo
orígenes exactos configurados, sin Allow-Credentials ni comodines. Exponer
Content-Disposition además de Total-registros/Location para descarga Fetch.
Registrar nuevos recursos y filtro en RestApplication. No cambiar DTO REST ni
renombrar DAO. La compatibilidad de DTO no preserva acceso anónimo a gestión.

## 8. Errores y estados de interfaz

Se reutiliza ApiError sin añadir códigos ficticios: `{"mensaje":"Texto seguro"}`.

| HTTP | Situación |
| --- | --- |
| 200 | Contexto, identificación, listado o PDF válidos |
| 204 | Cierre sin cuerpo |
| 400 | JSON mal formado (runtime) |
| 401 | Token ausente, inválido, expirado o revocado al leer/operar |
| 403 | Propósito incorrecto, sin rol requerido, clínica o asignación ajena, política denegada |
| 404 | Tipo/persona/cita no encontrados; ticket ajeno también404; recurso deshabilitado |
| 405 | Método no publicado (conservar Allow) |
| 409 | Documento ambiguo entre personas; conflictos de negocio heredados |
| 422 | Tipo omitido/inactivo, UUID/formato/valor/rango/paginación/campo desconocido inválidos |
| 500 | Fallo inesperado o configuración interna inválida, sin detalles internos |

Distinguir tipo existente inactivo422 de inexistente404. Una persona encontrada
sin asignación requerida devuelve403, no404. La caída de red no es un estado HTTP:
Control presenta desconexión, conserva sólo el formulario en memoria y permite
reintento; no convertir errores en listas vacías ni reutilizar una identidad
previa cuando cambia documento/tipo. Un aborto voluntario no es fallo de red.

Boundary muestra carga, validación, sin resultados y errores accesibles. Control
usa async/await, try/catch, AbortController al reemplazar búsqueda o desmontar,
bloqueo de envío duplicado y descarte de respuestas antiguas. Reintentar tras
recuperación de backend sin reenvíos de escritura automáticos. No mostrar SQL,
trazas o tokens en UI/logs. Todas las respuestas sensibles, también errores,
llevan Cache-Control:no-store; Service Worker sólo precarga recursos públicos
explícitos y nunca guarda respuestas REST ni PDF.

## 9. Fechas y zona

Filtro requerido: fechaDesde/fechaHasta como fechas civiles estrictas `YYYY-MM-DD`,
ambas reales y desde<=hasta; inválidas/ausentes422. Aplicar intervalo
`[desde 00:00 local, día siguiente a hasta 00:00 local)` a Consulta.fechaInicio,
reutilizando semántica de filtrosConsulta. No restringir fechas pasadas/futuras
con una regla nueva. Evitar desbordamiento al calcular día siguiente (422).

Salida de instantes con ISO-8601 UTC y Z, igual al backend existente. Mostrar en
zona IANA `GALENOS_CLINIC_TIME_ZONE` (default `America/El_Salvador`), publicada en
contexto; no asumir zona del navegador. fechaFin puede ser null. Consulta sin
fechaInicio no coincide con rango obligatorio. Expiración e emisión usan reloj
del servidor; no aceptar fecha/hora de emisión enviada por cliente.

## 10. Verificación pendiente y aislamiento

Este primer commit sólo documenta auditoría y contrato; no crea endpoints,
sesiones ni PDF y no acredita pruebas funcionales HU1/HU2.

Antes de completar implementación verificar las unidades y los escenarios de
HU1/HU2 de la solicitud: documentos inválidos/inexistentes, sólo paciente,
asignaciones múltiples/inactivas, clínica/identidad ajena, fechas, vacío/múltiples,
PDF legible y ticket ajeno, cierre/revocación y errores de red/servidor/reconexión.
Ejecutar mvn test, npm test, npm run check, npm run test:transport y declarar
separadamente integración HTTP real y navegador Chromium/Playwright disponible.

Las escrituras y fixtures se ejecutarán exclusivamente en una base sintética
nueva `galenos_rest_test_gt02_*`. ApiSettings actualmente impide modo academic
sobre esa base: se requiere adaptación limitada del perfil aislado y pruebas de
rechazo antes de integración. Nunca cambiar ni poblar clinica para probar.
No reutilizar base de desarrollo ni asumir que los fixtures existentes cubren
empleados con documento y múltiples asignaciones: deben ampliarse en el entorno
sintético. No arrancar ni desplegar sobre servidor existente para este entregable.

Puerta de continuidad solicitada: entregar el SHA de este commit y obtener
confirmación antes de interfaces o implementación; sin push ni merge a main.
