package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

/**
 * Backing bean JSF del catálogo de procedimientos.
 *
 * <p>Valida el nombre antes de persistir y controla la eliminación física:
 * los procedimientos referenciados por pasos o consultas no se eliminan,
 * solo se desactivan desde el formulario.</p>
 */
@Named
@ViewScoped
public class ProcedimientoModel extends AbstractModel<Procedimiento> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProcedimientoDAO procedimientoDAO;
    @EJB
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    @EJB
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @Inject
    transient FacesContext facesContext;

    private Procedimiento seleccionado;

    public ProcedimientoModel() {
    }

    public ProcedimientoModel(ProcedimientoDAO procedimientoDAO,
            ProcedimientoPasoDAO procedimientoPasoDAO,
            ConsultaProcedimientoDAO consultaProcedimientoDAO) {
        this.procedimientoDAO = procedimientoDAO;
        this.procedimientoPasoDAO = procedimientoPasoDAO;
        this.consultaProcedimientoDAO = consultaProcedimientoDAO;
    }

    @Override
    protected ProcedimientoDAO getDao() {
        return procedimientoDAO;
    }

    public Procedimiento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Procedimiento seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new Procedimiento();
        seleccionado.setActivo(Boolean.TRUE);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Procedimiento seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) {
            return;
        }
        seleccionado.setNombre(normalizar(seleccionado.getNombre()));
        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
        if (!validarAntesDeGuardar()) {
            return;
        }
        try {
            switch (getEstado()) {
                case CREACION -> {
                    getDao().guardar(seleccionado);
                    agregarMensaje("procedimiento.creadoCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case EDICION -> {
                    seleccionado = getDao().actualizar(seleccionado);
                    agregarMensaje("procedimiento.actualizadoCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case LISTADO -> { }
            }
        } catch (RuntimeException ex) {
            if (facesContext == null) {
                throw ex;
            }
            agregarMensaje("procedimiento.errorGuardar", FacesMessage.SEVERITY_ERROR);
            facesContext.validationFailed();
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        volverAlListado();
        agregarMensaje("procedimiento.cancelado", FacesMessage.SEVERITY_INFO);
    }

    private void volverAlListado() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Elimina físicamente el procedimiento seleccionado solo cuando ningún
     * paso ni consulta lo referencia; en caso contrario responde con un
     * error de negocio y el registro se desactiva desde el formulario.
     */
    public void eliminar() {
        if (seleccionado == null || seleccionado.getIdProcedimiento() == null) {
            return;
        }
        if (tieneReferencias()) {
            agregarError("procedimiento.eliminacionBloqueada");
            return;
        }
        try {
            if (getDao().eliminar(seleccionado.getIdProcedimiento())) {
                volverAlListado();
                agregarMensaje("procedimiento.eliminado", FacesMessage.SEVERITY_INFO);
            } else {
                agregarError("procedimiento.errorEliminar");
            }
        } catch (RuntimeException ex) {
            agregarError("procedimiento.errorEliminar");
        }
    }

    private boolean tieneReferencias() {
        UUID id = seleccionado.getIdProcedimiento();
        boolean referenciadoPorPaso = procedimientoPasoDAO.obtenerTodos().stream()
                .anyMatch(paso -> paso.getIdProcedimiento() != null
                && id.equals(paso.getIdProcedimiento().getIdProcedimiento()));
        if (referenciadoPorPaso) {
            return true;
        }
        return consultaProcedimientoDAO.obtenerTodos().stream()
                .anyMatch(consulta -> id.equals(consulta.getIdProcedimiento()));
    }

    private boolean validarAntesDeGuardar() {
        String nombre = seleccionado.getNombre();
        if (nombre == null || nombre.isEmpty()) {
            agregarError("procedimiento.nombreRequerido");
            return false;
        }
        if (nombre.length() < 2) {
            agregarError("procedimiento.nombreMinimo");
            return false;
        }
        if (nombre.length() > 155) {
            agregarError("procedimiento.nombreMaximo");
            return false;
        }
        if (tieneNombreDuplicado()) {
            agregarError("procedimiento.nombreDuplicado");
            return false;
        }
        return true;
    }

    private boolean tieneNombreDuplicado() {
        UUID idPropio = seleccionado.getIdProcedimiento();
        return getDao().obtenerTodos().stream()
                .filter(existente -> existente != null && existente != seleccionado)
                .filter(existente -> idPropio == null
                        || !idPropio.equals(existente.getIdProcedimiento()))
                .anyMatch(existente -> nombreEquals(existente));
    }

    private boolean nombreEquals(Procedimiento existente) {
        return seleccionado.getNombre() != null
                && existente != null
                && existente.getNombre() != null
                && seleccionado.getNombre().trim()
                        .equalsIgnoreCase(existente.getNombre().trim());
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
