package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

/**
 * Backing bean JSF del catálogo de relaciones procedimiento-paso-examen.
 */
@Named
@ViewScoped
public class ProcedimientoPasoExamenModel extends AbstractModel<ProcedimientoPasoExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ProcedimientoPasoExamen seleccionado;

    private List<ProcedimientoPaso> procedimientosPaso;
    private List<Examen> examenes;

    @Inject
    public ProcedimientoPasoExamenModel(
            ProcedimientoPasoExamenDAO ppeDAO,
            ProcedimientoPasoDAO procedimientoPasoDAO,
            ExamenDAO examenDAO) {
        super(ppeDAO);
        this.procedimientosPaso = procedimientoPasoDAO.obtenerTodos();
        this.examenes = examenDAO.obtenerTodos();
    }

    public ProcedimientoPasoExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ProcedimientoPasoExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<ProcedimientoPaso> getProcedimientosPaso() {
        return procedimientosPaso;
    }

    public List<Examen> getExamenes() {
        return examenes;
    }

    public void nuevo() {
        seleccionado = new ProcedimientoPasoExamen();
        seleccionado.setFechaCreacion(new Date());
        seleccionado.setActivo(true);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ProcedimientoPasoExamen seleccionado) {
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
