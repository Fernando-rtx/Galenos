package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

@Named
@ViewScoped
public class PersonaRolModel extends AbstractModel<PersonaRol> implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB
    private PersonaRolDAO personaRolDAO;
    @EJB
    private PersonaDAO personaDAO;
    @EJB
    private RolDAO rolDAO;
    @EJB
    private ClinicaDAO clinicaDAO;
    @Inject
    private transient FacesContext facesContext;
    private PersonaRol seleccionado;
    private Persona personaContexto;
    private List<Persona> personas;
    private List<Rol> roles;
    private List<Clinica> clinicas;

    public PersonaRolModel() {
    }

    public PersonaRolModel(PersonaRolDAO personaRolDAO, PersonaDAO personaDAO,
            RolDAO rolDAO, ClinicaDAO clinicaDAO) {
        this.personaRolDAO = personaRolDAO;
        this.personaDAO = personaDAO;
        this.rolDAO = rolDAO;
        this.clinicaDAO = clinicaDAO;
    }

    @Override
    protected PersonaRolDAO getDao() {
        return personaRolDAO;
    }

    public PersonaRol getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(PersonaRol seleccionado) {
        this.seleccionado = seleccionado;
    }

    public Persona getPersonaContexto() {
        return personaContexto;
    }

    public boolean isConPersonaContexto() {
        return personaContexto != null;
    }

    public String getIdPersonaContexto() {
        return personaContexto == null ? null : personaContexto.getIdPersona().toString();
    }

    public void setIdPersonaContexto(String id) {
        personaContexto = buscarPersona(id);
    }

    public List<Persona> getPersonas() {
        if (personas == null) {
            personas = personaDAO.obtenerTodos();
        }
        return personas;
    }

    public List<Rol> getRoles() {
        if (roles == null) {
            roles = rolDAO.obtenerTodos();
        }
        List<Rol> disponibles = new ArrayList<>();
        for (Rol rol : roles) {
            if (Boolean.TRUE.equals(rol.getActivo())) {
                disponibles.add(rol);
            }
        }
        // Conserva la opción ya asignada al editar una relación antigua.
        Rol actual = seleccionado == null ? null : seleccionado.getIdRol();
        if (getEstado() == ESTADO_CRUD.EDICION && actual != null
                && disponibles.stream().noneMatch(rol -> Objects.equals(rol.getIdRol(), actual.getIdRol()))) {
            disponibles.add(actual);
        }
        return disponibles;
    }

    public List<Clinica> getClinicas() {
        if (clinicas == null) {
            clinicas = clinicaDAO.obtenerTodos();
        }
        return clinicas;
    }

    public String getPersonaSeleccionadaId() {
        return seleccionado == null || seleccionado.getIdPersona() == null
                ? null : seleccionado.getIdPersona().getIdPersona().toString();
    }

    public void setPersonaSeleccionadaId(String id) {
        if (seleccionado != null) {
            seleccionado.setIdPersona(buscarPersona(id));
        }
    }

    public String getRolSeleccionadoId() {
        return seleccionado == null || seleccionado.getIdRol() == null
                ? null : seleccionado.getIdRol().getIdRol().toString();
    }

    public void setRolSeleccionadoId(String id) {
        if (seleccionado != null) {
            seleccionado.setIdRol(buscarRol(id));
        }
    }

    public String getClinicaSeleccionadaId() {
        return seleccionado == null || seleccionado.getIdClinica() == null
                ? null : seleccionado.getIdClinica().getIdClinica().toString();
    }

    public void setClinicaSeleccionadaId(String id) {
        if (seleccionado != null) {
            seleccionado.setIdClinica(buscarClinica(id));
        }
    }

    public void nuevo() {
        roles = null;
        seleccionado = new PersonaRol();
        seleccionado.setFechaCreacion(new Date());
        seleccionado.setIdPersona(personaContexto);
        setEstado(ESTADO_CRUD.CREACION);
    }

    /**
     * Devuelve únicamente los roles asignados a la persona recibida.
     *
     * @param persona persona dueña de las asignaciones a mostrar
     * @return roles de la persona o una lista vacía si aún no existe
     */
    public List<PersonaRol> getRolesPorPersona(Persona persona) {
        if (persona == null || persona.getIdPersona() == null) {
            return List.of();
        }
        return getDao().obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL,
                        persona.getIdPersona())),
                List.of());
    }

    /**
     * Inicia una asignación de rol para una persona persistida.
     *
     * @param persona persona a la que se asignará el rol
     */
    public void nuevoParaPersona(Persona persona) {
        if (persona == null || persona.getIdPersona() == null) {
            cancelar();
            return;
        }
        personaContexto = persona;
        nuevo();
    }

    /**
     * Selecciona una asignación solo cuando pertenece a la persona del tab.
     *
     * @param personaRol asignación elegida en la tabla contextual
     * @param persona persona actualmente editada
     */
    public void seleccionarParaPersona(PersonaRol personaRol, Persona persona) {
        if (perteneceAPersona(personaRol, persona)) {
            seleccionar(personaRol);
        }
    }

    public void seleccionarFilaParaPersona(SelectEvent<PersonaRol> evento) {
        if (evento != null && evento.getObject() != null) {
            seleccionar(evento.getObject());
        }
    }

    /**
     * Indica si el formulario activo corresponde a la persona del tab.
     *
     * @param persona persona actualmente editada
     * @return {@code true} si la asignación seleccionada pertenece a esa persona
     */
    public boolean isSeleccionadoParaPersona(Persona persona) {
        return perteneceAPersona(seleccionado, persona);
    }

    public void seleccionar(PersonaRol seleccionado) {
        roles = null;
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
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

    public void eliminarSeleccionadoParaPersona(Persona persona) {
        if (!perteneceAPersona(seleccionado, persona)
                || seleccionado.getIdPersonaRol() == null) {
            return;
        }
        try {
            if (getDao().eliminar(seleccionado.getIdPersonaRol())) {
                cancelar();
                agregarMensaje("personaRol.eliminado", FacesMessage.SEVERITY_INFO);
            } else {
                agregarError("personaRol.errorEliminar");
            }
        } catch (RuntimeException ex) {
            agregarError("personaRol.errorEliminarEnUso");
        }
    }

    private Persona buscarPersona(String id) {
        try {
            return id == null || id.isBlank()
                    ? null : personaDAO.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Rol buscarRol(String id) {
        try {
            return id == null || id.isBlank()
                    ? null : rolDAO.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Clinica buscarClinica(String id) {
        try {
            return id == null || id.isBlank()
                    ? null : clinicaDAO.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean perteneceAPersona(PersonaRol personaRol, Persona persona) {
        return personaRol != null
                && personaRol.getIdPersona() != null
                && personaRol.getIdPersona().getIdPersona() != null
                && persona != null
                && persona.getIdPersona() != null
                && personaRol.getIdPersona().getIdPersona().equals(persona.getIdPersona());
    }

    private boolean validarAntesDeGuardar() {
        boolean valido = true;
        if (seleccionado.getIdPersona() == null) {
            agregarError("personaRol.personaRequerida");
            valido = false;
        }
        if (seleccionado.getIdRol() == null) {
            agregarError("personaRol.rolRequerido");
            valido = false;
        }
        if (seleccionado.getIdClinica() == null) {
            agregarError("personaRol.clinicaRequerida");
            valido = false;
        }
        return valido;
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
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg").getString(clave);
        contexto.addMessage(null,
                new FacesMessage(severidad, mensaje, null));
    }
}
