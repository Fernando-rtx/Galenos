package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

/**
 * DAO concreto para ProcedimientoPasoExamen.
 *
 * <p>La entidad conserva su propio DAO porque la tabla puente tiene datos
 * propios como activo, fecha y observaciones.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoPasoExamenDAO
        extends DefaultDAO<ProcedimientoPasoExamen> {

    /** EntityManager inyectado con la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ProcedimientoPasoExamen. */
    public ProcedimientoPasoExamenDAO() {
        super(ProcedimientoPasoExamen.class);
    }

    /**
     * Devuelve el EntityManager que usara DefaultDAO.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
