package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

/**
 * DAO concreto para ProcedimientoPaso.
 *
 * <p>El DAO permite persistir los pasos de un procedimiento usando el contrato
 * generico.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoPasoDAO
        extends DefaultDAO<ProcedimientoPaso>
        implements ProcedimientoPasoDAOInterface {

    /** EntityManager inyectado mediante GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ProcedimientoPaso. */
    public ProcedimientoPasoDAO() {
        super(ProcedimientoPaso.class);
    }

    /**
     * Provee el EntityManager al padre generico.
     *
     * @return EntityManager de JPA.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Consulta los pasos asociados a un procedimiento con paginacion.
     *
     * @param idProcedimiento identificador del procedimiento padre.
     * @param first posicion inicial base cero.
     * @param max cantidad maxima de registros.
     * @return lista paginada de pasos de procedimiento.
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
