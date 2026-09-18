package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;

/** DAO EJB de {@link ConsultaProcedimientoPaso}; no repite el EntityManager centralizado en {@link DefaultDAO}. */
@Stateless
@LocalBean
public class ConsultaProcedimientoPasoDAO extends DefaultDAO<ConsultaProcedimientoPaso> {

    /** Configura el DAO genérico para la ejecución de pasos clínicos. */
    public ConsultaProcedimientoPasoDAO() {
        super(ConsultaProcedimientoPaso.class);
    }
}
