# HU4: estado real y diagnóstico de bloqueos

## Actualización autorizada — 2026-11-15

- Operación A aplicada exactamente: `orden_examen.id_examen UUID` nullable y FK
  `fk_orden_examen_examen` validada. Una transacción; 20 tablas conservadas. Las
  firmas del contenido anterior coincidieron antes/después, excluyendo solo la
  columna nueva; la orden previa conserva NULL y su resultado.
- Nueve altas nuevas aplicadas transaccionalmente con comprobación de colisiones
  y de conservación del contenido anterior. No se aplicó la B original, ni se
  activaron roles, ni se crearon respaldos u otras bases.
- Tipo **Pasaporte**, clínica **Clinica Central** para ambas identidades:

| Portal | Documento académico | Rol |
|---|---|---|
| `http://127.0.0.1:3000/#pacientes` | `ACADGT02PAC001` | Paciente |
| `http://127.0.0.1:3000/#empleados` | `ACADGT02REC001` | Atención al publico |

Consulta `18d1c9f4-c2f5-452f-88d5-0c43d4110348`, referencia
`GT02-HU34-20261115`, creada el **2026-11-15 12:49:15 America/El_Salvador**.
Procedimiento **Evaluación de Salud**, paso **Registro de ingreso**. Si se prueba
otro día, filtrar desde/hasta 2026-11-15. Los servicios ya estaban activos en
frontend 3000 y backend 9080; si están detenidos: `./run.sh` desde
`~/Desktop/GalenosSV_Actualizado` y `npm run dev` desde `~/Desktop/GalenoSVFE`.

Chromium contra BrowserSync y Liberty reales, sin sustituir REST: identificación,
clínica/asignación propia, cita y PDF Java descargado, tarjeta de dashboard,
apertura HU4, guardado de una nota académica por `POST v2/atencion/registrar`,
GET posterior desde una sesión nueva y SELECT confirman valor y estado EN_CURSO.
La única modificación clínica fue a la ejecución académica recién insertada
`6c3e2823-3133-4259-83b8-a8bdd0cd5044`. No se modificó la ejecución antigua sin
plantilla. Filtros pasados/restablecer, desconexión y reintento comprobados.
PersonaRol ajeno devuelve 403; una consulta de otra clínica se oculta con 404.
Paciente sin asignación de empleado rechazado403 y cierre de sesión realizado.

Pruebas relacionadas: **79 JUnit/Mockito**, **504 Mocha**, **9 de transporte
HTTP sintético**, sintaxis de **110 archivos**: aprobadas. Evidencia local en
`/private/tmp/gt02-academicos-reales/` (resultado.json, hu1–hu4.png y PDF académico).
El primer ensayo corrigió dos expectativas del verificador, sin cambios de
producción: el PDF requiere Accept adecuado y una consulta de otra clínica se
oculta con 404. La ejecución final de Chromium terminó aprobada.

Pendiente para completar HU3/HU4: el paso siguiente usa **enfermera**, actualmente
inactivo y sin asignaciones. Doctor está activo con dos asignaciones en esta
clínica; no se añadió otro responsable ni se alteró su prioridad. Ningún paso de
este procedimiento tiene exámenes activos habilitados. Se necesita decisión y
autorización de negocio para habilitar Enfermería y su asignación, y confirmar
qué exámenes corresponden a qué pasos. No se inventaron permisos ni se registró
historial completado por SQL. Avance, retroceso, finalización y órdenes de examen
**no fueron comprobados con persistencia real**. La FK de A se verificó por el
catálogo de PostgreSQL y su estado validado; no se insertaron órdenes de prueba.

## Diagnóstico histórico anterior a la autorización

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

## Preparación posterior, sin aplicación

Las operaciones A/B separadas, sus SQL exactos, UUID académicos reservados,
recorrido REST y recuperación están en
[la propuesta del15/11/2026](../db/proposals/gt02-hu34-20261115/README.md).
Se creó y verificó un respaldo privado de clinica, sin restaurarlo. No se aplicó
ningún cambio: A requiere autorización propia; B también requiere confirmar
la relación examen/paso y su alcance de negocio, además de autorización B.
