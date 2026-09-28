package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

/** Conversor Faces de {@link ProcedimientoPaso} resuelto por DAO; mismo patrón que PersonaRolConverter. */
@Named
@FacesConverter(forClass = ProcedimientoPaso.class)
public class ProcedimientoPasoConverter implements Converter<ProcedimientoPaso> {

    private final ProcedimientoPasoDAO procedimientoPasoDAO;

    @Inject
    public ProcedimientoPasoConverter(ProcedimientoPasoDAO procedimientoPasoDAO) {
        this.procedimientoPasoDAO = procedimientoPasoDAO;
    }

    @Override
    public ProcedimientoPaso getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return procedimientoPasoDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProcedimientoPaso value) {
        if (value == null) {
            return "";
        }
        return value.getIdProcedimientoPaso().toString();
    }
}
