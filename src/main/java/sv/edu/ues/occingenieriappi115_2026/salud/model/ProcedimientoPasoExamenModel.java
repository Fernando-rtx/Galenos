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
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
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

    private List<ProcedimientoPaso> procedimientosPaso;
    private List<Examen> examenes;

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
        this.examenes = examenDAO.obtenerTodos();
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
        return examenes;
    }

    public void nuevo() {
        seleccionado = new ProcedimientoPasoExamen();
        seleccionado.setFechaCreacion(new Date());
        seleccionado.setActivo(true);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ProcedimientoPasoExamen seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
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
        if (procedimientoPasoDAO.buscarPorId(paso.getIdProcedimientoPaso()) == null) {
            agregarError("ppe.pasoNoExiste");
            return false;
        }
        Examen examenPersistido = examenDAO.buscarPorId(examen.getIdExamen());
        if (examenPersistido == null) {
            agregarError("ppe.examenNoExiste");
            return false;
        }
        if (Boolean.FALSE.equals(examenPersistido.getActivo())) {
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
        List<ProcedimientoPasoExamen> relaciones = getDao().obtenerTodos();
        return relaciones == null ? List.of() : relaciones;
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
