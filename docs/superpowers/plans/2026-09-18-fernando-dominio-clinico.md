# Plan de Implementación — Dominio Clínico (Fernando)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar 6 pantallas CRUD del dominio clínico: Consulta, ConsultaProcedimiento, ConsultaProcedimientoPaso, OrdenExamen, ExamenResultado y ExamenTipoExamen.

**Architecture:** Seguir el patrón existente de TipoExamen: Backing Bean (`@Named @ViewScoped` que extiende `AbstractModel<T>`) + XHTML (template `general.xhtml` con `crud:botones-top/bottom`, `p:dataTable` lazy, `p:dialog` modal) + CSS específico + i18n en 4 idiomas + nav link en `general.xhtml`.

**Tech Stack:** Jakarta EE 11, PrimeFaces 15, CDI, JPA (Hibernate), OpenLiberty 26, PostgreSQL

## Estructura de Archivos

### Archivos a crear (por pantalla)

| Pantalla | Model | XHTML | CSS |
|----------|-------|-------|-----|
| Consulta | `model/ConsultaModel.java` | `paginas/Consulta.xhtml` | `estilos/consulta.css` |
| ConsultaProcedimiento | `model/ConsultaProcedimientoModel.java` | `paginas/ConsultaProcedimiento.xhtml` | `estilos/consulta-procedimiento.css` |
| ConsultaProcedimientoPaso | `model/ConsultaProcedimientoPasoModel.java` | `paginas/ConsultaProcedimientoPaso.xhtml` | `estilos/consulta-procedimiento-paso.css` |
| OrdenExamen | `model/OrdenExamenModel.java` | `paginas/OrdenExamen.xhtml` | `estilos/orden-examen.css` |
| ExamenResultado | `model/ExamenResultadoModel.java` | `paginas/ExamenResultado.xhtml` | `estilos/examen-resultado.css` |
| ExamenTipoExamen | `model/ExamenTipoExamenModel.java` | `paginas/ExamenTipoExamen.xhtml` | `estilos/examen-tipo-examen.css` |

### Archivos a modificar

- `src/main/resources/i18n/messages_es.properties` — agregar claves i18n
- `src/main/resources/i18n/messages_en.properties` — agregar claves i18n
- `src/main/resources/i18n/messages_de.properties` — agregar claves i18n
- `src/main/resources/i18n/messages_zh_CN.properties` — agregar claves i18n
- `src/main/webapp/WEB-INF/plantillas/general.xhtml` — agregar nav links

### Dependencias (ya existentes)

- DAOs: `ConsultaDAO`, `ConsultaProcedimientoDAO`, `ConsultaProcedimientoPasoDAO`, `OrdenExamenDAO`, `ExamenResultadoDAO`, `ExamenTipoExamenDAO`
- Entidades: Todas mapeadas en `entity/`
- Composite components: `crud:botones-top`, `crud:botones-bottom`
- Base path Java: `sv.edu.ues.occingenieriappi115_2026.salud`
- Base path webapp: `src/main/webapp/`

---

## Global Constraints

- Paquete base: `sv.edu.ues.occingenieriappi115_2026.salud`
- Patrón: `AbstractModel<T>` + `DefaultDAO<T>` (ya implementados)
- XHTML template: `general.xhtml`
- Composite components: `xmlns:crud="jakarta.faces.composite/crud"`
- CSS: arch individuales en `resources/estilos/`
- i18n: 4 archivos `messages_{locale}.properties`
- Nav: enlace en `general.xhtml` sidebar
- PK: UUID (generados por JPA)
- PrimeFaces widgets: `PF('widgetVar').show()`

---

## Task 1: Consulta (Pantalla base — Media-Alta)

**Files:**
- Create: `src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/model/ConsultaModel.java`
- Create: `src/main/webapp/paginas/Consulta.xhtml`
- Create: `src/main/webapp/resources/estilos/consulta.css`
- Modify: `src/main/resources/i18n/messages_es.properties` (agregar claves `consulta.*`)
- Modify: `src/main/webapp/WEB-INF/plantillas/general.xhtml` (agregar nav link)

**Interfaces:**
- Consumes: `ConsultaDAO` (ya existe), `Consulta` entity (ya existe), `PersonaRol` (para selector)
- Produces: `ConsultaModel` (bean CDI `@Named @ViewScoped`)

### Step 1: Crear ConsultaModel.java

```java
package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;

@Named
@ViewScoped
public class ConsultaModel extends AbstractModel<Consulta> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Consulta seleccionado;

    @Inject
    public ConsultaModel(ConsultaDAO consultaDAO) {
        super(consultaDAO);
    }

    public Consulta getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Consulta seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new Consulta();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Consulta seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
```

- [ ] Step 1 completado: Crear `ConsultaModel.java`

### Step 2: Crear Consulta.xhtml

- [ ] Step 2: Crear `Consulta.xhtml` con DataTable lazy, columnas: fechaInicio, fechaFin, referenciaExterna, observaciones, PersonaRol. Dialog con campos: fechaInicio (p:calendar), fechaFin (p:calendar), referenciaExterna (inputText), observaciones (inputTextarea), idPersonaRol (selectOneMenu con converter).

### Step 3: Crear consulta.css

- [ ] Step 3: Copiar estructura de `tipo-examen.css` y adaptar nombre de clases.

### Step 4: Agregar i18n messages_es.properties

Agregar al final del archivo:

```properties
consulta.titulo=Consultas
consulta.subtitulo=Gestiona las consultas clinicas del sistema.
consulta.lista=Lista de consultas
consulta.fechaInicio=Fecha de inicio
consulta.fechaFin=Fecha de fin
consulta.referenciaExterna=Referencia externa
consulta.observaciones=Observaciones
consulta.personaRol=Persona / Rol
consulta.buscar=Buscar por referencia
consulta.sinRegistros=No se encontraron consultas
consulta.sinObservaciones=Sin observaciones
consulta.nueva=Nueva consulta
consulta.editar=Editar consulta
consulta.informacion=Informacion de la consulta
consulta.completar=Completa la informacion de la consulta.
consulta.referenciaExternaPlaceholder=Ej. Ref-2026-001
consulta.observacionesPlaceholder=Agrega observaciones de la consulta...
consulta.acciones=Acciones
```

- [ ] Step 4: Agregar claves i18n en `messages_es.properties`

### Step 5: Agregar nav link en general.xhtml

En el `<aside class="layout-nav">`, agregar antes del cierre:

```xml
<h:link outcome="/paginas/Consulta" styleClass="nav-item">
    <i class="pi pi-calendar"></i>
    <span>#{msg['nav.consulta']}</span>
</h:link>
```

Y en `messages_es.properties` agregar: `nav.consulta=Consultas`

- [ ] Step 5: Agregar nav link en `general.xhtml` y clave `nav.consulta`

### Step 6: Verificar compilación

```bash
mvn compile -q
```

- [ ] Step 6: Ejecutar `mvn compile` y verificar que no hay errores

---

## Task 2: ConsultaProcedimiento (Alta)

**Files:**
- Create: `model/ConsultaProcedimientoModel.java`
- Create: `paginas/ConsultaProcedimiento.xhtml`
- Create: `estilos/consulta-procedimiento.css`

**Interfaces:**
- Consumes: `ConsultaProcedimientoDAO`, `ConsultaProcedimiento` entity, `Consulta` (selector)
- Produces: `ConsultaProcedimientoModel`

### Step 1: Crear ConsultaProcedimientoModel.java

```java
package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;

@Named
@ViewScoped
public class ConsultaProcedimientoModel extends AbstractModel<ConsultaProcedimiento> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ConsultaProcedimiento seleccionado;

    @Inject
    public ConsultaProcedimientoModel(ConsultaProcedimientoDAO dao) {
        super(dao);
    }

    public ConsultaProcedimiento getSeleccionado() { return seleccionado; }
    public void setSeleccionado(ConsultaProcedimiento seleccionado) { this.seleccionado = seleccionado; }

    public void nuevo() {
        seleccionado = new ConsultaProcedimiento();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ConsultaProcedimiento seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) return;
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {}
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
```

- [ ] Step 1: Crear `ConsultaProcedimientoModel.java`

### Step 2: Crear ConsultaProcedimiento.xhtml

- [ ] Step 2: Crear XHTML con columnas: idConsulta (selector), idProcedimiento (UUID), fechaInicio, fechaFin, observaciones. Dialog con campos correspondientes.

### Step 3: Crear consulta-procedimiento.css

- [ ] Step 3: Copiar estructura de `tipo-examen.css`

### Step 4: Agregar i18n (messages_es.properties)

```properties
consultaProcedimiento.titulo=Consulta - Procedimientos
consultaProcedimiento.subtitulo=Gestiona los procedimientos asociados a cada consulta.
consultaProcedimiento.lista=Lista de procedimientos de consulta
consultaProcedimiento.consulta=Consulta
consultaProcedimiento.idProcedimiento=ID Procedimiento
consultaProcedimiento.fechaInicio=Fecha de inicio
consultaProcedimiento.fechaFin=Fecha de fin
consultaProcedimiento.observaciones=Observaciones
consultaProcedimiento.buscar=Buscar
consultaProcedimiento.sinRegistros=No se encontraron procedimientos
consultaProcedimiento.sinObservaciones=Sin observaciones
consultaProcedimiento.nuevo=Nuevo procedimiento
consultaProcedimiento.editar=Editar procedimiento
consultaProcedimiento.informacion=Informacion del procedimiento
consultaProcedimiento.completar=Completa la informacion del procedimiento.
consultaProcedimiento.observacionesPlaceholder=Agrega observaciones...
consultaProcedimiento.acciones=Acciones
```

- [ ] Step 4: Agregar claves i18n

### Step 5: Agregar nav link

```xml
<h:link outcome="/paginas/ConsultaProcedimiento" styleClass="nav-item">
    <i class="pi pi-directions"></i>
    <span>#{msg['nav.consultaProcedimiento']}</span>
</h:link>
```

`nav.consultaProcedimiento=Procedimientos de consulta`

- [ ] Step 5: Agregar nav link y clave

### Step 6: Verificar compilación

```bash
mvn compile -q
```

- [ ] Step 6: Ejecutar `mvn compile`

---

## Task 3: ConsultaProcedimientoPaso (Alta)

**Files:**
- Create: `model/ConsultaProcedimientoPasoModel.java`
- Create: `paginas/ConsultaProcedimientoPaso.xhtml`
- Create: `estilos/consulta-procedimiento-paso.css`

**Interfaces:**
- Consumes: `ConsultaProcedimientoPasoDAO`, `ConsultaProcedimientoPaso` entity, `ConsultaProcedimiento` (selector), `PersonaRol` (selector)
- Produces: `ConsultaProcedimientoPasoModel`

### Step 1: Crear ConsultaProcedimientoPasoModel.java

```java
package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;

@Named
@ViewScoped
public class ConsultaProcedimientoPasoModel extends AbstractModel<ConsultaProcedimientoPaso> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ConsultaProcedimientoPaso seleccionado;

    @Inject
    public ConsultaProcedimientoPasoModel(ConsultaProcedimientoPasoDAO dao) {
        super(dao);
    }

    public ConsultaProcedimientoPaso getSeleccionado() { return seleccionado; }
    public void setSeleccionado(ConsultaProcedimientoPaso seleccionado) { this.seleccionado = seleccionado; }

    public void nuevo() {
        seleccionado = new ConsultaProcedimientoPaso();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ConsultaProcedimientoPaso seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) return;
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {}
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
```

- [ ] Step 1: Crear `ConsultaProcedimientoPasoModel.java`

### Step 2: Crear ConsultaProcedimientoPaso.xhtml

- [ ] Step 2: Columnas: idConsultaProcedimiento (selector), idPersonaRol (selector), fechaInicio, fechaFin, estado (inputText o select con opciones). Dialog con campos.

### Step 3: Crear consulta-procedimiento-paso.css

- [ ] Step 3: Copiar estructura de `tipo-examen.css`

### Step 4: Agregar i18n

```properties
consultaProcedimientoPaso.titulo=Pasos de Procedimiento
consultaProcedimientoPaso.subtitulo=Gestiona los pasos de cada procedimiento en consulta.
consultaProcedimientoPaso.lista=Lista de pasos
consultaProcedimientoPaso.consultaProcedimiento=Procedimiento de consulta
consultaProcedimientoPaso.personaRol=Responsable
consultaProcedimientoPaso.fechaInicio=Fecha de inicio
consultaProcedimientoPaso.fechaFin=Fecha de fin
consultaProcedimientoPaso.estado=Estado
consultaProcedimientoPaso.buscar=Buscar
consultaProcedimientoPaso.sinRegistros=No se encontraron pasos
consultaProcedimientoPaso.nuevo=Nuevo paso
consultaProcedimientoPaso.editar=Editar paso
consultaProcedimientoPaso.informacion=Informacion del paso
consultaProcedimientoPaso.completar=Completa la informacion del paso.
consultaProcedimientoPaso.estadoPlaceholder=Ej. PENDIENTE, EN_CURSO, COMPLETADO
consultaProcedimientoPaso.acciones=Acciones
```

- [ ] Step 4: Agregar claves i18n

### Step 5: Agregar nav link

```xml
<h:link outcome="/paginas/ConsultaProcedimientoPaso" styleClass="nav-item">
    <i class="pi pi-step-circle"></i>
    <span>#{msg['nav.consultaProcedimientoPaso']}</span>
</h:link>
```

`nav.consultaProcedimientoPaso=Pasos de procedimiento`

- [ ] Step 5: Agregar nav link y clave

### Step 6: Verificar compilación

```bash
mvn compile -q
```

- [ ] Step 6: Ejecutar `mvn compile`

---

## Task 4: OrdenExamen (Media)

**Files:**
- Create: `model/OrdenExamenModel.java`
- Create: `paginas/OrdenExamen.xhtml`
- Create: `estilos/orden-examen.css`

**Interfaces:**
- Consumes: `OrdenExamenDAO`, `OrdenExamen` entity, `ConsultaProcedimientoPaso` (selector)
- Produces: `OrdenExamenModel`

### Step 1: Crear OrdenExamenModel.java

```java
package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

@Named
@ViewScoped
public class OrdenExamenModel extends AbstractModel<OrdenExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    private OrdenExamen seleccionado;

    @Inject
    public OrdenExamenModel(OrdenExamenDAO dao) {
        super(dao);
    }

    public OrdenExamen getSeleccionado() { return seleccionado; }
    public void setSeleccionado(OrdenExamen seleccionado) { this.seleccionado = seleccionado; }

    public void nuevo() {
        seleccionado = new OrdenExamen();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(OrdenExamen seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) return;
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {}
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
```

- [ ] Step 1: Crear `OrdenExamenModel.java`

### Step 2: Crear OrdenExamen.xhtml

- [ ] Step 2: Columnas: idConsultaProcedimientoPaso (selector), fechaCreacion, indicaciones. Dialog con campos: idConsultaProcedimientoPaso (selectOneMenu), fechaCreacion (p:calendar), indicaciones (inputTextarea).

### Step 3: Crear orden-examen.css

- [ ] Step 3: Copiar estructura de `tipo-examen.css`

### Step 4: Agregar i18n

```properties
ordenExamen.titulo=Ordenes de examen
ordenExamen.subtitulo=Gestiona las ordenes de examen clinico.
ordenExamen.lista=Lista de ordenes
ordenExamen.consultaProcedimientoPaso=Paso de procedimiento
ordenExamen.fechaCreacion=Fecha de creacion
ordenExamen.indicaciones=Indicaciones
ordenExamen.buscar=Buscar
ordenExamen.sinRegistros=No se encontraron ordenes
ordenExamen.sinIndicaciones=Sin indicaciones
ordenExamen.nueva=Nueva orden
ordenExamen.editar=Editar orden
ordenExamen.informacion=Informacion de la orden
ordenExamen.completar=Completa la informacion de la orden.
ordenExamen.indicacionesPlaceholder=Agrega indicaciones del examen...
ordenExamen.acciones=Acciones
```

- [ ] Step 4: Agregar claves i18n

### Step 5: Agregar nav link

```xml
<h:link outcome="/paginas/OrdenExamen" styleClass="nav-item">
    <i class="pi pi-file-edit"></i>
    <span>#{msg['nav.ordenExamen']}</span>
</h:link>
```

`nav.ordenExamen=Ordenes de examen`

- [ ] Step 5: Agregar nav link y clave

### Step 6: Verificar compilación

```bash
mvn compile -q
```

- [ ] Step 6: Ejecutar `mvn compile`

---

## Task 5: ExamenResultado (Media)

**Files:**
- Create: `model/ExamenResultadoModel.java`
- Create: `paginas/ExamenResultado.xhtml`
- Create: `estilos/examen-resultado.css`

**Interfaces:**
- Consumes: `ExamenResultadoDAO`, `ExamenResultado` entity, `OrdenExamen` (selector)
- Produces: `ExamenResultadoModel`

### Step 1: Crear ExamenResultadoModel.java

```java
package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenResultadoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenResultado;

@Named
@ViewScoped
public class ExamenResultadoModel extends AbstractModel<ExamenResultado> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ExamenResultado seleccionado;

    @Inject
    public ExamenResultadoModel(ExamenResultadoDAO dao) {
        super(dao);
    }

    public ExamenResultado getSeleccionado() { return seleccionado; }
    public void setSeleccionado(ExamenResultado seleccionado) { this.seleccionado = seleccionado; }

    public void nuevo() {
        seleccionado = new ExamenResultado();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ExamenResultado seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) return;
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {}
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
```

- [ ] Step 1: Crear `ExamenResultadoModel.java`

### Step 2: Crear ExamenResultado.xhtml

- [ ] Step 2: Columnas: idOrdenExamen (selector), fechaCreacion, resultado, interpretacion, rutaAtestado. Dialog con campos.

### Step 3: Crear examen-resultado.css

- [ ] Step 3: Copiar estructura de `tipo-examen.css`

### Step 4: Agregar i18n

```properties
examenResultado.titulo=Resultados de examen
examenResultado.subtitulo=Gestiona los resultados de examenes clinicos.
examenResultado.lista=Lista de resultados
examenResultado.ordenExamen=Orden de examen
examenResultado.fechaCreacion=Fecha de creacion
examenResultado.resultado=Resultado
examenResultado.interpretacion=Interpretacion
examenResultado.rutaAtestado=Ruta del atestado
examenResultado.buscar=Buscar
examenResultado.sinRegistros=No se encontraron resultados
examenResultado.sinResultado=Sin resultado
examenResultado.nuevo=Nuevo resultado
examenResultado.editar=Editar resultado
examenResultado.informacion=Informacion del resultado
examenResultado.completar=Completa la informacion del resultado.
examenResultado.resultadoPlaceholder=Agrega el resultado del examen...
examenResultado.interpretacionPlaceholder=Agrega la interpretacion...
examenResultado.rutaAtestadoPlaceholder=Ruta del archivo atestado...
examenResultado.acciones=Acciones
```

- [ ] Step 4: Agregar claves i18n

### Step 5: Agregar nav link

```xml
<h:link outcome="/paginas/ExamenResultado" styleClass="nav-item">
    <i class="pi pi-check-circle"></i>
    <span>#{msg['nav.examenResultado']}</span>
</h:link>
```

`nav.examenResultado=Resultados de examen`

- [ ] Step 5: Agregar nav link y clave

### Step 6: Verificar compilación

```bash
mvn compile -q
```

- [ ] Step 6: Ejecutar `mvn compile`

---

## Task 6: ExamenTipoExamen (Alta — entidad puente con datos)

**Files:**
- Create: `model/ExamenTipoExamenModel.java`
- Create: `paginas/ExamenTipoExamen.xhtml`
- Create: `estilos/examen-tipo-examen.css`

**Interfaces:**
- Consumes: `ExamenTipoExamenDAO` (con consultas custom: `findByIdExamen`), `ExamenTipoExamen` entity, `Examen` (selector), `TipoExamen` (selector)
- Produces: `ExamenTipoExamenModel`

**Nota:** Esta pantalla es diferente a las demás porque maneja una entidad puente entre Examen y TipoExamen. El DAO tiene consultas custom (`findByIdExamen`, `countByIdExamen`, `countByIdExamenAndIdTipoExamen`). El bean puede necesitar métodos adicionales para buscar por Examen.

### Step 1: Crear ExamenTipoExamenModel.java

```java
package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenTipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;

@Named
@ViewScoped
public class ExamenTipoExamenModel extends AbstractModel<ExamenTipoExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ExamenTipoExamen seleccionado;

    @Inject
    public ExamenTipoExamenModel(ExamenTipoExamenDAO dao) {
        super(dao);
    }

    public ExamenTipoExamen getSeleccionado() { return seleccionado; }
    public void setSeleccionado(ExamenTipoExamen seleccionado) { this.seleccionado = seleccionado; }

    public void nuevo() {
        seleccionado = new ExamenTipoExamen();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ExamenTipoExamen seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) return;
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {}
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
```

- [ ] Step 1: Crear `ExamenTipoExamenModel.java`

### Step 2: Crear ExamenTipoExamen.xhtml

- [ ] Step 2: Columnas: idExamen (selector), idTipoExamen (selector), fechaCreacion, observaciones. Dialog con campos.

### Step 3: Crear examen-tipo-examen.css

- [ ] Step 3: Copiar estructura de `tipo-examen.css`

### Step 4: Agregar i18n

```properties
examenTipoExamen.titulo=Examen - Tipos de examen
examenTipoExamen.subtitulo=Asocia tipos de examen a cada examen clinico.
examenTipoExamen.lista=Lista de asociaciones
examenTipoExamen.examen=Examen
examenTipoExamen.tipoExamen=Tipo de examen
examenTipoExamen.fechaCreacion=Fecha de creacion
examenTipoExamen.observaciones=Observaciones
examenTipoExamen.buscar=Buscar
examenTipoExamen.sinRegistros=No se encontraron asociaciones
examenTipoExamen.sinObservaciones=Sin observaciones
examenTipoExamen.nueva=Nueva asociacion
examenTipoExamen.editar=Editar asociacion
examenTipoExamen.informacion=Informacion de la asociacion
examenTipoExamen.completar=Completa la informacion de la asociacion.
examenTipoExamen.observacionesPlaceholder=Agrega observaciones...
examenTipoExamen.acciones=Acciones
```

- [ ] Step 4: Agregar claves i18n

### Step 5: Agregar nav link

```xml
<h:link outcome="/paginas/ExamenTipoExamen" styleClass="nav-item">
    <i class="pi pi-link"></i>
    <span>#{msg['nav.examenTipoExamen']}</span>
</h:link>
```

`nav.examenTipoExamen=Examen - Tipos`

- [ ] Step 5: Agregar nav link y clave

### Step 6: Verificar compilación

```bash
mvn compile -q
```

- [ ] Step 6: Ejecutar `mvn compile`

---

## Task 7: i18n Completo (4 idiomas)

**Files:**
- Modify: `src/main/resources/i18n/messages_en.properties`
- Modify: `src/main/resources/i18n/messages_de.properties`
- Modify: `src/main/resources/i18n/messages_zh_CN.properties`

### Step 1: Traducciones messages_en.properties

Agregar las claves traducidas al inglés para todas las 6 pantallas (consulta.*, consultaProcedimiento.*, etc.)

- [ ] Step 1: Agregar claves en inglés

### Step 2: Traducciones messages_de.properties

Agregar las claves traducidas al alemán.

- [ ] Step 2: Agregar claves en alemán

### Step 3: Traducciones messages_zh_CN.properties

Agregar las claves traducidas al chino simplificado.

- [ ] Step 3: Agregar claves en chino

### Step 4: Verificar compilación

```bash
mvn compile -q
```

- [ ] Step 4: Ejecutar `mvn compile`

---

## Task 8: Verificación Final

### Step 1: Compilación completa

```bash
mvn clean compile
```

- [ ] Step 1: Ejecutar `mvn clean compile` y verificar 0 errores

### Step 2: Verificar que todas las pantallas están en general.xhtml

- [ ] Step 2: Verificar que los 6 nav links aparecen en el sidebar de `general.xhtml`

### Step 3: Verificar que todos los XHTMLs compilan

```bash
mvn clean package -DskipTests
```

- [ ] Step 3: Ejecutar `mvn clean package` y verificar que el WAR se genera correctamente

### Step 4: Verificar i18n completo

- [ ] Step 4: Verificar que todas las claves están en los 4 archivos .properties

---

## Resumen de Entregables

| # | Pantalla | Model | XHTML | CSS | i18n | Nav |
|---|----------|-------|-------|-----|------|-----|
| 1 | Consulta | ConsultaModel | Consulta.xhtml | consulta.css | ✅ | ✅ |
| 2 | ConsultaProcedimiento | ConsultaProcedimientoModel | ConsultaProcedimiento.xhtml | consulta-procedimiento.css | ✅ | ✅ |
| 3 | ConsultaProcedimientoPaso | ConsultaProcedimientoPasoModel | ConsultaProcedimientoPaso.xhtml | consulta-procedimiento-paso.css | ✅ | ✅ |
| 4 | OrdenExamen | OrdenExamenModel | OrdenExamen.xhtml | orden-examen.css | ✅ | ✅ |
| 5 | ExamenResultado | ExamenResultadoModel | ExamenResultado.xhtml | examen-resultado.css | ✅ | ✅ |
| 6 | ExamenTipoExamen | ExamenTipoExamenModel | ExamenTipoExamen.xhtml | examen-tipo-examen.css | ✅ | ✅ |

**Total archivos a crear:** 18 (6 Model + 6 XHTML + 6 CSS)
**Total archivos a modificar:** 5 (4 i18n + 1 general.xhtml)
