package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;

/**
 * DAO concreto para Examen.
 *
 * <p>La clase existe para permitir persistencia de Examen sin duplicar codigo
 * CRUD.</p>
 */
@Stateless
@LocalBean
public class ExamenDAO extends DefaultDAO<Examen> {

    /** EntityManager inyectado con la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra Examen. */
    public ExamenDAO() {
        super(Examen.class);
    }

    /**
     * Devuelve el EntityManager de esta clase concreta.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
