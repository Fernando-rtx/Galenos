package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;

/**
 * DAO concreto para Clinica.
 *
 * <p>Hereda las operaciones genericas de DefaultDAO y solo define la entidad
 * JPA administrada.</p>
 */
@Stateless
@LocalBean
public class ClinicaDAO extends DefaultDAO<Clinica> {

    /** EntityManager inyectado con la unidad de persistencia GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra Clinica. */
    public ClinicaDAO() {
        super(Clinica.class);
    }

    /**
     * Provee el EntityManager usado por DefaultDAO.
     *
     * @return EntityManager inyectado por Jakarta EE.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
