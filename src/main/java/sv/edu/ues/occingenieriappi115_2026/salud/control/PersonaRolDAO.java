package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

@ApplicationScoped
public class PersonaRolDAO extends DefaultDAO<PersonaRol> {

    public PersonaRolDAO() {
        super(PersonaRol.class);
    }
}
