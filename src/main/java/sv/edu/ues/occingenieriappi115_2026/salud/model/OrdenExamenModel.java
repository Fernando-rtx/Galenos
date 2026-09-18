package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

@Named
@ViewScoped
public class OrdenExamenModel extends AbstractModel<OrdenExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    private OrdenExamen seleccionado;
    private List<ConsultaProcedimientoPaso> consultaProcedimientoPasos;

    @Inject
    public OrdenExamenModel(
            OrdenExamenDAO ordenExamenDAO,
            ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO) {
        super(ordenExamenDAO);
        this.consultaProcedimientoPasos = consultaProcedimientoPasoDAO.obtenerTodos();
    }

    public OrdenExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(OrdenExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<ConsultaProcedimientoPaso> getConsultaProcedimientoPasos() {
        return consultaProcedimientoPasos;
    }

    public void nuevo() {
        seleccionado = new OrdenExamen();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(OrdenExamen seleccionado) {
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
