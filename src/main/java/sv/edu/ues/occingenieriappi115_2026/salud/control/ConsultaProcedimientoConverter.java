package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;

@FacesConverter(forClass = ConsultaProcedimiento.class)
public class ConsultaProcedimientoConverter implements Converter<ConsultaProcedimiento> {

    private final ConsultaProcedimientoDAO consultaProcedimientoDAO;

    @Inject
    public ConsultaProcedimientoConverter(ConsultaProcedimientoDAO consultaProcedimientoDAO) {
        this.consultaProcedimientoDAO = consultaProcedimientoDAO;
    }

    @Override
    public ConsultaProcedimiento getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return consultaProcedimientoDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ConsultaProcedimiento value) {
        if (value == null) {
            return "";
        }
        return value.getIdConsultaProcedimiento().toString();
    }
}
