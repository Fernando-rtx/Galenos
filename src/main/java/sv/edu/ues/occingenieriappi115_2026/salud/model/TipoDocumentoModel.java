package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoDocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

@Named
@ViewScoped
public class TipoDocumentoModel extends AbstractModel<TipoDocumento> implements Serializable {

    private static final long serialVersionUID = 1L;

    private TipoDocumento seleccionado;

    @Inject
    public TipoDocumentoModel(TipoDocumentoDAO tipoDocumentoDAO) {
        super(tipoDocumentoDAO);
    }

    public TipoDocumento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(TipoDocumento seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new TipoDocumento();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoDocumento seleccionado) {
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
