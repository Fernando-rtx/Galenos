# Galenos

Repositorio del proyecto Galenos - UES

## Estado actual

GalenosSV es una aplicación web académica Java 21/Jakarta EE 11 desplegable
como WAR en Open Liberty. Usa Jakarta Faces y PrimeFaces para las vistas, EJB
para los DAO, JPA para persistencia y PostgreSQL como base de datos.

El frontend principal es la SPA nativa del repositorio GalenoSVFE, con HU1–HU4,
identificación académica por documento, sesiones efímeras, bandeja por responsable
y atención REST. Faces/PrimeFaces corresponde a la interfaz heredada del backend.
La auditoría vigente está en [auditoría GT02](docs/auditoria-final-gt02.md).

El flujo de consulta sigue el esquema real: `consulta_procedimiento_paso`
relaciona la ejecución de un paso con `consulta_procedimiento`, el `PersonaRol`
responsable, sus fechas y su estado. No existe una FK hacia la plantilla
`procedimiento_paso`: la plantilla vive en las pantallas de procedimientos
(`ProcedimientoPaso`, `ProcedimientoPasoExamen` y `ProcedimientoPasoSecuencia`).
El modelo JPA se limita a las columnas que la base declara y `SchemaMappingTest`
verifica que cada `@Table` y cada columna mapeada existan en el SQL versionado.

Las pantallas huérfanas `ConsultaProcedimiento` y `ConsultaProcedimientoPaso` se
eliminaron: el flujo anidado de pestañas dentro de `Consulta` es el único camino
para registrar procedimientos, pasos y sus órdenes. Los selectores usan los
converters CDI `consultaProcedimientoConverter`, `personaRolConverter`,
`procedimientoConverter` y `rolConverter`. Al llegar por deep-link, `nuevo()`
preselecciona la orden recibida; `OrdenExamen` enlaza con sus resultados
mediante `ExamenResultado?idOrden=`, lo que cierra la cadena orden→resultado.
Las pantallas de plantillas de procedimiento ya forman parte del menú principal.

## Recorrido principal

```text
XHTML/PrimeFaces
  → backing bean @Named y @ViewScoped
  → DAO @Stateless y @LocalBean
  → DefaultDAO y EntityManager
  → GalenoPU
  → DataSource JNDI jdbc/pgdb
  → PostgreSQL
```

`DefaultDAO` centraliza el EntityManager y las operaciones CRUD. Los DAO
concretos identifican su entidad; los especializados añaden únicamente sus
consultas propias.

En el flujo de consulta, los `p:selectOneMenu` de procedimiento, responsable y
rol convierten entidades con `consultaProcedimientoConverter`,
`personaRolConverter` y `rolConverter`, y la vista enlaza hacia `OrdenExamen`
para consultar las órdenes generadas. Desde cada orden se navega a sus
resultados con el deep-link `ExamenResultado?idOrden=`, que preselecciona la
orden en `nuevo()`; los resultados se registran en `ExamenResultado`.

## Despliegue académico local

Este proyecto usa datos ficticios. El frontend y REST v2 requieren identificación
académica de paciente o empleado según la operación.
Para ejecutar el backend y conectar el frontend, sigue la
[guía de desarrollo local](docs/desarrollo-local.md).

## Comandos

Para la revisión GT02 sobre `clinica`, seguir [preparación de entrega](docs/preparacion-entrega.md).
Los comandos de demo que siguen crean un entorno distinto y no forman parte de esa revisión.

Para la integración del nuevo frontend con **datos ficticios**:

```sh
./demo start
```

Requiere JDK 21, Maven, Python 3.11+ y Docker. Prepara su propio PostgreSQL y
Liberty y muestra la URL de conexión. `./demo verify` ejecuta las pruebas;
`./demo stop` detiene ese entorno. Instrucciones y configuración en
[`docs/demo-setup.md`](docs/demo-setup.md), contrato en
[`docs/api-contract.md`](docs/api-contract.md).

- `mvn test`: compila y ejecuta las pruebas unitarias.
- `mvn clean package`: limpia, prueba y genera `target/GalenosSV.war`.
- `mvn liberty:run`: prepara Liberty y despliega la aplicación.

La configuración local de credenciales no debe subirse al repositorio. Se usa
`server.env.example` como referencia sin valores secretos.

Equipo:
- Guevara
- Cortez
- Itzep
