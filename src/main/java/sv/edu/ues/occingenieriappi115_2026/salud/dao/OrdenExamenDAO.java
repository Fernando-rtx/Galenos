package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

/**
 * DAO concreto para OrdenExamen.
 *
 * <p>El DAO existe para que la entidad participe del patron generico comun.</p>
 */
@Stateless
@LocalBean
public class OrdenExamenDAO extends DefaultDAO<OrdenExamen> {

    /** EntityManager inyectado mediante GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra OrdenExamen. */
    public OrdenExamenDAO() {
        super(OrdenExamen.class);
    }

    /**
     * Provee el EntityManager a DefaultDAO.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
