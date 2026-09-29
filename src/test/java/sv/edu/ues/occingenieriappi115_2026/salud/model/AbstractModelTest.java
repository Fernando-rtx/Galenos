package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void constructorRequiereDao() {
        DAOInterface<TestEntity> dao = mock(DAOInterface.class);
        ConcreteModel model = new ConcreteModel(dao);
        assertNotNull(model);
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
    void getRowKeyConEntidadValidaDevuelveUuid() {
        DAOInterface<TestEntity> dao = mock(DAOInterface.class);
        ConcreteModel model = new ConcreteModel(dao);
        TestEntity entidad = new TestEntity();
        UUID id = UUID.randomUUID();
        when(dao.obtenerId(entidad)).thenReturn(id);

        String resultado = model.getRowKey(entidad);

        assertEquals(id.toString(), resultado);
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
    void getRowDataConUuidValidoBuscaEnDao() {
        DAOInterface<TestEntity> dao = mock(DAOInterface.class);
        ConcreteModel model = new ConcreteModel(dao);
        UUID id = UUID.randomUUID();
        TestEntity esperado = new TestEntity();
        when(dao.buscarPorId(id)).thenReturn(esperado);

        TestEntity resultado = model.getRowData(id.toString());

        assertEquals(esperado, resultado);
        verify(dao).buscarPorId(id);
    }

    @Test
    void getRowDataConNullDevuelveNull() {
        ConcreteModel model = new ConcreteModel(mock(DAOInterface.class));
        assertNull(model.getRowData(null));
    }

    @Test
    void getRowDataConBlankDevuelveNull() {
        ConcreteModel model = new ConcreteModel(mock(DAOInterface.class));
        assertNull(model.getRowData("  "));
    }

    @Test
    void getRowDataConUuidInvalidoDevuelveNull() {
        ConcreteModel model = new ConcreteModel(mock(DAOInterface.class));
        assertNull(model.getRowData("no-es-uuid"));
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

    @Test
    void loadConSortVacioNoRompe() {
        DAOInterface<TestEntity> dao = mock(DAOInterface.class);
        ConcreteModel model = new ConcreteModel(dao);
        when(dao.contar(anyList())).thenReturn(0L);
        when(dao.obtenerPagina(anyInt(), anyInt(), anyList(), anyList())).thenReturn(List.of());

        List<TestEntity> resultado = model.load(0, 10, new java.util.HashMap<>(), null);

        assertNotNull(resultado);
    }
}
