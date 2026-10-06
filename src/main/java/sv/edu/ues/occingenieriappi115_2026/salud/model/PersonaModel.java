package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.primefaces.event.SelectEvent;
import org.primefaces.PrimeFaces;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;

@Named
@ViewScoped
public class PersonaModel extends AbstractModel<Persona> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern PATRON_NOMBRE
            = Pattern.compile("^[\\p{L}][\\p{L}\\p{M}' -]*$");

    @EJB
    private PersonaDAO personaDAO;
    @EJB
    private DocumentoDAO documentoDAO;
    private Persona seleccionado;

    public PersonaModel() {
    }

    public PersonaModel(PersonaDAO personaDAO) {
        this.personaDAO = personaDAO;
    }

    public PersonaModel(PersonaDAO personaDAO, DocumentoDAO documentoDAO) {
        this(personaDAO);
        this.documentoDAO = documentoDAO;
    }

    @Inject
    transient FacesContext facesContext;

    @Override
    protected PersonaDAO getDao() {
        return personaDAO;
    }

    public Persona getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Persona seleccionado) {
        this.seleccionado = seleccionado;
    }

    public String getIdPersonaContexto() {
        if (seleccionado == null || seleccionado.getIdPersona() == null) {
            return null;
        }

        return seleccionado.getIdPersona().toString();
    }

    public void setIdPersonaContexto(String id) {

        if (id == null || id.isBlank()) {
            return;
        }

        try {

            Persona persona = getDao().buscarPorId(UUID.fromString(id));

            if (persona != null) {
                seleccionar(persona);
            }

        } catch (IllegalArgumentException ex) {

            seleccionado = null;
            setEstado(ESTADO_CRUD.LISTADO);
        }
    }

    public void nuevo() {
        seleccionado = new Persona();
        seleccionado.setFechaCreacion(new Date());
        setEstado(ESTADO_CRUD.CREACION);
    }

    /** Limpia relaciones contextualizadas antes de iniciar otra persona. */
    public void nuevaPersona(DocumentoModel documento, MedioContactoModel contacto,
            PersonaRolModel personaRol) {
        if (documento != null) {
            documento.cancelar();
            documento.limpiarContexto();
        }
        if (contacto != null) {
            contacto.cancelar();
            contacto.limpiarContexto();
        }
        if (personaRol != null) {
            personaRol.cancelar();
            personaRol.limpiarContexto();
        }
        nuevo();
    }

    // =========================================================
    // EDITAR / SELECCIONAR
    // =========================================================

    public void seleccionar(Persona persona) {

        if (persona == null) {
            seleccionado = null;
            setEstado(ESTADO_CRUD.LISTADO);
            return;
        }

        seleccionado = persona;

        setEstado(ESTADO_CRUD.EDICION);
    }

    public void seleccionarFila(SelectEvent<Persona> evento) {

        if (evento == null || evento.getObject() == null) {
            return;
        }

        seleccionar(evento.getObject());
    }

    // =========================================================
    // FECHAS
    // =========================================================

    public Date getFechaMinimaNacimiento() {

        Calendar calendario = Calendar.getInstance();

        calendario.clear();
        calendario.set(1900, Calendar.JANUARY, 1);

        return calendario.getTime();
    }

    public Date getFechaMaximaNacimiento() {
        return new Date();
    }

    public int getAnioActual() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public String getRangoAniosNacimiento() {
        return "1900:" + getAnioActual();
    }

    // =========================================================
    // VALIDACIÓN DE NOMBRES Y APELLIDOS
    // =========================================================

    public void validarNombre(
            FacesContext contexto,
            UIComponent componente,
            Object valor) {

        validarNombre(contexto, componente.getId(), valor);
    }

    private void validarNombre(
            FacesContext contexto,
            String idComponente,
            Object valor) {

        String texto = valor == null
                ? ""
                : valor.toString().trim();

        String prefijo;

        if ("apellidos".equals(idComponente)) {
            prefijo = "persona.apellidos";
        } else {
            prefijo = "persona.nombres";
        }

        if (texto.isEmpty()) {
            lanzarValidacion(
                    contexto,
                    prefijo + "Requeridos"
            );
        }

        if (texto.length() < 2) {
            lanzarValidacion(
                    contexto,
                    prefijo + "Minimo"
            );
        }

        if (texto.length() > 255) {
            lanzarValidacion(
                    contexto,
                    prefijo + "Maximo"
            );
        }

        if (!PATRON_NOMBRE.matcher(texto).matches()) {
            lanzarValidacion(
                    contexto,
                    prefijo + "Formato"
            );
        }
    }

    // =========================================================
    // VALIDACIÓN FECHA DE NACIMIENTO
    // =========================================================

    public void validarFechaNacimiento(
            FacesContext contexto,
            UIComponent componente,
            Object valor) {

        validarFechaNacimiento(contexto, valor);
    }

    private void validarFechaNacimiento(
            FacesContext contexto,
            Object valor) {

        if (!(valor instanceof Date fecha)) {

            lanzarValidacion(
                    contexto,
                    "persona.fechaNacimientoRequerida"
            );

            return;
        }

        if (fecha.before(getFechaMinimaNacimiento())) {

            lanzarValidacion(
                    contexto,
                    "persona.fechaNacimientoMinima"
            );
        }

        LocalDate fechaNacimiento = Instant.ofEpochMilli(fecha.getTime())
                .atZone(DocumentoModel.ZONA_CLINICA)
                .toLocalDate();
        if (fechaNacimiento.isAfter(LocalDate.now(DocumentoModel.ZONA_CLINICA))) {

            lanzarValidacion(
                    contexto,
                    "persona.fechaNacimientoFutura"
            );
        }
    }

    // =========================================================
    // GUARDAR
    // =========================================================

    public void guardar() {

        if (seleccionado == null) {
            return;
        }

        seleccionado.setNombres(
                normalizar(seleccionado.getNombres())
        );

        seleccionado.setApellidos(
                normalizar(seleccionado.getApellidos())
        );

        if (!validarAntesDeGuardar()) {
            return;
        }

        try {
            switch (getEstado()) {

                case CREACION -> {
                    getDao().guardar(seleccionado);
                    agregarMensaje("persona.creadaCorrectamente");
                    setEstado(ESTADO_CRUD.EDICION);
                }

                case EDICION -> {
                    seleccionado = getDao().actualizar(seleccionado);
                    agregarMensaje("persona.actualizadaCorrectamente");
                    setEstado(ESTADO_CRUD.EDICION);
                }

                case LISTADO -> {
                    return;
                }
            }
            notificarGuardadoExitoso();
        } catch (RuntimeException ex) {
            if (facesContext == null) {
                throw ex;
            }
            agregarMensaje("persona.errorGuardar", FacesMessage.SEVERITY_ERROR);
            facesContext.validationFailed();
            return;
        }

    }

    private void notificarGuardadoExitoso() {
        if (facesContext != null && facesContext.getExternalContext() != null) {
            PrimeFaces.current().ajax().addCallbackParam("personaGuardada", true);
        }
    }

    // =========================================================
    // CANCELAR
    // =========================================================

    public void cancelar() {

        seleccionado = null;

        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cerrarEdicion(
            DocumentoModel documento,
            MedioContactoModel contacto,
            PersonaRolModel personaRol) {
        if (documento != null) {
            documento.cancelar();
        }
        if (contacto != null) {
            contacto.cancelar();
        }
        if (personaRol != null) {
            personaRol.cancelar();
        }
        if (seleccionado == null || seleccionado.getIdPersona() == null) {
            cancelar();
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private String normalizar(String valor) {

        if (valor == null) {
            return null;
        }

        return valor.trim();
    }

    private boolean validarAntesDeGuardar() {
        if (!validarCampo("nombres", seleccionado.getNombres(), true)) {
            return false;
        }
        if (!validarCampo("apellidos", seleccionado.getApellidos(), true)) {
            return false;
        }
        if (!validarCampo("fechaNacimiento", seleccionado.getFechaNacimiento(), false)) {
            return false;
        }
        if (getEstado() == ESTADO_CRUD.EDICION && seleccionado.getIdPersona() != null
                && DocumentoModel.errorFechaParaDui(seleccionado.getFechaNacimiento(),
                        LocalDate.now(DocumentoModel.ZONA_CLINICA)) != null) {
            // Consultar los documentos guardados: la colección de la entidad puede estar desactualizada.
            for (Documento documento : documentoDAO.obtenerPagina(0, Integer.MAX_VALUE,
                    List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL,
                            seleccionado.getIdPersona())), List.of())) {
                if (DocumentoModel.esDui(documento.getIdTipoDocumento())) {
                    agregarErrorCampo("fechaNacimiento", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            facesContext.getApplication().getResourceBundle(facesContext, "msg")
                                    .getString("persona.fechaNacimientoIncompatibleDui"), null));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validarCampo(String idComponente, Object valor, boolean nombre) {
        try {
            if (nombre) {
                validarNombre(facesContext, idComponente, valor);
            } else {
                validarFechaNacimiento(facesContext, valor);
            }
            return true;
        } catch (ValidatorException ex) {
            agregarErrorCampo(idComponente, ex.getFacesMessage());
            return false;
        }
    }

    private void agregarErrorCampo(String idComponente, FacesMessage mensaje) {
        FacesContext contexto = obtenerFacesContext();
        if (contexto == null) {
            throw new IllegalStateException("FacesContext es requerido para validar el formulario");
        }

        UIComponent componente = buscarComponente(contexto, idComponente);
        String clientId = idComponente;
        if (componente != null) {
            clientId = componente.getClientId(contexto);
            if (componente instanceof UIInput entrada) {
                entrada.setValid(false);
            }
        }

        contexto.addMessage(clientId, mensaje);
        contexto.validationFailed();
    }

    private FacesContext obtenerFacesContext() {
        return facesContext == null ? FacesContext.getCurrentInstance() : facesContext;
    }

    private UIComponent buscarComponente(FacesContext contexto, String id) {
        if (contexto == null || contexto.getViewRoot() == null) {
            return null;
        }
        return buscarComponente(contexto.getViewRoot(), id);
    }

    private UIComponent buscarComponente(UIComponent componente, String id) {
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

    private void agregarMensaje(String clave) {
        agregarMensaje(clave, FacesMessage.SEVERITY_INFO);
    }

    private void agregarMensaje(String clave, FacesMessage.Severity severidad) {
        if (facesContext == null) {
            return;
        }
        String mensaje = facesContext
                .getApplication()
                .getResourceBundle(facesContext, "msg")
                .getString(clave);
        facesContext.addMessage(null,
                new FacesMessage(severidad, mensaje, null));
    }
}
