package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.LockModeType;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;

/** DAO EJB de {@link ConsultaProcedimientoPaso}; no repite el EntityManager centralizado en {@link DefaultDAO}. */
@Stateless
@LocalBean
public class ConsultaProcedimientoPasoDAO extends DefaultDAO<ConsultaProcedimientoPaso> {

    /** Configura el DAO genérico para la ejecución de pasos clínicos. */
    public ConsultaProcedimientoPasoDAO() {
        super(ConsultaProcedimientoPaso.class);
    }

    /** Serializa las transiciones de un mismo paso dentro de la transacción EJB. */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ConsultaProcedimientoPaso buscarParaActualizar(UUID id) {
        return getEntityManager().find(ConsultaProcedimientoPaso.class, id, LockModeType.PESSIMISTIC_WRITE);
    }
}
