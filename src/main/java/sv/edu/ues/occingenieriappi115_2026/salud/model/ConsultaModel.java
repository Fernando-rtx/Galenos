package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import sv.edu.ues.occingenieriappi115_2026.salud.boundary.ClinicaActualBean;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FlujoConsultaException;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

@Named
@ViewScoped
public class ConsultaModel extends AbstractModel<Consulta> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ConsultaDAO consultaDAO;
    @EJB
    private PersonaRolDAO personaRolDAO;
    @EJB
    private DocumentoDAO documentoDAO;
    @Inject private ClinicaActualBean clinicaActualBean;
    @EJB private ConsultaFlujoService flujoService;
    private UUID clinicaFormulario;
    private long revisionFormulario;
    private Consulta seleccionado;
    private List<PersonaRol> personasRoles;
    private final Map<UUID, String> duiPorPersona = new HashMap<>();

    public ConsultaModel() {
    }

    public ConsultaModel(ConsultaDAO consultaDAO, PersonaRolDAO personaRolDAO) {
        this.consultaDAO = consultaDAO;
        this.personaRolDAO = personaRolDAO;
        inicializar();
    }

    public ConsultaModel(ConsultaDAO consultaDAO, PersonaRolDAO personaRolDAO,
            DocumentoDAO documentoDAO) {
        this.consultaDAO = consultaDAO;
        this.personaRolDAO = personaRolDAO;
        this.documentoDAO = documentoDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.personasRoles = List.of();
        duiPorPersona.clear();
    }

    public ConsultaModel(ConsultaDAO dao, PersonaRolDAO personas, DocumentoDAO documentos,
            ClinicaActualBean contexto, ConsultaFlujoService flujo) {
        this(dao, personas, documentos);
        clinicaActualBean = contexto;
        flujoService = flujo;
    }

    public boolean isClinicaSeleccionada() {
        return clinicaActualBean != null && clinicaActualBean.isSeleccionada();
    }

    private UUID clinicaActual() {
        return isClinicaSeleccionada() ? clinicaActualBean.getIdClinicaActual() : null;
    }

    private void capturarContexto() {
        clinicaFormulario = clinicaActual();
        revisionFormulario = clinicaActualBean == null ? -1 : clinicaActualBean.getRevision();
    }

    @Override
    protected boolean isConsultaPermitida() { return isClinicaSeleccionada(); }

    @Override
    protected List<FiltroDAO> filtrosAdicionales() {
        return List.of(new FiltroDAO("idPersonaRol.idClinica.idClinica", OperadorFiltro.IGUAL, clinicaActual()));
    }

    @Override
    public Consulta getRowData(String id) {
        if (!isClinicaSeleccionada()) { return null; }
        Consulta consulta = super.getRowData(id);
        return consulta != null && ConsultaFlujoService.personaEnClinica(consulta.getIdPersonaRol(), clinicaActual())
                ? consulta : null;
    }

    @Override
    protected ConsultaDAO getDao() {
        return consultaDAO;
    }

    public Consulta getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Consulta seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<PersonaRol> getPersonasRoles() {
        return personasRoles;
    }

    /** Devuelve solo pacientes que coinciden por nombre o número de DUI. */
    public List<PersonaRol> buscarPacientes(String consulta) {
        return personaRolDAO.buscarPacientesPorClinica(clinicaActual(), consulta, 15);
    }

    public String getDui(PersonaRol personaRol) {
        if (personaRol == null || personaRol.getIdPersona() == null
                || personaRol.getIdPersona().getIdPersona() == null) {
            return null;
        }
        UUID id = personaRol.getIdPersona().getIdPersona();
        if (!duiPorPersona.containsKey(id) && documentoDAO != null) {
            String dui = documentoDAO.obtenerPagina(0, Integer.MAX_VALUE,
                    List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL, id)), List.of())
                    .stream().filter(d -> d.getIdTipoDocumento() != null
                        && "dui".equals(normalizarBusqueda(d.getIdTipoDocumento().getNombre())))
                    .map(Documento::getValor).filter(java.util.Objects::nonNull).findFirst().orElse(null);
            duiPorPersona.put(id, dui);
        }
        return duiPorPersona.get(id);
    }

    public String getEtiquetaPaciente(PersonaRol paciente) {
        String nombre = nombreCompleto(paciente);
        String dui = getDui(paciente);
        return dui == null || dui.isBlank() ? nombre : nombre + " — DUI: " + dui;
    }

    private String nombreCompleto(PersonaRol personaRol) {
        if (personaRol == null || personaRol.getIdPersona() == null) {
            return "";
        }
        String nombres = personaRol.getIdPersona().getNombres();
        String apellidos = personaRol.getIdPersona().getApellidos();
        return ((nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos)).trim();
    }

    private String normalizarBusqueda(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Valida que la fecha de fin no sea anterior a la de inicio. La fecha de fin
     * es opcional, por lo que un valor nulo se acepta.
     *
     * @param contexto contexto Faces activo
     * @param componente componente que dispara la validación
     * @param valor fecha de fin ingresada
     */
    public void validarFechaFin(FacesContext contexto, UIComponent componente, Object valor) {
        if (!(valor instanceof Date fechaFin)) {
            return;
        }
        if (seleccionado == null || seleccionado.getFechaInicio() == null) {
            return;
        }
        if (fechaFin.before(seleccionado.getFechaInicio())) {
            lanzarValidacion(contexto, "consulta.fechaFinAnterior");
        }
    }

    public void nuevo() {
        capturarContexto();
        seleccionado = new Consulta();
        seleccionado.setFechaInicio(new Date());
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Consulta seleccionado) {
        capturarContexto();
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    /** Selecciona la consulta que PrimeFaces entrega al evento de doble clic. */
    public void seleccionarFila(SelectEvent<Consulta> evento) {
        if (evento != null && evento.getObject() != null) {
            seleccionar(evento.getObject());
        }
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) { return; }
        if (clinicaFormulario == null || !clinicaFormulario.equals(clinicaActual())
                || revisionFormulario != clinicaActualBean.getRevision()) {
            registrarError("consultaFlujo.contextoInvalido");
            return;
        }
        seleccionado.setReferenciaExterna(normalizar(seleccionado.getReferenciaExterna()));
        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
        try {
            seleccionado = flujoService.guardarConsulta(seleccionado, clinicaActual(), getEstado() == ESTADO_CRUD.EDICION);
            setEstado(ESTADO_CRUD.LISTADO);
        } catch (FlujoConsultaException ex) {
            registrarError(ex.getMessage());
        } catch (RuntimeException ex) {
            registrarError("consultaFlujo.errorGuardar");
        }
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.trim();
    }
}
