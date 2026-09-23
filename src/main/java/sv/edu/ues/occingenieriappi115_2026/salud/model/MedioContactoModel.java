package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import sv.edu.ues.occingenieriappi115_2026.salud.control.MedioContactoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoMedioContactoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;

@Named
@ViewScoped
public class MedioContactoModel extends AbstractModel<MedioContacto> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final PersonaDAO personaDAO;
    private final TipoMedioContactoDAO tipoMedioContactoDAO;
    @Inject
    private transient FacesContext facesContext;
    private MedioContacto seleccionado;
    private Persona personaContexto;
    private List<Persona> personas;
    private List<TipoMedioContacto> tiposMedioContacto;

    @Inject
    public MedioContactoModel(MedioContactoDAO dao, PersonaDAO personaDAO, TipoMedioContactoDAO tipoDAO) {
        super(dao);
        this.personaDAO = personaDAO;
        this.tipoMedioContactoDAO = tipoDAO;
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

    public String getPersonaSeleccionadaId() {
        return seleccionado == null || seleccionado.getIdPersona() == null
                ? null : seleccionado.getIdPersona().getIdPersona().toString();
    }

    public void setPersonaSeleccionadaId(String id) {
        if (seleccionado != null) {
            seleccionado.setIdPersona(buscarPersona(id));
        }
    }

    public String getTipoMedioSeleccionadoId() {
        return seleccionado == null || seleccionado.getIdTipoMedioContacto() == null
                ? null : seleccionado.getIdTipoMedioContacto().getIdTipoMedioContacto().toString();
    }

    public void setTipoMedioSeleccionadoId(String id) {
        if (seleccionado != null) {
            seleccionado.setIdTipoMedioContacto(buscarTipo(id));
        }
    }

    public void nuevo() {
        seleccionado = new MedioContacto();
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
        return getDao().obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL,
                        persona.getIdPersona())),
                List.of());
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
            seleccionar(contacto);
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
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        seleccionado.setValor(normalizar(seleccionado.getValor()));
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

    private Persona buscarPersona(String id) {
        try {
            return id == null || id.isBlank()
                    ? null : personaDAO.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private TipoMedioContacto buscarTipo(String id) {
        try {
            return id == null || id.isBlank()
                    ? null : tipoMedioContactoDAO.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean validarAntesDeGuardar() {
        boolean valido = true;
        if (seleccionado.getIdPersona() == null) {
            agregarError("persona", "medioContacto.personaRequerida");
            valido = false;
        }
        if (seleccionado.getIdTipoMedioContacto() == null) {
            agregarError("tipo", "medioContacto.tipoRequerido");
            valido = false;
        }
        if (seleccionado.getValor() == null || seleccionado.getValor().isBlank()) {
            agregarError("valor", "medioContacto.valorRequerido");
            valido = false;
        } else if (!cumpleExpresionRegular(seleccionado.getValor())) {
            agregarError("valor", "medioContacto.valorFormato");
            valido = false;
        }
        return valido;
    }

    private boolean cumpleExpresionRegular(String valor) {
        if (seleccionado.getIdTipoMedioContacto() == null) {
            return true;
        }
        String expresion = seleccionado.getIdTipoMedioContacto().getExpresionRegular();
        if (expresion == null || expresion.isBlank()) {
            return true;
        }
        try {
            return Pattern.compile(expresion).matcher(valor).matches();
        } catch (PatternSyntaxException ex) {
            return true;
        }
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
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
        FacesContext contexto = facesContext;
        if (contexto == null) {
            return;
        }
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg").getString(clave);
        String clientId = "layoutForm:" + idComponente;
        contexto.addMessage(clientId,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, mensaje));
        contexto.validationFailed();
        UIComponent componente = contexto.getViewRoot().findComponent(clientId);
        if (componente instanceof UIInput entrada) {
            entrada.setValid(false);
        }
    }
}
