# HU4: diagnóstico de bloqueos y propuesta pendiente de autorización

Base de ejecución: **clinica**. Rama: `feat/gt02-hu4-atencion`, desde `c72ed0f`.
Inspección mediante SELECT/BEGIN READ ONLY; ningún DDL, INSERT, UPDATE o DELETE
ejecutado. No se creó otra base ni se cargaron fixtures.

## Causas verificadas

**Ejecución sin plantilla.** La consulta `a11b2f21…` tiene una ejecución EN_CURSO
`e0da3669…`, con id_procedimiento_paso NULL, en el procedimiento inactivo
`02e33735…`. Existen dos plantillas para ese procedimiento y ambas tienen el mismo
rol. El responsable o el nombre del paso no identifican inequívocamente cuál se
ejecutaba. La fecha_inicio declarada tampoco demuestra cuándo se insertó el registro.

La relación fue añadida en Git en septiembre (`f779e37`/`955390b`), retirada al
alinear la aplicación con la base de entonces (`0da093f`) y restaurada en noviembre
(`0230590`). Antes de este último commit, crearProcedimiento creaba la ejecución
sin llamar a setIdProcedimientoPaso(inicial). El defecto histórico es verificable;
sin auditoría no se puede atribuir con certeza esta fila a aquella versión.
El código actual ya enlaza la plantilla y **no debe rellenar registros antiguos**.
El grafo del procedimiento sí tiene una raíz: `b3efc88e…`. Es la plantilla inicial
para una creación nueva; asignársela a una ejecución histórica requiere confirmar
que aquella ejecución era realmente inicial y nunca había transitado.

**Orden sin relación individual con Examen.** El SQL original `e1ba5e7` y la entidad
anterior a `0230590` no tenían orden_examen.id_examen. `0230590` añadió a la vez esa
relación JPA, su uso en Services/REST, la columna al SQL versionado y una migración.
clinica conserva la estructura anterior de orden_examen. Es una ampliación del
modelo desplegada en Java sin su parte SQL correspondiente; **la base no incumple
el diseño original por carecer de esa columna**. No hay historial de migraciones
que permita afirmar cómo se aplicaron las otras ampliaciones.

La ampliación tiene una razón de negocio para HU4: identificar el examen que se
seleccionó en cada orden. ProcedimientoPasoExamen solo expresa exámenes permitidos;
no cuál se pidió, y ExamenResultado apunta a la orden, no al examen. No existe una
columna equivalente que permita corregir el problema renombrando un JoinColumn.
Retirar/transientar el campo evitaría ese SELECT, pero perdería esa asociación y
sus validaciones. No se hizo ese cambio ni se dedujo el examen desde indicaciones.

## Comparación de estructura y claves

| Tabla | PostgreSQL frente a JPA/SQL versionado |
|---|---|
| orden_examen | Cuatro columnas: id_orden_examen UUID PK, id_consulta_procedimiento_paso UUID FK, fecha_creacion timestamptz, indicaciones text. Falta únicamente id_examen de la ampliación actual y su FK a examen. |
| examen | id_examen UUID PK, nombre varchar(255), activo boolean, observaciones text; coincide con Examen. No tiene relación inversa que sustituya la FK ausente en orden. |
| consulta_procedimiento_paso | id, asociación, responsable, fechas, estado varchar(20), id_procedimiento_paso UUID y valor text; coincide con el mapeo actual. La FK a plantilla existe como fk_consulta_procedimiento_paso_consulta_paso, convalidated=false. |
| procedimiento | UUID PK, nombre varchar(155), activo boolean, observaciones text; coincide con JPA. El procedimiento de la ejecución está inactivo. |
| procedimiento_paso | UUID PK, UUID FK a procedimiento, nombre varchar(155), indica_fin boolean, UUID FK a rol; coincide con JPA. La FK de rol está NOT VALID. |
| Relaciones complementarias | Orden→ejecución y resultado→orden existen y están validadas. ConsultaProcedimiento.idProcedimiento y el destino de ProcedimientoPasoSecuencia son UUID simples en el modelo; no se añadieron FKs para ellos. |

NOT VALID no significa que una FK permita UUID nuevos inexistentes; significa que
no se certificaron todas las filas anteriores. NULL está permitido por la columna,
incluso con una FK validada. Hay **cero referencias no nulas huérfanas** de ejecución
a plantilla. Validar o duplicar esa FK no arregla la plantilla NULL.

La migración antigua comprueba el nombre de la FK, pero la FK de plantilla ya
existe con otro nombre en clinica: ejecutarla completa podría duplicarla.
No se propone ejecutarla a ciegas ni recrear el esquema.

## Asignación inicial y corrección de código

ConsultaFlujoService.crearProcedimiento verifica clínica y procedimiento activo;
ProcedimientoPasoDAO obtiene la raíz única del grafo de secuencias y rechaza
ciclos/destinos ajenos. Se busca un PersonaRol activo del rol inicial en la clínica,
se crea ConsultaProcedimiento y luego su ejecución PENDIENTE con esa plantilla.
No corresponde saltar directamente al paso Doctor ni escoger el menor UUID de
plantilla. La selección de responsable mantiene la política existente del DAO.

Se corrigió un defecto adicional comprobado: el servicio aceptaba una plantilla
inicial con rol Paciente y generaba una ejecución que HU2/HU4 no pueden atender.
Ahora la rechaza antes de persistir, reutilizando IdentidadAcademicaService.paciente.
La prueba nueva falló antes de la corrección y pasó después. La prueba positiva
existente ahora exige que la ejecución conserve exactamente la plantilla inicial.
También se corrigieron comentarios obsoletos: SchemaMappingTest contrasta JPA
con el archivo SQL, **no** demuestra que ese esquema esté desplegado en PostgreSQL.

## Qué consulta sirve

Estado actual:20tablas,2consultas,1asociación,1ejecución,1orden y1resultado.
La consulta SQL con procedimiento activo, clínica/rol/responsable válidos y
plantilla coincidente devolvió **cero pasos atendibles**.

La consulta `b0662d9b-853c-42a1-bd41-5bfb4cc00844` no tiene procedimiento asociado:
es candidata para preparación manual autorizada, **no está lista para HU4**.
Los dos procedimientos activos empiezan por Atención al público; no hay ninguna
asignación para ese rol en clínicas activas. El rol Enfermera intermedio está
inactivo. Antes de una demostración completa se necesitan decisiones reales sobre
esas asignaciones/catálogo, además de compatibilizar las órdenes. No se reactivó
el procedimiento de desarrollo ni se alteró una consulta para hacerla aparecer.

## Propuesta mínima de PostgreSQL — NO EJECUTADA

La decisión pendiente es aceptar la asociación individual Orden→Examen de HU4,
no agregar una columna por el solo hecho de declararla Java. Si se aprueba:

1. Volver a consultar columnas/FKs y contar órdenes/resultados; detenerse si cambia
   el diagnóstico. Verificar PK examen.id_examen UUID y que no haya una columna
   o relación equivalente ya añadida por otro responsable.
2. Respaldar clinica con pg_dump a un archivo protegido y probar su lectura con
   pg_restore --list. Esto no exige crear otra base. Acordar una ventana sin escrituras.
3. Agregar solamente una columna UUID nullable y su FK. La orden anterior conserva
   todos sus valores y queda sin examen identificado (NULL); su resultado no cambia.
   No se hace backfill ni se activa ningún procedimiento o modifica la plantilla.

```sql
-- BORRADOR: no ejecutado. También un ensayo con ROLLBACK requiere autorización.
BEGIN;
SET LOCAL lock_timeout = '3s';
SET LOCAL statement_timeout = '30s';
DO $revision$
BEGIN
  IF current_database() <> 'clinica' THEN
    RAISE EXCEPTION 'La revisión corresponde exclusivamente a clinica';
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema='public' AND table_name='orden_examen'
      AND column_name='id_examen'
  ) THEN
    RAISE EXCEPTION 'El esquema cambió: revisar antes de continuar';
  END IF;
END;
$revision$;
ALTER TABLE public.orden_examen ADD COLUMN id_examen uuid;
ALTER TABLE public.orden_examen
  ADD CONSTRAINT fk_orden_examen_examen FOREIGN KEY (id_examen)
  REFERENCES public.examen(id_examen)
  ON UPDATE CASCADE ON DELETE RESTRICT;
SELECT count(*) AS ordenes, count(id_examen) AS ordenes_identificadas
FROM public.orden_examen;
ROLLBACK; -- Solo tras aprobación, el plan definitivo sustituiría esto por COMMIT.
```

**Registros afectados si se aprueba:** una tabla ampliada;20tablas totales.
La orden `a9c64f7f…` gana una columna NULL; cero cambios de sus datos anteriores,
del resultado, de las consultas o de los catálogos. No se modifica la FK de plantilla.

**Recuperación:** hasta COMMIT, cualquier fallo/timeout revierte el DDL completo.
Después del COMMIT, conservar la columna mientras se revisa una incidencia evita
perder asociaciones nuevas. Solo si se acredita que nunca fue utilizada, y con
autorización de reversión, podría retirarse la FK y la columna añadidas:

```sql
-- BORRADOR de reversión; no ejecutar si existen valores o nuevas dependencias.
BEGIN;
SET LOCAL lock_timeout = '3s';
LOCK TABLE public.orden_examen IN ACCESS EXCLUSIVE MODE;
DO $revision$
BEGIN
  IF current_database() <> 'clinica' OR
     EXISTS (SELECT 1 FROM public.orden_examen WHERE id_examen IS NOT NULL) THEN
    RAISE EXCEPTION 'La reversión requiere una revisión que conserve los datos';
  END IF;
END;
$revision$;
ALTER TABLE public.orden_examen DROP CONSTRAINT fk_orden_examen_examen;
ALTER TABLE public.orden_examen DROP COLUMN id_examen; -- sin CASCADE
ROLLBACK; -- COMMIT solo en un plan de reversión expresamente autorizado.
```

La reparación de la ejecución antigua no tiene un UPDATE propuesto: falta una
decisión clínica que determine su plantilla y si debe seguir habilitada. Para
preparar la segunda consulta se usarían las funcionalidades normales del backend,
con persona/asignación aprobadas; crear la asociación generaría su paso inicial.
Los registros nuevos afectados se identificarán antes de solicitar esa autorización.

## Verificación y límites

Pruebas relacionadas:79 aprobadas; regresión `mvn test`:843 aprobadas, cero fallos,
errores u omitidas. El test nuevo usa JUnit/Mockito, no persistencia real.
Lecturas REST en el backend normal9080: identificación200, bandeja200 y consulta
de otro responsable403. Solo se crearon/cerraron sesiones en memoria Java.
Las huellas de las20tablas, sus datos, columnas y restricciones no cambiaron.
Guardar, órdenes, avance, retroceso y rollback SQL siguen sin comprobarse con escrituras autorizadas.
No se declara que la migración esté aplicada ni que exista ya un caso funcional.

Corrección de código: commit `8f9c485`. Este diagnóstico y su SQL propuesto se
registran en un commit documental posterior. Frontend de producción sin cambios.
