package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/**
 * Backing bean JSF del catálogo de roles.
 */
@Named
@ViewScoped
public class RolModel extends AbstractModel<Rol> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private RolDAO rolDAO;

    private Rol seleccionado;

    public RolModel() {
    }

    public RolModel(RolDAO rolDAO) {
        this.rolDAO = rolDAO;
    }

    @Override
    protected RolDAO getDao() {
        return rolDAO;
    }

    public Rol getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Rol seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new Rol();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Rol seleccionado) {
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
