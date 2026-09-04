package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

/**
 * DAO concreto para Procedimiento.
 *
 * <p>Centraliza el acceso a persistencia de Procedimiento mediante DefaultDAO.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoDAO extends DefaultDAO<Procedimiento> {

    /** EntityManager inyectado con GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra Procedimiento. */
    public ProcedimientoDAO() {
        super(Procedimiento.class);
    }

    /**
     * Devuelve el EntityManager usado por las operaciones heredadas.
     *
     * @return EntityManager de persistencia.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
