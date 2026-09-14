package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

/**
 * DAO concreto para ProcedimientoPasoExamen.
 *
 * <p>La entidad conserva su propio DAO porque la tabla puente tiene datos
 * propios como activo, fecha y observaciones.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoPasoExamenDAO
        extends DefaultDAO<ProcedimientoPasoExamen>
        implements ProcedimientoPasoExamenDAOInterface {

    /** EntityManager inyectado con la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ProcedimientoPasoExamen. */
    public ProcedimientoPasoExamenDAO() {
        super(ProcedimientoPasoExamen.class);
    }

    /**
     * Devuelve el EntityManager que usara DefaultDAO.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Consulta los examenes asociados a un paso de procedimiento con paginacion.
     *
     * @param idProcedimientoPaso identificador del paso de procedimiento.
     * @param first posicion inicial base cero.
     * @param max cantidad maxima de registros.
     * @return lista paginada de relaciones procedimiento-paso-examen.
     */
    @Override
    public List<ProcedimientoPasoExamen> findByIdProcedimientoPaso(
            UUID idProcedimientoPaso,
            int first,
            int max) {

        if (idProcedimientoPaso == null) {
            throw new IllegalArgumentException(
                    "El identificador del paso de procedimiento no puede ser null"
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
            TypedQuery<ProcedimientoPasoExamen> query = getEntityManager()
                    .createNamedQuery(
                            "ProcedimientoPasoExamen.findByIdProcedimientoPaso",
                            ProcedimientoPasoExamen.class
                    );

            query.setParameter("idProcedimientoPaso", idProcedimientoPaso);
            query.setFirstResult(first);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al consultar los examenes por paso de procedimiento",
                    ex
            );
        }
    }

    /**
     * Cuenta los examenes asociados a un paso de procedimiento.
     *
     * @param idProcedimientoPaso identificador del paso de procedimiento.
     * @return cantidad de relaciones encontradas.
     */
    @Override
    public long countByIdProcedimientoPaso(UUID idProcedimientoPaso) {
        if (idProcedimientoPaso == null) {
            throw new IllegalArgumentException(
                    "El identificador del paso de procedimiento no puede ser null"
            );
        }

        try {
            TypedQuery<Long> query = getEntityManager()
                    .createNamedQuery(
                            "ProcedimientoPasoExamen.countByIdProcedimientoPaso",
                            Long.class
                    );

            query.setParameter("idProcedimientoPaso", idProcedimientoPaso);

            return query.getSingleResult();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al contar los examenes por paso de procedimiento",
                    ex
            );
        }
    }

    /**
     * Consulta los pasos de procedimiento asociados a un examen con paginacion.
     *
     * @param idExamen identificador del examen.
     * @param first posicion inicial base cero.
     * @param max cantidad maxima de registros.
     * @return lista paginada de relaciones procedimiento-paso-examen.
     */
    @Override
    public List<ProcedimientoPasoExamen> findByIdExamen(
            UUID idExamen,
            int first,
            int max) {

        if (idExamen == null) {
            throw new IllegalArgumentException(
                    "El identificador del examen no puede ser null"
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
            TypedQuery<ProcedimientoPasoExamen> query = getEntityManager()
                    .createNamedQuery(
                            "ProcedimientoPasoExamen.findByIdExamen",
                            ProcedimientoPasoExamen.class
                    );

            query.setParameter("idExamen", idExamen);
            query.setFirstResult(first);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al consultar los pasos de procedimiento por examen",
                    ex
            );
        }
    }

    /**
     * Cuenta los pasos de procedimiento asociados a un examen.
     *
     * @param idExamen identificador del examen.
     * @return cantidad de relaciones encontradas.
     */
    @Override
    public long countByIdExamen(UUID idExamen) {
        if (idExamen == null) {
            throw new IllegalArgumentException(
                    "El identificador del examen no puede ser null"
            );
        }

        try {
            TypedQuery<Long> query = getEntityManager()
                    .createNamedQuery(
                            "ProcedimientoPasoExamen.countByIdExamen",
                            Long.class
                    );

            query.setParameter("idExamen", idExamen);

            return query.getSingleResult();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al contar los pasos de procedimiento por examen",
                    ex
            );
        }
    }
}
