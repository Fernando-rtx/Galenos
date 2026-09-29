package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/**
 * Backing bean JSF del catálogo de roles.
 *
 * <p>Valida el nombre antes de persistir y bloquea la eliminación física
 * cuando el rol está asignado a personas o usado por pasos de procedimiento;
 * en ese caso se desactiva desde el formulario.</p>
 */
@Named
@ViewScoped
public class RolModel extends AbstractModel<Rol> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RolDAO rolDAO;
    @EJB
    private PersonaRolDAO personaRolDAO;
    @EJB
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    @Inject
    transient FacesContext facesContext;

    private Rol seleccionado;

    public RolModel() {
    }

    public RolModel(RolDAO rolDAO, PersonaRolDAO personaRolDAO,
            ProcedimientoPasoDAO procedimientoPasoDAO) {
        this.rolDAO = rolDAO;
        this.personaRolDAO = personaRolDAO;
        this.procedimientoPasoDAO = procedimientoPasoDAO;
    }

    @Override
    protected RolDAO getDao() {
        return rolDAO;
    }

    public Rol getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Rol seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new Rol();
        seleccionado.setActivo(Boolean.TRUE);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Rol seleccionado) {
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
                    agregarMensaje("rol.creadoCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case EDICION -> {
                    seleccionado = getDao().actualizar(seleccionado);
                    agregarMensaje("rol.actualizadoCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case LISTADO -> { }
            }
        } catch (RuntimeException ex) {
            if (facesContext == null) {
                throw ex;
            }
            agregarMensaje("rol.errorGuardar", FacesMessage.SEVERITY_ERROR);
            facesContext.validationFailed();
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        volverAlListado();
        agregarMensaje("rol.cancelado", FacesMessage.SEVERITY_INFO);
    }

    private void volverAlListado() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Elimina físicamente el rol solo cuando ninguna asignación de persona
     * ni ningún paso lo referencia; en caso contrario responde con un error
     * de negocio y el registro se desactiva desde el formulario.
     */
    public void eliminar() {
        if (seleccionado == null || seleccionado.getIdRol() == null) {
            return;
        }
        if (tieneReferencias()) {
            agregarError("rol.eliminacionBloqueada");
            return;
        }
        try {
            if (getDao().eliminar(seleccionado.getIdRol())) {
                volverAlListado();
                agregarMensaje("rol.eliminado", FacesMessage.SEVERITY_INFO);
            } else {
                agregarError("rol.errorEliminar");
            }
        } catch (RuntimeException ex) {
            agregarError("rol.errorEliminar");
        }
    }

    private boolean tieneReferencias() {
        UUID id = seleccionado.getIdRol();
        boolean asignado = personaRolDAO.obtenerTodos().stream()
                .anyMatch(asignacion -> asignacion.getIdRol() != null
                && id.equals(asignacion.getIdRol().getIdRol()));
        if (asignado) {
            return true;
        }
        return procedimientoPasoDAO.obtenerTodos().stream()
                .anyMatch(paso -> paso.getIdRol() != null
                && id.equals(paso.getIdRol().getIdRol()));
    }

    private boolean validarAntesDeGuardar() {
        String nombre = seleccionado.getNombre();
        if (nombre == null || nombre.isEmpty()) {
            agregarError("rol.nombreRequerido");
            return false;
        }
        if (nombre.length() < 2) {
            agregarError("rol.nombreMinimo");
            return false;
        }
        if (nombre.length() > 155) {
            agregarError("rol.nombreMaximo");
            return false;
        }
        if (tieneNombreDuplicado()) {
            agregarError("rol.nombreDuplicado");
            return false;
        }
        return true;
    }

    private boolean tieneNombreDuplicado() {
        UUID idPropio = seleccionado.getIdRol();
        return getDao().obtenerTodos().stream()
                .filter(existente -> existente != null && existente != seleccionado)
                .filter(existente -> idPropio == null
                        || !idPropio.equals(existente.getIdRol()))
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
