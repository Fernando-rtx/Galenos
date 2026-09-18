package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementación base y genérica de la capa de acceso a datos.
 *
 * <p>El parámetro {@code T} representa la entidad JPA administrada por cada
 * subclase. El constructor recibe su objeto {@link Class} porque el borrado de
 * tipos de Java impide conocer {@code T} en tiempo de ejecución; esa referencia
 * permite crear consultas Criteria, consultar el metamodelo y localizar la
 * clave primaria.</p>
 *
 * <p>Centraliza el {@link EntityManager} asociado a {@code GalenoPU}. Los veinte
 * DAO concretos lo heredan, evitando repetir {@code @PersistenceContext} y
 * aplicando DRY. Liberty inyecta el contexto de persistencia cuando construye
 * los EJB {@code @Stateless}.</p>
 *
 * <p>Las escrituras usan transacciones EJB {@code REQUIRED}: reutilizan una
 * transacción existente o crean una. Las lecturas usan {@code SUPPORTS}:
 * participan en la transacción del llamador cuando existe, sin exigir una
 * nueva. La Criteria API construye filtros, orden y paginación sin SQL manual.</p>
 *
 * @param <T> tipo de entidad JPA administrada
 */
public abstract class DefaultDAO<T> implements DAOInterface<T> {

    /** Tipo concreto necesario para Criteria, {@code find} y metamodelo JPA. */
    private final Class<T> entityClass;

    /** Contexto JPA común enlazado por Liberty con la unidad {@code GalenoPU}. */
    @PersistenceContext(unitName = "GalenoPU")
    private EntityManager entityManager;

    /**
     * Configura la entidad administrada por el DAO concreto.
     *
     * @param entityClass clase JPA de la entidad; nunca puede ser nula
     */
    protected DefaultDAO(Class<T> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "La clase de entidad es requerida");
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public T guardar(T entidad) {
        // persist registra una entidad nueva en el contexto JPA. Antes se
        // completa su UUID para no depender de una secuencia de PostgreSQL.
        Objects.requireNonNull(entidad, "La entidad a guardar es requerida");
        asignarUuidSiNoExiste(entidad);
        getEntityManager().persist(entidad);
        return entidad;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public T buscarPorId(UUID id) {
        // find usa la clave primaria y puede resolver la entidad desde el
        // contexto de persistencia antes de consultar PostgreSQL.
        Objects.requireNonNull(id, "El UUID de busqueda es requerido");
        return getEntityManager().find(entityClass, id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<T> obtenerTodos() {
        // Criteria API crea una consulta tipada para la clase recibida en el
        // constructor, sin escribir JPQL distinto para cada DAO concreto.
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        query.select(root);
        return getEntityManager().createQuery(query).getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<T> obtenerPagina(int primero, int tamano, List<FiltroDAO> filtros,
            List<OrdenDAO> ordenamientos) {
        if (primero < 0 || tamano <= 0) {
            throw new IllegalArgumentException("La paginacion debe tener valores positivos");
        }
        // Root representa la entidad de origen. Predicate y Order se generan
        // dinámicamente y PostgreSQL recibe solo la página solicitada.
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);
        query.where(construirPredicados(builder, root, filtros));
        query.orderBy(construirOrdenamientos(builder, root, ordenamientos));
        return getEntityManager().createQuery(query)
                .setFirstResult(primero)
                .setMaxResults(tamano)
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public T actualizar(T entidad) {
        // merge devuelve la instancia administrada; por eso se retorna el
        // resultado producido por JPA y no necesariamente el objeto recibido.
        Objects.requireNonNull(entidad, "La entidad a actualizar es requerida");
        validarUuidExistente(entidad);
        return getEntityManager().merge(entidad);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean eliminar(UUID id) {
        // remove requiere una instancia administrada, que primero se obtiene
        // por su UUID dentro de la misma transacción REQUIRED.
        T entidad = buscarPorId(id);
        if (entidad == null) {
            return false;
        }

        getEntityManager().remove(entidad);
        return true;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contar() {
        // El conteo separado permite a PrimeFaces calcular el paginador sin
        // traer todas las filas a memoria.
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<T> root = query.from(entityClass);

        query.select(builder.count(root));
        return getEntityManager().createQuery(query).getSingleResult();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contar(List<FiltroDAO> filtros) {
        // Se reutilizan los mismos predicados de la consulta paginada para que
        // el total corresponda exactamente al conjunto filtrado.
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(builder.count(root));
        query.where(construirPredicados(builder, root, filtros));
        return getEntityManager().createQuery(query).getSingleResult();
    }

    @Override
    public UUID obtenerId(T entidad) {
        Objects.requireNonNull(entidad, "La entidad es requerida");
        return leerUuid(entidad, obtenerCampoIdUuid());
    }

    /**
     * Da acceso controlado al EntityManager común a CRUD y DAO especializados.
     *
     * @return EntityManager inyectado por el contenedor
     * @throws IllegalStateException si la clase se usa fuera del contenedor y
     *         no se proporcionó un contexto de persistencia
     */
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
        // El nombre real del atributo @Id se obtiene del metamodelo y luego se
        // usa reflexión porque cada entidad denomina su UUID de forma distinta.
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

    private Predicate[] construirPredicados(CriteriaBuilder builder, Root<T> root,
            Collection<FiltroDAO> filtros) {
        if (filtros == null || filtros.isEmpty()) {
            return new Predicate[0];
        }
        // Cada FiltroDAO independiente de PrimeFaces se convierte a un
        // Predicate Criteria equivalente (LIKE, igualdad, comparación o null).
        List<Predicate> predicados = new ArrayList<>();
        for (FiltroDAO filtro : filtros) {
            PathInfo path = resolverPath(root, filtro.campo());
            Expression<?> expresion = path.path();
            Object valor = convertirValor(filtro.valor(), path.javaType());
            switch (filtro.operador()) {
                case CONTIENE -> predicados.add(builder.like(builder.lower(expresion.as(String.class)), "%" + valor.toString().toLowerCase() + "%"));
                case INICIA_CON -> predicados.add(builder.like(builder.lower(expresion.as(String.class)), valor.toString().toLowerCase() + "%"));
                case TERMINA_CON -> predicados.add(builder.like(builder.lower(expresion.as(String.class)), "%" + valor.toString().toLowerCase()));
                case IGUAL -> predicados.add(builder.equal(expresion, valor));
                case DISTINTO -> predicados.add(builder.notEqual(expresion, valor));
                case MENOR_QUE -> predicados.add(builder.lessThan(asComparable(expresion), (Comparable) valor));
                case MENOR_O_IGUAL -> predicados.add(builder.lessThanOrEqualTo(asComparable(expresion), (Comparable) valor));
                case MAYOR_QUE -> predicados.add(builder.greaterThan(asComparable(expresion), (Comparable) valor));
                case MAYOR_O_IGUAL -> predicados.add(builder.greaterThanOrEqualTo(asComparable(expresion), (Comparable) valor));
                case ES_NULO -> predicados.add(builder.isNull(expresion));
                case NO_ES_NULO -> predicados.add(builder.isNotNull(expresion));
            }
        }
        return predicados.toArray(Predicate[]::new);
    }

    private List<Order> construirOrdenamientos(CriteriaBuilder builder, Root<T> root,
            Collection<OrdenDAO> ordenamientos) {
        // Los OrdenDAO preservan la prioridad enviada por la tabla y se
        // traducen a expresiones ASC/DESC de Criteria.
        List<Order> ordenes = new ArrayList<>();
        if (ordenamientos != null) {
            for (OrdenDAO ordenamiento : ordenamientos) {
                PathInfo path = resolverPath(root, ordenamiento.campo());
                ordenes.add(ordenamiento.direccion() == DireccionOrden.ASCENDENTE
                        ? builder.asc(path.path()) : builder.desc(path.path()));
            }
        }
        return ordenes;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Expression<? extends Comparable> asComparable(Expression<?> expresion) {
        return (Expression<? extends Comparable>) expresion;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object convertirValor(Object valor, Class<?> tipo) {
        if (valor == null || tipo.isInstance(valor)) {
            return valor;
        }
        if (tipo == String.class) return valor.toString();
        if (tipo == UUID.class) return UUID.fromString(valor.toString());
        if (tipo == Boolean.class || tipo == boolean.class) return Boolean.valueOf(valor.toString());
        if (tipo.isEnum()) return Enum.valueOf((Class<? extends Enum>) tipo, valor.toString());
        if (Number.class.isAssignableFrom(tipo)) {
            String texto = valor.toString();
            if (tipo == Integer.class) return Integer.valueOf(texto);
            if (tipo == Long.class) return Long.valueOf(texto);
            if (tipo == Double.class) return Double.valueOf(texto);
        }
        return valor;
    }

    private PathInfo resolverPath(From<?, ?> origen, String nombre) {
        String[] segmentos = nombre.split("\\.");
        From<?, ?> actual = origen;
        jakarta.persistence.criteria.Path<?> path = origen;
        for (String segmento : segmentos) {
            Attribute<?, ?> atributo = getEntityType(actual).getAttribute(segmento);
            if (atributo instanceof jakarta.persistence.metamodel.SingularAttribute<?, ?> singular
                    && singular.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE) {
                actual = actual.join(segmento);
                path = actual;
            } else {
                path = path.get(segmento);
            }
        }
        return new PathInfo(path, path.getJavaType());
    }

    @SuppressWarnings("unchecked")
    private jakarta.persistence.metamodel.ManagedType<?> getEntityType(From<?, ?> origen) {
        return getEntityManager().getMetamodel().managedType(origen.getJavaType());
    }

    private record PathInfo(jakarta.persistence.criteria.Path<?> path, Class<?> javaType) {
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
