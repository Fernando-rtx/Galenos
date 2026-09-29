package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/**
 * Backing bean JSF del catálogo de pasos de procedimiento.
 */
@Named
@ViewScoped
public class ProcedimientoPasoModel extends AbstractModel<ProcedimientoPaso> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    @EJB
    private ProcedimientoDAO procedimientoDAO;
    @EJB
    private RolDAO rolDAO;
    @EJB
    private ProcedimientoPasoSecuenciaDAO secuenciaDAO;
    @EJB
    private ProcedimientoPasoExamenDAO procedimientoPasoExamenDAO;
    @Inject
    transient FacesContext facesContext;
    private ProcedimientoPaso seleccionado;

    private List<Procedimiento> procedimientos;
    private List<Rol> roles;

    public ProcedimientoPasoModel() {
    }

    public ProcedimientoPasoModel(ProcedimientoPasoDAO procedimientoPasoDAO,
            ProcedimientoDAO procedimientoDAO, RolDAO rolDAO) {
        this.procedimientoPasoDAO = procedimientoPasoDAO;
        this.procedimientoDAO = procedimientoDAO;
        this.rolDAO = rolDAO;
        inicializar();
    }

    public ProcedimientoPasoModel(ProcedimientoPasoDAO procedimientoPasoDAO,
            ProcedimientoDAO procedimientoDAO, RolDAO rolDAO,
            ProcedimientoPasoSecuenciaDAO secuenciaDAO,
            ProcedimientoPasoExamenDAO procedimientoPasoExamenDAO) {
        this(procedimientoPasoDAO, procedimientoDAO, rolDAO);
        this.secuenciaDAO = secuenciaDAO;
        this.procedimientoPasoExamenDAO = procedimientoPasoExamenDAO;
    }

    @PostConstruct
    public void inicializar() {
        this.procedimientos = procedimientoDAO.obtenerTodos();
        this.roles = rolDAO.obtenerTodos();
    }

    @Override
    protected ProcedimientoPasoDAO getDao() {
        return procedimientoPasoDAO;
    }

    public ProcedimientoPaso getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ProcedimientoPaso seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<Procedimiento> getProcedimientos() {
        return procedimientos;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void nuevo() {
        seleccionado = new ProcedimientoPaso();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ProcedimientoPaso seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) {
            return;
        }
        seleccionado.setNombre(normalizar(seleccionado.getNombre()));
        if (!validarAntesDeGuardar()) {
            return;
        }
        try {
            switch (getEstado()) {
                case CREACION -> {
                    getDao().guardar(seleccionado);
                    agregarMensaje("procedimientoPaso.creadoCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case EDICION -> {
                    seleccionado = getDao().actualizar(seleccionado);
                    agregarMensaje("procedimientoPaso.actualizadoCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case LISTADO -> { }
            }
        } catch (RuntimeException ex) {
            if (facesContext == null) {
                throw ex;
            }
            agregarMensaje("procedimientoPaso.errorGuardar", FacesMessage.SEVERITY_ERROR);
            facesContext.validationFailed();
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        volverAlListado();
        agregarMensaje("procedimientoPaso.cancelado", FacesMessage.SEVERITY_INFO);
    }

    private void volverAlListado() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Elimina físicamente el paso seleccionado solo cuando ninguna secuencia
     * ni ninguna relación paso-examen lo referencia; en caso contrario
     * responde con un error de negocio y el registro se desactiva desde el
     * formulario.
     */
    public void eliminar() {
        if (seleccionado == null || seleccionado.getIdProcedimientoPaso() == null) {
            return;
        }
        if (tieneReferencias()) {
            agregarError("procedimientoPaso.eliminacionBloqueada");
            return;
        }
        try {
            if (getDao().eliminar(seleccionado.getIdProcedimientoPaso())) {
                volverAlListado();
                agregarMensaje("procedimientoPaso.eliminado", FacesMessage.SEVERITY_INFO);
            } else {
                agregarError("procedimientoPaso.errorEliminar");
            }
        } catch (RuntimeException ex) {
            agregarError("procedimientoPaso.errorEliminar");
        }
    }

    private boolean tieneReferencias() {
        UUID id = seleccionado.getIdProcedimientoPaso();
        boolean enSecuencia = secuenciaDAO.obtenerTodos().stream()
                .anyMatch(secuencia -> secuencia != null
                && (secuencia.getIdProcedimientoPaso() != null
                && id.equals(secuencia.getIdProcedimientoPaso().getIdProcedimientoPaso())
                || secuencia.getIdProcedimientoPasoReferencia() != null
                && id.equals(secuencia.getIdProcedimientoPasoReferencia())));
        if (enSecuencia) {
            return true;
        }
        return procedimientoPasoExamenDAO.obtenerTodos().stream()
                .anyMatch(relacion -> relacion != null
                && relacion.getIdProcedimientoPaso() != null
                && id.equals(relacion.getIdProcedimientoPaso().getIdProcedimientoPaso()));
    }

    private boolean validarAntesDeGuardar() {
        String nombre = seleccionado.getNombre();
        if (nombre == null || nombre.isEmpty()) {
            agregarError("procedimientoPaso.nombreRequerido");
            return false;
        }
        if (nombre.length() < 2) {
            agregarError("procedimientoPaso.nombreMinimo");
            return false;
        }
        if (nombre.length() > 155) {
            agregarError("procedimientoPaso.nombreMaximo");
            return false;
        }
        if (!procedimientoValido()) {
            return false;
        }
        if (tieneNombreDuplicado()) {
            agregarError("procedimientoPaso.nombreDuplicado");
            return false;
        }
        return true;
    }

    private boolean procedimientoValido() {
        Procedimiento procedimiento = seleccionado.getIdProcedimiento();
        if (procedimiento == null || procedimiento.getIdProcedimiento() == null) {
            agregarError("procedimientoPaso.procedimientoRequerido");
            return false;
        }
        Procedimiento existente
                = procedimientoDAO.buscarPorId(procedimiento.getIdProcedimiento());
        if (existente == null) {
            agregarError("procedimientoPaso.procedimientoNoExiste");
            return false;
        }
        if (Boolean.FALSE.equals(existente.getActivo())) {
            agregarError("procedimientoPaso.procedimientoInactivo");
            return false;
        }
        return true;
    }

    private boolean tieneNombreDuplicado() {
        UUID idProcedimiento = seleccionado.getIdProcedimiento().getIdProcedimiento();
        UUID idPropio = seleccionado.getIdProcedimientoPaso();
        return getDao().obtenerTodos().stream()
                .filter(existente -> existente != null && existente != seleccionado)
                .filter(existente -> idPropio == null
                        || !idPropio.equals(existente.getIdProcedimientoPaso()))
                .filter(existente -> existente.getIdProcedimiento() != null
                        && idProcedimiento.equals(
                                existente.getIdProcedimiento().getIdProcedimiento()))
                .anyMatch(existente -> seleccionado.getNombre() != null
                        && existente.getNombre() != null
                        && seleccionado.getNombre().trim()
                                .equalsIgnoreCase(existente.getNombre().trim()));
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
