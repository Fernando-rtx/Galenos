package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;

/**
 * Backing bean JSF del catálogo de secuencias de pasos de procedimiento.
 */
@Named
@ViewScoped
public class ProcedimientoPasoSecuenciaModel extends AbstractModel<ProcedimientoPasoSecuencia> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ProcedimientoPasoSecuencia seleccionado;

    private List<ProcedimientoPaso> procedimientosPaso;

    @Inject
    public ProcedimientoPasoSecuenciaModel(
            ProcedimientoPasoSecuenciaDAO secuenciaDAO,
            ProcedimientoPasoDAO procedimientoPasoDAO) {
        super(secuenciaDAO);
        this.procedimientosPaso = procedimientoPasoDAO.obtenerTodos();
    }

    public ProcedimientoPasoSecuencia getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ProcedimientoPasoSecuencia seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<ProcedimientoPaso> getProcedimientosPaso() {
        return procedimientosPaso;
    }

    public void nuevo() {
        seleccionado = new ProcedimientoPasoSecuencia();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ProcedimientoPasoSecuencia seleccionado) {
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
