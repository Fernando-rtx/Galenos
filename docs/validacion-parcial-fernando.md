# Entrega y validación del parcial — Fernando

Implementación del 1–2 de octubre de 2026, zona America/El_Salvador.
Rama propia: `feature/fernando-parcial`, base remota `3004b20`.

## Resultado

Consulta requiere una clínica de trabajo, conserva ese contexto al navegar y
busca solo pacientes activos de esa clínica. Al agregar un procedimiento activo
crea automáticamente su primer paso con una persona del rol exacto requerido,
en una única transacción. Paciente, procedimiento, asociación del paso y
responsable se conservan al editar. El listado admite Desde/Hasta y calcula
días completos de El Salvador.

Se mantuvieron XHTML → Model → EJB/DAO → JPA → PostgreSQL, `getDao()`, los ámbitos
de vista y las entidades existentes. Los métodos compartidos agregados están
descritos en [contratos-parcial-fernando.md](contratos-parcial-fernando.md).

## Base original y bibliotecas del servidor

No se modificaron el SQL del proyecto, las entidades ni la estructura de la BD
original `clinica`. La comprobación del SQL suministrado en
`/home/fernando/Escritorio/clinica_ppi115_2026_08_20.sql` encontró exactamente el
mismo DDL ejecutable que en el repositorio, después de excluir comentarios y
normalizar espacios. SHA256 de ese DDL normalizado:
`4ae39bca3e436a428ca7874bed9ea630332a70d62c151a635aa5f6b8ac4fd1d7`.

La BD aislada `clinica_fernando_parcial` se creó con ese esquema, inicialmente
sin datos. Los registros de ensayo se guardaron desde las pantallas. No se
agregaron columnas ni claves foráneas para resolver la raíz o el responsable.
La plantilla no tiene una columna `inicial`: se deriva su única raíz acíclica.
El paso ejecutado conserva las columnas del esquema original, sin inventar una
FK a la plantilla.

PrimeFaces 15.0.17 Jakarta conserva `scope=provided`. Open Liberty lo suministra
mediante `PrimeFacesLib` y
`usr/shared/resources/primefaces/primefaces-15.0.17-jakarta.jar`. El WAR
`target/GalenosSV.war` fue inspeccionado como ZIP: cero JARs en `WEB-INF/lib`,
ningún PrimeFaces embebido. POM y configuración versionada de Liberty se
conservaron; JDBC también procede del servidor.

## Pruebas automatizadas

La suite completa terminó con **567 pruebas, 0 fallos y 0 errores**.
Se ejecutó con el agente Mockito explícito porque el sandbox impide el attach
dinámico de Byte Buddy:

```sh
mvn -o -DargLine=-javaagent:/home/fernando/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar test
```

Log local: `/tmp/galenos-parcial-tests-finales.log`. También se ejecutó
`clean test` durante el desarrollo y `mvn -o -DskipTests package` para generar el
WAR; el package no reemplaza la ejecución de test. No se afirma cobertura
JaCoCo, ya que el agente explícito de Mockito sustituye el argLine de esa
ejecución.

Las pruebas nuevas cubren sesiones y recuperación del catálogo de clínicas,
contextos obsoletos incluso A → B → A, raíces/ciclos/referencias inválidas,
roles y clínicas, procedimientos inactivos o nulos, relaciones inmutables,
propagación de errores, filtros compartidos entre carga/conteo y límites de día.
Las pruebas existentes de catálogos y CRUD siguen pasando.

## Ensayo con Open Liberty y PostgreSQL reales

Servidor de ensayo: `GalenosSVParcial`, HTTP 9081 y HTTPS 9444, instalación
`/home/fernando/openliberty/wlp`. Se desplegó el **WAR empaquetado** usando
`looseApplication=false`, con datasource a `clinica_fernando_parcial` y
PrimeFaces externo. La configuración de ensayo en `/tmp/galenos-parcial-runtime`
es local; no se publican credenciales. El servidor habitual `GalenosSV` conserva
su datasource a `clinica` y puerto 9080.

| Recorrido desde UI | Evidencia observada |
| --- | --- |
| TipoExamen | Dos activos y uno inactivo; modificar observaciones conserva inactivo después de recargar |
| TipoMedioContacto, revisión cruzada | Teléfono y correo activos con regex persistida, otro teléfono inactivo; editar indicaciones conserva estado tras recargar |
| Clínica de trabajo | Sin selección no hay alta de Consulta; clínica inactiva fuera del selector; selección persiste al navegar |
| Contexto entre pestañas/sesiones | A → B → A desde otra pestaña invalida el formulario pendiente; queda abierto con error y sin nueva consulta; dos sesiones conservan clínicas independientes |
| Búsqueda contextual | `Sinrol Ensayo` no se ofrece; `Pacienteotra Ensayo` solo se ofrece en `Ensayo Otra`; `Manuel Ensayo` se ofrece en `Ensayo Centro` con fecha de nacimiento |
| Consulta y recepción | Consulta `E2E-1`; plantilla Recepción → Signos → Evaluación; un paso PENDIENTE asignado a `Sofia Ensayo`, rol Atención al público, clínica Centro |
| Enfermería | Consulta `E2E-2`; plantilla con raíz Enfermera; un paso asignado a `Milena Ensayo`, Enfermera de Centro |
| Edición | Paciente/procedimiento/parentesco del paso deshabilitados y responsable solo lectura; editar asociación conserva un único paso |
| Falta de responsable | Plantilla activa cuyo rol no tiene personal; error visible dentro del diálogo, formulario conservado y ninguna nueva asociación |
| Fechas | Día 1 incluye E2E-1 y excluye E2E-2; día 2 incluye E2E-2 a las 23:59; rango invertido conserva resultados previos y muestra error; Limpiar devuelve ambos |
| Zona horaria | Consulta, procedimientos, pasos y sus calendarios usan America/El_Salvador; 08:00 se muestra como 08:00 en las tres vistas |
| Cambio de clínica | Centro muestra sus consultas; Otra no muestra E2E-1/E2E-2 y ofrece su propio paciente |
| Escritorio y móvil | Capturas de 1440×1000 y 390×844; selector y filtros permanecen utilizables |

### Rollback real

En la BD **aislada** se instaló temporalmente `ensayo_fallo_paso`, un disparador
que provoca error antes de insertar en `consulta_procedimiento_paso`. Se intentó
agregar Triage desde la UI de E2E-1. El formulario permaneció en creación y
mostró: «No se guardaron los cambios. Revisa los datos y vuelve a intentarlo.»

Antes y después había exactamente **2 asociaciones y 2 pasos**: el fallo no
dejó la tercera asociación ni un paso parcial. Se retiraron el disparador y su
función al terminar, incluso en la repetición del ensayo. No se aplicó este
disparador a la BD original ni se agregó al esquema del proyecto.

### Cómo revisar la entrega

1. Abrir `http://localhost:9081/GalenosSV/paginas/Consulta.xhtml` y seleccionar
   `Ensayo Centro`.
2. Abrir E2E-1 con doble clic y revisar Procedimientos/Pasos: recepción debe ser
   responsable; en E2E-2 debe aparecer enfermería.
3. Aplicar Desde/Hasta del 1 y del 2 de octubre de 2026; cambiar a Ensayo Otra y
   comprobar el aislamiento del listado y del buscador de pacientes.
4. Revisar catálogos TipoExamen y TipoMedioContacto y sus estados persistidos.

Capturas: [escritorio](evidencias/consulta-desktop.png),
[móvil](evidencias/consulta-mobile.png) y
[error dentro del diálogo](evidencias/consulta-error.png).

## Commits de implementación

| Commit | Bloque |
| --- | --- |
| `293bfdc` | Contexto de clínica actual |
| `2576052` | Consulta por clínica, contratos y primer paso automático |
| `e52f3cd` | Rango de fechas |
| `340262c` | Error recuperable al cargar clínicas |
| `186ee8b` | Diálogo conservado al fallar, errores dentro del diálogo y hora local coherente |

## Integración pendiente del equipo

Los contratos están implementados y documentados para revisar con Itzep y
Rodrigo. No se enviaron mensajes ni se asumió su aprobación. TipoExamen no
necesitó cambios de código; el rechazo del tipo inactivo desde Examen sigue
siendo parte de la asociación de Itzep. El simulacro de observables 1 → 10
requiere el trabajo completo de los tres integrantes.

Integrar las ramas revisadas en `feature/parcial-integracion`, repetir pruebas
y simulacro y después realizar el merge acordado a main. Esta entrega no
incluye login, dashboards, nuevas FK ni ejecución/avance real de pasos.
