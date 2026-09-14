# Guia de Trabajo en Equipo - GalenosSV

Guia completa de distribucion de trabajo equitativo entre Alexander (Itzep), Rodrigo Cortez y Fernando Guevara.

---

## 1. Equipo y responsables

| Integrante | Username GitHub | Rol principal |
|------------|-----------------|---------------|
| Alexander Itzep | itzep-stack | Arquitecto backend: Maven, Git, Liberty, PrimeFaces, AbstractModel, DAO generico, Examen |
| Rodrigo Cortez | (pendiente) | Backend: PostgreSQL, JPA, entidades, Persona, Consulta, Procedimiento |
| Fernando Guevara | Fernando-rtx | Frontend: Jakarta Faces, PrimeFaces, template, composites, i18n, clinica, catalogos |

---

## 2. Versiones congeladas

| Herramienta | Version |
|-------------|---------|
| JDK | 21 |
| NetBeans | 22 |
| Maven | 3.9.16 |
| Jakarta EE | 11 |
| Open Liberty | 26.0.0.8 |
| PostgreSQL | 18.6 |
| PostgreSQL JDBC | 42.7.4 |
| PrimeFaces | 17.x con classifier jakarta (scope provided) |

Crear `docs/ENTORNO.md` y registrar la version exacta de PrimeFaces. Los tres deben usar la misma.

---

## 3. Base de datos

### 3.1. Crear el proyecto en NetBeans

**Responsable:** Alexander

1. Abrir NetBeans 22
2. File -> New Project -> Maven -> Web Application
3. Nombre: `GalenosSV`, Group ID: `sv.edu.ues.occingenieriappi115_2026.salud`
4. Java 21, Jakarta EE 11, Servidor: Open Liberty
5. Verificar que `mvn clean test` pase

### 3.2. Script SQL

**Responsable:** Rodrigo

Ejecutar `clinica_ppi115_2026_08_20.sql` en PostgreSQL local. Cada integrante tiene su propio PostgreSQL con usuario y contraseña diferentes.

### 3.3. Tablas del SQL (20 tablas, todas con PK uuid)

| Tabla | FK principales |
|-------|----------------|
| `tipo_medio_contacto` | (ninguna) |
| `tipo_documento` | (ninguna) |
| `persona` | (ninguna) |
| `medio_contacto` | persona, tipo_medio_contacto |
| `documento` | persona, tipo_documento |
| `rol` | (ninguna) |
| `clinica` | (ninguna) |
| `persona_rol` | persona, rol, clinica |
| `consulta` | persona_rol |
| `procedimiento` | (ninguna) |
| `procedimiento_paso` | procedimiento, rol |
| `procedimiento_paso_secuencia` | procedimiento_paso |
| `consulta_procedimiento` | consulta, procedimiento |
| `consulta_procedimiento_paso` | consulta_procedimiento, persona_rol |
| `tipo_examen` | (ninguna) |
| `examen` | (ninguna) |
| `examen_tipo_examen` | examen, tipo_examen |
| `procedimiento_paso_examen` | procedimiento_paso, examen |
| `orden_examen` | consulta_procedimiento_paso |
| `examen_resultado` | orden_examen |

### 3.4. Datasource Open Liberty

**Responsable:** Alexander

Cada integrante ajusta `openliberty.xml` con sus credenciales locales de PostgreSQL.

---

## 4. Plan de trabajo por fases

### FASE 0 - Proyecto Maven base

**Responsable:** Alexander

Crear proyecto Maven WAR en NetBeans, configurar Java 21 y Jakarta EE 11, agregar README.md, .gitignore, docs/ENTORNO.md.

---

### FASE 1 - CDI y Jakarta Faces

**Responsable:** Fernando

Crear web.xml (FacesServlet `/faces/*`), faces-config.xml (locales + resource-bundle), beans.xml (CDI), index.html.

---

### FASE 2 - Datasource Open Liberty

**Responsable:** Alexander

Configurar JDBC en Liberty con configDropins, datasource JNDI `jdbc/pgdb`.

---

### FASE 3 - JPA y entidades

**Responsable:** Rodrigo

Crear las 20 entidades JPA. PKs como UUID. Relaciones @ManyToOne y @OneToMany segun FKs del SQL. NO usar @Lob en UUIDs.

---

### FASE 4 - DAO generico

**Responsable:** Alexander

Crear DAOInterface.java (6 operaciones) y DefaultDAO.java (Criteria API).

---

### FASE 5 - DAO concretos

Cada uno corrige los DAOs auto-generados por NetBeans (quitar @Lob de UUIDs).

---

### FASE 6 - Tests genericos DAO

**Responsable:** Rodrigo

Crear DefaultDAOTest.java con Mockito.

---

### FASE 7 - PrimeFaces provided

**Responsable:** Alexander

PrimeFaces fuera del WAR, scope provided en pom.xml.

---

### FASE 8 - AbstractModel + LazyDataModel

**Responsable:** Alexander

Crear ESTADO_CRUD.java y AbstractModel.java con LazyDataModel.

---

### FASE 9 - Composite Components

**Responsable:** Fernando

Crear botonesTop.xhtml y botonesDetalle.xhtml.

---

### FASE 10 - Internacionalizacion

**Responsable:** Fernando

Crear 5 archivos de mensajes (es, en, de, zh_CN).

---

### FASE 11 - Template general

**Responsable:** Fernando

Crear general.xhtml con grid responsive.

---

### FASE 12 - Models concretos

Cada uno crea sus models heredando AbstractModel.

---

### FASE 13 - Pantallas CRUD

Cada uno crea sus pantallas usando template y composites.

---

### FASE 14 - Repository especifico

**Responsable:** Alexander

Crear ExamenTipoExamenDAOInterface y ExamenTipoExamenDAO con metodos custom.

---

### FASE 15 - Tests repository

**Responsable:** Fernando

Crear ExamenTipoExamenDAOTest.java.

---

## 5. Archivos por persona (21 archivos cada uno)

### Alexander (Itzep) - 21 archivos

| # | Archivo | Que hace |
|---|---------|----------|
| 1 | `pom.xml` | Configuracion Maven con Jakarta EE 11, PrimeFaces scope provided |
| 2 | `docs/ENTORNO.md` | Documentar versiones exactas del entorno |
| 3 | `control/DAOInterface.java` | Interfaz generica con crear, modificar, eliminar, findById, findRange, contar |
| 4 | `control/DefaultDAO.java` | Implementacion con Criteria API. Class<T> en constructor, getEntityManager() abstracto |
| 5 | `boundary/ESTADO_CRUD.java` | Enum: NINGUNO, CREAR, MODIFICAR |
| 6 | `boundary/AbstractModel.java` | Modelo base con LazyDataModel<T>, nuevo/seleccionar/guardar/cancelar/contar/getRowKey/getRowData |
| 7 | `control/ExamenDAO.java` | DAO concreto, hereda DefaultDAO, quita @Lob de UUID |
| 8 | `control/TipoExamenDAO.java` | DAO concreto, hereda DefaultDAO, quita @Lob de UUID |
| 9 | `control/ExamenTipoExamenDAOInterface.java` | Interfaz con findByIdExamen, countByIdExamen, countByIdExamenAndIdTipoExamen |
| 10 | `control/ExamenTipoExamenDAO.java` | Implementacion con JPQL y parametros nombrados |
| 11 | `boundary/TipoExamenModel.java` | Model hereda AbstractModel, inyecta TipoExamenDAO, genera UUID |
| 12 | `boundary/ExamenModel.java` | Model con maestro-detalle, maneja ExamenTipoExamen como detalle lazy |
| 13 | `paginas/TipoExamen.xhtml` | Vista CRUD: dataTable + dialog + botones composites |
| 14 | `paginas/Examen.xhtml` | Vista maestro-detalle: tabla examenes + detalle tipos asociados |
| 15 | `control/ExamenDAOTest.java` | Tests del DAO |
| 16 | `control/TipoExamenDAOTest.java` | Tests del DAO |
| 17 | `control/ExamenTipoExamenDAOTest.java` | Tests de metodos especificos |
| 18 | `boundary/TipoExamenModelTest.java` | Tests del model |
| 19 | `boundary/ExamenModelTest.java` | Tests del model |
| 20 | `webapp/WEB-INF/plantillas/defecto.xhtml` | Plantilla alternativa para examenes |
| 21 | `webapp/resources/estilos/examen.css` | Estilos especificos del modulo examen |

---

### Rodrigo - 21 archivos

| # | Archivo | Que hace |
|---|---------|----------|
| 1 | `clinica_ppi115_2026_08_20.sql` | Script SQL con 20 tablas, ejecutar en PostgreSQL local |
| 2 | `META-INF/persistence.xml` | JTA con GalenoPU, datasource jdbc/pgdb, 20 clases entity |
| 3 | `entity/Persona.java` | idPersona (UUID), nombres, apellidos, fechaNacimiento, fechaCreacion |
| 4 | `entity/Documento.java` | idDocumento (UUID), valor, rutaFisica. FK a Persona y TipoDocumento |
| 5 | `entity/MedioContacto.java` | idMedioContacto (UUID), valor, fechaCreacion. FK a Persona y TipoMedioContacto |
| 6 | `entity/Consulta.java` | idConsulta (UUID), fechaInicio, fechaFin, referenciaExterna, observaciones. FK a PersonaRol |
| 7 | `entity/ConsultaProcedimiento.java` | FK a Consulta y Procedimiento |
| 8 | `entity/ConsultaProcedimientoPaso.java` | FK a ConsultaProcedimiento y PersonaRol |
| 9 | `entity/OrdenExamen.java` | FK a ConsultaProcedimientoPaso |
| 10 | `entity/ExamenResultado.java` | FK a OrdenExamen |
| 11 | `control/PersonaDAO.java` | DAO concreto, quita @Lob |
| 12 | `control/DocumentoDAO.java` | DAO concreto, quita @Lob |
| 13 | `control/MedioContactoDAO.java` | DAO concreto, quita @Lob |
| 14 | `control/ConsultaDAO.java` | DAO concreto, quita @Lob |
| 15 | `control/ConsultaProcedimientoDAO.java` | DAO concreto, quita @Lob |
| 16 | `control/ConsultaProcedimientoPasoDAO.java` | DAO concreto, quita @Lob |
| 17 | `control/OrdenExamenDAO.java` | DAO concreto, quita @Lob |
| 18 | `control/ExamenResultadoDAO.java` | DAO concreto, quita @Lob |
| 19 | `control/DefaultDAOTest.java` | Tests genericos con Mockito |
| 20 | `control/ConcreteDAOTest.java` | Tests de herencia de DAO concreto |
| 21 | `boundary/ConsultaModelTest.java` | Tests del model de Consulta |

---

### Fernando - 21 archivos

| # | Archivo | Que hace |
|---|---------|----------|
| 1 | `webapp/WEB-INF/web.xml` | FacesServlet `/faces/*`, welcome-file, session-timeout 30 min |
| 2 | `webapp/WEB-INF/faces-config.xml` | locales (es default), resource-bundle i18n.mensajes con var msg |
| 3 | `webapp/WEB-INF/beans.xml` | CDI habilitado para @Inject |
| 4 | `webapp/index.html` | Pagina de inicio basica |
| 5 | `webapp/resources/crud/botonesTop.xhtml` | Composite: Nuevo + Actualizar. cc.attrs.model, cc.attrs.actualizar |
| 6 | `webapp/resources/crud/botonesDetalle.xhtml` | Composite: Guardar + Cancelar. cc.attrs.model, cc.attrs.procesar |
| 7 | `src/main/resources/i18n/mensajes.properties` | Textos español (default) |
| 8 | `src/main/resources/i18n/mensajes_es.properties` | Textos español |
| 9 | `src/main/resources/i18n/mensajes_en.properties` | Textos ingles |
| 10 | `src/main/resources/i18n/mensajes_de.properties` | Textos aleman |
| 11 | `src/main/resources/i18n/mensajes_zh.properties` | Textos chino |
| 12 | `entity/Clinica.java` | idClinica (UUID), nombre, activo, tipo, comentarios |
| 13 | `entity/Rol.java` | idRol (UUID), nombre, activo, observaciones |
| 14 | `entity/PersonaRol.java` | idPersonaRol (UUID), fechaCreacion. FK a Persona, Rol, Clinica |
| 15 | `control/ClinicaDAO.java` | DAO concreto, quita @Lob |
| 16 | `control/RolDAO.java` | DAO concreto, quita @Lob |
| 17 | `control/PersonaRolDAO.java` | DAO concreto, quita @Lob |
| 18 | `boundary/ClinicaModel.java` | Model hereda AbstractModel, inyecta ClinicaDAO |
| 19 | `boundary/RolModel.java` | Model hereda AbstractModel, inyecta RolDAO |
| 20 | `boundary/PersonaRolModel.java` | Model hereda AbstractModel, inyecta PersonaRolDAO |
| 21 | `paginas/Clinica.xhtml` | Vista CRUD: dataTable + dialog + botones composites |

---

## 6. Pendientes despues de las 21 tareas

Estos archivos se crean en las fases finales cuando ya este todo lo base:

| Integrante | Archivos pendientes |
|------------|---------------------|
| **Alexander** | *(ya cumplio sus 21)* |
| **Rodrigo** | PersonaModel.java, ConsultaModel.java, ConsultaProcedimientoModel.java, ConsultaProcedimientoPasoModel.java, OrdenExamenModel.java + 5 vistas XHTML |
| **Fernando** | MedioContactoModel, TipoMedioContactoModel, DocumentoModel, TipoDocumentoModel + 4 vistas XHTML |

**Nota:** Cuando se llegue a las fases 12-13, cada uno completa sus models y vistas restantes. El objetivo es que nadie tenga mas de ~25 archivos en total al finalizar el proyecto.

---

## 7. Archivos de alto conflicto

No modificar simultaneamente sin coordinacion:

- `pom.xml`
- `DefaultDAO.java`
- `AbstractModel.java`
- `faces-config.xml`
- `general.xhtml`
- `mensajes.properties`

**Regla:** Avisar quien es el "dueño temporal" antes de modificar.

---

## 8. Checklist de Pull Request

- [ ] Respeta la arquitectura del ingeniero
- [ ] No duplica logica de DefaultDAO o AbstractModel
- [ ] No puso JPQL/SQL dentro del Model o XHTML
- [ ] No dejo textos hardcodeados
- [ ] PrimeFaces no quedo duplicado en el WAR
- [ ] Uso parametros nombrados en JPQL
- [ ] LazyDataModel no busca en lista vieja
- [ ] getRowKey/getRowData convierten bien el tipo de PK
- [ ] `mvn clean test` pasa

---

## 9. Definition of Done

| Criterio | Debe cumplirse |
|----------|----------------|
| Compilacion | `mvn clean test` -> BUILD SUCCESS |
| Servidor | Probado en Open Liberty |
| Base | Probado contra PostgreSQL local |
| Dependencias | Sin JAR duplicado |
| Git | `git diff` revisado |
| Revision | Otro companero aprueba el PR |
| Integracion | Merge a main despues de revision |

---

## 10. Comandos

```bash
cd ~/Desktop/GalenosSV
mvn clean test
mvn liberty:dev
# http://localhost:9080/<contexto>/faces/...
```
