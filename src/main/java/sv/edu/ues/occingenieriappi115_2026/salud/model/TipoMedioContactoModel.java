package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoMedioContactoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;

/**
 * Backing bean JSF para administrar {@link TipoMedioContacto}.
 *
 * <p>Es serializable porque vive en {@code @ViewScoped}; {@code @Named} permite
 * usarlo desde EL y CDI inyecta {@link TipoMedioContactoDAO}. La carga lazy se
 * hereda de {@link AbstractModel} y este bean controla la selección y estados.</p>
 */
@Named
@ViewScoped
public class TipoMedioContactoModel extends AbstractModel<TipoMedioContacto> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TipoMedioContactoDAO tipoMedioContactoDAO;

    /** Entidad actualmente enlazada al formulario del diálogo. */
    private TipoMedioContacto seleccionado;

    public TipoMedioContactoModel() {
    }

    public TipoMedioContactoModel(TipoMedioContactoDAO tipoMedioContactoDAO) {
        this.tipoMedioContactoDAO = tipoMedioContactoDAO;
    }

    @Override
    protected TipoMedioContactoDAO getDao() {
        return tipoMedioContactoDAO;
    }

    public TipoMedioContacto getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(TipoMedioContacto seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        // Inicia una creación sin persistir todavía.
        seleccionado = new TipoMedioContacto();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoMedioContacto seleccionado) {
        // Cambia a edición con la fila entregada por PrimeFaces.
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        // Delega en el DAO común según CREACION o EDICION.
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
        // Limpia el formulario y regresa a LISTADO sin tocar la base.
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
