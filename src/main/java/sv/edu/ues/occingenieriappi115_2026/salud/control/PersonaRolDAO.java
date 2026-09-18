package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

/** DAO EJB de {@link PersonaRol}, asociación entre persona, rol y clínica. */
@Stateless
@LocalBean
public class PersonaRolDAO extends DefaultDAO<PersonaRol> {

    /** Indica a la base genérica qué entidad asociativa administra. */
    public PersonaRolDAO() {
        super(PersonaRol.class);
    }
}
