package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;

/**
 * DAO concreto para Consulta.
 *
 * <p>La logica CRUD, busqueda, paginacion y conteo se mantiene en DefaultDAO
 * para no duplicarla.</p>
 */
@Stateless
@LocalBean
public class ConsultaDAO extends DefaultDAO<Consulta> {

    /** EntityManager asociado a GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra Consulta. */
    public ConsultaDAO() {
        super(Consulta.class);
    }

    /**
     * Devuelve el EntityManager que ejecuta las operaciones JPA.
     *
     * @return EntityManager inyectado por el contenedor.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
