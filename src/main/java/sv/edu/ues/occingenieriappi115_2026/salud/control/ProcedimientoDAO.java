package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

@ApplicationScoped
public class ProcedimientoDAO extends DefaultDAO<Procedimiento> {

    public ProcedimientoDAO() {
        super(Procedimiento.class);
    }
}