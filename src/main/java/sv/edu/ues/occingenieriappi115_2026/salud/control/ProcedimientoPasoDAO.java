package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

/**
 * DAO concreto para ProcedimientoPaso.
 *
 * <p>El DAO permite persistir los pasos de un procedimiento usando el contrato
 * generico.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoPasoDAO extends DefaultDAO<ProcedimientoPaso> {

    /** EntityManager inyectado mediante GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ProcedimientoPaso. */
    public ProcedimientoPasoDAO() {
        super(ProcedimientoPaso.class);
    }

    /**
     * Provee el EntityManager al padre generico.
     *
     * @return EntityManager de JPA.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
