package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenResultado;

/**
 * DAO concreto para ExamenResultado.
 *
 * <p>Se apoya en DefaultDAO para operaciones genericas de persistencia.</p>
 */
@Stateless
@LocalBean
public class ExamenResultadoDAO extends DefaultDAO<ExamenResultado> {

    /** EntityManager inyectado por GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ExamenResultado. */
    public ExamenResultadoDAO() {
        super(ExamenResultado.class);
    }

    /**
     * Provee el EntityManager requerido por DefaultDAO.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
