# Plan: Limpieza y cierre de la familia Consultas (sin cambios de BD)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Quitar lo que sobra en la familia (pantallas huérfanas, viewParam muerto, código huérfano) y cerrar lo que falta (deep-link orden→resultados, plantillas accesibles desde el menú, plantilla visible en pasos, preselección de contexto), sin modificar el esquema de la BD.

**Architecture:** Eliminación quirúrgica de las dos pantallas duplicadas y poda de los miembros del modelo que quedan sin uso; patrón `idPasoContexto` (ya existente y probado) replicado para `idOrdenContexto`; tres enlaces `h:link` en el menú; columna adicional en la tabla anidada de pasos.

**Tech Stack:** Jakarta EE 11 / Faces 4.1, PrimeFaces 15.0.17, PostgreSQL (sin cambios), JUnit 6 + Mockito, Open Liberty.

## Global Constraints

- **Prohibido modificar la estructura de la BD** (sin ALTERs nuevos, sin migraciones).
- Commits en español con prefijo convencional, sobre la rama actual (`feature/fernando`).
- TDD para todo cambio Java: test que falla → implementación → verde.
- Claves i18n nuevas en los 4 bundles locales **y en el base** `messages.properties` (regla de casa).
- No tocar el comportamiento de las pestañas anidadas de `Consulta.xhtml` (solo se añade una columna).
- Verificación: `mvn test` completo y smoke test con Liberty + Playwright al final.

---

### Task 1: Eliminar pantalla huérfana ConsultaProcedimiento

**Files:**
- Delete: `src/main/webapp/paginas/ConsultaProcedimiento.xhtml`
- Delete: `src/main/webapp/resources/estilos/consulta-procedimiento.css` (verificar existencia con `ls`; solo lo usa esa pantalla — única referencia en `ConsultaProcedimiento.xhtml:20`)

- [ ] **Step 1: Eliminar los dos archivos** (si el CSS no existe, solo el XHTML; no tocar `consulta-procedimiento-paso.css`, lo usa `Consulta.xhtml:28`).
- [ ] **Step 2: Verificar que no quedan referencias**

Run: `grep -rn "ConsultaProcedimiento.xhtml\|consulta-procedimiento.css" src/main/webapp`
Expected: sin coincidencias.

- [ ] **Step 3: Verificar empaquetado**

Run: `mvn clean package -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add -A src/main/webapp/paginas/ConsultaProcedimiento.xhtml src/main/webapp/resources/estilos/consulta-procedimiento.css
git commit -m "refactor: elimina pantalla huerfana ConsultaProcedimiento y su css"
```

### Task 2: Eliminar pantalla huérfana ConsultaProcedimientoPaso

**Files:**
- Delete: `src/main/webapp/paginas/ConsultaProcedimientoPaso.xhtml`
- Keep: `src/main/webapp/resources/estilos/consulta-procedimiento-paso.css` (usado por `Consulta.xhtml:28`)

- [ ] **Step 1: Eliminar el XHTML**.
- [ ] **Step 2: Verificar referencias**

Run: `grep -rn "ConsultaProcedimientoPaso.xhtml" src/main/webapp`
Expected: sin coincidencias.

- [ ] **Step 3: `mvn clean package -DskipTests`** — BUILD SUCCESS.
- [ ] **Step 4: Commit**

```bash
git add -A src/main/webapp/paginas/ConsultaProcedimientoPaso.xhtml
git commit -m "refactor: elimina pantalla huerfana ConsultaProcedimientoPaso"
```

### Task 3: Podar ConsultaProcedimientoModel (catálogo de consultas huérfano)

**Files:**
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModel.java`
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModelTest.java`

**Interfaces:**
- Elimina: campo `consultas`, getter `getConsultas()`, parámetro `ConsultaDAO` del constructor e `import ConsultaDAO`. Conserva: `procedimientos`, `procedimientoDAO`, `getProcedimientosPorConsulta`, `getNombreProcedimiento`, `nuevoParaConsulta`, etc. (las pestañas anidadas siguen usándolos).

- [ ] **Step 1: Actualizar los tests (dejar de compilar = RED)**

En `ConsultaProcedimientoModelTest.java`:
- Borrar el test `constructorCargaConsultas` (líneas 133-143) y el `import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;`.
- En TODAS las demás construcciones, eliminar el argumento central: reemplazar
  `new ConsultaProcedimientoModel(mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class))`
  por
  `new ConsultaProcedimientoModel(mock(ConsultaProcedimientoDAO.class), mock(ProcedimientoDAO.class))`
  y las variantes con variables (`dao, mock(ConsultaDAO.class), mock(ProcedimientoDAO.class)` → `dao, mock(ProcedimientoDAO.class)`; `..., mock(ConsultaDAO.class), procedimientoDAO)` → `..., procedimientoDAO)`).

- [ ] **Step 2: Ejecutar y verificar que falla**

Run: `mvn test -Dtest=ConsultaProcedimientoModelTest`
Expected: FAIL (constructor de 3 parámetros no existe).

- [ ] **Step 3: Implementar la poda en el modelo**

```java
@Inject
public ConsultaProcedimientoModel(ConsultaProcedimientoDAO consultaProcedimientoDAO,
        ProcedimientoDAO procedimientoDAO) {
    super(consultaProcedimientoDAO);
    this.procedimientoDAO = procedimientoDAO;
    this.procedimientos = procedimientoDAO.obtenerTodos().stream()
            .filter(p -> p.getActivo() == null || p.getActivo())
            .toList();
}
```

Borrar `private List<Consulta> consultas;`, `public List<Consulta> getConsultas()` y `import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;` (conservar `import ...entity.Consulta;` — se usa en `getProcedimientosPorConsulta`).

- [ ] **Step 4: Ejecutar y verificar verde**

Run: `mvn test -Dtest=ConsultaProcedimientoModelTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModelTest.java
git commit -m "refactor: poda catalogo de consultas huerfano de ConsultaProcedimientoModel"
```

### Task 4: Podar ConsultaProcedimientoPasoModel (listas huérfanas)

**Files:**
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModel.java`
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModelTest.java`

**Interfaces:**
- Elimina: campo `consultaProcedimientos`, getter `getConsultaProcedimientos()`, getter `getPersonasRoles()` (el campo `personasRoles` se conserva — lo usa `getPersonasRolesParaPaso`). Conserva: `consultaProcedimientoDAO` y constructor de 4 parámetros (los usa `getConsultaProcedimientosPorConsulta`).

- [ ] **Step 1: Actualizar tests (RED)**

En `ConsultaProcedimientoPasoModelTest.java` reemplazar el test `constructorCargaProcedimientosYPersonasRoles` (líneas 142-154) por:

```java
@Test
void constructorCargaPersonasRoles() {
    PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
    when(personaRolDao.obtenerTodos()).thenReturn(List.of(new PersonaRol()));

    ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
            mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
            personaRolDao, mock(ProcedimientoPasoDAO.class));

    assertEquals(1, model.getPersonasRolesParaPaso().size());
}
```

- [ ] **Step 2: Ejecutar y verificar que falla**

Run: `mvn test -Dtest=ConsultaProcedimientoPasoModelTest#constructorCargaPersonasRoles`
Expected: FAIL (`getConsultaProcedimientos` ya no existirá).

- [ ] **Step 3: Implementar la poda**

Borrar de `ConsultaProcedimientoPasoModel.java`: `private List<ConsultaProcedimiento> consultaProcedimientos;`, `this.consultaProcedimientos = consultaProcedimientoDAO.obtenerTodos();`, `public List<ConsultaProcedimiento> getConsultaProcedimientos()`, `public List<PersonaRol> getPersonasRoles()`. Conservar el resto intacto.

- [ ] **Step 4: Ejecutar y verificar verde**

Run: `mvn test -Dtest=ConsultaProcedimientoPasoModelTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModelTest.java
git commit -m "refactor: poda listas huerfanas de ConsultaProcedimientoPasoModel"
```

### Task 5: Eliminar viewParam idConsulta muerto

**Files:**
- Modify: `src/main/webapp/paginas/Consulta.xhtml:13-16`
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaModel.java:44-70`
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaModelTest.java`

- [ ] **Step 1: Quitar el bloque de metadata del XHTML**

Borrar de `Consulta.xhtml`:

```xml
<f:metadata>
    <f:viewParam name="idConsulta"
                 value="#{consultaModel.idConsultaContexto}" />
</f:metadata>
```

- [ ] **Step 2: Quitar los métodos del modelo (RED de tests)**

En `ConsultaModel.java` borrar `getIdConsultaContexto()`, `setIdConsultaContexto(String)` (con su javadoc) y `import java.util.UUID;`. En `ConsultaModelTest.java` borrar los tests `idDeContextoValidoSeleccionaLaConsulta`, `idDeContextoInvalidoLimpiaLaSeleccion`, `idDeContextoNuloNoModificaElEstado` (líneas 163-199) y `import java.util.UUID;`.

- [ ] **Step 3: Ejecutar y verificar que falla**

Run: `mvn test -Dtest=ConsultaModelTest`
Expected: FAIL (métodos borrados).

- [ ] **Step 4: Ejecutar y verificar verde** (los tests restantes pasan sin cambios)

Run: `mvn test -Dtest=ConsultaModelTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/webapp/paginas/Consulta.xhtml src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaModelTest.java
git commit -m "refactor: elimina viewParam idConsulta sin uso en Consulta"
```

### Task 6: Deep-link OrdenExamen → ExamenResultado (idOrden)

**Files:**
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ExamenResultadoModel.java`
- Modify: `src/main/webapp/paginas/OrdenExamen.xhtml` (columna nueva tras Indicaciones ~línea 120)
- Modify: `src/main/webapp/paginas/ExamenResultado.xhtml` (f:metadata tras línea 11)
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ExamenResultadoModelTest.java`
- Modify: `src/main/resources/i18n/messages_es/en/de/zh_CN.properties` y `messages.properties`: `ordenExamen.resultados` (es/base "Resultados", en "Results", de "Ergebnisse", zh_CN "结果")

**Interfaces:**
- Produce: `ExamenResultadoModel.getIdOrdenContexto()/setIdOrdenContexto(String)` (filtra `ordenExamenes`; guarda `ordenContexto`), y `nuevo()` preselecciona la orden si hay contexto.

- [ ] **Step 1: Escribir los tests que fallan**

```java
@Test
void idOrdenContextoValidoFiltraLaListaDeOrdenes() {
    OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
    OrdenExamen orden = new OrdenExamen();
    orden.setIdOrdenExamen(UUID.randomUUID());
    when(dao.buscarPorId(orden.getIdOrdenExamen())).thenReturn(orden);
    ExamenResultadoModel model = new ExamenResultadoModel(mock(ExamenResultadoDAO.class), dao);

    model.setIdOrdenContexto(orden.getIdOrdenExamen().toString());

    assertEquals(orden.getIdOrdenExamen().toString(), model.getIdOrdenContexto());
    assertEquals(List.of(orden), model.getOrdenExamenes());
}

@Test
void idOrdenContextoInvalidoConservaLaListaCompleta() {
    OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
    OrdenExamen orden = new OrdenExamen();
    orden.setIdOrdenExamen(UUID.randomUUID());
    when(dao.obtenerTodos()).thenReturn(List.of(orden));
    ExamenResultadoModel model = new ExamenResultadoModel(mock(ExamenResultadoDAO.class), dao);

    model.setIdOrdenContexto("no-es-uuid");

    assertEquals(List.of(orden), model.getOrdenExamenes());
}

@Test
void nuevoConContextoPreseleccionaLaOrden() {
    OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
    OrdenExamen orden = new OrdenExamen();
    orden.setIdOrdenExamen(UUID.randomUUID());
    when(dao.buscarPorId(orden.getIdOrdenExamen())).thenReturn(orden);
    ExamenResultadoModel model = new ExamenResultadoModel(mock(ExamenResultadoDAO.class), dao);
    model.setIdOrdenContexto(orden.getIdOrdenExamen().toString());

    model.nuevo();

    assertSame(orden, model.getSeleccionado().getIdOrdenExamen());
}
```

Agregar `import java.util.UUID;`.

- [ ] **Step 2: Ejecutar y verificar que falla**

Run: `mvn test -Dtest=ExamenResultadoModelTest`
Expected: FAIL (`setIdOrdenContexto` no existe).

- [ ] **Step 3: Implementar en el modelo**

```java
private final OrdenExamenDAO ordenExamenDAO;
private String idOrdenContexto;
private OrdenExamen ordenContexto;

@Inject
public ExamenResultadoModel(
        ExamenResultadoDAO examenResultadoDAO,
        OrdenExamenDAO ordenExamenDAO) {
    super(examenResultadoDAO);
    this.ordenExamenDAO = ordenExamenDAO;
    this.ordenExamenes = ordenExamenDAO.obtenerTodos();
}

public String getIdOrdenContexto() {
    return idOrdenContexto;
}

public void setIdOrdenContexto(String id) {
    if (id == null || id.isBlank()) {
        return;
    }
    this.idOrdenContexto = id;
    try {
        OrdenExamen orden = ordenExamenDAO.buscarPorId(UUID.fromString(id));
        ordenContexto = orden;
        ordenExamenes = orden == null ? List.of() : List.of(orden);
    } catch (IllegalArgumentException ex) {
        // id inválido: se conserva la lista completa cargada en el constructor
    }
}
```

Y `nuevo()` queda:

```java
public void nuevo() {
    seleccionado = new ExamenResultado();
    if (ordenContexto != null && ordenContexto.getIdOrdenExamen() != null) {
        seleccionado.setIdOrdenExamen(ordenContexto);
    }
    setEstado(ESTADO_CRUD.CREACION);
}
```

Agregar `import java.util.UUID;`.

- [ ] **Step 4: Ejecutar y verificar verde**

Run: `mvn test -Dtest=ExamenResultadoModelTest`
Expected: PASS.

- [ ] **Step 5: f:metadata en ExamenResultado.xhtml** (tras el cierre del tag `ui:composition`):

```xml
<f:metadata>
    <f:viewParam name="idOrden" value="#{examenResultadoModel.idOrdenContexto}" />
</f:metadata>
```

- [ ] **Step 6: Columna "Resultados" en OrdenExamen.xhtml** (tras la columna Indicaciones):

```xml
<p:column
    headerText="#{msg['ordenExamen.resultados']}"
    styleClass="action-cell">
    <h:link outcome="/paginas/ExamenResultado" value="#{msg['crud.ver']}">
        <f:param name="idOrden" value="#{item.idOrdenExamen}" />
    </h:link>
</p:column>
```

- [ ] **Step 7: Clave i18n en los 5 bundles** — `ordenExamen.resultados` con los valores indicados, junto al bloque `ordenExamen.*`.
- [ ] **Step 8: Suite completa + empaquetado**

Run: `mvn test && mvn clean package -DskipTests`
Expected: todo verde, BUILD SUCCESS.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ExamenResultadoModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ExamenResultadoModelTest.java src/main/webapp/paginas/OrdenExamen.xhtml src/main/webapp/paginas/ExamenResultado.xhtml src/main/resources/i18n/
git commit -m "feat: enlaza ordenes de examen con sus resultados (idOrden)"
```

### Task 7: Preseleccionar el paso al crear orden desde idPaso

**Files:**
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/OrdenExamenModel.java`
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/OrdenExamenModelTest.java`

- [ ] **Step 1: Test que falla**

```java
@Test
void nuevoConContextoPreseleccionaElPaso() {
    ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
    ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());
    when(dao.buscarPorId(paso.getIdConsultaProcedimientoPaso())).thenReturn(paso);
    OrdenExamenModel model = new OrdenExamenModel(mock(OrdenExamenDAO.class), dao);
    model.setIdPasoContexto(paso.getIdConsultaProcedimientoPaso().toString());

    model.nuevo();

    assertSame(paso, model.getSeleccionado().getIdConsultaProcedimientoPaso());
}
```

- [ ] **Step 2: `mvn test -Dtest=OrdenExamenModelTest#nuevoConContextoPreseleccionaElPaso`** — FAIL esperado.
- [ ] **Step 3: Implementar**

En `OrdenExamenModel`: agregar campo `private ConsultaProcedimientoPaso pasoContexto;`; dentro del `try` de `setIdPasoContexto`, tras el lookup, asignar `pasoContexto = paso;`. Cambiar `nuevo()`:

```java
public void nuevo() {
    seleccionado = new OrdenExamen();
    if (pasoContexto != null && pasoContexto.getIdConsultaProcedimientoPaso() != null) {
        seleccionado.setIdConsultaProcedimientoPaso(pasoContexto);
    }
    setEstado(ESTADO_CRUD.CREACION);
}
```

- [ ] **Step 4: `mvn test -Dtest=OrdenExamenModelTest`** — PASS.
- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/OrdenExamenModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/OrdenExamenModelTest.java
git commit -m "feat: preselecciona el paso al crear orden desde idPaso"
```

### Task 8: Columna plantilla en la tabla de pasos

**Files:**
- Modify: `src/main/webapp/paginas/Consulta.xhtml` (tabla `consultaProcedimientoPasoTable`, tras la columna "Procedimiento de consulta" ~líneas 491-496)

- [ ] **Step 1: Agregar la columna**

```xml
<p:column
    headerText="#{msg['consultaProcedimientoPaso.procedimientoPaso']}"
    styleClass="consulta-proc-cell">
    <h:outputText
        value="#{paso.idProcedimientoPaso != null ? paso.idProcedimientoPaso.nombre : '-'}" />
</p:column>
```

(La clave `consultaProcedimientoPaso.procedimientoPaso` ya existe en los 5 bundles; no tocar i18n.)

- [ ] **Step 2: `mvn clean package -DskipTests`** — BUILD SUCCESS.
- [ ] **Step 3: Commit**

```bash
git add src/main/webapp/paginas/Consulta.xhtml
git commit -m "feat: muestra la plantilla del paso en la tabla de pasos de Consulta"
```

### Task 9: Enlazar pantallas de plantillas en el menú

**Files:**
- Modify: `src/main/webapp/WEB-INF/plantillas/general.xhtml` (tras el enlace de Procedimiento)
- Modify: `src/main/resources/i18n/messages_es/en/de/zh_CN.properties` y `messages.properties`

- [ ] **Step 1: Agregar los tres enlaces**

```xml
<h:link
    outcome="/paginas/ProcedimientoPaso"
    value="#{msg['nav.procedimientoPaso']}"
    styleClass="layout-nav-link" />

<h:link
    outcome="/paginas/ProcedimientoPasoExamen"
    value="#{msg['nav.procedimientoPasoExamen']}"
    styleClass="layout-nav-link" />

<h:link
    outcome="/paginas/ProcedimientoPasoSecuencia"
    value="#{msg['nav.secuencia']}"
    styleClass="layout-nav-link" />
```

- [ ] **Step 2: Claves i18n** (agregar donde falten; `nav.procedimientoPaso` y `nav.secuencia` ya existen en es/base):

| clave | es/base | en | de | zh_CN |
|---|---|---|---|---|
| `nav.procedimientoPaso` | Pasos de procedimiento | Procedure steps | Verfahrensschritte | 程序步骤 |
| `nav.procedimientoPasoExamen` | Exámenes por paso | Step exams | Schrittexamen | 步骤检查 |
| `nav.secuencia` | Secuencias de pasos | Step sequences | Schrittsequenzen | 步骤序列 |

- [ ] **Step 3: `mvn clean package -DskipTests`** — BUILD SUCCESS.
- [ ] **Step 4: Commit**

```bash
git add src/main/webapp/WEB-INF/plantillas/general.xhtml src/main/resources/i18n/
git commit -m "feat: agrega pantallas de plantillas de procedimiento al menu"
```

### Task 10: Documentación (README)

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Actualizar** las secciones "Estado actual" y "Recorrido principal": registrar la eliminación de las pantallas huérfanas `ConsultaProcedimiento`/`ConsultaProcedimientoPaso` (el flujo anidado de Consulta queda como único camino), el deep-link `OrdenExamen → ExamenResultado?idOrden=`, la preselección de contexto al crear, y los tres enlaces nuevos del menú de plantillas. Sin cambios de BD.
- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: documenta limpieza y cierre del flujo de la familia consultas"
```
