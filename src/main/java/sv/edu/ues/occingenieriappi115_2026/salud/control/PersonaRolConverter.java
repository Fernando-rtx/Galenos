package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

@FacesConverter(forClass = PersonaRol.class)
public class PersonaRolConverter implements Converter<PersonaRol> {

    private final PersonaRolDAO personaRolDAO;

    @Inject
    public PersonaRolConverter(PersonaRolDAO personaRolDAO) {
        this.personaRolDAO = personaRolDAO;
    }

    @Override
    public PersonaRol getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return personaRolDAO.buscarPorId(UUID.fromString(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, PersonaRol value) {
        if (value == null) {
            return "";
        }
        return value.getIdPersonaRol().toString();
    }
}
