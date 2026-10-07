package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

class AbstractModelTest {

    private static class TestEntity {
    }

    private static class ConcreteModel extends AbstractModel<TestEntity> {
        private final DAOInterface<TestEntity> dao;

        ConcreteModel(DAOInterface<TestEntity> dao) {
            this.dao = dao;
        }

        @Override
        protected DAOInterface<TestEntity> getDao() {
            return dao;
        }
    }

    @Test
    void estadoInicialEsListado() {
        ConcreteModel model = new ConcreteModel(mock(DAOInterface.class));
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void setEstadoCambiaElEstado() {
        ConcreteModel model = new ConcreteModel(mock(DAOInterface.class));
        model.setEstado(ESTADO_CRUD.CREACION);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void getRowKeyConEntidadSinIdDevuelveNull() {
        DAOInterface<TestEntity> dao = mock(DAOInterface.class);
        ConcreteModel model = new ConcreteModel(dao);
        TestEntity entidad = new TestEntity();
        when(dao.obtenerId(entidad)).thenReturn(null);

        String resultado = model.getRowKey(entidad);

        assertNull(resultado);
    }

    @Test
    void countSinFiltrosDelegaAlDao() {
        DAOInterface<TestEntity> dao = mock(DAOInterface.class);
        ConcreteModel model = new ConcreteModel(dao);
        when(dao.contar(anyList())).thenReturn(5L);

        int resultado = model.count(null);

        assertEquals(5, resultado);
        verify(dao).contar(anyList());
    }

    @Test
    void loadSinFiltrosNiOrdenDelegaAlDao() {
        DAOInterface<TestEntity> dao = mock(DAOInterface.class);
        ConcreteModel model = new ConcreteModel(dao);
        List<TestEntity> esperado = List.of(new TestEntity(), new TestEntity());
        when(dao.contar(anyList())).thenReturn(2L);
        when(dao.obtenerPagina(anyInt(), anyInt(), anyList(), anyList())).thenReturn(esperado);

        List<TestEntity> resultado = model.load(0, 10, null, null);

        assertEquals(esperado, resultado);
        verify(dao).obtenerPagina(anyInt(), anyInt(), anyList(), anyList());
    }

    @Test
    void countConFiltrosVaciosNoRompe() {
        DAOInterface<TestEntity> dao = mock(DAOInterface.class);
        ConcreteModel model = new ConcreteModel(dao);
        when(dao.contar(anyList())).thenReturn(0L);

        int resultado = model.count(new java.util.HashMap<>());

        assertEquals(0, resultado);
    }

    private static class TestModel extends AbstractModel<Persona> {
        private final DAOInterface<Persona> dao;

        TestModel(DAOInterface<Persona> dao) {
            this.dao = dao;
        }

        @Override
        protected DAOInterface<Persona> getDao() {
            return dao;
        }
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
}
