package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class DefaultDAO<T> implements DAOInterface<T> {

    private final Class<T> entityClass;

    @PersistenceContext(unitName = "GalenoPU")
    private EntityManager entityManager;

    protected DefaultDAO(Class<T> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "La clase de entidad es requerida");
    }

    @Override
    @Transactional
    public T guardar(T entidad) {
        Objects.requireNonNull(entidad, "La entidad a guardar es requerida");
        asignarUuidSiNoExiste(entidad);
        getEntityManager().persist(entidad);
        return entidad;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public T buscarPorId(UUID id) {
        Objects.requireNonNull(id, "El UUID de busqueda es requerido");
        return getEntityManager().find(entityClass, id);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<T> obtenerTodos() {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        query.select(root);
        return getEntityManager().createQuery(query).getResultList();
    }

    @Override
    @Transactional
    public T actualizar(T entidad) {
        Objects.requireNonNull(entidad, "La entidad a actualizar es requerida");
        validarUuidExistente(entidad);
        return getEntityManager().merge(entidad);
    }

    @Override
    @Transactional
    public boolean eliminar(UUID id) {
        T entidad = buscarPorId(id);
        if (entidad == null) {
            return false;
        }

        getEntityManager().remove(entidad);
        return true;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public long contar() {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<T> root = query.from(entityClass);

        query.select(builder.count(root));
        return getEntityManager().createQuery(query).getSingleResult();
    }

    protected EntityManager getEntityManager() {
        if (entityManager == null) {
            throw new IllegalStateException("EntityManager no inicializado para " + entityClass.getName());
        }
        return entityManager;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    private void asignarUuidSiNoExiste(T entidad) {
        Field campoId = obtenerCampoIdUuid();
        UUID idActual = leerUuid(entidad, campoId);
        if (idActual == null) {
            escribirUuid(entidad, campoId, UUID.randomUUID());
        }
    }

    private void validarUuidExistente(T entidad) {
        Field campoId = obtenerCampoIdUuid();
        UUID idActual = leerUuid(entidad, campoId);
        if (idActual == null) {
            throw new IllegalArgumentException("La entidad " + entityClass.getSimpleName() + " debe tener UUID para actualizar");
        }
    }

    private Field obtenerCampoIdUuid() {
        EntityType<T> entityType = getEntityManager().getMetamodel().entity(entityClass);
        SingularAttribute<? super T, UUID> idAttribute = entityType.getId(UUID.class);
        return buscarCampo(idAttribute.getName());
    }

    private Field buscarCampo(String nombreCampo) {
        Class<?> claseActual = entityClass;
        while (claseActual != null) {
            try {
                Field campo = claseActual.getDeclaredField(nombreCampo);
                if (!UUID.class.equals(campo.getType())) {
                    throw new IllegalStateException("El identificador de " + entityClass.getName() + " no es UUID");
                }
                campo.setAccessible(true);
                return campo;
            } catch (NoSuchFieldException ex) {
                claseActual = claseActual.getSuperclass();
            }
        }
        throw new IllegalStateException("No se encontro el identificador UUID de " + entityClass.getName());
    }

    private UUID leerUuid(T entidad, Field campo) {
        try {
            return (UUID) campo.get(entidad);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("No se pudo leer el UUID de " + entityClass.getName(), ex);
        }
    }

    private void escribirUuid(T entidad, Field campo, UUID valor) {
        try {
            campo.set(entidad, valor);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("No se pudo asignar el UUID de " + entityClass.getName(), ex);
        }
    }
}
