package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

@Named
@ViewScoped
public class ConsultaModel extends AbstractModel<Consulta> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Consulta seleccionado;
    private List<PersonaRol> personasRoles;

    @Inject
    public ConsultaModel(ConsultaDAO consultaDAO, PersonaRolDAO personaRolDAO) {
        super(consultaDAO);
        this.personasRoles = personaRolDAO.obtenerTodos();
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

    public String getIdConsultaContexto() {
        if (seleccionado == null || seleccionado.getIdConsulta() == null) {
            return null;
        }
        return seleccionado.getIdConsulta().toString();
    }

    /**
     * Carga la consulta indicada en la URL para abrirla en edición. Si el id no
     * es un UUID válido, la vista permanece en modo listado.
     *
     * @param id identificador textual de la consulta
     */
    public void setIdConsultaContexto(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        try {
            Consulta consulta = getDao().buscarPorId(UUID.fromString(id));
            if (consulta != null) {
                seleccionar(consulta);
            }
        } catch (IllegalArgumentException ex) {
            seleccionado = null;
            setEstado(ESTADO_CRUD.LISTADO);
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
            lanzarValidacion(contexto, "consulta.fechaFinAnterior");
        }
    }

    public void nuevo() {
        seleccionado = new Consulta();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Consulta seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    /**
     * Abre la edición del registro ya seleccionado por la tabla. La tabla enlaza
     * la selección a {@link #seleccionado} antes de disparar el evento de fila,
     * por lo que este método solo cambia el estado a edición.
     */
    public void editarSeleccionado() {
        if (seleccionado != null) {
            setEstado(ESTADO_CRUD.EDICION);
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
