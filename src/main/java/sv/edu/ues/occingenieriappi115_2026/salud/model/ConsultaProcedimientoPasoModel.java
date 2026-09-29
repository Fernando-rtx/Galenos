package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

@Named
@ViewScoped
public class ConsultaProcedimientoPasoModel extends AbstractModel<ConsultaProcedimientoPaso> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;
    @EJB
    private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @EJB
    private PersonaRolDAO personaRolDAO;
    private ConsultaProcedimientoPaso seleccionado;
    private List<PersonaRol> personasRoles;

    public ConsultaProcedimientoPasoModel() {
    }

    public ConsultaProcedimientoPasoModel(ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO,
            ConsultaProcedimientoDAO consultaProcedimientoDAO, PersonaRolDAO personaRolDAO) {
        this.consultaProcedimientoPasoDAO = consultaProcedimientoPasoDAO;
        this.consultaProcedimientoDAO = consultaProcedimientoDAO;
        this.personaRolDAO = personaRolDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.personasRoles = personaRolDAO.obtenerTodos();
    }

    @Override
    protected ConsultaProcedimientoPasoDAO getDao() {
        return consultaProcedimientoPasoDAO;
    }

    public ConsultaProcedimientoPaso getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ConsultaProcedimientoPaso seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<PersonaRol> getPersonasRoles() {
        return personasRoles;
    }

    /** Busca responsables médicos por nombre o por el rol/especialidad asignado. */
    public List<PersonaRol> buscarMedicos(String consulta) {
        String criterio = normalizarBusqueda(consulta);
        if (criterio.isEmpty() || personasRoles == null) {
            return List.of();
        }
        return personasRoles.stream()
                .filter(this::esMedico)
                .filter(personaRol -> {
                    String nombre = personaRol.getIdPersona() == null ? ""
                            : normalizarBusqueda(texto(personaRol.getIdPersona().getNombres())
                                    + " " + texto(personaRol.getIdPersona().getApellidos()));
                    String rol = normalizarBusqueda(personaRol.getIdRol().getNombre());
                    String detalle = normalizarBusqueda(personaRol.getIdRol().getObservaciones());
                    return nombre.contains(criterio) || rol.contains(criterio)
                            || detalle.contains(criterio);
                })
                .limit(15)
                .toList();
    }

    public String getEtiquetaMedico(PersonaRol medico) {
        if (medico == null || medico.getIdPersona() == null || medico.getIdRol() == null) {
            return "";
        }
        return (texto(medico.getIdPersona().getNombres()) + " "
                + texto(medico.getIdPersona().getApellidos())).trim() + " — "
                + texto(medico.getIdRol().getNombre());
    }

    private boolean esMedico(PersonaRol personaRol) {
        if (personaRol == null || personaRol.getIdPersona() == null
                || personaRol.getIdRol() == null
                || Boolean.FALSE.equals(personaRol.getIdRol().getActivo())) {
            return false;
        }
        String nombreRol = normalizarBusqueda(personaRol.getIdRol().getNombre());
        if ("paciente".equals(nombreRol)) {
            return false;
        }
        String detalleRol = normalizarBusqueda(personaRol.getIdRol().getObservaciones());
        return nombreRol.startsWith("medico")
                || nombreRol.startsWith("doctor")
                || nombreRol.startsWith("especialista")
                || detalleRol.contains("medico")
                || detalleRol.contains("doctor")
                || detalleRol.contains("especialista");
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

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    public void nuevo() {
        seleccionado = new ConsultaProcedimientoPaso();
        seleccionado.setFechaInicio(new Date());
        setEstado(ESTADO_CRUD.CREACION);
    }

    /**
     * Devuelve los pasos de todos los procedimientos de la consulta recibida.
     *
     * @param consulta consulta cuyos pasos se desean mostrar
     * @return pasos de la consulta o lista vacía si aún no existe
     */
    public List<ConsultaProcedimientoPaso> getPasosPorConsulta(Consulta consulta) {
        if (consulta == null || consulta.getIdConsulta() == null) {
            return List.of();
        }
        return getDao().obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idConsultaProcedimiento.idConsulta.idConsulta",
                        OperadorFiltro.IGUAL, consulta.getIdConsulta())),
                List.of());
    }

    /**
     * Devuelve los procedimientos de la consulta para el selector del formulario.
     *
     * @param consulta consulta cuyos procedimientos se desean listar
     * @return procedimientos de la consulta o lista vacía si aún no existe
     */
    public List<ConsultaProcedimiento> getConsultaProcedimientosPorConsulta(Consulta consulta) {
        if (consulta == null || consulta.getIdConsulta() == null) {
            return List.of();
        }
        return consultaProcedimientoDAO.obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idConsulta.idConsulta", OperadorFiltro.IGUAL,
                        consulta.getIdConsulta())),
                List.of());
    }

    /**
     * Selecciona un paso solo cuando pertenece a la consulta del tab.
     *
     * @param paso paso elegido en la tabla contextual
     * @param consulta consulta actualmente editada
     */
    public void seleccionarParaConsulta(ConsultaProcedimientoPaso paso, Consulta consulta) {
        if (perteneceAConsulta(paso, consulta)) {
            seleccionar(paso);
        }
    }

    /**
     * Indica si el formulario activo corresponde a la consulta del tab.
     *
     * @param consulta consulta actualmente editada
     * @return {@code true} si el paso seleccionado pertenece a esa consulta
     */
    public boolean isSeleccionadoParaConsulta(Consulta consulta) {
        return perteneceAConsulta(seleccionado, consulta);
    }

    /**
     * Indica si el formulario de pasos debe mostrarse para la consulta del tab.
     * Un paso recién creado aún no tiene procedimiento asignado, por lo que se
     * muestra igualmente para que el usuario elija el procedimiento en el combo.
     *
     * @param consulta consulta actualmente editada
     * @return {@code true} si hay un paso nuevo o un paso de esa consulta en edición
     */
    public boolean isFormularioVisibleParaConsulta(Consulta consulta) {
        if (consulta == null || consulta.getIdConsulta() == null) {
            return false;
        }
        if (getEstado() == ESTADO_CRUD.CREACION
                && seleccionado != null
                && seleccionado.getIdConsultaProcedimiento() == null) {
            return true;
        }
        return isSeleccionadoParaConsulta(consulta);
    }

    private boolean perteneceAConsulta(ConsultaProcedimientoPaso paso, Consulta consulta) {
        return paso != null
                && paso.getIdConsultaProcedimiento() != null
                && paso.getIdConsultaProcedimiento().getIdConsulta() != null
                && paso.getIdConsultaProcedimiento().getIdConsulta().getIdConsulta() != null
                && consulta != null
                && consulta.getIdConsulta() != null
                && paso.getIdConsultaProcedimiento().getIdConsulta().getIdConsulta()
                        .equals(consulta.getIdConsulta());
    }

    public void seleccionar(ConsultaProcedimientoPaso seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Valida que el responsable elegido exista en el catálogo cargado. La
     * relación con la plantilla de paso no existe en la base, así que no se
     * infiere un rol a partir de ella.
     *
     * @param contexto contexto Faces activo
     * @param componente componente que dispara la validación
     * @param valor responsable elegido
     */
    public void validarResponsable(FacesContext contexto, UIComponent componente, Object valor) {
        if (!(valor instanceof PersonaRol responsable)) {
            return;
        }
        if (!personasRoles.contains(responsable)) {
            lanzarValidacion(contexto, "consultaProcedimientoPaso.responsableNoExiste");
        }
        if (!esMedico(responsable)) {
            lanzarValidacion(contexto, "consultaProcedimientoPaso.responsableDebeSerMedico");
        }
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
            lanzarValidacion(contexto, "consultaProcedimientoPaso.fechaFinAnterior");
        }
    }
}
