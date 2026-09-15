# Entorno de desarrollo - GalenosSV

| Herramienta | Versión |
|---|---|
| JDK | 21 |
| NetBeans | 22 |
| Maven | 3.9.16 |
| Jakarta EE | 11 |
| Open Liberty | 26.0.0.8 |
| PostgreSQL | 18.6 |
| PostgreSQL JDBC | 42.7.4 |
| PrimeFaces | Pendiente de acordar versión exacta |

## Proyecto

- Nombre: `GalenosSV`
- Group ID: `sv.edu.ues.occingenieriappi115_2026.salud`
- Artifact ID: `GalenosSV`
- Version: `1.0-SNAPSHOT`
- Packaging: `war`
- Java: `21`
- Jakarta EE: `11`

## Rama de trabajo

- `feature/itzep`

## FASE 2 - Open Liberty y PostgreSQL

- Configuracion Liberty versionada en `src/main/liberty/config/server.xml`.
- Servidor Maven Liberty: `GalenosSV`. Se usa un nombre especifico del proyecto porque el plugin crea el servidor dentro de `target/liberty/wlp/usr/servers/GalenosSV`, sin tocar los servidores locales existentes en `/opt/openliberty/usr/servers`.
- Feature principal: `jakartaee-11.0`.
- Datasource JNDI: `jdbc/pgdb`.
- Base por defecto: `universidad` en `localhost:5432`.
- Credenciales: crear `src/main/liberty/config/server.env` desde `server.env.example`. Ese archivo esta ignorado por Git.
- Driver JDBC: Maven lo copia como libreria de Liberty a `${server.config.dir}/jdbc` mediante `liberty-maven-plugin`; no debe empaquetarse en `WEB-INF/lib`.

Comando de arranque:

```bash
mvn liberty:run
```

URL esperada:

```text
http://localhost:9080/GalenosSV/
```
