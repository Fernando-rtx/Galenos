package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto;

/**
 * DAO concreto para MedioContacto.
 *
 * <p>El comportamiento comun de persistencia se hereda de DefaultDAO.</p>
 */
@Stateless
@LocalBean
public class MedioContactoDAO extends DefaultDAO<MedioContacto> {

    /** EntityManager inyectado para la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra MedioContacto. */
    public MedioContactoDAO() {
        super(MedioContacto.class);
    }

    /**
     * Devuelve el EntityManager configurado para esta entidad.
     *
     * @return EntityManager inyectado por Jakarta EE.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
