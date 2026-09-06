package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;

/**
 * DAO concreto para ConsultaProcedimiento.
 *
 * <p>Representa el punto de persistencia de la entidad que relaciona una
 * consulta con un procedimiento.</p>
 */
@Stateless
@LocalBean
public class ConsultaProcedimientoDAO
        extends DefaultDAO<ConsultaProcedimiento> {

    /** EntityManager inyectado por Jakarta EE usando GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ConsultaProcedimiento. */
    public ConsultaProcedimientoDAO() {
        super(ConsultaProcedimiento.class);
    }

    /**
     * Provee el EntityManager al DAO generico.
     *
     * @return EntityManager de persistencia.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
