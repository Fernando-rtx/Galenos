package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

/** Conversor Faces de {@link Procedimiento} resuelto por DAO; mismo patrón que PersonaRolConverter. */
@Named
@FacesConverter(forClass = Procedimiento.class)
public class ProcedimientoConverter implements Converter<Procedimiento> {

    private final ProcedimientoDAO procedimientoDAO;

    @Inject
    public ProcedimientoConverter(ProcedimientoDAO procedimientoDAO) {
        this.procedimientoDAO = procedimientoDAO;
    }

    @Override
    public Procedimiento getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return procedimientoDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Procedimiento value) {
        if (value == null) {
            return "";
        }
        return value.getIdProcedimiento().toString();
    }
}
