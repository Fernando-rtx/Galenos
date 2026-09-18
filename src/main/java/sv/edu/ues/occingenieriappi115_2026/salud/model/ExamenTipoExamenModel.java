package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenTipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

@Named
@ViewScoped
public class ExamenTipoExamenModel extends AbstractModel<ExamenTipoExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ExamenTipoExamen seleccionado;
    private List<Examen> examenes;
    private List<TipoExamen> tipoExamenes;

    @Inject
    public ExamenTipoExamenModel(
            ExamenTipoExamenDAO examenTipoExamenDAO,
            ExamenDAO examenDAO,
            TipoExamenDAO tipoExamenDAO) {
        super(examenTipoExamenDAO);
        this.examenes = examenDAO.obtenerTodos();
        this.tipoExamenes = tipoExamenDAO.obtenerTodos();
    }

    public ExamenTipoExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ExamenTipoExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<Examen> getExamenes() {
        return examenes;
    }

    public List<TipoExamen> getTipoExamenes() {
        return tipoExamenes;
    }

    public void nuevo() {
        seleccionado = new ExamenTipoExamen();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ExamenTipoExamen seleccionado) {
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
