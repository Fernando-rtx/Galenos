package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

/**
 * DAO concreto para ProcedimientoPaso.
 *
 * <p>Hereda CRUD y EntityManager de {@link DefaultDAO}. Sus métodos propios
 * consumen las NamedQueries {@code ProcedimientoPaso.findByIdProcedimiento} y
 * {@code countByIdProcedimiento} para navegar la relación con paginación y
 * conteo sin escribir SQL directo.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoPasoDAO
        extends DefaultDAO<ProcedimientoPaso>
        implements ProcedimientoPasoDAOInterface {

    /** Construye el DAO indicando que administra ProcedimientoPaso. */
    public ProcedimientoPasoDAO() {
        super(ProcedimientoPaso.class);
    }

    /**
     * Consulta los pasos asociados a un procedimiento con paginacion.
     *
     * @param idProcedimiento identificador del procedimiento padre.
     * @param first posicion inicial base cero.
     * @param max cantidad maxima de registros.
     * @return lista paginada de pasos de procedimiento.
     * @throws IllegalArgumentException si el ID o la paginación no son válidos
     * @throws IllegalStateException si falla la consulta JPA
     */
    @Override
    public List<ProcedimientoPaso> findByIdProcedimiento(
            UUID idProcedimiento,
            int first,
            int max) {

        if (idProcedimiento == null) {
            throw new IllegalArgumentException(
                    "El identificador del procedimiento no puede ser null"
            );
        }

        if (first < 0) {
            throw new IllegalArgumentException(
                    "El primer registro no puede ser menor que cero"
            );
        }

        if (max <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de registros debe ser mayor que cero"
            );
        }

        try {
            TypedQuery<ProcedimientoPaso> query = getEntityManager()
                    .createNamedQuery(
                            "ProcedimientoPaso.findByIdProcedimiento",
                            ProcedimientoPaso.class
                    );

            query.setParameter("idProcedimiento", idProcedimiento);
            query.setFirstResult(first);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al consultar los pasos por procedimiento",
                    ex
            );
        }
    }

    /**
     * Cuenta los pasos asociados a un procedimiento.
     *
     * @param idProcedimiento identificador del procedimiento padre.
     * @return cantidad de pasos encontrados.
     * @throws IllegalArgumentException si el ID es nulo
     * @throws IllegalStateException si falla el conteo JPA
     */
    @Override
    public long countByIdProcedimiento(UUID idProcedimiento) {
        if (idProcedimiento == null) {
            throw new IllegalArgumentException(
                    "El identificador del procedimiento no puede ser null"
            );
        }

        try {
            TypedQuery<Long> query = getEntityManager()
                    .createNamedQuery(
                            "ProcedimientoPaso.countByIdProcedimiento",
                            Long.class
                    );

            query.setParameter("idProcedimiento", idProcedimiento);

            return query.getSingleResult();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al contar los pasos por procedimiento",
                    ex
            );
        }
    }
}
