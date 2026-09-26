package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenResultadoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenResultado;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

@Named
@ViewScoped
public class ExamenResultadoModel extends AbstractModel<ExamenResultado> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ExamenResultadoDAO examenResultadoDAO;
    @EJB
    private OrdenExamenDAO ordenExamenDAO;
    private ExamenResultado seleccionado;
    private List<OrdenExamen> ordenExamenes;

    public ExamenResultadoModel() {
    }

    public ExamenResultadoModel(ExamenResultadoDAO examenResultadoDAO, OrdenExamenDAO ordenExamenDAO) {
        this.examenResultadoDAO = examenResultadoDAO;
        this.ordenExamenDAO = ordenExamenDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.ordenExamenes = ordenExamenDAO.obtenerTodos();
    }

    @Override
    protected ExamenResultadoDAO getDao() {
        return examenResultadoDAO;
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
