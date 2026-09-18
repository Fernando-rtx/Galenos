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
