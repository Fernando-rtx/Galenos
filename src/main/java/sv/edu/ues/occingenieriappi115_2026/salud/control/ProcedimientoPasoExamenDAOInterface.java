package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

/**
 * Contrato EJB local para recorrer la relación paso-examen en ambos sentidos,
 * además de las operaciones CRUD heredadas del contrato genérico.
 */
@Local
public interface ProcedimientoPasoExamenDAOInterface
        extends DAOInterface<ProcedimientoPasoExamen> {

    /** @return página de exámenes asociados al paso indicado */
    List<ProcedimientoPasoExamen> findByIdProcedimientoPaso(
            UUID idProcedimientoPaso,
            int first,
            int max);

    /** @return cantidad de exámenes asociados al paso */
    long countByIdProcedimientoPaso(UUID idProcedimientoPaso);

    /** @return página de pasos asociados al examen indicado */
    List<ProcedimientoPasoExamen> findByIdExamen(
            UUID idExamen,
            int first,
            int max);

    /** @return cantidad de pasos asociados al examen */
    long countByIdExamen(UUID idExamen);
}
