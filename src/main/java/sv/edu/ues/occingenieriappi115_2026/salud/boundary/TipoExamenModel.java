package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

@Named
@ViewScoped
public class TipoExamenModel extends AbstractModel<TipoExamen>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TipoExamenDAO tipoExamenDAO;

    @Override
    protected DAOInterface<TipoExamen> getDAO() {
        return tipoExamenDAO;
    }

    @Override
    public TipoExamen instanciaRegistro() {
        TipoExamen nuevo = new TipoExamen();
        nuevo.setIdTipoExamen(UUID.randomUUID());
        return nuevo;
    }

    @Override
    protected Object getIdByRegistro(TipoExamen registro) {
        if (registro == null) {
            return null;
        }

        return registro.getIdTipoExamen();
    }

    @Override
    protected Object getIdByRowKey(String rowKey) {
        return UUID.fromString(rowKey);
    }

    @Override
    public String getNombreModelo() {
        return "Tipo de examen";
    }
}
