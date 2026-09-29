# Galenos

Repositorio del proyecto Galenos - UES

## Estado actual

GalenosSV es una aplicación web académica Java 21/Jakarta EE 11 desplegable
como WAR en Open Liberty. Usa Jakarta Faces y PrimeFaces para las vistas, EJB
para los DAO, JPA para persistencia y PostgreSQL como base de datos.

La interfaz implementada actualmente comprende los catálogos de tipos de
examen, tipos de documento y tipos de medio de contacto. El repositorio también
contiene el modelo JPA y DAO de otras áreas clínicas, pero eso no significa que
existan todavía pantallas o flujos completos para pacientes, citas, expedientes,
pagos o inventario.

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

## Comandos

- `mvn test`: compila y ejecuta las pruebas unitarias.
- `mvn clean package`: limpia, prueba y genera `target/GalenosSV.war`.
- `mvn liberty:run`: prepara Liberty y despliega la aplicación.

La configuración local de credenciales no debe subirse al repositorio. Se usa
`server.env.example` como referencia sin valores secretos.

Equipo:
- Guevara
- Cortez
- Itzep
