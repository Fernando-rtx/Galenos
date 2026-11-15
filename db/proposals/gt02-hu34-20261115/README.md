# Propuestas A y B — HU3/HU4, clinica

**Estado: PREPARADAS, NO APLICADAS.** Rama `feat/gt02-hu4-atencion`.
Se reutilizó [el diagnóstico existente](../../../docs/gt02-hu4-bloqueos.md).
No se crean bases, tablas, roles nuevos, procedimientos nuevos ni secuencias.
No cambia el código de aplicación ni se abren permisos REST administrativos.

Hay dos autorizaciones independientes. Los parámetros de confirmación de los
scripts evitan ejecuciones accidentales; **no sustituyen la autorización del usuario**.
Incluso un ensayo SQL con ROLLBACK necesita autorización y no se ejecutó aquí.

## Respaldo real, privado y verificable

Creado con pg_dump formato custom de la misma clinica:

```
.gt02/backups/clinica_pre_hu34_20261115T175438Z/clinica.dump
SHA-256: 68eb7a2333343846e185b099d2021db8c6e148729cb9147fc343388ade76b67b
Tamaño:37856bytes. Archivo:600; directorio privado:700.
```

Tiene20entradas TABLE y20TABLE DATA. pg_restore leyó todo el archivo hacia
/dev/null **sin conexión de destino**. No se restauró ni se creó otra base:
la restauración real no se declara ensayada. manifest.json, toc.txt y SHA256SUMS
están junto al archivo, fuera de Git. Se compararon contenido, columnas y
restricciones antes/después: sin cambios.

Verificación de solo lectura que debe repetirse inmediatamente antes de A:

```sh
python3 scripts/gt02_respaldo_clinica.py --verificar-manifest \
  .gt02/backups/clinica_pre_hu34_20261115T175438Z/manifest.json
```

Antes de B se usa el mismo comando con `--despues-a`: admite exclusivamente
la columna/FK de A y compara todos los datos anteriores. Si cambia cualquier
otra cosa, detenerse y renovar respaldo/revisión. Crear un respaldo nuevo también
es de solo lectura mediante `python3 scripts/gt02_respaldo_clinica.py`.

## Operación A — Compatibilidad de OrdenExamen

**SQL exacto:** [operacion-a.sql](operacion-a.sql).

- Tabla alterada: únicamente orden_examen. Añade id_examen UUID nullable y FK
  validada a examen.id_examen, ON UPDATE CASCADE/ON DELETE RESTRICT.
- Las20tablas permanecen. La orden previa conserva sus cuatro valores; la columna
  nueva queda NULL y su resultado se conserva. No se adivina qué examen era.
- Es necesaria para conservar la asociación individual Orden→Examen que ya usan
  JPA, Services y REST desde0230590; no se deriva una columna desde una entidad sin
  contrastarla con el SQL versionado y las relaciones reales.
- No se ejecuta la migración antigua completa: la FK de plantilla ya existe con
  otro nombre y no debe duplicarse.
- Precondiciones: base clinica,20tablas, columna todavía ausente, PK examen UUID,
  inventario actual1orden/1resultado y respaldo coincidente. Si la columna existe
  o cambian los datos, el script aborta para revisión.
- Riesgo: ALTER necesita bloqueo exclusivo breve. lock_timeout3s y
  statement_timeout30s; cualquier fallo revierte toda A.
- Recuperación: antes de COMMIT, ROLLBACK revierte el DDL. Después, mantener la
  columna compatible no pierde datos; retirarla requiere otra autorización y
  demostrar que nunca fue utilizada. El diagnóstico enlazado contiene ese borrador
  de reversión sin CASCADE. No restaurar un dump encima de trabajo posterior a ciegas.

Solo después de autorizar A se usarían estos comandos, desde la carpeta backend:

```sh
docker cp db/proposals/gt02-hu34-20261115 postgres_db:/tmp/gt02-hu34-20261115
docker exec postgres_db psql -X -U postgres -d clinica \
  -v autorizacion_a=AUTORIZAR_A_GT02_HU34_20261115 \
  -f /tmp/gt02-hu34-20261115/operacion-a.sql
```

No se ejecutaron estos comandos. El equipo usa psql del contenedor, por eso se
copiarían los archivos de propuesta allí y `\ir` resolvería el manifiesto local.
No hay un ejecutor que aplique A/B automáticamente.

## Operación B — Escenario y primera atención real

**SQL exacto:** [operacion-b.sql](operacion-b.sql).
**UUID reservados:** [escenario.psql](escenario.psql).
**Atención inicial por REST:** `scripts/gt02_preparar_historial_rest.py`.

Se propone Clínica Central existente `f7c682b3…`, procedimiento activo
«Evaluación de Salud» `0957a74a…` y su grafo persistido:

```
Registro de ingreso → Preparación del paciente → Diagnostico Final
Atención al publico → enfermera                → Doctor
```

La demostración propuesta realiza Recepción→Enfermería y retroceso al paso previo.
El último paso Doctor sigue existiendo en el procedimiento: no se modifica,
se omite ni se inventa una secuencia alternativa. No se propone un Doctor académico
porque el backend elige el menor UUID PersonaRol disponible: añadirlo para ganar
esa prioridad podría redirigir otras consultas. Finalización con Doctor requiere
revisar aparte la selección del responsable; no se declara cubierta por este plan.

### Decisión todavía pendiente: permiso de examen

No hay ningún examen habilitado en los procedimientos activos. **No se inventó
un permiso**. El usuario debe confirmar examen_id y paso_examen_id; para el alcance
propuesto el paso es Preparación del paciente `b2b4a3ba…`.
El catálogo incluye Hemograma completo `af0251cf…` y Radiografia de torah
`08a79fd8…`, pero no se seleccionó ninguno automáticamente.

Hasta esa confirmación, B no es aplicable: no hay valores por defecto en el
manifiesto y el SQL falla antes de escribir si faltan ambos parámetros. El cliente
REST también se detiene antes de cualquier petición mientras no estén definidos
en escenario.psql. Si se decide otro paso/alcance, revisar B antes de autorizarla.

### Registros propuestos

| Tabla | Nuevos/cambios |
|---|---|
| persona |3personas: Paciente Academico, Recepcion Academica, Enfermeria Academica; apellidos Galenos y nacimientos académicos válidos |
| documento |3Pasaportes sintéticos ACADGT02PAC001 / ACADGT02REC001 / ACADGT02ENF001, válidos para el patrón REST actual |
| persona_rol |3asignaciones propias en Clínica Central: Paciente, Atención al publico y enfermera |
| rol |Un cambio previo explícito: enfermera `1347c830…`, activo=false→true. No se cambia Doctor ni se otorgan privilegios administrativos |
| consulta |Una nueva `18d1c9f4-c2f5-452f-88d5-0c43d4110348`, fecha de preparación, referencia GT02-HU34-20261115 |
| consulta_procedimiento |Una nueva `126e4e9f…`, enlazada al procedimiento activo existente |
| consulta_procedimiento_paso |Una inicial `6c3e2823…`, PENDIENTE, sin valor ni fechaFin; responsable Recepción y plantilla real Registro de ingreso |
| procedimiento_paso_examen |Un permiso nuevo `04ff1984…`, SOLO para la relación que el usuario confirme |

Son13altas y1modificación de catálogo en8tablas. No se usan documentos personales
verdaderos ni se modifica la consulta antigua incompleta. No se insertan atenciones
completadas, resultados de laboratorio ni historiales ficticios.

Los UUID y documentos de escenario.psql son datos reservados de esta operación;
no son una lista de empleados para el login y no se importan en JavaScript/Java.
HU2 consultará esas relaciones mediante JPA como cualquier otro registro válido.

### Dependencias, riesgos y transacciones

1. A autorizada/aplicada y verificada; respaldo vigente; relación examen/paso
   confirmada. Las colisiones de UUID/documentos deben seguir siendo cero.
2. El rol Enfermería se habilita globalmente: se verificó que no tiene asignaciones
   actuales. Si aparecen otras antes de B, se aborta para revisar el impacto.
3. Las asignaciones son por clínica/rol, no exclusivas de esta consulta. Recepción
   y Enfermería podrían recibir nuevas consultas compatibles en esa clínica durante
   la demostración. Usar una ventana controlada y recuperar al terminar.
4. Un permiso ProcedimientoPasoExamen también tiene alcance global para ese paso.
   Su efecto clínico requiere la confirmación expresa solicitada.
5. Las13altas y la habilitación se preparan en una única transacción, con guardas,
   bloqueos breves y restricciones activas. Un error revierte todo ese SQL.
6. Las altas administrativas REST están cerradas por AccesoAcademicoFilter en modo
   académico. Por eso la preparación acotada usa SQL aprobado; no se habilita CRUD
   general ni se introduce BASIC/LTPA. Todas las atenciones/transiciones usan el EJB
   y REST normales, que vuelven a validar contexto y permisos.
7. SQL y varias llamadas REST no forman una transacción global: cada acción REST
   tiene su transacción REQUIRED. Un fallo intermedio se registra y requiere GET
   y revisión antes de reenviar; no hay reintento automático del POST.

### Fase REST después de la autorización B

El script de atención exige autorización B y el manifest del respaldo. Solo usa
el backend normal9080. Lee y valida el contexto recibido; no transmite UUID de
otro empleado ni guarda tokens en archivos. La secuencia exacta es:

1. POST `v2/identidad_academica/empleado`, tipo Pasaporte existente y
   valor ACADGT02REC001. Selección de su PersonaRol Recepción/Clínica Central.
2. GET `v2/atencion/consulta/18d1c9f4-c2f5-452f-88d5-0c43d4110348`, contexto propio;
   exige la ejecución inicial esperada, PENDIENTE y sin atención previa.
3. POST `v2/atencion/registrar`: idConsulta e idConsultaProcedimientoPaso recibidos,
   contexto propio, accion GUARDAR_PARCIAL, valor «Recepción académica: documento
   verificado y paciente recibido.», examenes [].
4. POST al mismo recurso con accion AVANZAR e idPasoSiguiente `b2b4a3ba…`, usando
   el enlace real. Java completa Recepción y crea Enfermería PENDIENTE con fecha,
   responsable y UUID nuevos. No se escribe historial desde SQL.
5. Identificar ACADGT02ENF001 y GET del detalle; comprobar responsable/fecha/valor
   de Recepción y examen autorizado. Revocar las sesiones de preparación.

Los cuerpos y cabeceras Authorization/X-Persona-Rol están en el script. El registro
privado `.gt02/preparacion-hu34/fase-rest.json` conserva solo acciones confirmadas
y UUID nuevos, sin tokens. Tras esta fase hay una consulta visible en HU3 de hoy,
con historial anterior real, lista para «Atender consulta» en HU4.

En la SPA normal3000: Enfermería guarda atención y selecciona el examen aprobado;
Java genera una OrdenExamen real. Después puede RETROCEDER a la ejecución inicial
completada; el backend conserva aquella atención y crea otra ejecución para el
responsable de Recepción. Se verifica el regreso al dashboard. HU1 utiliza
ACADGT02PAC001 para consultar la cita y PDF; un Paciente no entra como empleado.
No se avanzará al Doctor sin resolver su selección de responsable.

Después de completar la decisión de examen en escenario.psql y autorizar B,
se volvería a copiar la carpeta revisada al contenedor. La aplicación SQL usa
`psql -f /tmp/gt02-hu34-20261115/operacion-b.sql` con
`-v autorizacion_b=AUTORIZAR_B_GT02_HU34_20261115` sobre clinica.
La fase REST se invocaría desde el backend con:

```sh
python3 scripts/gt02_preparar_historial_rest.py \
  --autorizacion AUTORIZAR_B_GT02_HU34_20261115 \
  --respaldo-manifest .gt02/backups/clinica_pre_hu34_20261115T175438Z/manifest.json
```

No se ejecutó esta invocación. Si B falla entre llamadas, consultar el log y el
estado GET; no repetir la preparación completa ni sobrescribir el paso atendido.

### Recuperación exclusiva de B

Antes de COMMIT del seed basta ROLLBACK. Tras SQL/REST, cerrar sesiones y usar,
solo con autorización de reversión, [recuperacion-b.sql](recuperacion-b.sql):

- Identifica exclusivamente la consulta/CP y los UUID reservados; elimina sus
  resultados, órdenes y ejecuciones dependientes en orden de FK, luego las altas.
- No usa CASCADE, TRUNCATE ni eliminación por fecha o por todas las filas.
- Aborta si personas/asignaciones se usaron fuera del caso, aparecieron nuevas
  asignaciones Enfermería, hay pasos fuera del recorrido o se utilizó el permiso
  en otra consulta. No borra actividad de terceros.
- Retira únicamente el permiso creado y devuelve enfermera.activo a su valor
  previo false solo bajo esas condiciones. Toda la recuperación es transaccional.
- A permanece aplicada; no se borra su columna para recuperar los datos de B.

La recuperación exigiría `autorizacion_recuperacion_b=REVERTIR_B_GT02_HU34_20261115`.
También exige el mismo examen_id confirmado al preparar B.
Después se compararía de nuevo el respaldo con `--verificar-manifest ... --despues-a`:
deberían coincidir todos los valores y objetos previos, salvo la compatibilidad A.

## Qué se verificó en esta ejecución

Solo SELECT, lectura de código/plan, creación y verificación del respaldo y
preparación de archivos. Se comprobaron catálogo, grafo, formatos y colisiones.
Las cuatro historias con el caso nuevo **todavía no se han ejecutado**.
No se enviaron POST de atención ni se aplicó ninguna operación SQL propuesta.
No se restauró el respaldo. No hay cambios de código de producción ni nuevas
dependencias frontend. La sintaxis Python se comprobó sin ejecutar el cliente REST.

Para autorizar: A puede revisarse por separado. B requiere primero resolver la
relación examen/paso y confirmar el alcance Recepción↔Enfermería. Después se
solicita autorización B completa, incluida la habilitación del rol y el permiso.
