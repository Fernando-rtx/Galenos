package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

@Named
@FacesConverter(forClass = TipoExamen.class)
public class TipoExamenConverter implements Converter<TipoExamen> {

    private final TipoExamenDAO tipoExamenDAO;

    @Inject
    public TipoExamenConverter(TipoExamenDAO tipoExamenDAO) {
        this.tipoExamenDAO = tipoExamenDAO;
    }

    @Override
    public TipoExamen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return tipoExamenDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoExamen value) {
        if (value == null) {
            return "";
        }
        return value.getIdTipoExamen().toString();
    }
}
