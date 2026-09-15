package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

/**
 * Contrato especifico para consultas propias de ProcedimientoPasoExamen.
 */
@Local
public interface ProcedimientoPasoExamenDAOInterface
        extends DAOInterface<ProcedimientoPasoExamen> {

    List<ProcedimientoPasoExamen> findByIdProcedimientoPaso(
            UUID idProcedimientoPaso,
            int first,
            int max);

    long countByIdProcedimientoPaso(UUID idProcedimientoPaso);

    List<ProcedimientoPasoExamen> findByIdExamen(
            UUID idExamen,
            int first,
            int max);

    long countByIdExamen(UUID idExamen);
}
