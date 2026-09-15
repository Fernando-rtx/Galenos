package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

@ApplicationScoped
public class OrdenExamenDAO extends DefaultDAO<OrdenExamen> {

    public OrdenExamenDAO() {
        super(OrdenExamen.class);
    }
}
