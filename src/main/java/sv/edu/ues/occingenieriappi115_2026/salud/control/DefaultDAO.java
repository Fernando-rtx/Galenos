package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

/**
 * DAO generico que centraliza la logica comun de persistencia.
 *
 * <p>Las clases hijas solo indican que entidad administran y proveen el
 * {@link EntityManager}. Esto mantiene el patron visto en clase:
 * DAOInterface, DefaultDAO y DAO concreto por entidad.</p>
 *
 * @param <T> entidad JPA administrada por el DAO concreto.
 */
public abstract class DefaultDAO<T> implements DAOInterface<T> {

    /**
     * Clase real de la entidad administrada.
     *
     * <p>Java elimina la informacion de genericos en tiempo de ejecucion, por
     * eso no se puede usar T.class. Cada DAO concreto pasa su clase con
     * super(Entidad.class).</p>
     */
    protected final Class<T> entityClass;

    /**
     * Define la entidad concreta que manejara este DAO generico.
     *
     * @param entityClass clase JPA real que se usara en find y Criteria API.
     * @throws IllegalArgumentException si la clase de entidad es null.
     */
    protected DefaultDAO(Class<T> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException(
                    "La clase de entidad no puede ser null"
            );
        }

        this.entityClass = entityClass;
    }

    /**
     * Devuelve el EntityManager que usara el DAO.
     *
     * <p>El DAO concreto lo provee porque el contenedor Jakarta EE inyecta el
     * EntityManager en la clase EJB final con @PersistenceContext.</p>
     *
     * @return EntityManager inyectado por el contenedor o simulado en pruebas.
     */
    public abstract EntityManager getEntityManager();

    /**
     * Expone la clase de entidad a las subclases y a pruebas especializadas.
     *
     * @return clase real de la entidad administrada.
     */
    protected Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * Guarda una entidad nueva usando EntityManager.persist.
     *
     * @param registro entidad que se desea crear.
     * @throws IllegalArgumentException si el registro es null.
     * @throws IllegalStateException si ocurre un error de persistencia.
     */
    @Override
    public void crear(T registro)
            throws IllegalArgumentException, IllegalStateException {

        // La validacion evita enviar null a JPA, donde el error seria menos
        // claro para quien consume el DAO.
        if (registro == null) {
            throw new IllegalArgumentException(
                    "La entidad no puede ser null"
            );
        }

        try {
            getEntityManager().persist(registro);

        } catch (Exception ex) {
            // Se mantiene una excepcion uniforme de la capa DAO para no filtrar
            // detalles internos del proveedor JPA al resto del proyecto.
            throw new IllegalStateException(
                    "Error al crear la entidad",
                    ex
            );
        }
    }

    /**
     * Actualiza una entidad existente usando EntityManager.merge.
     *
     * @param registro entidad con los datos que se desean sincronizar.
     * @return entidad administrada que devuelve JPA despues del merge.
     * @throws IllegalArgumentException si el registro es null.
     * @throws IllegalStateException si ocurre un error de persistencia.
     */
    @Override
    public T modificar(T registro)
            throws IllegalArgumentException, IllegalStateException {

        // No se permite null porque merge necesita una entidad concreta para
        // copiar su estado al contexto de persistencia.
        if (registro == null) {
            throw new IllegalArgumentException(
                    "La entidad no puede ser null"
            );
        }

        try {
            return getEntityManager().merge(registro);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al modificar la entidad",
                    ex
            );
        }
    }

    /**
     * Busca una entidad por su llave primaria.
     *
     * @param id identificador unico del registro.
     * @return entidad encontrada o null si no existe.
     * @throws IllegalArgumentException si el identificador es null.
     * @throws IllegalStateException si ocurre un error de persistencia.
     */
    @Override
    public T findById(Object id)
            throws IllegalArgumentException, IllegalStateException {

        // La llave primaria es obligatoria para EntityManager.find.
        // En este proyecto normalmente sera UUID, pero Object permite mantener
        // el contrato generico del DAO.
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador no puede ser null"
            );
        }

        try {
            return getEntityManager().find(entityClass, id);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al buscar la entidad",
                    ex
            );
        }
    }

    /**
     * Elimina una entidad usando su llave primaria.
     *
     * @param id identificador unico del registro que se desea eliminar.
     * @throws IllegalArgumentException si el identificador es null.
     * @throws IllegalStateException si ocurre un error de persistencia.
     */
    @Override
    public void eliminar(Object id)
            throws IllegalArgumentException, IllegalStateException {

        // Primero se localiza la entidad administrada. Si no existe, no se
        // invoca remove porque JPA no tiene nada que eliminar.
        T encontrado = findById(id);

        if (encontrado == null) {
            return;
        }

        try {
            getEntityManager().remove(encontrado);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al eliminar la entidad",
                    ex
            );
        }
    }

    /**
     * Consulta un rango de entidades usando Jakarta Persistence Criteria API.
     *
     * @param first posicion inicial base cero.
     * @param max cantidad maxima de registros a recuperar.
     * @return lista de entidades recuperadas.
     * @throws IllegalArgumentException si los parametros de paginacion son
     * invalidos.
     * @throws IllegalStateException si ocurre un error de consulta.
     */
    @Override
    public List<T> findRange(int first, int max)
            throws IllegalArgumentException, IllegalStateException {

        // first representa la posicion inicial de la pagina; no puede ser
        // negativa porque JPA espera posiciones base cero.
        if (first < 0) {
            throw new IllegalArgumentException(
                    "El primer registro no puede ser menor que cero"
            );
        }

        // max controla el tamano de pagina; debe ser mayor que cero para evitar
        // consultas ambiguas o paginas vacias solicitadas por error.
        if (max <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de registros debe ser mayor que cero"
            );
        }

        try {
            // Obtiene el constructor de consultas Criteria desde JPA. Este
            // objeto permite construir consultas dinamicas sin escribir JPQL
            // repetido para cada entidad.
            CriteriaBuilder cb =
                    getEntityManager().getCriteriaBuilder();

            // Crea una consulta cuyo resultado sera del mismo tipo de entidad
            // que administra este DAO.
            CriteriaQuery<T> cq =
                    cb.createQuery(entityClass);

            // Define la entidad principal desde la cual se hara la consulta.
            // Root<T> representa la tabla/entidad consultada.
            Root<T> root =
                    cq.from(entityClass);

            // Indica que se desea recuperar la entidad completa.
            cq.select(root);

            // Convierte la consulta Criteria en una consulta ejecutable de JPA.
            TypedQuery<T> query =
                    getEntityManager().createQuery(cq);

            // Define desde que posicion iniciar la pagina.
            query.setFirstResult(first);

            // Limita la cantidad maxima de registros retornados.
            query.setMaxResults(max);

            // Ejecuta la consulta y retorna la lista tipada de resultados.
            return query.getResultList();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al consultar los registros",
                    ex
            );
        }
    }

    /**
     * Cuenta los registros de la entidad administrada usando Criteria API.
     *
     * @return cantidad total de registros como long.
     * @throws IllegalStateException si ocurre un error de consulta.
     */
    @Override
    public long contar()
            throws IllegalStateException {

        try {
            // Obtiene el constructor de Criteria desde EntityManager. Se usa
            // Criteria porque el DAO debe contar cualquier entidad sin depender
            // de NamedQuery repetidos.
            CriteriaBuilder cb =
                    getEntityManager().getCriteriaBuilder();

            // Las consultas COUNT devuelven Long en JPA, por eso la consulta se
            // construye con Long.class.
            CriteriaQuery<Long> cq =
                    cb.createQuery(Long.class);

            // Root<T> representa la entidad sobre la cual se contaran filas.
            Root<T> root =
                    cq.from(entityClass);

            // SELECT COUNT(entidad). CriteriaBuilder genera la expresion de
            // conteo de forma tipada.
            cq.select(cb.count(root));

            // createQuery convierte el CriteriaQuery<Long> en una consulta JPA
            // ejecutable. getSingleResult devuelve un unico valor: el total.
            return getEntityManager()
                    .createQuery(cq)
                    .getSingleResult();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al contar los registros",
                    ex
            );
        }
    }
}
