package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException;
import jakarta.ejb.EJBException;
import jakarta.persistence.PersistenceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.MedioContactoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoMedioContactoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;

@Named
@ViewScoped
public class MedioContactoModel extends AbstractModel<MedioContacto> implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB private PersonaFlujoService personaService;
    private static final Logger LOG = Logger.getLogger(MedioContactoModel.class.getName());
    @EJB
    private MedioContactoDAO medioContactoDAO;
    @EJB
    private PersonaDAO personaDAO;
    @EJB
    private TipoMedioContactoDAO tipoMedioContactoDAO;
    @Inject
    private transient FacesContext facesContext;
    private MedioContacto seleccionado;
    private UUID tipoMedioContactoOriginalId;
    private TipoMedioContacto tipoMedioContactoOriginal;
    private Persona personaContexto;
    private Persona personaOriginal;
    private UUID personaOriginalId;
    private List<Persona> personas;
    private List<TipoMedioContacto> tiposMedioContacto;

    public MedioContactoModel() {
    }

    public MedioContactoModel(MedioContactoDAO medioContactoDAO, PersonaDAO personaDAO,
            TipoMedioContactoDAO tipoMedioContactoDAO) {
        this.medioContactoDAO = medioContactoDAO;
        this.personaDAO = personaDAO;
        this.tipoMedioContactoDAO = tipoMedioContactoDAO;
        this.personaService = new PersonaFlujoService(medioContactoDAO, personaDAO, tipoMedioContactoDAO);
    }

    @Override
    protected MedioContactoDAO getDao() {
        return medioContactoDAO;
    }

    public MedioContacto getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(MedioContacto seleccionado) {
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

    public List<TipoMedioContacto> getTiposMedioContacto() {
        if (tiposMedioContacto == null) {
            tiposMedioContacto = tipoMedioContactoDAO.obtenerTodos();
        }
        return tiposMedioContacto;
    }

    public List<TipoMedioContacto> getTiposMedioContactoAsignables() {
        List<TipoMedioContacto> asignables = new ArrayList<>();
        for (TipoMedioContacto tipo : getTiposMedioContacto()) {
            if (Boolean.TRUE.equals(tipo.getActivo())) {
                asignables.add(tipo);
            }
        }
        TipoMedioContacto actual = seleccionado == null ? null : seleccionado.getIdTipoMedioContacto();
        if (getEstado() == ESTADO_CRUD.EDICION && actual != null
                && asignables.stream().noneMatch(tipo -> mismoTipo(tipo, actual))) {
            asignables.add(actual);
        }
        return asignables;
    }

    public boolean isTipoSeleccionadoInactivo() {
        TipoMedioContacto tipo = seleccionado == null ? null : seleccionado.getIdTipoMedioContacto();
        if (tipo == null || tipo.getIdTipoMedioContacto() == null) {
            return false;
        }
        TipoMedioContacto actualizado = getTiposMedioContacto().stream()
                .filter(candidato -> mismoTipo(candidato, tipo))
                .findFirst()
                .orElse(null);
        return actualizado == null || !Boolean.TRUE.equals(actualizado.getActivo());
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

    public String getTipoMedioSeleccionadoId() {
        return seleccionado == null || seleccionado.getIdTipoMedioContacto() == null
                ? null : seleccionado.getIdTipoMedioContacto().getIdTipoMedioContacto().toString();
    }

    public void setTipoMedioSeleccionadoId(String id) {
        if (seleccionado != null) {
            TipoMedioContacto tipo = buscarTipo(id);
            UUID idRecibido = tipo == null ? null : tipo.getIdTipoMedioContacto();
            if (getEstado() == ESTADO_CRUD.EDICION
                    && !java.util.Objects.equals(tipoMedioContactoOriginalId, idRecibido)) {
                return;
            }
            seleccionado.setIdTipoMedioContacto(tipo);
        }
    }

    public void nuevo() {
        seleccionado = new MedioContacto();
        tipoMedioContactoOriginalId = null;
        tipoMedioContactoOriginal = null;
        seleccionado.setFechaCreacion(new Date());
        seleccionado.setIdPersona(personaContexto);
        setEstado(ESTADO_CRUD.CREACION);
    }

    /**
     * Devuelve únicamente los contactos asociados a la persona recibida.
     *
     * @param persona persona dueña de los contactos a mostrar
     * @return contactos de la persona o una lista vacía si aún no existe
     */
    public List<MedioContacto> getContactosPorPersona(Persona persona) {
        if (persona == null || persona.getIdPersona() == null) {
            return List.of();
        }
        return medioContactoDAO.buscarPorPersona(persona.getIdPersona());
    }

    /**
     * Inicia un contacto dentro del contexto de una persona persistida.
     *
     * @param persona persona que será asignada al contacto
     */
    public void nuevoParaPersona(Persona persona) {
        if (persona == null || persona.getIdPersona() == null) {
            cancelar();
            return;
        }
        tiposMedioContacto = tipoMedioContactoDAO.obtenerTodos();
        personaContexto = persona;
        nuevo();
    }

    /**
     * Selecciona un contacto solo cuando pertenece a la persona del tab.
     *
     * @param contacto contacto elegido en la tabla contextual
     * @param persona persona actualmente editada
     */
    public void seleccionarParaPersona(MedioContacto contacto, Persona persona) {
        if (perteneceAPersona(contacto, persona)) {
            tiposMedioContacto = tipoMedioContactoDAO.obtenerTodos();
            seleccionar(contacto);
        }
    }

    public void seleccionarFilaParaPersona(SelectEvent<MedioContacto> evento) {
        if (evento != null && evento.getObject() != null) {
            tiposMedioContacto = tipoMedioContactoDAO.obtenerTodos();
            seleccionar(evento.getObject());
        }
    }

    /**
     * Indica si el formulario activo corresponde a la persona del tab.
     *
     * @param persona persona actualmente editada
     * @return {@code true} si el contacto seleccionado pertenece a esa persona
     */
    public boolean isSeleccionadoParaPersona(Persona persona) {
        return perteneceAPersona(seleccionado, persona);
    }

    public void seleccionar(MedioContacto seleccionado) {
        this.seleccionado = seleccionado;
        personaOriginal = seleccionado == null ? null : seleccionado.getIdPersona();
        personaOriginalId = personaOriginal == null ? null : personaOriginal.getIdPersona();
        tipoMedioContactoOriginalId = seleccionado == null || seleccionado.getIdTipoMedioContacto() == null
                ? null : seleccionado.getIdTipoMedioContacto().getIdTipoMedioContacto();
        tipoMedioContactoOriginal = seleccionado == null ? null : seleccionado.getIdTipoMedioContacto();
        tiposMedioContacto = tipoMedioContactoDAO.obtenerTodos();
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) return;
        try {
            boolean edicion = getEstado() == ESTADO_CRUD.EDICION;
            var guardado = personaService.guardarContacto(seleccionado, edicion, personaOriginalId, tipoMedioContactoOriginal);
            if (edicion) seleccionado = guardado;
            agregarMensaje("medioContacto.guardado", FacesMessage.SEVERITY_INFO);cancelar();
        } catch (ReglaNegocioException ex) {
            if ("persona".equals(ex.getCampo())) seleccionado.setIdPersona(personaOriginal);
            agregarError(ex.getCampo(), ex.getMessage());
        } catch (PersistenceException | EJBException ex) {
            LOG.log(Level.SEVERE, "No se pudo guardar el medio de contacto", ex);
            if (facesContext == null) throw ex;
            agregarError(null, "medioContacto.errorGuardar");
        }
    }

    public void cancelar() {
        seleccionado = null;
        tipoMedioContactoOriginalId = null;
        tipoMedioContactoOriginal = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void limpiarContexto() {
        personaContexto = null;
    }

    public void eliminarSeleccionadoParaPersona(Persona persona) {
        if (!perteneceAPersona(seleccionado, persona)
                || seleccionado.getIdMedioContacto() == null) {
            return;
        }
        try {
            if (getDao().eliminar(seleccionado.getIdMedioContacto())) {
                cancelar();
                agregarMensaje("medioContacto.eliminado", FacesMessage.SEVERITY_INFO);
            } else {
                agregarError(null, "medioContacto.errorEliminar");
            }
        } catch (RuntimeException ex) {
            agregarError(null, "medioContacto.errorEliminar");
        }
    }

    private Persona buscarPersona(String id) {
        if (id == null || id.isBlank()) return null;
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return personaDAO.buscarPorId(uuid);
    }

    private TipoMedioContacto buscarTipo(String id) {
        if (id == null || id.isBlank()) return null;
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return tipoMedioContactoDAO.buscarPorId(uuid);
    }







    private boolean mismoTipo(TipoMedioContacto primero, TipoMedioContacto segundo) {
        return primero.getIdTipoMedioContacto() != null
                && primero.getIdTipoMedioContacto().equals(segundo.getIdTipoMedioContacto());
    }







    private boolean perteneceAPersona(MedioContacto contacto, Persona persona) {
        return contacto != null
                && contacto.getIdPersona() != null
                && contacto.getIdPersona().getIdPersona() != null
                && persona != null
                && persona.getIdPersona() != null
                && contacto.getIdPersona().getIdPersona().equals(persona.getIdPersona());
    }

    private void agregarError(String idComponente, String clave) {
        agregarMensaje(idComponente, clave, FacesMessage.SEVERITY_ERROR);
        if (facesContext != null) {
            facesContext.validationFailed();
        }
    }

    private void agregarMensaje(String clave, FacesMessage.Severity severidad) {
        agregarMensaje(null, clave, severidad);
    }

    private void agregarMensaje(String idComponente, String clave, FacesMessage.Severity severidad) {
        FacesContext contexto = facesContext;
        if (contexto == null) {
            return;
        }
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg").getString(clave);
        UIComponent componente = idComponente == null
                ? null : buscarComponenteFlexible(contexto.getViewRoot(), idComponente);
        String clientId = componente == null ? null : componente.getClientId(contexto);
        if (componente instanceof UIInput entrada) {
            entrada.setValid(false);
        }
        contexto.addMessage(clientId,
                new FacesMessage(severidad, mensaje, null));
    }

    private UIComponent buscarComponenteFlexible(UIComponent raiz, String id) {
        UIComponent componente = buscarComponente(raiz, id);
        if (componente == null && "contactoTipo".equals(id)) {
            componente = buscarComponente(raiz, "tipo");
        } else if (componente == null && "contactoValor".equals(id)) {
            componente = buscarComponente(raiz, "valor");
        }
        return componente;
    }

    private UIComponent buscarComponente(UIComponent componente, String id) {
        if (componente == null) {
            return null;
        }
        if (id.equals(componente.getId())) {
            return componente;
        }
        var hijos = componente.getFacetsAndChildren();
        while (hijos.hasNext()) {
            UIComponent encontrado = buscarComponente(hijos.next(), id);
            if (encontrado != null) {
                return encontrado;
            }
        }
        return null;
    }
}
