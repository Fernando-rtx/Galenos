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
    private Persona personaOriginal;
    private UUID personaOriginalId;
    private UUID clinicaOriginalId;
    private UUID rolOriginalId;
    private Clinica clinicaOriginal;
    private Rol rolOriginal;
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

    public List<Clinica> getClinicasDisponibles() {
        UUID clinicaActual = getEstado() == ESTADO_CRUD.EDICION ? clinicaOriginalId : null;
        List<Clinica> catalogo = getClinicas();
        if (catalogo == null) {
            return List.of();
        }
        return catalogo.stream()
                .filter(clinica -> clinica != null
                && (Boolean.TRUE.equals(clinica.getActivo())
                || (clinicaActual != null && clinicaActual.equals(clinica.getIdClinica()))))
                .toList();
    }

    public String getPersonaSeleccionadaId() {
        return seleccionado == null || seleccionado.getIdPersona() == null
                ? null : seleccionado.getIdPersona().getIdPersona().toString();
    }

    public void setPersonaSeleccionadaId(String id) {
        if (seleccionado != null && getEstado() != ESTADO_CRUD.EDICION) {
            seleccionado.setIdPersona(buscarPersona(id));
        }
    }

    public String getRolSeleccionadoId() {
        return seleccionado == null || seleccionado.getIdRol() == null
                ? null : seleccionado.getIdRol().getIdRol().toString();
    }

    public void setRolSeleccionadoId(String id) {
        if (seleccionado != null) {
            Rol rol = buscarRol(id);
            UUID recibido = rol == null ? null : rol.getIdRol();
            if (getEstado() == ESTADO_CRUD.EDICION && !Objects.equals(rolOriginalId, recibido)) {
                return;
            }
            seleccionado.setIdRol(rol);
        }
    }

    public String getClinicaSeleccionadaId() {
        return seleccionado == null || seleccionado.getIdClinica() == null
                ? null : seleccionado.getIdClinica().getIdClinica().toString();
    }

    public void setClinicaSeleccionadaId(String id) {
        if (seleccionado != null) {
            Clinica clinica = buscarClinica(id);
            UUID recibido = clinica == null ? null : clinica.getIdClinica();
            if (getEstado() == ESTADO_CRUD.EDICION && !Objects.equals(clinicaOriginalId, recibido)) {
                return;
            }
            seleccionado.setIdClinica(clinica);
        }
    }

    public void nuevo() {
        roles = null;
        seleccionado = new PersonaRol();
        clinicaOriginalId = null;
        rolOriginalId = null;
        clinicaOriginal = null;
        rolOriginal = null;
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

    /** Devuelve personas únicas que tienen ese rol dentro de esa clínica. */
    public List<Persona> obtenerPersonasPorRolYClinica(UUID idRol, UUID idClinica) {
        if (idRol == null || idClinica == null) {
            return List.of();
        }
        return getDao().buscarPorRolYClinica(idRol, idClinica).stream()
                .map(PersonaRol::getIdPersona)
                .filter(Objects::nonNull)
                .filter(persona -> persona.getIdPersona() != null)
                .collect(java.util.stream.Collectors.toMap(Persona::getIdPersona,
                        persona -> persona, (primera, segunda) -> primera,
                        java.util.LinkedHashMap::new))
                .values().stream().toList();
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
        personaOriginal = seleccionado == null ? null : seleccionado.getIdPersona();
        personaOriginalId = personaOriginal == null ? null : personaOriginal.getIdPersona();
        clinicaOriginalId = seleccionado == null || seleccionado.getIdClinica() == null
                ? null : seleccionado.getIdClinica().getIdClinica();
        rolOriginalId = seleccionado == null || seleccionado.getIdRol() == null
                ? null : seleccionado.getIdRol().getIdRol();
        clinicaOriginal = seleccionado == null ? null : seleccionado.getIdClinica();
        rolOriginal = seleccionado == null ? null : seleccionado.getIdRol();
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
        agregarMensaje("personaRol.guardado", FacesMessage.SEVERITY_INFO);
        cancelar();
    }

    public void cancelar() {
        seleccionado = null;
        clinicaOriginalId = null;
        rolOriginalId = null;
        clinicaOriginal = null;
        rolOriginal = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void limpiarContexto() {
        personaContexto = null;
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
        if (getEstado() == ESTADO_CRUD.EDICION && (personaOriginalId == null
                || seleccionado.getIdPersona() == null
                || !personaOriginalId.equals(seleccionado.getIdPersona().getIdPersona()))) {
            seleccionado.setIdPersona(personaOriginal);
            agregarError("persona", "relaciones.noEditables");
            return false;
        }
        if (seleccionado.getIdPersona() == null
                || seleccionado.getIdPersona().getIdPersona() == null) {
            agregarError("persona", "personaRol.personaRequerida");
            return false;
        }
        Persona personaPersistida = personaDAO.buscarPorId(seleccionado.getIdPersona().getIdPersona());
        if (personaPersistida == null || (personaContexto != null
                && !Objects.equals(personaContexto.getIdPersona(), personaPersistida.getIdPersona()))) {
            agregarError("persona", "personaRol.personaRequerida");
            return false;
        }
        seleccionado.setIdPersona(personaPersistida);

        if (seleccionado.getIdRol() == null || seleccionado.getIdRol().getIdRol() == null) {
            agregarError("rol", "personaRol.rolRequerido");
            return false;
        }
        if (seleccionado.getIdClinica() == null || seleccionado.getIdClinica().getIdClinica() == null) {
            agregarError("clinica", "personaRol.clinicaRequerida");
            return false;
        }

        if (getEstado() == ESTADO_CRUD.EDICION && !relacionesNoCambiaron()) {
            agregarError("rol", "personaRol.relacionNoEditable");
            return false;
        }

        Rol rolPersistido = rolDAO.buscarPorId(seleccionado.getIdRol().getIdRol());
        if (rolPersistido == null) {
            agregarError("rol", "personaRol.rolRequerido");
            return false;
        }
        if (getEstado() == ESTADO_CRUD.CREACION && !Boolean.TRUE.equals(rolPersistido.getActivo())) {
            agregarError("rol", "personaRol.rolInactivo");
            return false;
        }
        seleccionado.setIdRol(rolPersistido);

        Clinica clinicaPersistida = clinicaDAO.buscarPorId(seleccionado.getIdClinica().getIdClinica());
        if (clinicaPersistida == null) {
            agregarError("clinica", "personaRol.clinicaNoExiste");
            return false;
        }
        if (getEstado() == ESTADO_CRUD.CREACION && !Boolean.TRUE.equals(clinicaPersistida.getActivo())) {
            agregarError("clinica", "personaRol.clinicaInactiva");
            return false;
        }
        seleccionado.setIdClinica(clinicaPersistida);

        if (getEstado() == ESTADO_CRUD.CREACION
                && personaRolDAO.existeAsignacion(personaPersistida.getIdPersona(),
                        clinicaPersistida.getIdClinica(), rolPersistido.getIdRol())) {
            agregarError("personaRol.duplicada");
            return false;
        }
        return true;
    }

    private boolean relacionesNoCambiaron() {
        if (seleccionado.getIdPersonaRol() == null) {
            return false;
        }
        PersonaRol persistido = personaRolDAO.buscarPorId(seleccionado.getIdPersonaRol());
        UUID rolPersistido = persistido == null || persistido.getIdRol() == null
                ? null : persistido.getIdRol().getIdRol();
        UUID clinicaPersistida = persistido == null || persistido.getIdClinica() == null
                ? null : persistido.getIdClinica().getIdClinica();
        UUID rolActual = seleccionado.getIdRol() == null ? null : seleccionado.getIdRol().getIdRol();
        UUID clinicaActual = seleccionado.getIdClinica() == null
                ? null : seleccionado.getIdClinica().getIdClinica();
        if (!Objects.equals(rolOriginalId, rolPersistido)
                || !Objects.equals(clinicaOriginalId, clinicaPersistida)
                || !Objects.equals(rolPersistido, rolActual)
                || !Objects.equals(clinicaPersistida, clinicaActual)) {
            if (persistido != null) {
                seleccionado.setIdRol(rolOriginal);
                seleccionado.setIdClinica(clinicaOriginal);
            }
            return false;
        }
        return persistido != null;
    }

    private void agregarError(String clave) {
        agregarMensaje(clave, FacesMessage.SEVERITY_ERROR);
        if (facesContext != null) {
            facesContext.validationFailed();
        }
    }

    private void agregarError(String idComponente, String clave) {
        FacesContext contexto = facesContext;
        if (contexto == null) {
            return;
        }
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg").getString(clave);
        jakarta.faces.component.UIComponent componente = buscarComponenteFlexible(contexto.getViewRoot(), idComponente);
        String clientId = componente == null ? null : componente.getClientId(contexto);
        if (componente instanceof jakarta.faces.component.UIInput entrada) {
            entrada.setValid(false);
        }
        contexto.addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
        contexto.validationFailed();
    }

    private jakarta.faces.component.UIComponent buscarComponenteFlexible(
            jakarta.faces.component.UIComponent raiz, String id) {
        var componente = buscarComponente(raiz, id);
        if (componente == null && "rol".equals(id)) {
            componente = buscarComponente(raiz, "rolSeleccionado");
        } else if (componente == null && "clinica".equals(id)) {
            componente = buscarComponente(raiz, "clinicaSeleccionada");
        }
        return componente;
    }

    private jakarta.faces.component.UIComponent buscarComponente(
            jakarta.faces.component.UIComponent componente, String id) {
        if (componente == null || id == null) {
            return null;
        }
        if (id.equals(componente.getId())) {
            return componente;
        }
        var hijos = componente.getFacetsAndChildren();
        while (hijos.hasNext()) {
            var encontrado = buscarComponente(hijos.next(), id);
            if (encontrado != null) {
                return encontrado;
            }
        }
        return null;
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
