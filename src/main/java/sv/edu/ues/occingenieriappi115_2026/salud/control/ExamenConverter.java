package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;

@FacesConverter(forClass = Examen.class)
public class ExamenConverter implements Converter<Examen> {

    private final ExamenDAO examenDAO;

    @Inject
    public ExamenConverter(ExamenDAO examenDAO) {
        this.examenDAO = examenDAO;
    }

    @Override
    public Examen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return examenDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Examen value) {
        if (value == null) {
            return "";
        }
        return value.getIdExamen().toString();
    }
}
