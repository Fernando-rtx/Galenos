package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;

/**
 * DAO concreto para Persona.
 *
 * <p>No define operaciones especiales todavia; todas las comunes vienen de
 * DefaultDAO.</p>
 */
@Stateless
@LocalBean
public class PersonaDAO extends DefaultDAO<Persona> {

    /** EntityManager inyectado con GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra Persona. */
    public PersonaDAO() {
        super(Persona.class);
    }

    /**
     * Devuelve el EntityManager para persistencia.
     *
     * @return EntityManager inyectado por el contenedor.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
