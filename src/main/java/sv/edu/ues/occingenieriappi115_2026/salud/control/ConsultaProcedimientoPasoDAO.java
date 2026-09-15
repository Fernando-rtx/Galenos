package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;

@ApplicationScoped
public class ConsultaProcedimientoPasoDAO extends DefaultDAO<ConsultaProcedimientoPaso> {

    public ConsultaProcedimientoPasoDAO() {
        super(ConsultaProcedimientoPaso.class);
    }
}
