package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.Local;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;

/**
 * Contrato EJB local para consultas propias de {@link ExamenTipoExamen}.
 * Extiende el CRUD genérico y agrega operaciones de relación que el cliente
 * puede usar sin depender de la implementación JPA concreta.
 */
@Local
public interface ExamenTipoExamenDAOInterface
        extends DAOInterface<ExamenTipoExamen> {

    /**
     * @param idExamen UUID del examen
     * @param first desplazamiento base cero
     * @param max tamaño máximo
     * @return relaciones paginadas del examen
     */
    List<ExamenTipoExamen> findByIdExamen(UUID idExamen, int first, int max);

    /**
     * @param idExamen UUID del examen @return total de relaciones
     */
    long countByIdExamen(UUID idExamen);

    /**
     * @return total para una pareja examen/tipo; sirve para detectar duplicados
     */
    long countByIdExamenAndIdTipoExamen(UUID idExamen, UUID idTipoExamen);
}
