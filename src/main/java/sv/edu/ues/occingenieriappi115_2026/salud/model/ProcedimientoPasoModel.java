package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/**
 * Backing bean JSF del catálogo de pasos de procedimiento.
 */
@Named
@ViewScoped
public class ProcedimientoPasoModel extends AbstractModel<ProcedimientoPaso> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ProcedimientoPaso seleccionado;

    private List<Procedimiento> procedimientos;
    private List<Rol> roles;

    @Inject
    public ProcedimientoPasoModel(
            ProcedimientoPasoDAO procedimientoPasoDAO,
            ProcedimientoDAO procedimientoDAO,
            RolDAO rolDAO) {
        super(procedimientoPasoDAO);
        this.procedimientos = procedimientoDAO.obtenerTodos();
        this.roles = rolDAO.obtenerTodos();
    }

    public ProcedimientoPaso getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ProcedimientoPaso seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<Procedimiento> getProcedimientos() {
        return procedimientos;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void nuevo() {
        seleccionado = new ProcedimientoPaso();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ProcedimientoPaso seleccionado) {
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
