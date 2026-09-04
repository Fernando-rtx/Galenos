package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;

/**
 * DAO concreto para ExamenTipoExamen.
 *
 * <p>La entidad se mantiene como tabla puente con datos propios, por eso tiene
 * su propio DAO concreto.</p>
 */
@Stateless
@LocalBean
public class ExamenTipoExamenDAO extends DefaultDAO<ExamenTipoExamen> {

    /** EntityManager inyectado con la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ExamenTipoExamen. */
    public ExamenTipoExamenDAO() {
        super(ExamenTipoExamen.class);
    }

    /**
     * Devuelve el EntityManager usado por la capa generica.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
