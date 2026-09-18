package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

/**
 * DAO concreto para la entidad TipoExamen.
 *
 * <p>No contiene CRUD propio porque esa logica y el EntityManager comun viven
 * en {@link DefaultDAO}. La clase solo indica la entidad que administra.</p>
 */
@Stateless
@LocalBean
public class TipoExamenDAO
        extends DefaultDAO<TipoExamen> {

    /**
     * Construye el DAO indicando a la clase padre cual entidad JPA administra.
     */
    public TipoExamenDAO() {
        super(TipoExamen.class);
    }
}
