package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

/**
 * Contrato EJB local que añade al CRUD genérico las consultas de pasos por
 * procedimiento. Desacopla a los consumidores del uso directo de NamedQueries.
 */
@Local
public interface ProcedimientoPasoDAOInterface
        extends DAOInterface<ProcedimientoPaso> {

    /** @return página de pasos pertenecientes al procedimiento indicado */
    List<ProcedimientoPaso> findByIdProcedimiento(
            UUID idProcedimiento,
            int first,
            int max);

    /** @param idProcedimiento UUID padre @return cantidad de pasos */
    long countByIdProcedimiento(UUID idProcedimiento);
}
