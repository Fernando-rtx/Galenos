package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DefaultDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DireccionOrden;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class Fase8Test {

    private static class TestEntity {
        private UUID id;
        private String nombre;

        TestEntity(UUID id) {
            this.id = id;
        }
    }

    private static class TestDAO extends DefaultDAO<TestEntity> {
        private final EntityManager entityManager;

        TestDAO(EntityManager entityManager) {
            super(TestEntity.class);
            this.entityManager = entityManager;
        }

        @Override
        protected EntityManager getEntityManager() {
            return entityManager;
        }
    }

    private static class TestModel extends AbstractModel<Persona> {
        TestModel(DAOInterface<Persona> dao) {
            super(dao);
        }
    }

    @Test
    void obtenerPaginaAplicaOffsetYLimite() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<TestEntity> criteria = mock(CriteriaQuery.class);
        Root<TestEntity> root = mock(Root.class);
        TypedQuery<TestEntity> typed = mock(TypedQuery.class);
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(TestEntity.class)).thenReturn(criteria);
        when(criteria.from(TestEntity.class)).thenReturn(root);
        when(em.createQuery(criteria)).thenReturn(typed);
        when(typed.setFirstResult(20)).thenReturn(typed);
        when(typed.setMaxResults(10)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.of());

        new TestDAO(em).obtenerPagina(20, 10, List.of(), List.of());

        verify(typed).setFirstResult(20);
        verify(typed).setMaxResults(10);
    }

    @Test
    void obtenerPaginaAplicaOrdenAscendenteYDescendente() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<TestEntity> criteria = mock(CriteriaQuery.class);
        Root<TestEntity> root = mock(Root.class);
        Path<String> path = mock(Path.class);
        TypedQuery<TestEntity> typed = mock(TypedQuery.class);
        Order asc = mock(Order.class);
        Order desc = mock(Order.class);
        configurarConsulta(em, cb, criteria, root, typed);
        Metamodel metamodel = mock(Metamodel.class);
        ManagedType<TestEntity> managedType = mock(ManagedType.class);
        Attribute<TestEntity, String> attribute = mock(Attribute.class);
        when(em.getMetamodel()).thenReturn(metamodel);
        doReturn(TestEntity.class).when(root).getJavaType();
        doReturn(managedType).when(metamodel).managedType(TestEntity.class);
        doReturn(attribute).when(managedType).getAttribute("nombre");
        doReturn(path).when(root).get("nombre");
        doReturn(String.class).when(path).getJavaType();
        when(cb.asc(path)).thenReturn(asc);
        when(cb.desc(path)).thenReturn(desc);
        when(typed.setFirstResult(0)).thenReturn(typed);
        when(typed.setMaxResults(5)).thenReturn(typed);

        new TestDAO(em).obtenerPagina(0, 5, List.of(), List.of(
                new OrdenDAO("nombre", DireccionOrden.ASCENDENTE),
                new OrdenDAO("nombre", DireccionOrden.DESCENDENTE)));

        verify(cb).asc(path);
        verify(cb).desc(path);
    }

    @Test
    void contarConFiltroUsaCriteria() {
        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Long> criteria = mock(CriteriaQuery.class);
        Root<TestEntity> root = mock(Root.class);
        Path<String> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        TypedQuery<Long> typed = mock(TypedQuery.class);
        Metamodel metamodel = mock(Metamodel.class);
        ManagedType<TestEntity> managedType = mock(ManagedType.class);
        Attribute<TestEntity, String> attribute = mock(Attribute.class);
        configurarConsulta(em, cb, criteria, root, typed);
        when(em.getMetamodel()).thenReturn(metamodel);
        doReturn(TestEntity.class).when(root).getJavaType();
        doReturn(managedType).when(metamodel).managedType(TestEntity.class);
        doReturn(attribute).when(managedType).getAttribute("nombre");
        doReturn(path).when(root).get("nombre");
        doReturn(String.class).when(path).getJavaType();
        when(cb.equal(path, "Ana")).thenReturn(predicate);
        when(typed.getSingleResult()).thenReturn(1L);

        assertEquals(1L, new TestDAO(em).contar(
                List.of(new FiltroDAO("nombre", OperadorFiltro.IGUAL, "Ana"))));

        verify(cb).equal(path, "Ana");
        verify(criteria).where(any(Predicate[].class));
    }

    @Test
    void obtenerIdObtieneUuidDesdeElMetamodelo() {
        EntityManager em = mock(EntityManager.class);
        configurarId(em);
        UUID id = UUID.randomUUID();

        assertEquals(id, new TestDAO(em).obtenerId(new TestEntity(id)));
    }

    @Test
    void abstractModelDelegaCountYLoad() {
        DAOInterface<Persona> dao = mock(DAOInterface.class);
        when(dao.contar(List.of())).thenReturn(4L);
        when(dao.obtenerPagina(5, 2, List.of(), List.of())).thenReturn(List.of());
        TestModel model = new TestModel(dao);

        assertEquals(4, model.count(Map.of()));
        assertTrue(model.load(5, 2, Map.of(), Map.of()).isEmpty());

        verify(dao, times(2)).contar(List.of());
        verify(dao).obtenerPagina(5, 2, List.of(), List.of());
    }

    @Test
    void rowKeyYRowDataUsanUuid() {
        DAOInterface<Persona> dao = mock(DAOInterface.class);
        UUID id = UUID.randomUUID();
        Persona persona = new Persona(id);
        when(dao.obtenerId(persona)).thenReturn(id);
        when(dao.buscarPorId(id)).thenReturn(persona);
        TestModel model = new TestModel(dao);

        assertEquals(id.toString(), model.getRowKey(persona));
        assertSame(persona, model.getRowData(id.toString()));
        verify(dao).buscarPorId(id);
    }

    @Test
    void rowDataInvalidoNuloYBlancoDevuelveNull() {
        TestModel model = new TestModel(mock(DAOInterface.class));

        assertNull(model.getRowData("no-es-uuid"));
        assertNull(model.getRowData(null));
        assertNull(model.getRowData("  "));
    }

    @Test
    void estadoCrudContieneLosTresEstadosDefinidos() {
        assertEquals(List.of(ESTADO_CRUD.LISTADO, ESTADO_CRUD.CREACION, ESTADO_CRUD.EDICION),
                List.of(ESTADO_CRUD.values()));
    }

    private static void configurarConsulta(EntityManager em, CriteriaBuilder cb,
            CriteriaQuery<?> criteria, Root<?> root, TypedQuery<?> typed) {
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(any(Class.class))).thenReturn(criteria);
        when(criteria.from(any(Class.class))).thenReturn(root);
        when(em.createQuery(any(CriteriaQuery.class))).thenReturn(typed);
        doReturn(criteria).when(criteria).where(any(Predicate[].class));
        doReturn(criteria).when(criteria).orderBy(any(List.class));
    }

    @SuppressWarnings("unchecked")
    private static void configurarId(EntityManager em) {
        Metamodel metamodel = mock(Metamodel.class);
        EntityType<TestEntity> entityType = mock(EntityType.class);
        SingularAttribute<TestEntity, UUID> idAttribute = mock(SingularAttribute.class);
        when(em.getMetamodel()).thenReturn(metamodel);
        when(metamodel.entity(TestEntity.class)).thenReturn(entityType);
        doReturn(idAttribute).when(entityType).getId(UUID.class);
        when(idAttribute.getName()).thenReturn("id");
    }
}
