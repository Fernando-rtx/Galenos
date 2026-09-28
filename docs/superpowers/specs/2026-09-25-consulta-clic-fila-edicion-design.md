# Mejora: abrir la edición de Consulta con clic en la fila

Fecha: 2026-09-25
Estado: aprobado (diseño)

## Objetivo

En la pantalla `Consulta.xhtml`, abrir el diálogo de edición haciendo clic
sobre la fila del registro y eliminar el botón "Editar" de la columna Acciones.

## Contexto

- La tabla `consultasTable` ya tiene `selectionMode="single"` y un
  `p:ajax event="rowSelect"` que hace `update="edicionDialog"` y
  `oncomplete="PF('consultaDialog').show()"`.
- El listener actual es `#{consultaModel.seleccionar(item)}`. En el evento
  `rowSelect` la variable `item` de la tabla llega nula, por lo que el modelo
  guarda `seleccionado = null`: el diálogo abre vacío.
- El botón "Editar" duplica el flujo y sí funciona porque usa
  `action` con `process="@this"`, donde la variable sí está disponible.

## Alcance

Incluye:

- Quitar la columna "Acciones" con el botón Editar de `consultasTable`.
- Corregir el listener de `rowSelect` para que la edición abra con datos.

No incluye (decisiones explícitas):

- Cambios en la estructura de la base de datos (solo agregar/quitar registros).
- Cambios en entidades JPA.
- Filtrado de catálogos activos, validaciones de fechas u otras mejoras.
- Modificar las tablas internas de Procedimientos y Pasos (ya editan con
  doble clic y usan el seleccionado enlazado).

## Diseño

Enfoque A: usar el `seleccionado` que la tabla ya enlaza por `selection`.

1. `Consulta.xhtml`: eliminar el `p:column` de la columna "Acciones" que
   contiene el `p:commandButton` de edición.
2. `Consulta.xhtml`: cambiar el listener del `p:ajax` de `rowSelect` a
   `#{consultaModel.editarSeleccionado}` (se mantienen `update="edicionDialog"`
   y `oncomplete="PF('consultaDialog').show()"`).
3. `ConsultaModel`: agregar el método sin argumentos `editarSeleccionado()`
   que, si `seleccionado != null`, ejecuta `setEstado(EDICION)`.

Se descartaron:

- Enfoque B: `seleccionar(consultaModel.seleccionado)` en línea, menos
  explícito y menos testeable.
- Enfoque C: listener con `RowSelectEvent`, que acopla el modelo a la API de
  JSF.

## Comportamiento esperado

- Clic en cualquier fila abre el diálogo "Editar consulta" con las pestañas
  Detalle, Procedimientos y Pasos y los datos del registro.
- Clic en la misma fila después de cerrar el diálogo lo vuelve a abrir
  (verificado que PrimeFaces reemite `rowSelect`).
- El botón "Nuevo" sigue abriendo el diálogo en modo creación.
- "Cancelar" limpia la selección y refresca la tabla.

## Testing

Unitarios (`ConsultaModelTest`, TDD):

- `editarSeleccionado` cambia el estado a EDICION cuando hay seleccionado.
- `editarSeleccionado` no lanza y no cambia nada cuando el seleccionado es
  nulo.

E2E (navegador):

- El clic en la fila abre el diálogo con pestañas y datos del registro.
- Guardar sigue funcionando.
- "Nuevo" sigue funcionando.
- La columna Acciones ya no existe.

## Riesgos

- Bajo. El cambio no toca persistencia ni entidades. Si el usuario cierra el
  diálogo con la X, la selección permanece en la tabla; se verificó que un
  nuevo clic reabre el diálogo.
