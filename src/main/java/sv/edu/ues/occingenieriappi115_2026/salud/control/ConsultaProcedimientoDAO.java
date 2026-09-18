package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;

/** DAO EJB de {@link ConsultaProcedimiento}, respaldado por la infraestructura DRY de {@link DefaultDAO}. */
@Stateless
@LocalBean
public class ConsultaProcedimientoDAO extends DefaultDAO<ConsultaProcedimiento> {

    /** Identifica la entidad que usarán las operaciones genéricas. */
    public ConsultaProcedimientoDAO() {
        super(ConsultaProcedimiento.class);
    }
}
