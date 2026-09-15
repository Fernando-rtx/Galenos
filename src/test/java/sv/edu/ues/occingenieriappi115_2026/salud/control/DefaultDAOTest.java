package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del DAO generico.
 *
 * <p>Se prueban las 6 operaciones de {@link DAOInterface} en su
 * implementacion de {@link DefaultDAO}: guardar, buscarPorId, obtenerTodos,
 * actualizar, eliminar y contar.</p>
 *
 * <p>DefaultDAO es abstracto, por eso se usa una subclase interna que expone
 * su entidad de prueba. Todas las dependencias de JPA se simulan con Mockito
 * para probar la logica sin abrir una conexion real a PostgreSQL.</p>
 *
 * <p>Se prueba ademas el manejo de errores caracteristico del DAO:
 * entidades o identificadores null causan {@link IllegalArgumentException} y
 * los fallos de JPA se envuelven en {@link IllegalStateException}.</p>
 */
public class DefaultDAOTest {

    /**
     * Entidad minima usada solo para validar el comportamiento generico.
     */
    private static class TestEntity {

        /**
         * UUID que representa la llave primaria. Debe llamarse "id" porque el
         * DAO localiza el campo por el nombre del id del metamodel JPA.
         */
        private UUID id;

        /**
         * Constructor sin argumentos requerido por reflexion.
         */
        TestEntity() {
        }

        /**
         * Crea la entidad de prueba con un identificador conocido.
         *
         * @param id identificador simulado.
         */
        TestEntity(UUID id) {
            this.id = id;
        }
    }

    /**
     * Implementacion concreta usada para probar DefaultDAO.
     *
     * <p>En produccion el contenedor Jakarta EE inyecta el EntityManager con
     * {@code @PersistenceContext}. Aqui la subclase lo recibe por constructor
     * y lo entrega a traves de {@link #getEntityManager()}, que es protected
     * en la clase padre.</p>
     */
    private static class TestDAO extends DefaultDAO<TestEntity> {

        /**
         * EntityManager simulado que sustituye al inyectado por Jakarta EE.
         */
        private final EntityManager em;

        /**
         * Constructor normal usado por la mayoria de pruebas.
         *
         * @param em EntityManager simulado.
         */
        TestDAO(EntityManager em) {
            super(TestEntity.class);
            this.em = em;
        }

        /**
         * Constructor auxiliar para validar que DefaultDAO rechaza entityClass
         * null.
         *
         * @param entityClass clase de entidad enviada al DAO generico.
         * @param em EntityManager simulado.
         */
        TestDAO(Class<TestEntity> entityClass, EntityManager em) {
            super(entityClass);
            this.em = em;
        }

        /**
         * Devuelve el EntityManager simulado para que DefaultDAO ejecute sus
         * operaciones.
         *
         * @return EntityManager de prueba.
         */
        @Override
        protected EntityManager getEntityManager() {
            return em;
        }
    }

    /**
     * Configura el metamodel JPA para que el DAO encuentre el campo "id".
     *
     * <p>{@link DefaultDAO} localiza la llave primaria consultando el metamodel
     * cuando guarda o actualiza. Este metodo simula el flujo de
     * {@code entityManager.getMetamodel().entity(TestEntity.class).getId(UUID.class)}</p>
     *
     * @param em EntityManager simulado.
     */
    @SuppressWarnings("unchecked")
    private static void configurarMetamodel(EntityManager em) {
        jakarta.persistence.metamodel.Metamodel metamodel
                = Mockito.mock(jakarta.persistence.metamodel.Metamodel.class);
        EntityType<TestEntity> entityType = Mockito.mock(EntityType.class);
        SingularAttribute<TestEntity, UUID> idAttribute
                = Mockito.mock(SingularAttribute.class);

        Mockito.when(em.getMetamodel()).thenReturn(metamodel);
        Mockito.when(metamodel.entity(TestEntity.class)).thenReturn(entityType);
        Mockito.when(entityType.getId(UUID.class))
                .thenAnswer(invocation -> idAttribute);
        Mockito.when(idAttribute.getName()).thenReturn("id");
    }

    @Test
    public void testConstructorEntityClassNull() {
        // Escenario: una subclase intenta construir el DAO sin indicar entidad.
        // Esperado: se rechaza porque DefaultDAO necesita Class<T> en runtime.
        // El constructor usa Objects.requireNonNull, que lanza NullPointerException.
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        assertThrows(
                NullPointerException.class,
                () -> new TestDAO(null, mockEM)
        );
    }

    @Test
    public void testGetEntityClass() {
        // Escenario: el DAO concreto pasa TestEntity.class al constructor padre.
        // Esperado: DefaultDAO conserva esa clase para find y Criteria API.
        TestDAO cut = new TestDAO(Mockito.mock(EntityManager.class));

        assertSame(TestEntity.class, cut.getEntityClass());
    }

    @Test
    public void testGuardarCorrectamente() {
        // Escenario: se persiste una entidad que ya tiene UUID asignado.
        // Esperado: no se sobrescribe el id y se invoca persist.
        UUID id = UUID.randomUUID();
        TestEntity registro = new TestEntity(id);
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        configurarMetamodel(mockEM);

        TestDAO cut = new TestDAO(mockEM);

        TestEntity resultado = cut.guardar(registro);

        assertSame(registro, resultado);
        assertEquals(id, registro.id);
        Mockito.verify(mockEM).persist(registro);
    }

    @Test
    public void testGuardarAsignaUuidSiNoExiste() {
        // Escenario: la entidad llega sin llave primaria.
        // Esperado: el DAO genera un UUID aleatorio antes de persistir.
        TestEntity registro = new TestEntity();
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        configurarMetamodel(mockEM);

        TestDAO cut = new TestDAO(mockEM);

        cut.guardar(registro);

        assertNotNull(registro.id);
        Mockito.verify(mockEM).persist(registro);
    }

    @Test
    public void testGuardarRegistroNull() {
        // Escenario: la capa superior intenta crear un registro null.
        // Esperado: Objects.requireNonNull lanza NullPointerException antes de JPA.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                NullPointerException.class,
                () -> cut.guardar(null)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testGuardarErrorPersistencia() {
        // Escenario: EntityManager.persist falla.
        // Esperado: el DAO actual de main no envuelve el error en guardar,
        // por lo que el RuntimeException se propaga tal cual.
        UUID id = UUID.randomUUID();
        TestEntity registro = new TestEntity(id);
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        configurarMetamodel(mockEM);

        Mockito.doThrow(new RuntimeException("Error simulado"))
                .when(mockEM)
                .persist(registro);

        TestDAO cut = new TestDAO(mockEM);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cut.guardar(registro)
        );

        assertEquals("Error simulado", ex.getMessage());
    }

    @Test
    public void testBuscarPorIdRegistroExistente() {
        // Escenario: JPA encuentra una entidad por UUID.
        // Esperado: buscarPorId devuelve exactamente la entidad encontrada.
        UUID id = UUID.randomUUID();
        TestEntity esperado = new TestEntity(id);
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(esperado);

        TestDAO cut = new TestDAO(mockEM);

        assertSame(esperado, cut.buscarPorId(id));
        Mockito.verify(mockEM).find(TestEntity.class, id);
    }

    @Test
    public void testBuscarPorIdRegistroInexistente() {
        // Escenario: JPA no encuentra la llave primaria solicitada.
        // Esperado: se retorna null, que es el comportamiento de find.
        UUID id = UUID.randomUUID();
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(null);

        TestDAO cut = new TestDAO(mockEM);

        assertNull(cut.buscarPorId(id));
    }

    @Test
    public void testBuscarPorIdIdentificadorNull() {
        // Escenario: se busca sin llave primaria.
        // Esperado: Objects.requireNonNull lanza NullPointerException antes de JPA.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                NullPointerException.class,
                () -> cut.buscarPorId(null)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testObtenerTodosCorrectamente() {
        // Escenario: la consulta Criteria devuelve varios registros.
        // Esperado: obtenerTodos entrega la lista completa.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<TestEntity> cq = Mockito.mock(CriteriaQuery.class);
        Root<TestEntity> root = Mockito.mock(Root.class);
        TypedQuery<TestEntity> query = Mockito.mock(TypedQuery.class);

        List<TestEntity> registros = List.of(
                new TestEntity(UUID.randomUUID()),
                new TestEntity(UUID.randomUUID())
        );

        Mockito.when(mockEM.getCriteriaBuilder()).thenReturn(cb);
        Mockito.when(cb.createQuery(TestEntity.class)).thenReturn(cq);
        Mockito.when(cq.from(TestEntity.class)).thenReturn(root);
        Mockito.when(cq.select(root)).thenReturn(cq);
        Mockito.when(mockEM.createQuery(cq)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(registros);

        TestDAO cut = new TestDAO(mockEM);

        assertEquals(registros, cut.obtenerTodos());
    }

    @Test
    public void testObtenerTodosVacio() {
        // Escenario: no hay registros en la tabla.
        // Esperado: la lista retornada es vacia y no null.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<TestEntity> cq = Mockito.mock(CriteriaQuery.class);
        Root<TestEntity> root = Mockito.mock(Root.class);
        TypedQuery<TestEntity> query = Mockito.mock(TypedQuery.class);

        Mockito.when(mockEM.getCriteriaBuilder()).thenReturn(cb);
        Mockito.when(cb.createQuery(TestEntity.class)).thenReturn(cq);
        Mockito.when(cq.from(TestEntity.class)).thenReturn(root);
        Mockito.when(cq.select(root)).thenReturn(cq);
        Mockito.when(mockEM.createQuery(cq)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(List.of());

        TestDAO cut = new TestDAO(mockEM);

        List<TestEntity> resultado = cut.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testActualizarCorrectamente() {
        // Escenario: se actualiza una entidad con UUID ya asignado.
        // Esperado: merge se invoca y se devuelve la instancia administrada.
        UUID id = UUID.randomUUID();
        TestEntity registro = new TestEntity(id);
        TestEntity registroMergeado = new TestEntity(id);
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        configurarMetamodel(mockEM);

        Mockito.when(mockEM.merge(registro)).thenReturn(registroMergeado);

        TestDAO cut = new TestDAO(mockEM);

        TestEntity resultado = cut.actualizar(registro);

        assertSame(registroMergeado, resultado);
        Mockito.verify(mockEM).merge(registro);
    }

    @Test
    public void testActualizarRegistroNull() {
        // Escenario: se intenta actualizar null.
        // Esperado: Objects.requireNonNull lanza NullPointerException antes de JPA.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                NullPointerException.class,
                () -> cut.actualizar(null)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testActualizarSinUuid() {
        // Escenario: la entidad llega sin llave primaria y se intenta merge.
        // Esperado: el DAO rechaza la operacion porque actualizar exige UUID.
        TestEntity registro = new TestEntity();
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        configurarMetamodel(mockEM);

        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.actualizar(registro)
        );

        Mockito.verify(mockEM, Mockito.never()).merge(Mockito.any());
    }

    @Test
    public void testEliminarIdExistente() {
        // Escenario: la entidad existe y JPA puede eliminarla.
        // Esperado: se busca por UUID, luego se invoca remove y devuelve true.
        UUID id = UUID.randomUUID();
        TestEntity encontrado = new TestEntity(id);
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(encontrado);

        TestDAO cut = new TestDAO(mockEM);

        assertTrue(cut.eliminar(id));
        Mockito.verify(mockEM).find(TestEntity.class, id);
        Mockito.verify(mockEM).remove(encontrado);
    }

    @Test
    public void testEliminarIdInexistente() {
        // Escenario: no existe registro para el UUID indicado.
        // Esperado: remove no se invoca y el metodo devuelve false.
        UUID id = UUID.randomUUID();
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(null);

        TestDAO cut = new TestDAO(mockEM);

        assertEquals(false, cut.eliminar(id));
        Mockito.verify(mockEM).find(TestEntity.class, id);
        Mockito.verify(mockEM, Mockito.never()).remove(Mockito.any());
    }

    @Test
    public void testEliminarIdentificadorNull() {
        // Escenario: se intenta eliminar sin llave primaria.
        // Esperado: buscarPorId internamente lanza NullPointerException por
        // Objects.requireNonNull antes de tocar JPA.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                NullPointerException.class,
                () -> cut.eliminar(null)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testContarVariosRegistros() {
        // Escenario: COUNT devuelve varios registros.
        // Esperado: se retorna el valor producido por getSingleResult.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<Long> cq = Mockito.mock(CriteriaQuery.class);
        Root<TestEntity> root = Mockito.mock(Root.class);
        Expression<Long> countExpression = Mockito.mock(Expression.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);

        Mockito.when(mockEM.getCriteriaBuilder()).thenReturn(cb);
        Mockito.when(cb.createQuery(Long.class)).thenReturn(cq);
        Mockito.when(cq.from(TestEntity.class)).thenReturn(root);
        Mockito.when(cb.count(root)).thenReturn(countExpression);
        Mockito.when(cq.select(countExpression)).thenReturn(cq);
        Mockito.when(mockEM.createQuery(cq)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(7L);

        TestDAO cut = new TestDAO(mockEM);

        assertEquals(7L, cut.contar());
        Mockito.verify(cb).createQuery(Long.class);
        Mockito.verify(cb).count(root);
    }

    @Test
    public void testContarCeroRegistros() {
        // Escenario: no existen registros para la entidad.
        // Esperado: contar retorna 0.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<Long> cq = Mockito.mock(CriteriaQuery.class);
        Root<TestEntity> root = Mockito.mock(Root.class);
        Expression<Long> countExpression = Mockito.mock(Expression.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);

        Mockito.when(mockEM.getCriteriaBuilder()).thenReturn(cb);
        Mockito.when(cb.createQuery(Long.class)).thenReturn(cq);
        Mockito.when(cq.from(TestEntity.class)).thenReturn(root);
        Mockito.when(cb.count(root)).thenReturn(countExpression);
        Mockito.when(cq.select(countExpression)).thenReturn(cq);
        Mockito.when(mockEM.createQuery(cq)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(0L);

        TestDAO cut = new TestDAO(mockEM);

        assertEquals(0L, cut.contar());
    }
}