package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenResultadoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenResultado;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

@Named
@ViewScoped
public class ExamenResultadoModel extends AbstractModel<ExamenResultado> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ExamenResultado seleccionado;
    private List<OrdenExamen> ordenExamenes;
    private final OrdenExamenDAO ordenExamenDAO;
    private String idOrdenContexto;
    private OrdenExamen ordenContexto;

    @Inject
    public ExamenResultadoModel(
            ExamenResultadoDAO examenResultadoDAO,
            OrdenExamenDAO ordenExamenDAO) {
        super(examenResultadoDAO);
        this.ordenExamenDAO = ordenExamenDAO;
        this.ordenExamenes = ordenExamenDAO.obtenerTodos();
    }

    public String getIdOrdenContexto() {
        return idOrdenContexto;
    }

    public void setIdOrdenContexto(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        this.idOrdenContexto = id;
        try {
            OrdenExamen orden = ordenExamenDAO.buscarPorId(UUID.fromString(id));
            ordenContexto = orden;
            ordenExamenes = orden == null ? List.of() : List.of(orden);
        } catch (IllegalArgumentException ex) {
            // id inválido: se conserva la lista completa cargada en el constructor
        }
    }

    public ExamenResultado getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ExamenResultado seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<OrdenExamen> getOrdenExamenes() {
        return ordenExamenes;
    }

    public void nuevo() {
        seleccionado = new ExamenResultado();
        seleccionado.setFechaCreacion(new Date());
        if (ordenContexto != null && ordenContexto.getIdOrdenExamen() != null) {
            seleccionado.setIdOrdenExamen(ordenContexto);
        }
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ExamenResultado seleccionado) {
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
}
