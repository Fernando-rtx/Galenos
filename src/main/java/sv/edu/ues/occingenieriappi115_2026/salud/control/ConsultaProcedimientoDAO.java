package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;

@ApplicationScoped
public class ConsultaProcedimientoDAO extends DefaultDAO<ConsultaProcedimiento> {

    public ConsultaProcedimientoDAO() {
        super(ConsultaProcedimiento.class);
    }
}
