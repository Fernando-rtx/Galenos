package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;

@ApplicationScoped
public class PersonaDAO extends DefaultDAO<Persona> {

    public PersonaDAO() {
        super(Persona.class);
    }
}
