package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;

/**
 * Contrato especifico para consultas propias de ExamenTipoExamen.
 */
@Local
public interface ExamenTipoExamenDAOInterface
        extends DAOInterface<ExamenTipoExamen> {

    List<ExamenTipoExamen> findByIdExamen(
            UUID idExamen,
            int first,
            int max);

    long countByIdExamen(UUID idExamen);

    long countByIdExamenAndIdTipoExamen(
            UUID idExamen,
            UUID idTipoExamen);
}
