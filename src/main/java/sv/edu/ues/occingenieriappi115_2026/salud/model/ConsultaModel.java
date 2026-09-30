package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
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
        List<PersonaRol> roles = personaRolDAO == null ? List.of() : personaRolDAO.obtenerTodos();
        this.personasRoles = roles == null ? List.of() : roles;
        duiPorPersona.clear();
        if (documentoDAO == null) {
            return;
        }
        List<Documento> documentos = documentoDAO.obtenerTodos();
        if (documentos == null) {
            return;
        }
        for (Documento documento : documentos) {
            if (documento == null || documento.getValor() == null
                    || documento.getIdPersona() == null
                    || documento.getIdPersona().getIdPersona() == null
                    || documento.getIdTipoDocumento() == null
                    || !"dui".equals(normalizarBusqueda(documento.getIdTipoDocumento().getNombre()))) {
                continue;
            }
            duiPorPersona.putIfAbsent(documento.getIdPersona().getIdPersona(), documento.getValor());
        }
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
        String nombreBuscado = normalizarBusqueda(consulta);
        String duiBuscado = consulta == null ? "" : consulta.replaceAll("\\D", "");
        if (personasRoles == null || (nombreBuscado.isEmpty() && duiBuscado.isEmpty())) {
            return List.of();
        }
        return personasRoles.stream()
                .filter(this::esPaciente)
                .filter(paciente -> coincideNombre(paciente, nombreBuscado)
                        || coincideDui(paciente, duiBuscado))
                .limit(15)
                .toList();
    }

    public String getDui(PersonaRol personaRol) {
        if (personaRol == null || personaRol.getIdPersona() == null
                || personaRol.getIdPersona().getIdPersona() == null) {
            return null;
        }
        return duiPorPersona.get(personaRol.getIdPersona().getIdPersona());
    }

    public String getEtiquetaPaciente(PersonaRol paciente) {
        String nombre = nombreCompleto(paciente);
        String dui = getDui(paciente);
        return dui == null || dui.isBlank() ? nombre : nombre + " — DUI: " + dui;
    }

    private boolean esPaciente(PersonaRol personaRol) {
        return personaRol != null
                && personaRol.getIdRol() != null
                && !Boolean.FALSE.equals(personaRol.getIdRol().getActivo())
                && "paciente".equals(normalizarBusqueda(personaRol.getIdRol().getNombre()));
    }

    private boolean coincideNombre(PersonaRol personaRol, String consulta) {
        if (consulta.isEmpty() || personaRol.getIdPersona() == null) {
            return false;
        }
        String nombreCompleto = normalizarBusqueda(nombreCompleto(personaRol));
        return nombreCompleto.contains(consulta);
    }

    private boolean coincideDui(PersonaRol personaRol, String consulta) {
        if (consulta.isEmpty()) {
            return false;
        }
        String dui = getDui(personaRol);
        return dui != null && dui.replaceAll("\\D", "").contains(consulta);
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
        seleccionado = new Consulta();
        seleccionado.setFechaInicio(new Date());
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Consulta seleccionado) {
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
        if (seleccionado == null) {
            return;
        }
        seleccionado.setReferenciaExterna(normalizar(seleccionado.getReferenciaExterna()));
        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
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

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.trim();
    }
}
