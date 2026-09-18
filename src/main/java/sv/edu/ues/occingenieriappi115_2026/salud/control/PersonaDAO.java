package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;

/** DAO EJB de {@link Persona}; mantiene la clase concreta y reutiliza toda la infraestructura de {@link DefaultDAO}. */
@Stateless
@LocalBean
public class PersonaDAO extends DefaultDAO<Persona> {

    /** Configura el DAO genérico para personas. */
    public PersonaDAO() {
        super(Persona.class);
    }
}
