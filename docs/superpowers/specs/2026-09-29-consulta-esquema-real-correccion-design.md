# Corrección: alinear Consulta y Exámenes al esquema real

Fecha: 2026-09-29
Estado: aprobado

## Objetivo

Retirar del código la relación JPA no soportada
`consulta_procedimiento_paso.id_procedimiento_paso`, restaurar los scripts SQL
para que documenten el esquema real y completar las validaciones y acciones de
los botones de Consulta y Exámenes, sin perder la funcionalidad válida del
PR #6. La corrección adapta el código a la base de datos, nunca la base de
datos al código.

## Contexto

- El PR #6 (`feature/fernando`, mergeado en `main` el 2026-09-28) introdujo la
  relación `ConsultaProcedimientoPaso.idProcedimientoPaso` (`@ManyToOne` a
  `ProcedimientoPaso`), la migración `clinica_migracion_2026_09_27.sql`, la
  columna y el CHECK en el SQL versionado, un selector/columna en
  `Consulta.xhtml`, un converter y pruebas del modelo.
- La base real (`clinica_ppi115_2026_08_20.sql`, dump del Escritorio) NO tiene
  la columna `id_procedimiento_paso` en `consulta_procedimiento_paso`; esa tabla
  solo relaciona `consulta_procedimiento`, `persona_rol`, fechas y estado.
- Al abrir Órdenes de examen, `OrdenExamenModel` carga la entidad y PostgreSQL
  responde que la columna no existe, lo que dispara WELD-000049/PSQLException.
- El plan técnico en `Plan_de_trabajo_Fernando_GalenosSV.docx` (secciones 3 a 9)
  define la corrección obligatoria y el barrido de botones.

## Alcance

Incluye:

- Retirar la relación inválida de entidad, modelo, vista, SQL, pruebas, i18n y
  documentación.
- Restaurar `clinica_ppi115_2026_08_20.sql` al esquema real y eliminar
  `clinica_migracion_2026_09_27.sql`.
- Alinear la base docker local al esquema real.
- Agregar una prueba de mapeo de esquema que detecte discrepancias
  entidad↔tabla.
- Auditar y completar botones/validaciones de Consulta, Examen,
  ExamenTipoExamen, OrdenExamen, ExamenResultado y TipoExamen.
- Verificar `mvn clean test`, `mvn clean package` y HTTP sobre Liberty.

No incluye (decisiones explícitas):

- Agregar la columna a PostgreSQL o ejecutar la migración.
- Cambios en `persistence.xml`, `beans.xml`, `pom.xml` o `server.env`.
- Revertir con git revert ni reescribir historia (se usan commits nuevos).
- Tocar las pantallas de plantillas `ProcedimientoPaso`, `ProcedimientoPasoExamen`
  y `ProcedimientoPasoSecuencia`, que usan legítimamente `procedimientoPasoConverter`.

## Diseño

Enfoque A: edición quirúrgica hacia adelante sobre `feature/fernando`.

1. `ConsultaProcedimientoPaso.java`: eliminar el campo `idProcedimientoPaso`, sus
   anotaciones `@ManyToOne`/`@JoinColumn`, getter, setter e imports sobrantes.
2. `ConsultaProcedimientoPasoModel.java`: retirar `ProcedimientoPasoDAO`,
   `getPasosDeProcedimiento`, `getRolRequerido`, `getPersonasRolesParaPaso` y
   `limpiarResponsableInvalido`; simplificar `validarResponsable` para que no
   infiera un rol que la BD no posee.
3. `Consulta.xhtml`: en el tab de pasos, eliminar la columna de plantilla, el
   `selectOneMenu` `pasoProcedimientoPaso` con sus `p:ajax` y el filtrado por rol
   del responsable. Quedan procedimiento de consulta, responsable, fechas,
   estado y el enlace a órdenes.
4. `clinica_ppi115_2026_08_20.sql`: revertir los cambios de `f779e37` y
   `eaa5250` (sin columna ni CHECK). Eliminar `clinica_migracion_2026_09_27.sql`.
5. `SchemaMappingTest`: parsea los `CREATE TABLE` del SQL versionado y, por
   reflexión, verifica que cada `@Table` y cada `@Column`/`@JoinColumn` de las
   entidades exista en su tabla real.
6. `ConsultaProcedimientoPasoModelTest`: retirar los casos que asumían la
   relación inexistente y ajustar los de guardar/validar.
7. i18n: eliminar `consultaProcedimientoPaso.procedimientoPaso` y
   `consultaProcedimientoPaso.rolNoCorresponde` de los 4 bundles.
8. `README.md`: quitar la instrucción de migración y documentar el contrato real.
9. Barrido de botones (secciones 5-7 del plan): revisar Nuevo, Editar, Guardar,
   Cancelar, Agregar/Quitar y Eliminar en cada pantalla; aplicar reglas de un
   solo FacesMessage, validación antes de persistir, cancelar sin residuos y
   errores de persistencia capturados.

## Comportamiento esperado

- `OrdenExamen.xhtml` abre y lista sin consultar `id_procedimiento_paso`.
- El tab de pasos de Consulta conserva responsable, fechas, estado y
  procedimientos, sin selector basado en una relación inexistente.
- Todo botón de Consulta y Exámenes valida, muestra un único mensaje y no deja
  selecciones residuales al cancelar.
- Las relaciones JPA coinciden exactamente con las columnas y FKs reales.
- `SchemaMappingTest` falla si en el futuro se vuelve a mapear una columna que no
  existe en el SQL versionado.

## Testing

- `mvn clean test`: cero failures, cero errors, sin skipped inesperados.
- `mvn clean package`: WAR generado.
- `git diff --check` y búsqueda de marcadores de conflicto.
- HTTP sobre `mvn liberty:run` con la skill webapp-testing: OrdenExamen,
  ExamenResultado, Consulta, Examen, ExamenTipoExamen y TipoExamen.
- Revisión de logs: sin WELD-000049, PSQLException, FacesException, ELException,
  MethodNotFoundException ni PropertyNotFoundException.

## Riesgos

- Bajo en base de datos: no se agregan columnas; solo se limpia el docker local.
- El barrido de botones puede revelar brechas en pantallas fuera del flujo de
  consulta; se corrigen en el mismo esfuerzo.
- El conteo de pruebas del plan (482) no coincide con el local (361); el criterio
  real es verde total tras agregar las pruebas nuevas.

## Criterios de aceptación

Tal cual el plan técnico: OrdenExamen abre sin `id_procedimiento_paso`; Consulta
conserva responsable, fechas, estado y procedimientos; botones de Consulta y
Exámenes validados; relaciones JPA iguales al esquema real; arquitectura
`@EJB`/`getDao()`/`@PostConstruct` intacta; todo verde en tests y package.
