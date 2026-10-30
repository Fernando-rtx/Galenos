package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import sv.edu.ues.occingenieriappi115_2026.salud.control.CatalogoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
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
    private CatalogoService catalogoService;

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
        this.catalogoService = new CatalogoService(clinicaDAO, personaRolDAO);
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

    public void seleccionarFila() {
        if (seleccionado != null) {
            setEstado(ESTADO_CRUD.EDICION);
        }
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) return;
        boolean edicion = getEstado() == ESTADO_CRUD.EDICION;
        try {
            var guardado = catalogoService.guardarClinica(seleccionado, edicion);
            if (edicion) seleccionado = guardado;
        } catch (ReglaNegocioException ex) {
            agregarError(ex.getMessage());
            return;
        } catch (RuntimeException ex) {
            if (facesContext == null) throw ex;
            agregarError("clinica.errorGuardar");
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
        agregarMensaje("clinica." + (edicion ? "actualizadoCorrectamente" : "creadoCorrectamente"), FacesMessage.SEVERITY_INFO);
    }

    public void cancelar() {
        volverAlListado();
        agregarMensaje("clinica.cancelado", FacesMessage.SEVERITY_INFO);
    }

    private void volverAlListado() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Elimina físicamente la clínica solo cuando ninguna asignación de
     * persona la referencia; en caso contrario responde con un error de
     * negocio y el registro se desactiva desde el formulario.
     */
    public void eliminar() {
        if (seleccionado == null || seleccionado.getIdClinica() == null) return;
        try {
            catalogoService.eliminarClinica(seleccionado.getIdClinica());
            volverAlListado();
            agregarMensaje("clinica.eliminado", FacesMessage.SEVERITY_INFO);
        } catch (ReglaNegocioException ex) {
            agregarError(ex.getMessage());
        } catch (RuntimeException ex) {
            agregarError("clinica.errorEliminar");
        }
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
