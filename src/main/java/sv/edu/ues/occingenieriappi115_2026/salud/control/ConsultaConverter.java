package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;

@FacesConverter(forClass = Consulta.class)
public class ConsultaConverter implements Converter<Consulta> {

    private final ConsultaDAO consultaDAO;

    @Inject
    public ConsultaConverter(ConsultaDAO consultaDAO) {
        this.consultaDAO = consultaDAO;
    }

    @Override
    public Consulta getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return consultaDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Consulta value) {
        if (value == null) {
            return "";
        }
        return value.getIdConsulta().toString();
    }
}
