package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;

/**
 * DAO concreto para ConsultaProcedimientoPaso.
 *
 * <p>Se limita a conectar la entidad concreta con DefaultDAO.</p>
 */
@Stateless
@LocalBean
public class ConsultaProcedimientoPasoDAO
        extends DefaultDAO<ConsultaProcedimientoPaso> {

    /** EntityManager inyectado con la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ConsultaProcedimientoPaso. */
    public ConsultaProcedimientoPasoDAO() {
        super(ConsultaProcedimientoPaso.class);
    }

    /**
     * Devuelve el EntityManager usado para persistencia.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
