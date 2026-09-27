# Refactor: alinear la familia Consulta al patrón de Persona

Fecha: 2026-09-26
Estado: implementado

## Objetivo

Llevar las tres pantallas de la familia Consulta (`Consulta`,
`ConsultaProcedimiento` y `ConsultaProcedimientoPaso`) al patrón que itzep usa
en `Persona`: una hoja de estilos base compartida con deltas mínimos por
pantalla, validaciones de negocio en el modelo, deep-linking por `viewParam` y
apertura de edición con doble clic.

## Contexto

- `Persona.xhtml` depende de `galenos.css` (incluido por la plantilla) y deja
  `persona.css` en 4 líneas. Las pantallas de Consulta, en cambio, duplicaban
  ~360 líneas de CSS estructural cada una.
- `PersonaModel` valida contenido (regex de nombres, rangos de fecha,
  longitudes), normaliza con `trim` y expone `idPersonaContexto` para cargar
  una persona desde la URL.
- Las pantallas de Consulta solo usaban `required`, `p:messages` global y no
  tenían deep-linking; abrían la edición con `rowSelect` (un clic).
- Los datos de fechas (inicio/fin) no tenían coherencia temporal validada.

## Alcance

Incluye:

- Deduplicar el CSS de la familia Consulta hacia `galenos.css`.
- Unificar la clase de tabla como `.clinical-table`.
- Validación de coherencia de fechas en los tres modelos de la familia.
- Normalización (`trim`) de referencia/observaciones al guardar.
- Deep-linking `?idConsulta=<uuid>` en `Consulta.xhtml`.
- Cambio a `rowDblselect` con texto guía `crud.dobleClicEditar`.
- Mensajes de error por campo con `<p:message>`.
- Claves i18n nuevas en los 5 bundles.

No incluye (decisiones explícitas):

- Cambios en la base de datos ni en entidades JPA.
- Refactor de las otras 19 pantallas con CSS duplicado (catálogos de itzep).
- Validación de longitudes máximas de texto.
- `viewParam` de filtrado por persona/rol.

## Diseño

Enfoque A: base única en `galenos.css` + deltas por pantalla.

1. `galenos.css` absorbe los refinamientos que estaban duplicados en
   `consulta.css`: `.clinical-card` (sombra en dos capas), `.page-header`,
   `.page-title`, `.page-subtitle`, `.section-divider`, `.table-heading`,
   `.table-title`, `.clinical-dialog` (barra, título, contenido e inputs),
   `.dialog-intro`, `.dialog-form`, `.form-field`, `.form-label`,
   `.dialog-actions` sticky y `.edit-button`.
2. La tabla se unifica como `.clinical-table`; se eliminan
   `.consulta-table`, `.consulta-procedimiento-table` y
   `.consulta-procedimiento-paso-table`.
3. `consulta.css` queda con `.consulta-date`, `.personarol-cell` y
   `.personarol-name`; `consulta-procedimiento.css` con
   `.consulta-procedimiento-date` y `.uuid-text`; `consulta-procedimiento-paso.css`
   con `.persona-rol-text`, `.paso-date` y `.estado-badge` más sus estados.
4. `AbstractModel.lanzarValidacion(FacesContext, clave)` centraliza el acceso
   al bundle `msg`. `PersonaModel` y `ExamenModel` dejan de duplicarlo.
5. `ConsultaModel` agrega `idConsultaContexto` (getter/setter que carga por id),
   `validarFechaFin` y `normalizar()`. `ConsultaProcedimientoModel` y
   `ConsultaProcedimientoPasoModel` agregan `validarFechaFin`; el primero además
   normaliza observaciones.
6. Los XHTML cambian `rowSelect` por `rowDblselect`, agregan
   `<p:message for="...">` por campo y enlazan `validator` en los campos de
   fecha de fin. `Consulta.xhtml` agrega `f:metadata`/`f:viewParam`.

Regla de fechas: si `fechaFin` no es nulo y es anterior a `fechaInicio`, se
lanza un `ValidatorException` con la clave i18n `*.fechaFinAnterior`. La fecha
de fin es opcional.

## Comportamiento esperado

- Doble clic en una fila abre el diálogo de edición con los datos.
- Un clic simple solo selecciona la fila, sin abrir el diálogo.
- `?idConsulta=<uuid>` abre esa consulta en edición; un id inválido no rompe y
  deja la vista en modo listado.
- Guardar recorta espacios en referencia y observaciones.
- Si la fecha de fin es anterior a la de inicio, se muestra el error junto al
  campo y no se persiste.

## Testing

Unitarios (`mvn -o test`, TDD):

- `ConsultaModel`: contexto válido, inválido y en blanco; fecha fin anterior,
  igual y nula; recorte de referencia y observaciones.
- `ConsultaProcedimientoModel`: fecha fin anterior y nula; recorte de
  observaciones.
- `ConsultaProcedimientoPasoModel`: fecha fin anterior y nula.

Resultado: 333 pruebas, 0 fallos (321 previas + 12 nuevas). Los tres XHTML
validan como XML.

## Riesgos

- Bajo en persistencia: no se tocaron entidades ni el esquema.
- El enfoque A promueve los refinamientos visuales de Consulta a toda la app,
  por lo que Persona y los catálogos heredan encabezado centrado, título en
  negrita y acciones de diálogo sticky. Es un cambio estético intencional de
  consistencia.
- No verificado en navegador (requiere Liberty + PostgreSQL); la verificación
  fue compilación, pruebas unitarias y validación XML.
