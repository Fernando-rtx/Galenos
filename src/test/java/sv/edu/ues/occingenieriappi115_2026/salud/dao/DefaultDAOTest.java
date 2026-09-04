package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias del DAO generico.
 *
 * Ese ConcreteDAOTest sirve para revisar que todos tus DAO estén bien armados.
 * Por ejemplo, verifica que TipoExamenDAO: sea un DAO de Jakarta EE
 * (@Stateless, @LocalBean); sepa que trabaja con TipoExamen.class; tenga su
 * EntityManager; use GalenoPU; y devuelva correctamente ese EntityManager.
 * <p>
 * DefaultDAO es abstracto, por eso se usa una subclase interna. Todas las
 * dependencias de JPA se simulan con Mockito para probar la logica sin abrir
 * una conexion real a PostgreSQL.</p>
 */
public class DefaultDAOTest {

    /**
     * Entidad minima usada solo para validar el comportamiento generico.
     */
    private static class TestEntity {

        /**
         * UUID que representa una llave primaria igual a las usadas por las
         * entidades reales del proyecto.
         */
        private final UUID id;

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
        public EntityManager getEntityManager() {
            return em;
        }
    }

    /**
     * Agrupa los mocks necesarios para probar findRange con Criteria API.
     */
    private static class RangeCriteriaMocks {

        /**
         * EntityManager simulado.
         */
        private final EntityManager em = Mockito.mock(EntityManager.class);

        /**
         * Constructor de consultas Criteria simulado.
         */
        private final CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);

        /**
         * Consulta Criteria tipada para TestEntity.
         */
        @SuppressWarnings("unchecked")
        private final CriteriaQuery<TestEntity> cq
                = Mockito.mock(CriteriaQuery.class);

        /**
         * Root representa la entidad principal consultada.
         */
        @SuppressWarnings("unchecked")
        private final Root<TestEntity> root = Mockito.mock(Root.class);

        /**
         * TypedQuery ejecutable que devuelve la lista final.
         */
        @SuppressWarnings("unchecked")
        private final TypedQuery<TestEntity> query
                = Mockito.mock(TypedQuery.class);

        /**
         * Configura el flujo normal de Criteria API para una consulta paginada.
         *
         * @param registros registros que devolvera la consulta simulada.
         */
        void prepararConsultaExitosa(List<TestEntity> registros) {
            Mockito.when(em.getCriteriaBuilder()).thenReturn(cb);
            Mockito.when(cb.createQuery(TestEntity.class)).thenReturn(cq);
            Mockito.when(cq.from(TestEntity.class)).thenReturn(root);
            Mockito.when(cq.select(root)).thenReturn(cq);
            Mockito.when(em.createQuery(cq)).thenReturn(query);
            Mockito.when(query.setFirstResult(Mockito.anyInt()))
                    .thenReturn(query);
            Mockito.when(query.setMaxResults(Mockito.anyInt()))
                    .thenReturn(query);
            Mockito.when(query.getResultList()).thenReturn(registros);
        }
    }

    /**
     * Agrupa los mocks necesarios para probar contar con Criteria API.
     */
    private static class CountCriteriaMocks {

        /**
         * EntityManager simulado.
         */
        private final EntityManager em = Mockito.mock(EntityManager.class);

        /**
         * Constructor de consultas Criteria simulado.
         */
        private final CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);

        /**
         * Consulta Criteria cuyo resultado sera Long por tratarse de COUNT.
         */
        @SuppressWarnings("unchecked")
        private final CriteriaQuery<Long> cq
                = Mockito.mock(CriteriaQuery.class);

        /**
         * Root representa la entidad sobre la que se cuentan registros.
         */
        @SuppressWarnings("unchecked")
        private final Root<TestEntity> root = Mockito.mock(Root.class);

        /**
         * Expresion Criteria que representa SELECT COUNT(root).
         */
        @SuppressWarnings("unchecked")
        private final Expression<Long> countExpression
                = Mockito.mock(Expression.class);

        /**
         * TypedQuery que ejecuta el COUNT y devuelve un unico Long.
         */
        @SuppressWarnings("unchecked")
        private final TypedQuery<Long> query = Mockito.mock(TypedQuery.class);

        /**
         * Configura el flujo normal de Criteria API para una consulta COUNT.
         *
         * @param total valor que devolvera getSingleResult.
         */
        void prepararConteoExitoso(long total) {
            Mockito.when(em.getCriteriaBuilder()).thenReturn(cb);
            Mockito.when(cb.createQuery(Long.class)).thenReturn(cq);
            Mockito.when(cq.from(TestEntity.class)).thenReturn(root);
            Mockito.when(cb.count(root)).thenReturn(countExpression);
            Mockito.when(cq.select(countExpression)).thenReturn(cq);
            Mockito.when(em.createQuery(cq)).thenReturn(query);
            Mockito.when(query.getSingleResult()).thenReturn(total);
        }
    }

    @Test
    public void testConstructorEntityClassNull() {
        // Escenario: una subclase intenta construir el DAO sin indicar entidad.
        // Esperado: se rechaza porque DefaultDAO necesita Class<T> en runtime.
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        assertThrows(
                IllegalArgumentException.class,
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
    public void testCrearCorrectamente() {
        // Escenario: se persiste una entidad valida.
        // El EntityManager se simula para verificar que se invoque persist.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);
        TestEntity registro = new TestEntity(UUID.randomUUID());

        cut.crear(registro);

        Mockito.verify(mockEM).persist(registro);
    }

    @Test
    public void testCrearRegistroNull() {
        // Escenario: la capa superior intenta crear un registro null.
        // Esperado: se lanza IllegalArgumentException antes de llamar a JPA.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.crear(null)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testCrearErrorPersistencia() {
        // Escenario: EntityManager.persist falla.
        // Esperado: el DAO envuelve el error en IllegalStateException.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestEntity registro = new TestEntity(UUID.randomUUID());

        Mockito.doThrow(new RuntimeException("Error simulado"))
                .when(mockEM)
                .persist(registro);

        TestDAO cut = new TestDAO(mockEM);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.crear(registro)
        );

        assertEquals("Error al crear la entidad", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testModificarCorrectamente() {
        // Escenario: se modifica una entidad valida.
        // El mock devuelve la instancia administrada que produciria merge.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestEntity registro = new TestEntity(UUID.randomUUID());
        TestEntity registroMergeado = new TestEntity(registro.id);

        Mockito.when(mockEM.merge(registro)).thenReturn(registroMergeado);

        TestDAO cut = new TestDAO(mockEM);

        TestEntity resultado = cut.modificar(registro);

        assertSame(registroMergeado, resultado);
        Mockito.verify(mockEM).merge(registro);
    }

    @Test
    public void testModificarRegistroNull() {
        // Escenario: se intenta modificar null.
        // Esperado: no se llama merge porque no hay entidad que sincronizar.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.modificar(null)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testModificarErrorPersistencia() {
        // Escenario: EntityManager.merge falla.
        // Esperado: el DAO conserva la causa y reporta error de modificacion.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestEntity registro = new TestEntity(UUID.randomUUID());

        Mockito.when(mockEM.merge(registro))
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mockEM);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.modificar(registro)
        );

        assertEquals("Error al modificar la entidad", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testFindByIdRegistroExistente() {
        // Escenario: JPA encuentra una entidad por UUID.
        // Esperado: findById devuelve exactamente la entidad encontrada.
        UUID id = UUID.randomUUID();
        TestEntity esperado = new TestEntity(id);
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(esperado);

        TestDAO cut = new TestDAO(mockEM);

        assertSame(esperado, cut.findById(id));
        Mockito.verify(mockEM).find(TestEntity.class, id);
    }

    @Test
    public void testFindByIdRegistroInexistente() {
        // Escenario: JPA no encuentra la llave primaria solicitada.
        // Esperado: se retorna null, que es el comportamiento de EntityManager.find.
        UUID id = UUID.randomUUID();
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(null);

        TestDAO cut = new TestDAO(mockEM);

        assertNull(cut.findById(id));
    }

    @Test
    public void testFindByIdIdentificadorNull() {
        // Escenario: se busca sin llave primaria.
        // Esperado: el DAO rechaza el null antes de consultar JPA.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findById(null)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testFindByIdErrorPersistencia() {
        // Escenario: EntityManager.find falla internamente.
        // Esperado: el error se envuelve como IllegalStateException.
        UUID id = UUID.randomUUID();
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id))
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mockEM);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findById(id)
        );

        assertEquals("Error al buscar la entidad", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testEliminarIdExistente() {
        // Escenario: la entidad existe y JPA puede eliminarla.
        // Esperado: se busca por UUID y luego se invoca remove.
        UUID id = UUID.randomUUID();
        TestEntity encontrado = new TestEntity(id);
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(encontrado);

        TestDAO cut = new TestDAO(mockEM);

        cut.eliminar(id);

        Mockito.verify(mockEM).find(TestEntity.class, id);
        Mockito.verify(mockEM).remove(encontrado);
    }

    @Test
    public void testEliminarIdInexistente() {
        // Escenario: no existe registro para el UUID indicado.
        // Esperado: remove no se invoca porque no hay entidad administrada.
        UUID id = UUID.randomUUID();
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(null);

        TestDAO cut = new TestDAO(mockEM);

        cut.eliminar(id);

        Mockito.verify(mockEM).find(TestEntity.class, id);
        Mockito.verify(mockEM, Mockito.never()).remove(Mockito.any());
    }

    @Test
    public void testEliminarIdentificadorNull() {
        // Escenario: se intenta eliminar sin llave primaria.
        // Esperado: la validacion de findById impide tocar JPA.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.eliminar(null)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testEliminarErrorDuranteFind() {
        // Escenario: falla la busqueda previa a la eliminacion.
        // Esperado: se propaga el error de busqueda y nunca se llama remove.
        UUID id = UUID.randomUUID();
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id))
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mockEM);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.eliminar(id)
        );

        assertEquals("Error al buscar la entidad", ex.getMessage());
        Mockito.verify(mockEM, Mockito.never()).remove(Mockito.any());
    }

    @Test
    public void testEliminarErrorDuranteRemove() {
        // Escenario: la entidad existe, pero JPA falla al eliminar.
        // Esperado: el DAO reporta error especifico de eliminacion.
        UUID id = UUID.randomUUID();
        TestEntity encontrado = new TestEntity(id);
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.find(TestEntity.class, id)).thenReturn(encontrado);
        Mockito.doThrow(new RuntimeException("Error simulado"))
                .when(mockEM)
                .remove(encontrado);

        TestDAO cut = new TestDAO(mockEM);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.eliminar(id)
        );

        assertEquals("Error al eliminar la entidad", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testFindRangeFirstCeroMaxUno() {
        // Escenario: primera pagina con un solo registro.
        // Esperado: Criteria API recibe paginacion first=0 y max=1.
        RangeCriteriaMocks mocks = new RangeCriteriaMocks();
        List<TestEntity> registros
                = List.of(new TestEntity(UUID.randomUUID()));
        mocks.prepararConsultaExitosa(registros);

        TestDAO cut = new TestDAO(mocks.em);

        List<TestEntity> resultado = cut.findRange(0, 1);

        assertEquals(registros, resultado);
        Mockito.verify(mocks.query).setFirstResult(0);
        Mockito.verify(mocks.query).setMaxResults(1);
    }

    @Test
    public void testFindRangeFirstMayorCeroMaxMayorUno() {
        // Escenario: pagina posterior con varios registros.
        // Esperado: se aplican los valores enviados al TypedQuery.
        RangeCriteriaMocks mocks = new RangeCriteriaMocks();
        List<TestEntity> registros = List.of(
                new TestEntity(UUID.randomUUID()),
                new TestEntity(UUID.randomUUID())
        );
        mocks.prepararConsultaExitosa(registros);

        TestDAO cut = new TestDAO(mocks.em);

        List<TestEntity> resultado = cut.findRange(5, 2);

        assertEquals(registros, resultado);
        Mockito.verify(mocks.query).setFirstResult(5);
        Mockito.verify(mocks.query).setMaxResults(2);
    }

    @Test
    public void testFindRangeFirstNegativo() {
        // Escenario: posicion inicial negativa.
        // Esperado: se rechaza antes de crear una consulta Criteria.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findRange(-1, 10)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testFindRangeMaxCero() {
        // Escenario: tamano de pagina cero.
        // Esperado: no se consulta JPA porque max debe ser positivo.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findRange(0, 0)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testFindRangeMaxNegativo() {
        // Escenario: tamano de pagina negativo.
        // Esperado: la validacion evita una consulta invalida.
        EntityManager mockEM = Mockito.mock(EntityManager.class);
        TestDAO cut = new TestDAO(mockEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findRange(0, -1)
        );

        Mockito.verifyNoInteractions(mockEM);
    }

    @Test
    public void testFindRangeErrorCriteriaBuilder() {
        // Escenario: falla EntityManager al entregar CriteriaBuilder.
        // Esperado: el DAO envuelve el error como problema de consulta.
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.getCriteriaBuilder())
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mockEM);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findRange(0, 10)
        );

        assertEquals("Error al consultar los registros", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testFindRangeErrorCreateCriteriaQuery() {
        // Escenario: CriteriaBuilder no puede crear CriteriaQuery<TestEntity>.
        // Esperado: se reporta error de consulta manteniendo la causa.
        RangeCriteriaMocks mocks = new RangeCriteriaMocks();

        Mockito.when(mocks.em.getCriteriaBuilder()).thenReturn(mocks.cb);
        Mockito.when(mocks.cb.createQuery(TestEntity.class))
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mocks.em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findRange(0, 10)
        );

        assertEquals("Error al consultar los registros", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testFindRangeErrorCreateTypedQuery() {
        // Escenario: EntityManager no puede convertir CriteriaQuery en query ejecutable.
        // Esperado: el DAO entrega una excepcion uniforme de consulta.
        RangeCriteriaMocks mocks = new RangeCriteriaMocks();

        Mockito.when(mocks.em.getCriteriaBuilder()).thenReturn(mocks.cb);
        Mockito.when(mocks.cb.createQuery(TestEntity.class)).thenReturn(mocks.cq);
        Mockito.when(mocks.cq.from(TestEntity.class)).thenReturn(mocks.root);
        Mockito.when(mocks.cq.select(mocks.root)).thenReturn(mocks.cq);
        Mockito.when(mocks.em.createQuery(mocks.cq))
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mocks.em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findRange(0, 10)
        );

        assertEquals("Error al consultar los registros", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testFindRangeErrorGetResultList() {
        // Escenario: la consulta Criteria se construye, pero falla al ejecutarse.
        // Esperado: getResultList queda cubierto como punto de error.
        RangeCriteriaMocks mocks = new RangeCriteriaMocks();
        mocks.prepararConsultaExitosa(List.of());

        Mockito.when(mocks.query.getResultList())
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mocks.em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findRange(0, 10)
        );

        assertEquals("Error al consultar los registros", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testContarCeroRegistros() {
        // Escenario: COUNT devuelve cero.
        // Esperado: contar retorna 0 y usa CriteriaQuery<Long>.
        CountCriteriaMocks mocks = new CountCriteriaMocks();
        mocks.prepararConteoExitoso(0L);

        TestDAO cut = new TestDAO(mocks.em);

        assertEquals(0L, cut.contar());
        Mockito.verify(mocks.cb).createQuery(Long.class);
        Mockito.verify(mocks.cb).count(mocks.root);
    }

    @Test
    public void testContarVariosRegistros() {
        // Escenario: COUNT devuelve varios registros.
        // Esperado: se retorna el unico valor producido por getSingleResult.
        CountCriteriaMocks mocks = new CountCriteriaMocks();
        mocks.prepararConteoExitoso(7L);

        TestDAO cut = new TestDAO(mocks.em);

        assertEquals(7L, cut.contar());
        Mockito.verify(mocks.query).getSingleResult();
    }

    @Test
    public void testContarErrorCriteriaBuilder() {
        // Escenario: EntityManager falla al crear el flujo Criteria.
        // Esperado: el DAO reporta error de conteo.
        EntityManager mockEM = Mockito.mock(EntityManager.class);

        Mockito.when(mockEM.getCriteriaBuilder())
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mockEM);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                cut::contar
        );

        assertEquals("Error al contar los registros", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testContarErrorCreateCriteriaQuery() {
        // Escenario: CriteriaBuilder no puede crear CriteriaQuery<Long>.
        // Esperado: la causa se conserva dentro de IllegalStateException.
        CountCriteriaMocks mocks = new CountCriteriaMocks();

        Mockito.when(mocks.em.getCriteriaBuilder()).thenReturn(mocks.cb);
        Mockito.when(mocks.cb.createQuery(Long.class))
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mocks.em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                cut::contar
        );

        assertEquals("Error al contar los registros", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    public void testContarErrorGetSingleResult() {
        // Escenario: la consulta COUNT se crea, pero falla al ejecutarse.
        // Esperado: el DAO envuelve el error de getSingleResult.
        CountCriteriaMocks mocks = new CountCriteriaMocks();
        mocks.prepararConteoExitoso(1L);

        Mockito.when(mocks.query.getSingleResult())
                .thenThrow(new RuntimeException("Error simulado"));

        TestDAO cut = new TestDAO(mocks.em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                cut::contar
        );

        assertEquals("Error al contar los registros", ex.getMessage());
        assertNotNull(ex.getCause());
    }
}
