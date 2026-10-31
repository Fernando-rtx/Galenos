package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
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
    @EJB private PersonaFlujoService personaService;
    static final ZoneId ZONA_CLINICA = ZoneId.of("America/El_Salvador");
    @EJB
    private DocumentoDAO documentoDAO;
    @EJB
    private PersonaDAO personaDAO;
    @EJB
    private TipoDocumentoDAO tipoDocumentoDAO;
    @Inject
    private transient FacesContext facesContext;
    private Documento seleccionado;
    private UUID tipoDocumentoOriginalId;
    private Persona personaOriginal;
    private UUID personaOriginalId;
    private Persona personaContexto;
    private List<Persona> personas;
    private UUID personaDocumentosCargadosId;
    private List<Documento> documentosPersona;

    public DocumentoModel() {
    }

    public DocumentoModel(DocumentoDAO documentoDAO, PersonaDAO personaDAO, TipoDocumentoDAO tipoDocumentoDAO) {
        this.documentoDAO = documentoDAO;
        this.personaDAO = personaDAO;
        this.tipoDocumentoDAO = tipoDocumentoDAO;
        this.personaService = new PersonaFlujoService(documentoDAO, personaDAO, tipoDocumentoDAO);
    }

    @Override
    protected DocumentoDAO getDao() {
        return documentoDAO;
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
        if (personaContexto == null) {
            return null;
        }
        return personaContexto.getIdPersona().toString();
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
        List<TipoDocumento> activos = new ArrayList<>();
        for (TipoDocumento tipo : tipoDocumentoDAO.obtenerTodos()) {
            if (Boolean.TRUE.equals(tipo.getActivo())) {
                activos.add(tipo);
            }
        }

        // Un documento antiguo puede conservar un tipo desactivado; al editarlo,
        // JSF necesita que la opción actual siga presente para validarla.
        if (getEstado() == ESTADO_CRUD.EDICION && seleccionado != null
                && seleccionado.getIdTipoDocumento() != null) {
            boolean estaEnLaLista = false;
            for (TipoDocumento tipo : activos) {
                if (tipo.getIdTipoDocumento().equals(
                        seleccionado.getIdTipoDocumento().getIdTipoDocumento())) {
                    estaEnLaLista = true;
                    break;
                }
            }
            if (!estaEnLaLista) {
                activos.add(seleccionado.getIdTipoDocumento());
            }
        }
        return activos;
    }

    public String getPersonaSeleccionadaId() {
        if (seleccionado == null || seleccionado.getIdPersona() == null) {
            return null;
        }
        return seleccionado.getIdPersona().getIdPersona().toString();
    }

    public void setPersonaSeleccionadaId(String id) {
        if (seleccionado != null && getEstado() != ESTADO_CRUD.EDICION) {
            seleccionado.setIdPersona(buscarPersona(id));
        }
    }

    public String getTipoDocumentoSeleccionadoId() {
        if (seleccionado == null || seleccionado.getIdTipoDocumento() == null) {
            return null;
        }
        return seleccionado.getIdTipoDocumento().getIdTipoDocumento().toString();
    }

    public void setTipoDocumentoSeleccionadoId(String id) {
        if (seleccionado != null) {
            TipoDocumento tipo = buscarTipoDocumento(id);
            if (getEstado() == ESTADO_CRUD.EDICION) {
                UUID idRecibido = null;
                if (tipo != null) {
                    idRecibido = tipo.getIdTipoDocumento();
                }
                if (!java.util.Objects.equals(tipoDocumentoOriginalId, idRecibido)) {
                    return;
                }
            }
            seleccionado.setIdTipoDocumento(tipo);
        }
    }

    /** Aviso inmediato del combo; Guardar vuelve a validar con la persona registrada. */
    public void validarEdadDuiSeleccionado() {
        if (getEstado() == ESTADO_CRUD.CREACION && seleccionado != null
                && esDui(seleccionado.getIdTipoDocumento())) {
            validarEdadDui(seleccionado.getIdPersona());
        }
    }

    public void nuevo() {
        seleccionado = new Documento();
        tipoDocumentoOriginalId = null;
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
        if (documentosPersona == null
                || !persona.getIdPersona().equals(personaDocumentosCargadosId)) {
            recargarDocumentosPorPersona(persona);
        }
        return documentosPersona;
    }

    private void recargarDocumentosPorPersona(Persona persona) {
        if (persona == null || persona.getIdPersona() == null) {
            personaDocumentosCargadosId = null;
            documentosPersona = List.of();
            return;
        }
        documentosPersona = getDao().obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL,
                        persona.getIdPersona())),
                List.of());
        personaDocumentosCargadosId = persona.getIdPersona();
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
        recargarDocumentosPorPersona(persona);
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
        personaOriginal = seleccionado == null ? null : seleccionado.getIdPersona();
        personaOriginalId = personaOriginal == null ? null : personaOriginal.getIdPersona();
        if (seleccionado == null || seleccionado.getIdTipoDocumento() == null) {
            this.tipoDocumentoOriginalId = null;
        } else {
            this.tipoDocumentoOriginalId = seleccionado.getIdTipoDocumento().getIdTipoDocumento();
        }
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) return;
        boolean edicion = getEstado() == ESTADO_CRUD.EDICION;
        Persona personaDocumento = seleccionado.getIdPersona();
        try {
            if (getEstado() == ESTADO_CRUD.LISTADO) return;
            var guardado = personaService.guardarDocumento(seleccionado, edicion, personaOriginalId, tipoDocumentoOriginalId);
            if (edicion) seleccionado = guardado;
        } catch (ReglaNegocioException ex) {
            if ("persona".equals(ex.getCampo())) seleccionado.setIdPersona(personaOriginal);
            if ("documento.tipoNoEditable".equals(ex.getMessage())) seleccionado.setIdTipoDocumento(
                    tipoDocumentoOriginalId == null ? null : tipoDocumentoDAO.buscarPorId(tipoDocumentoOriginalId));
            agregarError(ex.getCampo(), ex.getMessage());return;
        }
        recargarDocumentosPorPersona(personaDocumento);seleccionado = null;tipoDocumentoOriginalId = null;
        setEstado(ESTADO_CRUD.LISTADO);agregarMensaje("documento.guardado", FacesMessage.SEVERITY_INFO);
    }

    public void cancelar() {
        seleccionado = null;
        tipoDocumentoOriginalId = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void limpiarContexto() {
        personaContexto = null;
        personaDocumentosCargadosId = null;
        documentosPersona = null;
    }

    public void eliminarSeleccionadoParaPersona(Persona persona) {
        if (!perteneceAPersona(seleccionado, persona)|| seleccionado.getIdDocumento() == null) {
            return;
        }
        try {
            if (getDao().eliminar(seleccionado.getIdDocumento())) {
                recargarDocumentosPorPersona(persona);
                cancelar();
                agregarMensaje("documento.eliminado", FacesMessage.SEVERITY_INFO);
            } else {
                agregarError(null, "documento.errorEliminar");
            }
        } catch (RuntimeException ex) {
            agregarError(null, "documento.errorEliminar");
        }
    }

    private Persona buscarPersona(String id) {
        try {
            if (id == null || id.isBlank()) {
                return null;
            }
            return personaDAO.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private TipoDocumento buscarTipoDocumento(String id) {
        try {
            if (id == null || id.isBlank()) {
                return null;
            }
            return tipoDocumentoDAO.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }





    static boolean esDui(TipoDocumento tipo) {
        return PersonaFlujoService.esDui(tipo);
    }

    /** Reutilizada al asignar DUI y al editar una persona que ya lo posee. */
    static String errorFechaParaDui(Date fechaNacimiento, LocalDate hoy) {
        return PersonaFlujoService.errorFechaParaDui(fechaNacimiento, hoy);
    }

    private boolean validarEdadDui(Persona persona) {
        String error = errorFechaParaDui(persona == null ? null : persona.getFechaNacimiento(),
                LocalDate.now(ZONA_CLINICA));
        if (error != null) {
            agregarError("tipo", error);
            return false;
        }
        return true;
    }





    private boolean perteneceAPersona(Documento documento, Persona persona) {
        if (documento == null || documento.getIdPersona() == null
                || documento.getIdPersona().getIdPersona() == null
                || persona == null || persona.getIdPersona() == null) {
            return false;
        }
        return documento.getIdPersona().getIdPersona().equals(persona.getIdPersona());
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
        UIComponent componente = null;
        if (idComponente != null) {
            componente = buscarComponente(contexto.getViewRoot(), idComponente);
        }

        String clientId = null;
        if (componente != null) {
            clientId = componente.getClientId(contexto);
        }
        if (componente instanceof UIInput) {
            UIInput entrada = (UIInput) componente;
            entrada.setValid(false);
        }
        contexto.addMessage(clientId,
                new FacesMessage(severidad, mensaje, null));
    }

    private UIComponent buscarComponente(UIComponent componente, String id) {
        if (componente == null) {
            return null;
        }
        if (id.equals(componente.getId())) {
            return componente;
        }
        Iterator<UIComponent> hijos = componente.getFacetsAndChildren();
        while (hijos.hasNext()) {
            UIComponent encontrado = buscarComponente(hijos.next(), id);
            if (encontrado != null) {
                return encontrado;
            }
        }
        return null;
    }
}
