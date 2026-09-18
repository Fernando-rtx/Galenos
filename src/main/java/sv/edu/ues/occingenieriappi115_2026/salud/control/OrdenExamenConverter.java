package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

@FacesConverter(forClass = OrdenExamen.class)
public class OrdenExamenConverter implements Converter<OrdenExamen> {

    private final OrdenExamenDAO ordenExamenDAO;

    @Inject
    public OrdenExamenConverter(OrdenExamenDAO ordenExamenDAO) {
        this.ordenExamenDAO = ordenExamenDAO;
    }

    @Override
    public OrdenExamen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ordenExamenDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, OrdenExamen value) {
        if (value == null) {
            return "";
        }
        return value.getIdOrdenExamen().toString();
    }
}
