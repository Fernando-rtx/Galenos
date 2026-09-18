package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;

@Named
@ViewScoped
public class ConsultaProcedimientoModel extends AbstractModel<ConsultaProcedimiento> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ConsultaProcedimiento seleccionado;
    private List<Consulta> consultas;

    @Inject
    public ConsultaProcedimientoModel(ConsultaProcedimientoDAO consultaProcedimientoDAO, ConsultaDAO consultaDAO) {
        super(consultaProcedimientoDAO);
        this.consultas = consultaDAO.obtenerTodos();
    }

    public ConsultaProcedimiento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ConsultaProcedimiento seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<Consulta> getConsultas() {
        return consultas;
    }

    public void nuevo() {
        seleccionado = new ConsultaProcedimiento();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ConsultaProcedimiento seleccionado) {
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
