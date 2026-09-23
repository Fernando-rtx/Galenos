package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoDocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

@Named
@ViewScoped
public class DocumentoModel extends AbstractModel<Documento> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final PersonaDAO personaDAO;
    private final TipoDocumentoDAO tipoDocumentoDAO;
    @Inject
    private transient FacesContext facesContext;
    private Documento seleccionado;
    private Persona personaContexto;
    private List<Persona> personas;
    private List<TipoDocumento> tiposDocumento;

    @Inject
    public DocumentoModel(DocumentoDAO dao, PersonaDAO personaDAO, TipoDocumentoDAO tipoDocumentoDAO) {
        super(dao);
        this.personaDAO = personaDAO;
        this.tipoDocumentoDAO = tipoDocumentoDAO;
    }

    public Documento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Documento seleccionado) {
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

    public List<TipoDocumento> getTiposDocumento() {
        if (tiposDocumento == null) {
            tiposDocumento = tipoDocumentoDAO.obtenerTodos();
        }
        return tiposDocumento;
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

    public String getTipoDocumentoSeleccionadoId() {
        return seleccionado == null || seleccionado.getIdTipoDocumento() == null
                ? null : seleccionado.getIdTipoDocumento().getIdTipoDocumento().toString();
    }

    public void setTipoDocumentoSeleccionadoId(String id) {
        if (seleccionado != null) {
            seleccionado.setIdTipoDocumento(buscarTipoDocumento(id));
        }
    }

    public void nuevo() {
        seleccionado = new Documento();
        seleccionado.setIdPersona(personaContexto);
        setEstado(ESTADO_CRUD.CREACION);
    }

    /**
     * Devuelve únicamente los documentos asociados a la persona recibida.
     * La consulta se mantiene en el DAO de Documento a través del contrato
     * genérico, sin trasladar su CRUD al modelo de Persona.
     *
     * @param persona persona dueña de los documentos a mostrar
     * @return documentos de la persona o una lista vacía si aún no existe
     */
    public List<Documento> getDocumentosPorPersona(Persona persona) {
        if (persona == null || persona.getIdPersona() == null) {
            return List.of();
        }
        return getDao().obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL,
                        persona.getIdPersona())),
                List.of());
    }

    /**
     * Inicia un documento dentro del contexto de una persona persistida.
     *
     * @param persona persona que será asignada al documento
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
     * Selecciona un documento solo cuando pertenece a la persona del tab.
     *
     * @param documento documento elegido en la tabla contextual
     * @param persona persona actualmente editada
     */
    public void seleccionarParaPersona(Documento documento, Persona persona) {
        if (perteneceAPersona(documento, persona)) {
            seleccionar(documento);
        }
    }

    /**
     * Indica si el formulario activo corresponde a la persona del tab.
     *
     * @param persona persona actualmente editada
     * @return {@code true} si el documento seleccionado pertenece a esa persona
     */
    public boolean isSeleccionadoParaPersona(Persona persona) {
        return perteneceAPersona(seleccionado, persona);
    }

    public void seleccionar(Documento seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        seleccionado.setValor(normalizar(seleccionado.getValor()));
        seleccionado.setRutaFisica(normalizar(seleccionado.getRutaFisica()));
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

    private TipoDocumento buscarTipoDocumento(String id) {
        try {
            return id == null || id.isBlank()
                    ? null : tipoDocumentoDAO.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean validarAntesDeGuardar() {
        boolean valido = true;
        if (seleccionado.getIdPersona() == null) {
            agregarError("persona", "documento.personaRequerida");
            valido = false;
        }
        if (seleccionado.getIdTipoDocumento() == null) {
            agregarError("tipo", "documento.tipoRequerido");
            valido = false;
        }
        if (seleccionado.getValor() == null || seleccionado.getValor().isBlank()) {
            agregarError("valor", "documento.valorRequerido");
            valido = false;
        } else if (!cumpleExpresionRegular(seleccionado.getValor())) {
            agregarError("valor", "documento.valorFormato");
            valido = false;
        }
        return valido;
    }

    private boolean cumpleExpresionRegular(String valor) {
        if (seleccionado.getIdTipoDocumento() == null) {
            return true;
        }
        String expresion = seleccionado.getIdTipoDocumento().getExpresionRegular();
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

    private boolean perteneceAPersona(Documento documento, Persona persona) {
        return documento != null
                && documento.getIdPersona() != null
                && documento.getIdPersona().getIdPersona() != null
                && persona != null
                && persona.getIdPersona() != null
                && documento.getIdPersona().getIdPersona().equals(persona.getIdPersona());
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
