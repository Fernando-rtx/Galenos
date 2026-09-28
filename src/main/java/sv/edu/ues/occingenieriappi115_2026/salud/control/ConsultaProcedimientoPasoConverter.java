package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;

@Named
@FacesConverter(forClass = ConsultaProcedimientoPaso.class)
public class ConsultaProcedimientoPasoConverter implements Converter<ConsultaProcedimientoPaso> {

    private final ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;

    @Inject
    public ConsultaProcedimientoPasoConverter(ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO) {
        this.consultaProcedimientoPasoDAO = consultaProcedimientoPasoDAO;
    }

    @Override
    public ConsultaProcedimientoPaso getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return consultaProcedimientoPasoDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ConsultaProcedimientoPaso value) {
        if (value == null) {
            return "";
        }
        return value.getIdConsultaProcedimientoPaso().toString();
    }
}
