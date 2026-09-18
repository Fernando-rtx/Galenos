package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

/**
 * Backing bean JSF del catálogo de procedimientos.
 */
@Named
@ViewScoped
public class ProcedimientoModel extends AbstractModel<Procedimiento> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Procedimiento seleccionado;

    @Inject
    public ProcedimientoModel(ProcedimientoDAO procedimientoDAO) {
        super(procedimientoDAO);
    }

    public Procedimiento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Procedimiento seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new Procedimiento();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Procedimiento seleccionado) {
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
