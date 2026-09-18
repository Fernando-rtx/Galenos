package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

/** DAO EJB de {@link OrdenExamen}; hereda CRUD, EntityManager y transacciones de {@link DefaultDAO}. */
@Stateless
@LocalBean
public class OrdenExamenDAO extends DefaultDAO<OrdenExamen> {

    /** Identifica la entidad administrada en tiempo de ejecución. */
    public OrdenExamenDAO() {
        super(OrdenExamen.class);
    }
}
