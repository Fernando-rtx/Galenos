package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

/**
 * DAO concreto para PersonaRol.
 *
 * <p>PersonaRol relaciona persona, rol y clinica, por lo que tambien participa
 * como entidad persistible propia.</p>
 */
@Stateless
@LocalBean
public class PersonaRolDAO extends DefaultDAO<PersonaRol> {

    /** EntityManager inyectado usando la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra PersonaRol. */
    public PersonaRolDAO() {
        super(PersonaRol.class);
    }

    /**
     * Provee el EntityManager al DAO generico.
     *
     * @return EntityManager de JPA.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
