package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.boundary.ClinicaActualBean;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FlujoConsultaException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

@Named
@ViewScoped
public class ConsultaProcedimientoModel extends AbstractModel<ConsultaProcedimiento> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @Inject private ClinicaActualBean clinicaActualBean;
    @EJB private ConsultaFlujoService flujoService;
    private UUID clinicaFormulario;
    private long revisionFormulario;
    private ConsultaProcedimiento seleccionado;
    private List<Procedimiento> procedimientos;
    @EJB
    private ProcedimientoDAO procedimientoDAO;
    private final Map<UUID, String> nombresProcedimientos = new HashMap<>();

    public ConsultaProcedimientoModel() {
    }

    public ConsultaProcedimientoModel(ConsultaProcedimientoDAO consultaProcedimientoDAO,
            ProcedimientoDAO procedimientoDAO) {
        this.consultaProcedimientoDAO = consultaProcedimientoDAO;
        this.procedimientoDAO = procedimientoDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.procedimientos = procedimientoDAO.obtenerTodos().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .toList();
    }

    private UUID clinicaActual() {
        return clinicaActualBean == null ? null : clinicaActualBean.getIdClinicaActual();
    }

    private void capturarContexto() {
        clinicaFormulario = clinicaActual();
        revisionFormulario = clinicaActualBean == null ? -1 : clinicaActualBean.getRevision();
    }

    private boolean contextoValido() {
        return clinicaFormulario != null && clinicaFormulario.equals(clinicaActual())
                && revisionFormulario == clinicaActualBean.getRevision();
    }

    private boolean consultaVisible(Consulta consulta) {
        return consulta != null && ConsultaFlujoService.personaEnClinica(consulta.getIdPersonaRol(), clinicaActual());
    }

    @Override
    protected ConsultaProcedimientoDAO getDao() {
        return consultaProcedimientoDAO;
    }

    public ConsultaProcedimiento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ConsultaProcedimiento seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<Procedimiento> getProcedimientos() {
        inicializar();
        return procedimientos;
    }

    /**
     * Resuelve el nombre del procedimiento de catálogo asociado a un registro
     * de consulta. El resultado se cachea para no repetir la búsqueda por cada
     * fila renderizada.
     *
     * @param consultaProcedimiento registro cuyo procedimiento se desea mostrar
     * @return nombre del procedimiento, el id si no existe en el catálogo, o
     * cadena vacía si no hay procedimiento asignado
     */
    public String getNombreProcedimiento(ConsultaProcedimiento consultaProcedimiento) {
        if (consultaProcedimiento == null || consultaProcedimiento.getIdProcedimiento() == null) {
            return "";
        }
        return nombresProcedimientos.computeIfAbsent(
                consultaProcedimiento.getIdProcedimiento(), this::buscarNombreProcedimiento);
    }

    private String buscarNombreProcedimiento(UUID idProcedimiento) {
        Procedimiento procedimiento = procedimientoDAO.buscarPorId(idProcedimiento);
        return procedimiento != null && procedimiento.getNombre() != null
                ? procedimiento.getNombre()
                : idProcedimiento.toString();
    }

    public void nuevo() {
        capturarContexto();
        seleccionado = new ConsultaProcedimiento();
        seleccionado.setFechaInicio(new Date());
        setEstado(ESTADO_CRUD.CREACION);
    }

    /**
     * Devuelve los procedimientos asociados a la consulta recibida. La consulta
     * se mantiene en el DAO de ConsultaProcedimiento a través del contrato
     * genérico, sin trasladar su CRUD al modelo de Consulta.
     *
     * @param consulta consulta dueña de los procedimientos a mostrar
     * @return procedimientos de la consulta o lista vacía si aún no existe
     */
    public List<ConsultaProcedimiento> getProcedimientosPorConsulta(Consulta consulta) {
        if (consulta == null || consulta.getIdConsulta() == null || !consultaVisible(consulta)) {
            return List.of();
        }
        return getDao().obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idConsulta.idConsulta", OperadorFiltro.IGUAL,
                        consulta.getIdConsulta()),
                        new FiltroDAO("idConsulta.idPersonaRol.idClinica.idClinica", OperadorFiltro.IGUAL, clinicaActual())),
                List.of());
    }

    /**
     * Inicia un procedimiento dentro del contexto de una consulta persistida.
     *
     * @param consulta consulta que será asignada al procedimiento
     */
    public void nuevoParaConsulta(Consulta consulta) {
        if (consulta == null || consulta.getIdConsulta() == null || !consultaVisible(consulta)) {
            cancelar();
            return;
        }
        nuevo();
        seleccionado.setIdConsulta(consulta);
    }

    /**
     * Selecciona un procedimiento solo cuando pertenece a la consulta del tab.
     *
     * @param procedimiento procedimiento elegido en la tabla contextual
     * @param consulta consulta actualmente editada
     */
    public void seleccionarParaConsulta(ConsultaProcedimiento procedimiento, Consulta consulta) {
        if (perteneceAConsulta(procedimiento, consulta)) {
            seleccionar(procedimiento);
        }
    }

    /**
     * Indica si el formulario activo corresponde a la consulta del tab.
     *
     * @param consulta consulta actualmente editada
     * @return {@code true} si el procedimiento seleccionado pertenece a esa consulta
     */
    public boolean isSeleccionadoParaConsulta(Consulta consulta) {
        return perteneceAConsulta(seleccionado, consulta);
    }

    private boolean perteneceAConsulta(ConsultaProcedimiento procedimiento, Consulta consulta) {
        return procedimiento != null
                && procedimiento.getIdConsulta() != null
                && procedimiento.getIdConsulta().getIdConsulta() != null
                && consulta != null
                && consulta.getIdConsulta() != null
                && consultaVisible(consulta)
                && procedimiento.getIdConsulta().getIdConsulta().equals(consulta.getIdConsulta());
    }

    public void seleccionar(ConsultaProcedimiento seleccionado) {
        capturarContexto();
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) { return; }
        if (!contextoValido()) { registrarError("consultaFlujo.contextoInvalido"); return; }
        try {
            seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
            seleccionado = getEstado() == ESTADO_CRUD.CREACION
                    ? flujoService.crearProcedimiento(seleccionado, clinicaActual())
                    : flujoService.actualizarProcedimiento(seleccionado, clinicaActual());
            setEstado(ESTADO_CRUD.LISTADO);
            agregarMensaje(jakarta.faces.application.FacesMessage.SEVERITY_INFO, "mensajes.guardado");
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
            lanzarValidacion(contexto, "consultaProcedimiento.fechaFinAnterior");
        }
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.trim();
    }
}
