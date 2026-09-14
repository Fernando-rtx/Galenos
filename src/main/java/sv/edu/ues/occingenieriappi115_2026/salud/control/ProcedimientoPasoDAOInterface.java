package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

/**
 * Contrato especifico para consultas propias de ProcedimientoPaso.
 */
@Local
public interface ProcedimientoPasoDAOInterface
        extends DAOInterface<ProcedimientoPaso> {

    List<ProcedimientoPaso> findByIdProcedimiento(
            UUID idProcedimiento,
            int first,
            int max);

    long countByIdProcedimiento(UUID idProcedimiento);
}
