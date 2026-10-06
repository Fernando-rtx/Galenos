package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

/**
 * Backing bean JSF del catálogo de relaciones procedimiento-paso-examen.
 *
 * <p>Antes de persistir verifica que paso y examen existan en catálogo y
 * que la asociación no esté duplicada.</p>
 */
@Named
@ViewScoped
public class ProcedimientoPasoExamenModel extends AbstractModel<ProcedimientoPasoExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProcedimientoPasoExamenDAO procedimientoPasoExamenDAO;
    @EJB
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    @EJB
    private ExamenDAO examenDAO;
    @Inject
    transient FacesContext facesContext;
    private ProcedimientoPasoExamen seleccionado;
    private UUID pasoOriginalId;
    private UUID examenOriginalId;

    private List<ProcedimientoPaso> procedimientosPaso;
    private List<Examen> examenes;
    private Procedimiento procedimientoSeleccionado;
    private List<ProcedimientoPasoExamen> relacionesCache;

    public ProcedimientoPasoExamenModel() {
    }

    public ProcedimientoPasoExamenModel(ProcedimientoPasoExamenDAO procedimientoPasoExamenDAO,
            ProcedimientoPasoDAO procedimientoPasoDAO, ExamenDAO examenDAO) {
        this.procedimientoPasoExamenDAO = procedimientoPasoExamenDAO;
        this.procedimientoPasoDAO = procedimientoPasoDAO;
        this.examenDAO = examenDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.procedimientosPaso = procedimientoPasoDAO.obtenerTodos();
        this.examenes = examenDAO.obtenerTodos().stream()
                .filter(examen -> examen != null && Boolean.TRUE.equals(examen.getActivo()))
                .toList();
    }

    @Override
    protected ProcedimientoPasoExamenDAO getDao() {
        return procedimientoPasoExamenDAO;
    }

    public ProcedimientoPasoExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ProcedimientoPasoExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<ProcedimientoPaso> getProcedimientosPaso() {
        return procedimientosPaso;
    }

    public List<Examen> getExamenes() {
        Examen actual = seleccionado == null ? null : seleccionado.getIdExamen();
        if (getEstado() == ESTADO_CRUD.EDICION && actual != null && !examenes.contains(actual)) {
            List<Examen> disponibles = new java.util.ArrayList<>(examenes);
            disponibles.add(actual);
            return disponibles;
        }
        return examenes;
    }

    public List<Procedimiento> getProcedimientos() {
        if (procedimientosPaso == null) {
            return List.of();
        }
        return procedimientosPaso.stream()
                .filter(paso -> paso != null && paso.getIdProcedimiento() != null
                && paso.getIdProcedimiento().getIdProcedimiento() != null)
                .map(ProcedimientoPaso::getIdProcedimiento)
                .filter(procedimiento -> Boolean.TRUE.equals(procedimiento.getActivo())
                || (getEstado() == ESTADO_CRUD.EDICION && procedimientoSeleccionado != null
                && procedimiento.getIdProcedimiento().equals(procedimientoSeleccionado.getIdProcedimiento())))
                .collect(java.util.stream.Collectors.toMap(
                        Procedimiento::getIdProcedimiento,
                        procedimiento -> procedimiento,
                        (primero, ignorado) -> primero,
                        java.util.LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(Procedimiento::getNombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public Procedimiento getProcedimientoSeleccionado() {
        return procedimientoSeleccionado;
    }

    public void setProcedimientoSeleccionado(Procedimiento procedimientoSeleccionado) {
        this.procedimientoSeleccionado = procedimientoSeleccionado;
    }

    public List<ProcedimientoPaso> getPasosDelProcedimiento() {
        if (procedimientoSeleccionado == null
                || procedimientoSeleccionado.getIdProcedimiento() == null
                || procedimientosPaso == null) {
            return List.of();
        }
        UUID idProcedimiento = procedimientoSeleccionado.getIdProcedimiento();
        return procedimientosPaso.stream()
                .filter(paso -> paso != null && paso.getIdProcedimiento() != null
                && idProcedimiento.equals(paso.getIdProcedimiento().getIdProcedimiento()))
                .toList();
    }

    public boolean isEstructuraEditable() {
        return getEstado() != ESTADO_CRUD.EDICION;
    }

    public void procedimientoCambio() {
        if (getEstado() == ESTADO_CRUD.CREACION) {
            seleccionado.setIdProcedimientoPaso(null);
        }
    }

    public List<ProcedimientoPasoExamen> getExamenesPorProcedimiento(Procedimiento procedimiento) {
        if (procedimiento == null || procedimiento.getIdProcedimiento() == null) {
            return List.of();
        }
        UUID idProcedimiento = procedimiento.getIdProcedimiento();
        return obtenerRelaciones().stream()
                .filter(relacion -> relacion != null
                && relacion.getIdProcedimientoPaso() != null
                && relacion.getIdProcedimientoPaso().getIdProcedimiento() != null)
                .filter(relacion -> idProcedimiento.equals(relacion
                .getIdProcedimientoPaso()
                .getIdProcedimiento()
                .getIdProcedimiento()))
                .toList();
    }

    public List<ProcedimientoPasoExamen> getExamenesPorPaso(ProcedimientoPaso paso) {
        if (paso == null || paso.getIdProcedimientoPaso() == null) {
            return List.of();
        }
        UUID idPaso = paso.getIdProcedimientoPaso();
        return obtenerRelaciones().stream()
                .filter(relacion -> relacion != null && relacion.getIdProcedimientoPaso() != null
                && idPaso.equals(relacion.getIdProcedimientoPaso().getIdProcedimientoPaso()))
                .sorted(Comparator.comparing(relacion -> relacion.getIdExamen() == null
                        ? null : relacion.getIdExamen().getNombre(),
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public void invalidarCache() {
        relacionesCache = null;
    }

    public void nuevo() {
        inicializar();
        seleccionado = new ProcedimientoPasoExamen();
        procedimientoSeleccionado = null;
        seleccionado.setFechaCreacion(new Date());
        seleccionado.setActivo(true);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ProcedimientoPasoExamen seleccionado) {
        this.seleccionado = seleccionado;
        prepararEdicion();
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void seleccionarFila() {
        if (seleccionado != null) {
            prepararEdicion();
            setEstado(ESTADO_CRUD.EDICION);
        }
    }

    private void prepararEdicion() {
        pasoOriginalId = seleccionado == null || seleccionado.getIdProcedimientoPaso() == null
                ? null : seleccionado.getIdProcedimientoPaso().getIdProcedimientoPaso();
        examenOriginalId = seleccionado == null || seleccionado.getIdExamen() == null
                ? null : seleccionado.getIdExamen().getIdExamen();
        procedimientoSeleccionado = seleccionado == null
                || seleccionado.getIdProcedimientoPaso() == null
                ? null : seleccionado.getIdProcedimientoPaso().getIdProcedimiento();
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) {
            return;
        }
        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
        if (!validarAntesDeGuardar()) {
            return;
        }
        try {
            switch (getEstado()) {
                case CREACION -> {
                    getDao().guardar(seleccionado);
                    agregarMensaje("ppe.creadaCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case EDICION -> {
                    seleccionado = getDao().actualizar(seleccionado);
                    agregarMensaje("ppe.actualizadaCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case LISTADO -> { }
            }
        } catch (RuntimeException ex) {
            if (facesContext == null) {
                throw ex;
            }
            agregarMensaje("ppe.errorGuardar", FacesMessage.SEVERITY_ERROR);
            facesContext.validationFailed();
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
        relacionesCache = null;
    }

    public void cancelar() {
        volverAlListado();
        agregarMensaje("ppe.cancelado", FacesMessage.SEVERITY_INFO);
    }

    private void volverAlListado() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Quita la asociación paso-examen sin borrar ni el paso ni el examen:
     * solo elimina la fila de la tabla de unión.
     *
     * @param fila relación seleccionada en la tabla
     */
    public void quitar(ProcedimientoPasoExamen fila) {
        if (fila == null || fila.getIdProcedimientoPasoExamen() == null) {
            agregarError("ppe.errorQuitar");
            return;
        }
        try {
            ProcedimientoPasoExamen persistida
                    = getDao().buscarPorId(fila.getIdProcedimientoPasoExamen());
            if (persistida == null
                    || !getDao().eliminar(persistida.getIdProcedimientoPasoExamen())) {
                agregarError("ppe.errorQuitar");
                return;
            }
        } catch (RuntimeException ex) {
            agregarError("ppe.errorQuitar");
            return;
        }
        if (seleccionado != null && fila.getIdProcedimientoPasoExamen().equals(
                seleccionado.getIdProcedimientoPasoExamen())) {
            volverAlListado();
        }
        agregarMensaje("ppe.quitada", FacesMessage.SEVERITY_INFO);
        relacionesCache = null;
    }

    /**
     * Aplica las reglas de negocio de la relación paso-examen antes de
     * persistir.
     *
     * @return {@code true} si la relación es válida; si no, agrega el error
     * faces y devuelve {@code false}
     */
    private boolean validarAntesDeGuardar() {
        ProcedimientoPaso paso = seleccionado.getIdProcedimientoPaso();
        Examen examen = seleccionado.getIdExamen();
        if (paso == null || paso.getIdProcedimientoPaso() == null) {
            agregarError("ppe.pasoRequerido");
            return false;
        }
        if (examen == null || examen.getIdExamen() == null) {
            agregarError("ppe.examenRequerido");
            return false;
        }
        if (getEstado() == ESTADO_CRUD.EDICION
                && (!java.util.Objects.equals(pasoOriginalId, paso.getIdProcedimientoPaso())
                || !java.util.Objects.equals(examenOriginalId, examen.getIdExamen()))) {
            agregarError("relaciones.noEditables");
            return false;
        }
        if (procedimientoPasoDAO.buscarPorId(paso.getIdProcedimientoPaso()) == null) {
            agregarError("ppe.pasoNoExiste");
            return false;
        }
        if (procedimientoSeleccionado != null
                && (procedimientoSeleccionado.getIdProcedimiento() == null
                || paso.getIdProcedimiento() == null
                || !procedimientoSeleccionado.getIdProcedimiento().equals(
                        paso.getIdProcedimiento().getIdProcedimiento()))) {
            agregarError("ppe.pasoFueraProcedimiento");
            return false;
        }
        Examen examenPersistido = examenDAO.buscarPorId(examen.getIdExamen());
        if (examenPersistido == null) {
            agregarError("ppe.examenNoExiste");
            return false;
        }
        if (getEstado() == ESTADO_CRUD.CREACION && !Boolean.TRUE.equals(examenPersistido.getActivo())) {
            agregarError("ppe.examenInactivo");
            return false;
        }
        for (ProcedimientoPasoExamen existente : obtenerRelaciones()) {
            if (mismoRegistro(existente)
                    || existente.getIdProcedimientoPaso() == null
                    || existente.getIdExamen() == null) {
                continue;
            }
            if (paso.getIdProcedimientoPaso().equals(
                    existente.getIdProcedimientoPaso().getIdProcedimientoPaso())
                    && examen.getIdExamen().equals(existente.getIdExamen().getIdExamen())) {
                agregarError("ppe.asociacionDuplicada");
                return false;
            }
        }
        return true;
    }

    private List<ProcedimientoPasoExamen> obtenerRelaciones() {
        if (relacionesCache == null) {
            List<ProcedimientoPasoExamen> relaciones = getDao().obtenerTodos();
            relacionesCache = relaciones == null ? List.of() : relaciones;
        }
        return relacionesCache;
    }

    private boolean mismoRegistro(ProcedimientoPasoExamen existente) {
        UUID idEditando = seleccionado.getIdProcedimientoPasoExamen();
        return idEditando != null
                && idEditando.equals(existente.getIdProcedimientoPasoExamen());
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private void agregarError(String clave) {
        agregarMensaje(clave, FacesMessage.SEVERITY_ERROR);
        if (facesContext != null) {
            facesContext.validationFailed();
        }
    }

    private void agregarMensaje(String clave, FacesMessage.Severity severidad) {
        FacesContext contexto = facesContext;
        if (contexto == null) {
            return;
        }
        String mensaje = contexto.getApplication()
                .getResourceBundle(contexto, "msg")
                .getString(clave);
        contexto.addMessage(null, new FacesMessage(severidad, mensaje, null));
    }
}
