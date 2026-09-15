package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

/**
 * DAO concreto para la entidad TipoExamen.
 *
 * <p>No contiene CRUD propio porque esa logica vive en {@link DefaultDAO}. La
 * clase solo indica la entidad que administra y provee el EntityManager
 * inyectado por Jakarta EE.</p>
 */
@Stateless
@LocalBean
public class TipoExamenDAO
        extends DefaultDAO<TipoExamen> {

    /**
     * EntityManager inyectado por el contenedor usando la unidad de
     * persistencia real declarada en persistence.xml.
     */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /**
     * Construye el DAO indicando a la clase padre cual entidad JPA administra.
     */
    public TipoExamenDAO() {
        super(TipoExamen.class);
    }

    /**
     * Devuelve el EntityManager que ejecutara las operaciones genericas.
     *
     * @return EntityManager inyectado por Jakarta EE.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
