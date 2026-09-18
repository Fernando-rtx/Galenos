package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

/** DAO EJB de {@link Procedimiento}; aplica DRY al heredar persistencia y transacciones comunes. */
@Stateless
@LocalBean
public class ProcedimientoDAO extends DefaultDAO<Procedimiento> {

    /** Configura el tipo de entidad para Criteria y operaciones CRUD. */
    public ProcedimientoDAO() {
        super(Procedimiento.class);
    }
}
