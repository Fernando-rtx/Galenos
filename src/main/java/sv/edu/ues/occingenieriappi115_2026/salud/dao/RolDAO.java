package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/**
 * DAO concreto para Rol.
 *
 * <p>Hereda las operaciones comunes del DAO generico.</p>
 */
@Stateless
@LocalBean
public class RolDAO extends DefaultDAO<Rol> {

    /** EntityManager inyectado con GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra Rol. */
    public RolDAO() {
        super(Rol.class);
    }

    /**
     * Devuelve el EntityManager de la unidad de persistencia.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
