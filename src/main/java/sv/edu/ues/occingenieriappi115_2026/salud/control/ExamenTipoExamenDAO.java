package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;

/**
 * DAO concreto para ExamenTipoExamen.
 *
 * <p>La entidad se mantiene como tabla puente con datos propios, por eso tiene
 * su propio DAO concreto.</p>
 */
@Stateless
@LocalBean
public class ExamenTipoExamenDAO
        extends DefaultDAO<ExamenTipoExamen>
        implements ExamenTipoExamenDAOInterface {

    /** EntityManager inyectado con la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra ExamenTipoExamen. */
    public ExamenTipoExamenDAO() {
        super(ExamenTipoExamen.class);
    }

    /**
     * Devuelve el EntityManager usado por la capa generica.
     *
     * @return EntityManager inyectado.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Consulta los tipos de examen asociados a un examen con paginacion.
     *
     * @param idExamen identificador del examen padre.
     * @param first posicion inicial base cero.
     * @param max cantidad maxima de registros.
     * @return lista paginada de relaciones examen-tipo examen.
     */
    @Override
    public List<ExamenTipoExamen> findByIdExamen(
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
            TypedQuery<ExamenTipoExamen> query = getEntityManager()
                    .createNamedQuery(
                            "ExamenTipoExamen.findByIdExamen",
                            ExamenTipoExamen.class
                    );

            query.setParameter("idExamen", idExamen);
            query.setFirstResult(first);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al consultar los tipos de examen por examen",
                    ex
            );
        }
    }

    /**
     * Cuenta los tipos de examen asociados a un examen.
     *
     * @param idExamen identificador del examen padre.
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
                            "ExamenTipoExamen.countByIdExamen",
                            Long.class
                    );

            query.setParameter("idExamen", idExamen);

            return query.getSingleResult();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al contar los tipos de examen por examen",
                    ex
            );
        }
    }

    /**
     * Cuenta si ya existe una asociacion entre un examen y un tipo de examen.
     *
     * @param idExamen identificador del examen padre.
     * @param idTipoExamen identificador del tipo de examen asociado.
     * @return cantidad de relaciones encontradas para la pareja indicada.
     */
    @Override
    public long countByIdExamenAndIdTipoExamen(
            UUID idExamen,
            UUID idTipoExamen) {

        if (idExamen == null) {
            throw new IllegalArgumentException(
                    "El identificador del examen no puede ser null"
            );
        }

        if (idTipoExamen == null) {
            throw new IllegalArgumentException(
                    "El identificador del tipo de examen no puede ser null"
            );
        }

        try {
            TypedQuery<Long> query = getEntityManager()
                    .createNamedQuery(
                            "ExamenTipoExamen.countByIdExamenAndIdTipoExamen",
                            Long.class
                    );

            query.setParameter("idExamen", idExamen);
            query.setParameter("idTipoExamen", idTipoExamen);

            return query.getSingleResult();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al contar la asociacion examen tipo de examen",
                    ex
            );
        }
    }
}
