package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;

/**
 * DAO concreto para ProcedimientoPasoSecuencia.
 *
 * <p>Esta entidad modela la secuencia entre pasos y por eso tiene DAO propio
 * aunque no tenga consultas especificas todavia.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoPasoSecuenciaDAO
        extends DefaultDAO<ProcedimientoPasoSecuencia> {

    /** EntityManager inyectado por Jakarta EE usando GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ProcedimientoPasoSecuencia. */
    public ProcedimientoPasoSecuenciaDAO() {
        super(ProcedimientoPasoSecuencia.class);
    }

    /**
     * Provee el EntityManager al DAO generico.
     *
     * @return EntityManager configurado por el contenedor.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
