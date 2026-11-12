# GalenosSV GT02 — Entrega HU1/HU2

Implementación sobre las ramas feat/gt02-hu1-hu2 de Galenos y GalenoSVFE.
Contrato definitivo: [gt02-contratos-rest.md](gt02-contratos-rest.md).
La rúbrica oficial independiente no estuvo disponible: se verificaron los requisitos
aportados por el usuario, sin afirmar cotejo literal con un documento no localizado.

## Una aplicación y una base compartida

La ejecución normal conserva PostgreSQL **clinica**, datasource **jdbc/pgdb**,
GALENOS_API_MODE=academic, WAR GalenosSV y contextRoot /GalenosSV. Se comprobó la
configuración privada de GalenosSVAcademico: puerto9080 y baseclinica, sin imprimir
credenciales. No se sustituyó ese servidor ni se desplegó automáticamente sobre él.

No se modifica ninguna de las veinte entidades JPA, el esquema SQL ni persistence.xml.
JPA conserva schema-generation=none. No se cambió ni eliminó ningún registro de
clinica. La comprobación HTTP de lectura comparó hashes de las veinte tablas antes
y después: todos coincidieron. Identificar/cerrar sesiones cambia sólo memoria Java.

## Reutilización y extensiones

- PersonaDAO, DocumentoDAO, TipoDocumentoDAO, PersonaRolDAO, RolDAO,
  ConsultaDAO, ConsultaProcedimientoDAO y ProcedimientoDAO existentes.
- ConsultaDAO añade buscar/contar por las asignaciones paciente autorizadas,
  intervalo de fechas y paginación, ejecutados en PostgreSQL.
- Nuevos servicios IdentidadAcademicaService, SesionAcademicaService y
  CitasPacienteService cubren casos de uso ausentes; no duplican CRUD existentes.
- IdentificacionException conserva errores esperados al atravesar EJB;
  ApiExceptionMapper responde con ApiError existente, sin trazas.
- Nuevos recursos IdentidadAcademicaResource y CitasPacienteResource registrados
  en RestApplication; filtro AccesoAcademicoFilter, CORS y contexto acotados.
- TicketPdf genera un PDF binario Java real con Java2D/ImageIO, sin bibliotecas
  nuevas. PDFKit abrió y renderizó un ticket y un fixture de tres páginas.
- El frontend conserva el shell, dashboard, navegación de módulos, apariencia,
  Services y controles existentes. Reutiliza TipoDocumentoService, DefaultService,
  DefaultResponse, DefaultError y tarjeta-consulta. Sus comportamientos previos
  se mantienen cuando no se activa la presentación portalPaciente.
- Dos Custom Elements nuevos BusquedaCitasModel e InicioSesionModel comparten
  presentación y usan el tema CSS existente. Control coordina estado; Boundary
  renderiza/emite eventos. Entity no conoce Fetch ni DOM.
- El Service Worker conserva lista pública cerrada. No guarda REST, sesiones,
  documentos ni PDF. Se verificó Cache Storage desde Chromium.

## Resumen HTTP

Base /GalenosSV/resources. Todas las respuestas sensibles llevan Cache-Control:no-store.

| Método | Ruta | Entrada | Salida |
| --- | --- | --- | --- |
| GET | /v1/tipo_documento | first/max | DTO existente de tipos, Total-registros |
| POST | /v2/identidad_academica/paciente | JSON idTipoDocumento, valor | token, expiraEn, contexto PACIENTE |
| POST | /v2/identidad_academica/empleado | JSON idTipoDocumento, valor | token, expiraEn, contexto EMPLEADO |
| GET | /v2/identidad_academica/paciente o empleado | Authorization Bearer | contexto propio revalidado |
| DELETE | /v2/identidad_academica/paciente o empleado | Authorization Bearer | 204, revocación |
| GET | /v2/citas_paciente | Bearer paciente; idClinica, fechaDesde, fechaHasta, first/max | CitaPaciente[], Total-registros |
| GET | /v2/citas_paciente/{id}/ticket | Bearer paciente; Accept application/pdf, application/json | PDF200, attachment; cita ajena/inexistente404 JSON |

Contexto: tipo, idPersona, personaNombre, zonaHoraria y asignaciones con
idPersonaRol, idRol, rolNombre, idClinica, clinicaNombre. No expone documento ni
fecha de nacimiento. CitaPaciente: idConsulta, idClinica, clinicaNombre,
fechaInicio/fechaFin y procedimientos con IDs y nombre. Fechas UTC en JSON;
rango civil y presentación en America/El_Salvador por defecto.

Errores:400 JSON mal formado,401 sin sesión/expirada,403 sin asignación o acceso,
404 inexistente,409 documento ambiguo,422 validación,500 inesperado seguro.
El contrato detalla casos y respuestas del cierre idempotente.

## Permisos e integración HU3/HU4

No existe PersonaRol.activo. Asignación disponible exige que exista, corresponda
a la persona y que su Rol y Clinica estén activos. El rol paciente se reconoce
con el criterio existente de nombre. El esquema no clasifica empleados:
GALENOS_EMPLEADO_ROLES debe contener UUID de roles empleados reales configurados
por el servidor. Vacío no permite entrar; nunca incluir Paciente. No se agrega
una columna ni se infiere permiso de empleado por cualquier rol arbitrario.

Se verificó Doctor en clinica para el ensayo de sólo lectura; ese ID se configuró
únicamente en el runtime privado de verificación, no se fijó en el código ni se
alteró el server.env compartido. Para desplegar, acordar los IDs existentes que
corresponden a empleados y configurar esta variable en el servidor compartido.

Responsable 2 puede importar en frontend:

```js
import {
  obtenerContextoEmpleado,
  actualizarContextoEmpleado,
  solicitudEmpleado,
} from './control/contexto-empleado.js';

const contexto = obtenerContextoEmpleado();
// Elegir una entrada de contexto.asignaciones, sin mezclar IDs de entradas.
// solicitudEmpleado añade Bearer y X-Persona-Rol desde el control HU2 en memoria.
```

Backend: llamar IdentidadAcademicaService.exigirEmpleado(bearer, idPersonaRol,
idClinica) antes del caso de uso. Devuelve una PersonaRol revalidada. Comprobar
además permiso de acción/recurso/responsable y clínica. Nunca autorizar por un UUID
recibido del navegador ni por puedeEscribir del antiguo contexto.

Las lecturas clínicas existentes conservan DTO y parámetros, y ahora requieren
Bearer empleado y X-Persona-Rol de la misma clínica. clinica_disponible sólo devuelve
clínicas propias; con X-Persona-Rol se limita a la asignación elegida. contexto
conserva sus claves, con actor academico-empleado y puedeEscribir=false.

**Límite deliberado:** el acceso anónimo de gestión anterior deja de permitirse.
Las escrituras y listados globales siguen denegados hasta definir una política
explícita con Responsable 2. Los módulos anteriores siguen en el código y el
dashboard de lectura funciona con la asignación elegida; esto no acredita que el
CRUD administrativo anterior funcione sin adaptar su autorización. No se tocaron
los componentes internos de HU3/HU4 ni se otorgó administración ilimitada.

## Pruebas reproducibles y evidencia

- mvn test:795 pruebas, cero fallos/errores/omitidas, Java21; incluye fixtures y
  mocks unitarios, no acredita integración por sí solo.
- npm test:441 pruebas, incluidas18 nuevas para HU1/HU2 con Fetch simulado.
- npm run check:95 archivos con sintaxis válida.
- npm run test:transport:9 pruebas HTTP con servidor sintético local; no PostgreSQL.
- test/integration/gt02_http.py:47 comprobaciones HTTP reales contra Liberty y
  PostgreSQL temporal ficticio: identificación, clínicas, fechas, paginación,
  consultas ajenas, PDF, roles múltiples, errores HTTP, CORS y revocación.
- test/integration/gt02_clinica_readonly.py:HTTP real contra clinica con JDBC de
  sólo lectura; empleado válido, contexto/cierre, paciente inactivo rechazado,
  acceso global sin sesión rechazado, hashes iguales en veinte tablas.
- GalenoSVFE/test/browser/gt02.py:Chromium -> Fetch -> Liberty -> PostgreSQL temporal;
  sin mock de Fetch, descarga real, desconexión/reconexión del navegador, selección
  de PersonaRol, dashboard existente y verificación de almacenamiento.
- PDFKit nativo:apertura y renderizado de un ticket y PDF de tres páginas, con
  revisión visual de acentos, márgenes y contenido. No se generó el PDF en Python
  ni en JavaScript.

Evidencia sanitizada: test/evidence/gt02-http.json y gt02-clinica-readonly.json
(backend); gt02-browser.json, gt02-hu1.png y gt02-hu2.png (frontend).
PDF de ensayo local en .gt02/evidence/ticket.pdf; no contiene personas reales.
No se afirma caída real de PostgreSQL ni rollback REST probado; errores de servidor
se prueban con dobles, y la desconexión real se provoca en Chromium. No hay datos
clínicos reales, documentos o tokens en la evidencia versionada.

La base temporal **galenos_rest_test_gt02_20261111_hu12** fue autorizada por el
usuario exclusivamente para pruebas. El runner verificó que no existía antes de
crearla y cargó esquema versionado y fixtures ficticios; nunca copió clinica.
No se configura como base normal ni se elimina automáticamente. Intentar preparar
otra vez ese nombre debe fallar sin cambiar su contenido.

Comandos para repetir sobre una base de ensayo NUEVA, desde el backend:

```sh
export GT02_DATABASE=galenos_rest_test_gt02_nombre_nuevo
python3 scripts/gt02_test_runtime.py prepare
mvn -q -DskipTests -Dliberty.install.dir=/private/tmp/gt02-build package
python3 scripts/gt02_test_runtime.py stage
python3 scripts/gt02_test_runtime.py run
# En otra terminal:
python3 test/integration/gt02_http.py
```

El runner usa por defecto el contenedor local postgres_db, esquema versionado
y prefijo GT02; necesita Docker, Java21, Liberty y sus bibliotecas ya disponibles.
Su configuración con credenciales es privada en .gt02/, ignorada por Git. Para
lectura de clinica usar gt02_readonly_runtime.py stage/run, con
GALENOS_EMPLEADO_ROLES explícito; fuerza default_transaction_read_only=on en JDBC,
puerto19084 y no reemplaza el servidor compartido. El ensayo ficticio usa19085.
Para Chromium, apuntar temporalmente environment.local.js del frontend al backend
de ensayo y ejecutar su script con un entorno donde Playwright/Chromium estén
instalados; al terminar restaurar la configuración local normal9080.

## Límites pendientes de despliegue y rúbrica

El rol Paciente de clinica está inactivo. HU1 correctamente lo rechaza; su recorrido
válido y PDF se comprobaron contra PostgreSQL ficticio. No se cambió ese rol para
hacer pasar una prueba. Habilitarlo o preparar pacientes reales requiere autorización.
HU2 sí se comprobó con una identidad disponible en clinica sin persistir sus datos.

El PDF contiene imágenes de las páginas Java2D: es un PDF real legible, pero su texto
no es seleccionable ni está etiquetado para lectores de pantalla. Las fuentes del
servidor determinan la cobertura de caracteres; se verificaron acentos españoles.
Las sesiones son académicas y efímeras, no autenticación fuerte de producción.

El despliegue compartido9080 aún no fue reemplazado; no hubo push ni merge a main.
Antes de integrarlo, coordinar IDs empleados y políticas HU3/HU4 y cotejar la rúbrica
oficial cuando esté disponible. No se afirma cobertura de las cuatro historias.
