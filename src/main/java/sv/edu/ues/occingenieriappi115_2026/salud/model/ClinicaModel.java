package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;

/**
 * Backing bean JSF del catálogo de clínicas.
 *
 * <p>Valida el nombre antes de persistir y bloquea la eliminación física
 * cuando personas están asignadas a la clínica; en ese caso se desactiva
 * desde el formulario.</p>
 */
@Named
@ViewScoped
public class ClinicaModel extends AbstractModel<Clinica> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ClinicaDAO clinicaDAO;
    @EJB
    private PersonaRolDAO personaRolDAO;
    @Inject
    transient FacesContext facesContext;

    private Clinica seleccionado;

    public ClinicaModel() {
    }

    public ClinicaModel(ClinicaDAO clinicaDAO, PersonaRolDAO personaRolDAO) {
        this.clinicaDAO = clinicaDAO;
        this.personaRolDAO = personaRolDAO;
    }

    @Override
    protected ClinicaDAO getDao() {
        return clinicaDAO;
    }

    public Clinica getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Clinica seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new Clinica();
        seleccionado.setActivo(Boolean.TRUE);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Clinica seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) {
            return;
        }
        seleccionado.setNombre(normalizar(seleccionado.getNombre()));
        seleccionado.setTipo(normalizar(seleccionado.getTipo()));
        seleccionado.setComentarios(normalizar(seleccionado.getComentarios()));
        if (!validarAntesDeGuardar()) {
            return;
        }
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> { }
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Elimina físicamente la clínica solo cuando ninguna asignación de
     * persona la referencia; en caso contrario responde con un error de
     * negocio y el registro se desactiva desde el formulario.
     */
    public void eliminar() {
        if (seleccionado == null || seleccionado.getIdClinica() == null) {
            return;
        }
        if (tieneReferencias()) {
            agregarError("clinica.eliminacionBloqueada");
            return;
        }
        try {
            if (getDao().eliminar(seleccionado.getIdClinica())) {
                cancelar();
                agregarMensaje("clinica.eliminado", FacesMessage.SEVERITY_INFO);
            } else {
                agregarError("clinica.errorEliminar");
            }
        } catch (RuntimeException ex) {
            agregarError("clinica.errorEliminar");
        }
    }

    private boolean tieneReferencias() {
        UUID id = seleccionado.getIdClinica();
        return personaRolDAO.obtenerTodos().stream()
                .anyMatch(asignacion -> asignacion.getIdClinica() != null
                && id.equals(asignacion.getIdClinica().getIdClinica()));
    }

    private boolean validarAntesDeGuardar() {
        String nombre = seleccionado.getNombre();
        if (nombre == null || nombre.isEmpty()) {
            agregarError("clinica.nombreRequerido");
            return false;
        }
        if (nombre.length() < 2) {
            agregarError("clinica.nombreMinimo");
            return false;
        }
        if (nombre.length() > 255) {
            agregarError("clinica.nombreMaximo");
            return false;
        }
        return true;
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
