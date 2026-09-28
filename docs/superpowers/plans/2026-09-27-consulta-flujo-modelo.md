# Cerrar el flujo de Consulta con el modelo de datos (GalenosSV) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hacer que la pantalla Consulta refleje fielmente el ER: enlazar la ejecución de pasos (`consulta_procedimiento_paso`) con su plantilla (`procedimiento_paso`), reparar las pantallas rotas por falta de converters, y dejar navegable la cadena completa consulta → procedimiento → paso → orden → resultado.

**Architecture:** Cambio aditivo de esquema (columna + FK nullable), actualización de la entidad JPA `ConsultaProcedimientoPaso`, nuevos converters Faces por `forClass` siguiendo el patrón existente (`PersonaRolConverter`), y ajustes de los backing beans `@ViewScoped` para filtrar pasos por procedimiento y validar el rol responsable. La UI sigue el patrón master-detail actual de `Consulta.xhtml`.

**Tech Stack:** Java 21, Jakarta EE 11 / Faces 4.1 (incluye `UUIDConverter` estándar), PrimeFaces 15.0.17, PostgreSQL 12 (docker `postgres-local`, BD `clinica`), Open Liberty 26, JUnit 6 + Mockito.

## Global Constraints

- Commits en español con prefijo convencional (`feat:`, `fix:`, `test:`, `docs:`) como el historial existente, sobre la rama de trabajo actual.
- Código y mensajes en español; claves i18n nuevas en los 4 bundles (`messages_es/en/de/zh_CN.properties`).
- TDD: cada cambio de Java lleva su test (patrón existente: `new Modelo(mock(DAO...))`).
- La migración de BD debe ser aditiva y no romper las filas existentes de `consulta_procedimiento_paso` (columna nullable).
- Verificación unitaria con `mvn test`; comandos de BD con `docker exec -i postgres-local psql -U fernando -d clinica`.
- El equipo documentó que `consulta_procedimiento.id_procedimiento` "se conserva como UUID": **no se le agrega FK en este plan**.

---

### Task 1: RolConverter (nuevo)

**Files:**
- Create: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/control/RolConverter.java`
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/control/RolConverterTest.java`

**Interfaces:**
- Consume: `RolDAO.buscarPorId(UUID)`, entidad `Rol` con `getIdRol()`.
- Produce: bean CDI `rolConverter` (`@Named @FacesConverter(forClass = Rol.class)`), conversión `Rol` ↔ UUID string. Lo consumirá Task 4.

- [ ] **Step 1: Write the failing test**

```java
package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RolConverterTest {

    @Test
    void getAsObjectNuloOEnBlancoDevuelveNull() {
        RolConverter converter = new RolConverter(mock(RolDAO.class));

        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), null));
        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), "  "));
    }

    @Test
    void getAsObjectBuscaPorUuid() {
        RolDAO dao = mock(RolDAO.class);
        UUID id = UUID.randomUUID();
        Rol rol = new Rol(id);
        when(dao.buscarPorId(id)).thenReturn(rol);
        RolConverter converter = new RolConverter(dao);

        assertEquals(rol, converter.getAsObject(
                mock(FacesContext.class), mock(UIComponent.class), id.toString()));
    }

    @Test
    void getAsStringNuloDevuelveVacioYObtieneId() {
        RolConverter converter = new RolConverter(mock(RolDAO.class));
        Rol rol = new Rol(UUID.randomUUID());

        assertEquals("", converter.getAsString(mock(FacesContext.class), mock(UIComponent.class), null));
        assertEquals(rol.getIdRol().toString(), converter.getAsString(
                mock(FacesContext.class), mock(UIComponent.class), rol));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=RolConverterTest`
Expected: FAIL (compilation error: `RolConverter` no existe).

- [ ] **Step 3: Write minimal implementation**

```java
package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/** Conversor Faces de {@link Rol} resuelto por DAO; mismo patrón que PersonaRolConverter. */
@Named
@FacesConverter(forClass = Rol.class)
public class RolConverter implements Converter<Rol> {

    private final RolDAO rolDAO;

    @Inject
    public RolConverter(RolDAO rolDAO) {
        this.rolDAO = rolDAO;
    }

    @Override
    public Rol getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return rolDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Rol value) {
        if (value == null) {
            return "";
        }
        return value.getIdRol().toString();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=RolConverterTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/control/RolConverter.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/control/RolConverterTest.java
git commit -m "feat: agrega RolConverter para selects de rol"
```

### Task 2: ProcedimientoConverter (nuevo)

**Files:**
- Create: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/control/ProcedimientoConverter.java`
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/control/ProcedimientoConverterTest.java`

**Interfaces:**
- Consume: `ProcedimientoDAO.buscarPorId(UUID)`, entidad `Procedimiento` con `getIdProcedimiento()`.
- Produce: bean CDI `procedimientoConverter`. Lo consumirá Task 4.

- [ ] **Step 1: Write the failing test**

```java
package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcedimientoConverterTest {

    @Test
    void getAsObjectNuloOEnBlancoDevuelveNull() {
        ProcedimientoConverter converter = new ProcedimientoConverter(mock(ProcedimientoDAO.class));

        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), null));
        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), "  "));
    }

    @Test
    void getAsObjectBuscaPorUuid() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        UUID id = UUID.randomUUID();
        Procedimiento procedimiento = new Procedimiento(id);
        when(dao.buscarPorId(id)).thenReturn(procedimiento);
        ProcedimientoConverter converter = new ProcedimientoConverter(dao);

        assertEquals(procedimiento, converter.getAsObject(
                mock(FacesContext.class), mock(UIComponent.class), id.toString()));
    }

    @Test
    void getAsStringNuloDevuelveVacioYObtieneId() {
        ProcedimientoConverter converter = new ProcedimientoConverter(mock(ProcedimientoDAO.class));
        Procedimiento procedimiento = new Procedimiento(UUID.randomUUID());

        assertEquals("", converter.getAsString(mock(FacesContext.class), mock(UIComponent.class), null));
        assertEquals(procedimiento.getIdProcedimiento().toString(), converter.getAsString(
                mock(FacesContext.class), mock(UIComponent.class), procedimiento));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ProcedimientoConverterTest`
Expected: FAIL (compilation error: `ProcedimientoConverter` no existe).

- [ ] **Step 3: Write minimal implementation**

```java
package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

/** Conversor Faces de {@link Procedimiento} resuelto por DAO; mismo patrón que PersonaRolConverter. */
@Named
@FacesConverter(forClass = Procedimiento.class)
public class ProcedimientoConverter implements Converter<Procedimiento> {

    private final ProcedimientoDAO procedimientoDAO;

    @Inject
    public ProcedimientoConverter(ProcedimientoDAO procedimientoDAO) {
        this.procedimientoDAO = procedimientoDAO;
    }

    @Override
    public Procedimiento getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return procedimientoDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Procedimiento value) {
        if (value == null) {
            return "";
        }
        return value.getIdProcedimiento().toString();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ProcedimientoConverterTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/control/ProcedimientoConverter.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/control/ProcedimientoConverterTest.java
git commit -m "feat: agrega ProcedimientoConverter para selects de procedimiento"
```

### Task 3: ProcedimientoPasoConverter (nuevo)

**Files:**
- Create: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/control/ProcedimientoPasoConverter.java`
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/control/ProcedimientoPasoConverterTest.java`

**Interfaces:**
- Consume: `ProcedimientoPasoDAO.buscarPorId(UUID)`, entidad `ProcedimientoPaso` con `getIdProcedimientoPaso()`.
- Produce: bean CDI `procedimientoPasoConverter`. Lo consumirán Task 4 y Task 10.

- [ ] **Step 1: Write the failing test**

```java
package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcedimientoPasoConverterTest {

    @Test
    void getAsObjectNuloOEnBlancoDevuelveNull() {
        ProcedimientoPasoConverter converter = new ProcedimientoPasoConverter(mock(ProcedimientoPasoDAO.class));

        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), null));
        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), "  "));
    }

    @Test
    void getAsObjectBuscaPorUuid() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        UUID id = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(id);
        when(dao.buscarPorId(id)).thenReturn(paso);
        ProcedimientoPasoConverter converter = new ProcedimientoPasoConverter(dao);

        assertEquals(paso, converter.getAsObject(
                mock(FacesContext.class), mock(UIComponent.class), id.toString()));
    }

    @Test
    void getAsStringNuloDevuelveVacioYObtieneId() {
        ProcedimientoPasoConverter converter = new ProcedimientoPasoConverter(mock(ProcedimientoPasoDAO.class));
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());

        assertEquals("", converter.getAsString(mock(FacesContext.class), mock(UIComponent.class), null));
        assertEquals(paso.getIdProcedimientoPaso().toString(), converter.getAsString(
                mock(FacesContext.class), mock(UIComponent.class), paso));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ProcedimientoPasoConverterTest`
Expected: FAIL (compilation error: `ProcedimientoPasoConverter` no existe).

- [ ] **Step 3: Write minimal implementation**

```java
package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

/** Conversor Faces de {@link ProcedimientoPaso} resuelto por DAO; mismo patrón que PersonaRolConverter. */
@Named
@FacesConverter(forClass = ProcedimientoPaso.class)
public class ProcedimientoPasoConverter implements Converter<ProcedimientoPaso> {

    private final ProcedimientoPasoDAO procedimientoPasoDAO;

    @Inject
    public ProcedimientoPasoConverter(ProcedimientoPasoDAO procedimientoPasoDAO) {
        this.procedimientoPasoDAO = procedimientoPasoDAO;
    }

    @Override
    public ProcedimientoPaso getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return procedimientoPasoDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProcedimientoPaso value) {
        if (value == null) {
            return "";
        }
        return value.getIdProcedimientoPaso().toString();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ProcedimientoPasoConverterTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/control/ProcedimientoPasoConverter.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/control/ProcedimientoPasoConverterTest.java
git commit -m "feat: agrega ProcedimientoPasoConverter para selects de pasos"
```

### Task 4: Aplicar converters en pantallas rotas

**Files:**
- Modify: `src/main/webapp/paginas/ProcedimientoPaso.xhtml` (selects `idProcedimiento` ~línea 211, `idRol` ~línea 236)
- Modify: `src/main/webapp/paginas/ProcedimientoPasoExamen.xhtml` (selects de examen ~línea 125 y de paso ~línea 138)
- Modify: `src/main/webapp/paginas/ProcedimientoPasoSecuencia.xhtml` (select de paso ~línea 195)

**Interfaces:**
- Consume: beans CDI `procedimientoConverter`, `rolConverter`, `procedimientoPasoConverter` (Tasks 1-3) y `examenConverter` (ya existe).

- [ ] **Step 1: Agregar converters en los tres XHTML**

En `ProcedimientoPaso.xhtml`, al `p:selectOneMenu id="idProcedimiento"` agregar atributo `converter="#{procedimientoConverter}"`; al `p:selectOneMenu id="idRol"` agregar `converter="#{rolConverter}"`.

En `ProcedimientoPasoExamen.xhtml`, al select de examen agregar `converter="#{examenConverter}"`; al select de paso agregar `converter="#{procedimientoPasoConverter}"`.

En `ProcedimientoPasoSecuencia.xhtml`, al select de paso agregar `converter="#{procedimientoPasoConverter}"`.

- [ ] **Step 2: Auditar los selects restantes**

Confirmar con grep que los selects que usan UUID crudos (`itemValue="#{x.id...}"` en `Documento.xhtml`, `Examen.xhtml`, `Persona.xhtml`, `MedioContacto.xhtml`, `PersonaRol.xhtml`, `Consulta.xhtml`, `ConsultaProcedimiento.xhtml`) no necesitan converter: Faces 4.1 resuelve `UUID` con el `UUIDConverter` estándar. No hay acción si solo usan UUID.

- [ ] **Step 3: Verificar el empaquetado**

Run: `mvn clean package -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add src/main/webapp/paginas/ProcedimientoPaso.xhtml src/main/webapp/paginas/ProcedimientoPasoExamen.xhtml src/main/webapp/paginas/ProcedimientoPasoSecuencia.xhtml
git commit -m "fix: agrega converters a selects de ProcedimientoPaso, ProcedimientoPasoExamen y Secuencia"
```

### Task 5: Corregir welcome-file (mapeo FacesServlet)

**Files:**
- Modify: `src/main/webapp/WEB-INF/web.xml` (servlet-mapping líneas 15-18; welcome-file líneas 26-28 queda igual)

**Interfaces:**
- Produce: FacesServlet mapeado a `*.xhtml`; el welcome-file `paginas/TipoDocumento.xhtml` se procesa por Faces.

- [ ] **Step 1: Cambiar el mapeo**

```xml
<servlet-mapping>
    <servlet-name>Faces Servlet</servlet-name>
    <url-pattern>*.xhtml</url-pattern>
</servlet-mapping>
```

El welcome-file `paginas/TipoDocumento.xhtml` no cambia. Los `<h:link outcome>` regeneran sus URLs solos con el nuevo mapeo.

- [ ] **Step 2: Verificar el empaquetado**

Run: `mvn clean package -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add src/main/webapp/WEB-INF/web.xml
git commit -m "fix: mapea FacesServlet a *.xhtml para que funcione el welcome-file"
```

### Task 6: i18n para estados de paso

**Files:**
- Modify: `src/main/resources/i18n/messages_es.properties`, `messages_en.properties`, `messages_de.properties`, `messages_zh_CN.properties`
- Modify: `src/main/webapp/paginas/Consulta.xhtml` (selects de estado ~líneas 637-647)
- Modify: `src/main/webapp/paginas/ConsultaProcedimientoPaso.xhtml` (selects de estado ~líneas 298-312)

**Interfaces:**
- Produce: claves `estado.pendiente`, `estado.enCurso`, `estado.completado` (valores técnicos iguales en los 4 bundles).

- [ ] **Step 1: Agregar claves en los 4 bundles**

```properties
estado.pendiente=PENDIENTE
estado.enCurso=EN_CURSO
estado.completado=COMPLETADO
```

- [ ] **Step 2: Usar claves en los selects de estado**

En `Consulta.xhtml` y `ConsultaProcedimientoPaso.xhtml` reemplazar `itemLabel="PENDIENTE"` por `itemLabel="#{msg['estado.pendiente']}"`, `itemLabel="EN_CURSO"` por `itemLabel="#{msg['estado.enCurso']}"` y `itemLabel="COMPLETADO"` por `itemLabel="#{msg['estado.completado']}"`. Los `itemValue` permanecen iguales.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/i18n/ src/main/webapp/paginas/Consulta.xhtml src/main/webapp/paginas/ConsultaProcedimientoPaso.xhtml
git commit -m "feat: i18n de los estados de paso en Consulta y ConsultaProcedimientoPaso"
```

### Task 7: Migración de esquema (enlace paso → plantilla)

**Files:**
- Create: `clinica_migracion_2026_09_27.sql` (raíz del repo)
- Modify: `clinica_ppi115_2026_08_20.sql` (columna en `CREATE TABLE public.consulta_procedimiento_paso` + FK e índice en la sección de ALTERs + actualizar comentario de la tabla)

**Interfaces:**
- Produce: columna `consulta_procedimiento_paso.id_procedimiento_paso uuid NULL`, FK `fk_consulta_procedimiento_paso_procedimiento_paso → procedimiento_paso(id_procedimiento_paso)` `ON UPDATE CASCADE ON DELETE RESTRICT`, índice `fki_fk_cpp_paso`.

- [ ] **Step 1: Actualizar el archivo canónico**

En `clinica_ppi115_2026_08_20.sql`, en el `CREATE TABLE public.consulta_procedimiento_paso` agregar tras `estado`:

```sql
    id_procedimiento_paso uuid
```

En la sección de ALTERs (tras la FK a persona_rol) agregar:

```sql
ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT fk_consulta_procedimiento_paso_procedimiento_paso FOREIGN KEY (id_procedimiento_paso) REFERENCES public.procedimiento_paso(id_procedimiento_paso) ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX fki_fk_cpp_paso ON public.consulta_procedimiento_paso USING btree (id_procedimiento_paso);
```

Actualizar el comentario de la tabla para mencionar que el paso ejecuta una plantilla de `procedimiento_paso`.

- [ ] **Step 2: Crear el script de migración para la BD viva**

```sql
-- Migración 2026-09-27: enlaza la ejecución de un paso con su plantilla procedimiento_paso.
BEGIN;

ALTER TABLE public.consulta_procedimiento_paso
    ADD COLUMN IF NOT EXISTS id_procedimiento_paso uuid;

ALTER TABLE public.consulta_procedimiento_paso
    DROP CONSTRAINT IF EXISTS fk_consulta_procedimiento_paso_procedimiento_paso;

ALTER TABLE public.consulta_procedimiento_paso
    ADD CONSTRAINT fk_consulta_procedimiento_paso_procedimiento_paso
    FOREIGN KEY (id_procedimiento_paso) REFERENCES public.procedimiento_paso(id_procedimiento_paso)
    ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS fki_fk_cpp_paso ON public.consulta_procedimiento_paso (id_procedimiento_paso);

COMMIT;
```

- [ ] **Step 3: Aplicar a la BD local**

Run: `cat clinica_migracion_2026_09_27.sql | docker exec -i postgres-local psql -U fernando -d clinica`
Expected: `BEGIN` / `ALTER TABLE` / `CREATE INDEX` / `COMMIT` sin errores.

- [ ] **Step 4: Verificar en BD**

Run:
```bash
docker exec postgres-local psql -U fernando -d clinica -c "\d public.consulta_procedimiento_paso"
docker exec postgres-local psql -U fernando -d clinica -c "SELECT count(*) FROM consulta_procedimiento_paso;"
```
Expected: la columna y la FK aparecen; el conteo sigue en 13 (filas intactas).

- [ ] **Step 5: Commit**

```bash
git add clinica_migracion_2026_09_27.sql clinica_ppi115_2026_08_20.sql
git commit -m "feat: enlaza consulta_procedimiento_paso con procedimiento_paso en el esquema"
```

### Task 8: Entidad ConsultaProcedimientoPaso (relación a plantilla)

**Files:**
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/entity/ConsultaProcedimientoPaso.java` (junto a `idConsultaProcedimiento` ~líneas 56-63)

**Interfaces:**
- Consume: columna `id_procedimiento_paso` (Task 7), entidad `ProcedimientoPaso`.
- Produce: `ProcedimientoPaso getIdProcedimientoPaso()` / `setIdProcedimientoPaso(ProcedimientoPaso)` con `@ManyToOne`. Lo consumirá Task 9.

- [ ] **Step 1: Agregar campo, getter y setter**

```java
@JoinColumn(name = "id_procedimiento_paso", referencedColumnName = "id_procedimiento_paso")
@ManyToOne
private ProcedimientoPaso idProcedimientoPaso;
```

Con javadoc breve y `get/setIdProcedimientoPaso()`, siguiendo el estilo de `idConsultaProcedimiento`.

- [ ] **Step 2: Ejecutar tests existentes**

Run: `mvn test`
Expected: PASS (cambio aditivo; ningún test usa el campo nuevo).

- [ ] **Step 3: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/entity/ConsultaProcedimientoPaso.java
git commit -m "feat: agrega relacion ManyToOne a ProcedimientoPaso en ConsultaProcedimientoPaso"
```

### Task 9: Modelo ConsultaProcedimientoPasoModel (pasos por procedimiento + rol)

**Files:**
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModel.java` (constructor ~líneas 32-41 + métodos nuevos)
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModelTest.java` (actualizar TODAS las construcciones con el 4º parámetro + tests nuevos)

**Interfaces:**
- Consume: `ProcedimientoPasoDAO.findByIdProcedimiento(UUID, int, int)` (ya existe), `PersonaRolDAO.obtenerTodos()`, entidad `ConsultaProcedimientoPaso.idProcedimientoPaso` (Task 8).
- Produce: `List<ProcedimientoPaso> getPasosDeProcedimiento(ConsultaProcedimiento)`, `List<PersonaRol> getPersonasRolesParaPaso()`, `Rol getRolRequerido()`, `void limpiarResponsableInvalido()`, `void validarResponsable(FacesContext, UIComponent, Object)`. Lo consumirá Task 10.

- [ ] **Step 1: Write the failing tests**

Actualizar TODAS las construcciones existentes del modelo añadiendo `mock(ProcedimientoPasoDAO.class)` como cuarto parámetro. Agregar estos tests:

```java
@Test
void getPasosDeProcedimientoSinProcedimientoDevuelveVacia() {
    ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
    ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
            mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
            mock(PersonaRolDAO.class), dao);

    assertTrue(model.getPasosDeProcedimiento(new ConsultaProcedimiento()).isEmpty());
    verifyNoInteractions(dao);
}

@Test
void getPasosDeProcedimientoDelegaEnDao() {
    ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
    ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
            mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
            mock(PersonaRolDAO.class), dao);
    UUID idProcedimiento = UUID.randomUUID();
    ConsultaProcedimiento cp = new ConsultaProcedimiento();
    cp.setIdProcedimiento(idProcedimiento);
    ProcedimientoPaso paso = new ProcedimientoPaso();
    when(dao.findByIdProcedimiento(idProcedimiento, 0, Integer.MAX_VALUE))
            .thenReturn(List.of(paso));

    assertEquals(List.of(paso), model.getPasosDeProcedimiento(cp));
}

@Test
void getPersonasRolesParaPasoFiltraPorRolRequerido() {
    PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
    Rol rolMedico = new Rol(UUID.randomUUID());
    Rol rolEnfermero = new Rol(UUID.randomUUID());
    PersonaRol medico = new PersonaRol();
    medico.setIdRol(rolMedico);
    PersonaRol enfermero = new PersonaRol();
    enfermero.setIdRol(rolEnfermero);
    when(personaRolDao.obtenerTodos()).thenReturn(List.of(medico, enfermero));
    ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
            mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
            personaRolDao, mock(ProcedimientoPasoDAO.class));
    ProcedimientoPaso plantilla = new ProcedimientoPaso();
    plantilla.setIdRol(rolMedico);
    ConsultaProcedimientoPaso seleccionado = new ConsultaProcedimientoPaso();
    seleccionado.setIdProcedimientoPaso(plantilla);
    model.seleccionar(seleccionado);

    assertEquals(List.of(medico), model.getPersonasRolesParaPaso());
}

@Test
void limpiarResponsableInvalidoPoneNullCuandoNoCorresponde() {
    PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
    Rol rolMedico = new Rol(UUID.randomUUID());
    PersonaRol medico = new PersonaRol();
    medico.setIdRol(rolMedico);
    when(personaRolDao.obtenerTodos()).thenReturn(List.of(medico));
    ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
            mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
            personaRolDao, mock(ProcedimientoPasoDAO.class));
    ProcedimientoPaso plantilla = new ProcedimientoPaso();
    plantilla.setIdRol(new Rol(UUID.randomUUID()));
    ConsultaProcedimientoPaso seleccionado = new ConsultaProcedimientoPaso();
    seleccionado.setIdProcedimientoPaso(plantilla);
    seleccionado.setIdPersonaRol(medico);
    model.seleccionar(seleccionado);

    model.limpiarResponsableInvalido();

    assertNull(model.getSeleccionado().getIdPersonaRol());
}

@Test
void validarResponsableConRolIncorrectoEsRechazado() {
    ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
            mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
            mock(PersonaRolDAO.class), mock(ProcedimientoPasoDAO.class));
    ProcedimientoPaso plantilla = new ProcedimientoPaso();
    plantilla.setIdRol(new Rol(UUID.randomUUID()));
    ConsultaProcedimientoPaso seleccionado = new ConsultaProcedimientoPaso();
    seleccionado.setIdProcedimientoPaso(plantilla);
    model.seleccionar(seleccionado);
    PersonaRol responsable = new PersonaRol();
    responsable.setIdRol(new Rol(UUID.randomUUID()));

    assertThrows(ValidatorException.class, () -> model.validarResponsable(
            contexto(), componente("pasoPersonaRol"), responsable));
}
```

Agregar la clave `consultaProcedimientoPaso.rolNoCorresponde` al bundle de prueba del helper `contexto()` existente y los imports de `ProcedimientoPaso`, `ProcedimientoPasoDAO` y `Rol`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=ConsultaProcedimientoPasoModelTest`
Expected: FAIL (el constructor de 4 parámetros no existe).

- [ ] **Step 3: Write minimal implementation**

```java
private final ProcedimientoPasoDAO procedimientoPasoDAO;

@Inject
public ConsultaProcedimientoPasoModel(
        ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO,
        ConsultaProcedimientoDAO consultaProcedimientoDAO,
        PersonaRolDAO personaRolDAO,
        ProcedimientoPasoDAO procedimientoPasoDAO) {
    super(consultaProcedimientoPasoDAO);
    this.consultaProcedimientoDAO = consultaProcedimientoDAO;
    this.procedimientoPasoDAO = procedimientoPasoDAO;
    this.consultaProcedimientos = consultaProcedimientoDAO.obtenerTodos();
    this.personasRoles = personaRolDAO.obtenerTodos();
}

public List<ProcedimientoPaso> getPasosDeProcedimiento(ConsultaProcedimiento consultaProcedimiento) {
    if (consultaProcedimiento == null || consultaProcedimiento.getIdProcedimiento() == null) {
        return List.of();
    }
    return procedimientoPasoDAO.findByIdProcedimiento(
            consultaProcedimiento.getIdProcedimiento(), 0, Integer.MAX_VALUE);
}

public Rol getRolRequerido() {
    ProcedimientoPaso plantilla = seleccionado == null ? null : seleccionado.getIdProcedimientoPaso();
    return plantilla == null ? null : plantilla.getIdRol();
}

public List<PersonaRol> getPersonasRolesParaPaso() {
    Rol rolRequerido = getRolRequerido();
    if (rolRequerido == null || rolRequerido.getIdRol() == null) {
        return personasRoles;
    }
    return personasRoles.stream()
            .filter(pr -> pr.getIdRol() != null
                    && rolRequerido.getIdRol().equals(pr.getIdRol().getIdRol()))
            .toList();
}

public void limpiarResponsableInvalido() {
    if (seleccionado == null || seleccionado.getIdPersonaRol() == null) {
        return;
    }
    if (!getPersonasRolesParaPaso().contains(seleccionado.getIdPersonaRol())) {
        seleccionado.setIdPersonaRol(null);
    }
}

public void validarResponsable(FacesContext contexto, UIComponent componente, Object valor) {
    if (!(valor instanceof PersonaRol responsable)) {
        return;
    }
    Rol rolRequerido = getRolRequerido();
    if (rolRequerido != null && rolRequerido.getIdRol() != null
            && (responsable.getIdRol() == null
            || !rolRequerido.getIdRol().equals(responsable.getIdRol().getIdRol()))) {
        lanzarValidacion(contexto, "consultaProcedimientoPaso.rolNoCorresponde");
    }
}
```

Con imports de `Rol`, `ProcedimientoPaso`, `ProcedimientoPasoDAO` y javadoc breve por método.

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=ConsultaProcedimientoPasoModelTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModelTest.java
git commit -m "feat: filtra pasos por procedimiento y valida rol del responsable"
```

### Task 10: UI de pasos en Consulta.xhtml

**Files:**
- Modify: `src/main/webapp/paginas/Consulta.xhtml` (pestaña Pasos ~líneas 466-668: botón "Nuevo paso" ~467-472, combo `pasoConsultaProcedimiento` ~538-554, combo `pasoPersonaRol` ~565-583)
- Modify: `src/main/resources/i18n/messages_es.properties` (+ en/de/zh): `consultaProcedimientoPaso.procedimientoPaso=Paso de procedimiento` y `consultaProcedimientoPaso.rolNoCorresponde=El responsable no corresponde al rol requerido por el paso.`

**Interfaces:**
- Consume: `procedimientoPasoConverter` (Task 3) y los métodos de Task 9.

- [ ] **Step 1: Deshabilitar "Nuevo paso" sin procedimientos**

Al `p:commandButton` de la pestaña Pasos agregar:

```xml
disabled="#{consultaProcedimientoModel.getProcedimientosPorConsulta(consultaModel.seleccionado).isEmpty()}"
```

- [ ] **Step 2: Combo de plantilla de paso (tras el combo `pasoConsultaProcedimiento`)**

```xml
<div class="form-field">
    <p:outputLabel for="pasoProcedimientoPaso"
                   value="#{msg['consultaProcedimientoPaso.procedimientoPaso']}"
                   styleClass="form-label" />
    <p:selectOneMenu id="pasoProcedimientoPaso"
                     value="#{consultaProcedimientoPasoModel.seleccionado.idProcedimientoPaso}"
                     converter="#{procedimientoPasoConverter}">
        <f:selectItem itemLabel="#{msg['crud.seleccionar']}" itemValue="#{null}" />
        <f:selectItems value="#{consultaProcedimientoPasoModel.getPasosDeProcedimiento(consultaProcedimientoPasoModel.seleccionado.idConsultaProcedimiento)}"
                       var="pp"
                       itemLabel="#{pp.nombre}"
                       itemValue="#{pp}" />
        <p:ajax event="change"
                listener="#{consultaProcedimientoPasoModel.limpiarResponsableInvalido}"
                process="@this"
                update="pasoPersonaRol" />
    </p:selectOneMenu>
    <p:message for="pasoProcedimientoPaso" />
</div>
```

- [ ] **Step 3: Combo responsable filtrado y validado**

En `pasoPersonaRol`: cambiar `value="#{consultaProcedimientoPasoModel.personasRoles}"` por `value="#{consultaProcedimientoPasoModel.getPersonasRolesParaPaso()}"` y agregar `validator="#{consultaProcedimientoPasoModel.validarResponsable}"`.

- [ ] **Step 4: AJAX en el combo de consultaProcedimiento**

En `pasoConsultaProcedimiento` agregar para refrescar plantilla y responsable al cambiar de procedimiento:

```xml
<p:ajax event="change"
        listener="#{consultaProcedimientoPasoModel.limpiarResponsableInvalido}"
        process="@this"
        update="pasoProcedimientoPaso pasoPersonaRol" />
```

- [ ] **Step 5: Commit**

```bash
git add src/main/webapp/paginas/Consulta.xhtml src/main/resources/i18n/
git commit -m "feat: enlaza pasos de consulta con la plantilla de procedimiento y su rol"
```

### Task 11: Enlazar OrdenExamen y ExamenResultado en el menú

**Files:**
- Modify: `src/main/webapp/WEB-INF/plantillas/general.xhtml` (tras el enlace de Examen ~líneas 78-86)
- Modify: `src/main/resources/i18n/messages_es.properties` (+ en/de/zh): `nav.ordenExamen=Órdenes de examen`, `nav.examenResultado=Resultados de examen`

- [ ] **Step 1: Agregar enlaces tras "Examen"**

```xml
<h:link outcome="/paginas/OrdenExamen"
         value="#{msg['nav.ordenExamen']}"
         styleClass="layout-nav-link" />

<h:link outcome="/paginas/ExamenResultado"
         value="#{msg['nav.examenResultado']}"
         styleClass="layout-nav-link" />
```

- [ ] **Step 2: Commit**

```bash
git add src/main/webapp/WEB-INF/plantillas/general.xhtml src/main/resources/i18n/
git commit -m "feat: agrega OrdenExamen y ExamenResultado a la navegacion principal"
```

### Task 12: Deep-link paso → órdenes de examen

**Files:**
- Modify: `src/main/webapp/paginas/Consulta.xhtml` (columna nueva en la tabla de pasos ~líneas 519-525)
- Modify: `src/main/webapp/paginas/OrdenExamen.xhtml` (`f:metadata` tras `ui:composition` ~líneas 13-16)
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/OrdenExamenModel.java` (guardar `consultaProcedimientoPasoDAO` como campo + método `setIdPasoContexto`)
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/OrdenExamenModelTest.java` (nuevo)

**Interfaces:**
- Consume: `ConsultaProcedimientoPasoDAO.buscarPorId(UUID)`.
- Produce: `OrdenExamenModel.setIdPasoContexto(String)` — filtra `consultaProcedimientoPasos` al paso indicado (lista completa si el id es inválido).

- [ ] **Step 1: Write the failing test**

```java
package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrdenExamenModelTest {

    @Test
    void idPasoContextoValidoFiltraLaListaDePasos() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());
        when(dao.buscarPorId(paso.getIdConsultaProcedimientoPaso())).thenReturn(paso);
        OrdenExamenModel model = new OrdenExamenModel(mock(OrdenExamenDAO.class), dao);

        model.setIdPasoContexto(paso.getIdConsultaProcedimientoPaso().toString());

        assertEquals(List.of(paso), model.getConsultaProcedimientoPasos());
    }

    @Test
    void idPasoContextoInvalidoConservaLaListaCompleta() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());
        when(dao.obtenerTodos()).thenReturn(List.of(paso));
        OrdenExamenModel model = new OrdenExamenModel(mock(OrdenExamenDAO.class), dao);

        model.setIdPasoContexto("no-es-uuid");

        assertEquals(List.of(paso), model.getConsultaProcedimientoPasos());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=OrdenExamenModelTest`
Expected: FAIL (compilation error: `setIdPasoContexto` no existe).

- [ ] **Step 3: Write minimal implementation**

En `OrdenExamenModel`, guardar el DAO como campo y agregar:

```java
private final ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

@Inject
public OrdenExamenModel(
        OrdenExamenDAO ordenExamenDAO,
        ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO) {
    super(ordenExamenDAO);
    this.consultaProcedimientoPasoDAO = consultaProcedimientoPasoDAO;
    this.consultaProcedimientoPasos = consultaProcedimientoPasoDAO.obtenerTodos();
}

public void setIdPasoContexto(String id) {
    if (id == null || id.isBlank()) {
        return;
    }
    try {
        ConsultaProcedimientoPaso paso =
                consultaProcedimientoPasoDAO.buscarPorId(UUID.fromString(id));
        consultaProcedimientoPasos = paso == null ? List.of() : List.of(paso);
    } catch (IllegalArgumentException ex) {
        // id inválido: se conserva la lista completa cargada en el constructor
    }
}
```

Con import de `UUID`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=OrdenExamenModelTest`
Expected: PASS.

- [ ] **Step 5: f:metadata en OrdenExamen.xhtml**

```xml
<f:metadata>
    <f:viewParam name="idPaso" value="#{ordenExamenModel.idPasoContexto}" />
</f:metadata>
```

- [ ] **Step 6: Columna de enlace en la tabla de pasos de Consulta.xhtml**

```xml
<p:column headerText="#{msg['consultaProcedimientoPaso.ordenes']}"
          styleClass="action-cell">
    <h:link outcome="/paginas/OrdenExamen" value="#{msg['crud.ver']}">
        <f:param name="idPaso" value="#{paso.idConsultaProcedimientoPaso}" />
    </h:link>
</p:column>
```

Agregar clave `consultaProcedimientoPaso.ordenes=Órdenes` en los 4 bundles (y `crud.ver=Ver` si no existe).

- [ ] **Step 7: Run full tests**

Run: `mvn test`
Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/webapp/paginas/Consulta.xhtml src/main/webapp/paginas/OrdenExamen.xhtml src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/OrdenExamenModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/OrdenExamenModelTest.java src/main/resources/i18n/
git commit -m "feat: enlaza pasos de consulta con sus ordenes de examen"
```

### Task 13: CHECK de estado en BD

**Files:**
- Modify: `clinica_ppi115_2026_08_20.sql` (sección ALTERs) y `clinica_migracion_2026_09_27.sql` (Task 7)

- [ ] **Step 1: Agregar en ambos archivos**

En el canónico:

```sql
ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT ck_consulta_procedimiento_paso_estado CHECK (estado IN ('PENDIENTE', 'EN_CURSO', 'COMPLETADO'));
```

En la migración (idempotente):

```sql
ALTER TABLE public.consulta_procedimiento_paso
    DROP CONSTRAINT IF EXISTS ck_consulta_procedimiento_paso_estado;

ALTER TABLE public.consulta_procedimiento_paso
    ADD CONSTRAINT ck_consulta_procedimiento_paso_estado
    CHECK (estado IN ('PENDIENTE', 'EN_CURSO', 'COMPLETADO'));
```

- [ ] **Step 2: Aplicar migración**

Run: `cat clinica_migracion_2026_09_27.sql | docker exec -i postgres-local psql -U fernando -d clinica`
Expected: éxito (los registros existentes cumplen el CHECK).

- [ ] **Step 3: Commit**

```bash
git add clinica_ppi115_2026_08_20.sql clinica_migracion_2026_09_27.sql
git commit -m "feat: agrega CHECK de estados validos en consulta_procedimiento_paso"
```

### Task 14: Fecha de inicio por defecto en los modelos

**Files:**
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaModel.java` (`nuevo()` ~líneas 92-95)
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModel.java` (`nuevo()` ~líneas 84-87)
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModel.java` (`nuevo()`)
- Test: `ConsultaModelTest`, `ConsultaProcedimientoModelTest`, `ConsultaProcedimientoPasoModelTest` (asserts en los tests `nuevoCrea...`)

- [ ] **Step 1: Write the failing assertions**

En cada test `nuevoCrea...` agregar:

```java
assertNotNull(model.getSeleccionado().getFechaInicio());
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest='ConsultaModelTest,ConsultaProcedimientoModelTest,ConsultaProcedimientoPasoModelTest'`
Expected: FAIL en los 3 asserts nuevos.

- [ ] **Step 3: Write minimal implementation**

En los tres `nuevo()` agregar tras crear la entidad:

```java
seleccionado.setFechaInicio(new Date());
```

Con import `java.util.Date`.

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest='ConsultaModelTest,ConsultaProcedimientoModelTest,ConsultaProcedimientoPasoModelTest'`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaModel.java src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModel.java src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoPasoModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/
git commit -m "feat: asigna fecha de inicio por defecto al crear consulta, procedimiento y paso"
```

### Task 15: Filtrar procedimientos inactivos en los selectores

**Files:**
- Modify: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModel.java` (constructor ~línea 41)
- Test: `src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModelTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void constructorFiltraProcedimientosInactivos() {
    ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
    Procedimiento activo = new Procedimiento();
    activo.setActivo(true);
    Procedimiento inactivo = new Procedimiento();
    inactivo.setActivo(false);
    when(dao.obtenerTodos()).thenReturn(List.of(activo, inactivo));

    ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
            mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), dao);

    assertEquals(List.of(activo), model.getProcedimientos());
}
```

Con imports de `ProcedimientoDAO`, `ConsultaDAO` y `Procedimiento` si faltan.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ConsultaProcedimientoModelTest#constructorFiltraProcedimientosInactivos`
Expected: FAIL (el inactivo también aparece).

- [ ] **Step 3: Write minimal implementation**

```java
this.procedimientos = procedimientoDAO.obtenerTodos().stream()
        .filter(p -> p.getActivo() == null || p.getActivo())
        .toList();
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ConsultaProcedimientoModelTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModel.java src/test/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaProcedimientoModelTest.java
git commit -m "feat: filtra procedimientos inactivos en el selector de Consulta"
```

### Task 16: Documentación (README)

**Files:**
- Modify: `README.md` (secciones "Estado actual" y "Recorrido principal")

- [ ] **Step 1: Actualizar el README**

En "Estado actual" y "Recorrido principal" reflejar: enlace paso→plantilla (`id_procedimiento_paso`), converters nuevos (`rolConverter`, `procedimientoConverter`, `procedimientoPasoConverter`), navegación a órdenes/resultados, y el script `clinica_migracion_2026_09_27.sql` con su comando de aplicación:

```bash
cat clinica_migracion_2026_09_27.sql | docker exec -i postgres-local psql -U fernando -d clinica
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: documenta el cierre del flujo de consulta y la migracion 2026-09-27"
```
