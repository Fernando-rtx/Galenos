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

El flujo de consulta quedó cerrado con el modelo de datos:
`consulta_procedimiento_paso` enlaza cada paso registrado con su plantilla de
procedimiento mediante `id_procedimiento_paso` (FK a
`procedimiento_paso`). Los selectores de procedimiento, paso y rol usan los
converters CDI `procedimientoConverter`, `procedimientoPasoConverter` y
`rolConverter`, y desde la consulta se navega a las órdenes generadas
(`OrdenExamen`).

Las pantallas huérfanas `ConsultaProcedimiento` y `ConsultaProcedimientoPaso` se
eliminaron: el flujo anidado de pestañas dentro de `Consulta` es ahora el único
camino para registrar procedimientos, pasos y sus órdenes. Al llegar por
deep-link, `nuevo()` preselecciona el paso o la orden recibidos; en particular
`OrdenExamen` enlaza con sus resultados mediante `ExamenResultado?idOrden=`, lo
que cierra la cadena orden→resultado. Las pantallas de plantillas de
procedimiento (`ProcedimientoPaso`, `ProcedimientoPasoExamen` y
`ProcedimientoPasoSecuencia`) ya forman parte del menú principal. Estos cambios
son solo de navegación y no modifican la base de datos.

El enlace paso→plantilla se aplica con el script `clinica_migracion_2026_09_27.sql`
(raíz del repositorio):

```bash
cat clinica_migracion_2026_09_27.sql | docker exec -i postgres-local psql -U fernando -d clinica
```

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

En el flujo de consulta, los `p:selectOneMenu` de procedimiento, paso y rol
convierten entidades con `procedimientoConverter`, `procedimientoPasoConverter`
y `rolConverter`, y la vista enlaza hacia `OrdenExamen` para consultar las
órdenes generadas. Desde cada orden se navega a sus resultados con el deep-link
`ExamenResultado?idOrden=`, que preselecciona la orden en `nuevo()`; los
resultados se registran en `ExamenResultado`.

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
