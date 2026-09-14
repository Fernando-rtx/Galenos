package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la logica ejecutable comun de AbstractModel.
 */
public class AbstractModelTest {

    /**
     * Entidad minima usada para probar el modelo generico sin depender de una
     * entidad JPA real.
     */
    private static class TestEntity {

        private final UUID id;

        TestEntity(UUID id) {
            this.id = id;
        }
    }

    /**
     * Subclase concreta minima para ejercitar AbstractModel en pruebas.
     */
    private static class TestModel extends AbstractModel<TestEntity> {

        private static final long serialVersionUID = 1L;

        private final DAOInterface<TestEntity> dao;
        private boolean listasInicializadas;

        TestModel(DAOInterface<TestEntity> dao) {
            this.dao = dao;
        }

        @Override
        protected DAOInterface<TestEntity> getDAO() {
            return dao;
        }

        @Override
        public TestEntity instanciaRegistro() {
            return new TestEntity(UUID.randomUUID());
        }

        @Override
        public void inicializarListas() {
            listasInicializadas = true;
        }

        @Override
        protected Object getIdByRegistro(TestEntity registro) {
            if (registro == null) {
                return null;
            }

            return registro.id;
        }

        @Override
        protected Object getIdByRowKey(String rowKey) {
            return UUID.fromString(rowKey);
        }

        @Override
        public String getNombreModelo() {
            return "Entidad de prueba";
        }
    }

    @Test
    public void testInicializarPreparaEstadoListasYModeloLazy() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);

        Mockito.when(dao.contar()).thenReturn(2L);

        TestModel cut = new TestModel(dao);

        cut.inicializar();

        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
        assertNull(cut.getRegistro());
        assertNotNull(cut.getModelo());
        assertEquals(2, cut.getModelo().getRowCount());
        assertTrue(cut.listasInicializadas);
        Mockito.verify(dao).contar();
        Mockito.verify(dao, Mockito.never())
                .findRange(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void testLazyLoadUsaDAOFindRange() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        List<TestEntity> registros = List.of(
                new TestEntity(UUID.randomUUID()),
                new TestEntity(UUID.randomUUID())
        );

        Mockito.when(dao.contar()).thenReturn(20L);
        Mockito.when(dao.findRange(5, 2)).thenReturn(registros);

        TestModel cut = new TestModel(dao);
        cut.inicializarRegistros();

        List<TestEntity> resultado = cut.getModelo().load(
                5,
                2,
                Map.<String, SortMeta>of(),
                Map.<String, FilterMeta>of()
        );

        assertEquals(registros, resultado);
        assertEquals(20, cut.getModelo().getRowCount());
        Mockito.verify(dao, Mockito.times(2)).contar();
        Mockito.verify(dao).findRange(5, 2);
    }

    @Test
    public void testNuevoPreparaRegistroYEstadoCrear() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);

        cut.nuevo();

        assertEquals(ESTADO_CRUD.CREAR, cut.getEstado());
        assertNotNull(cut.getRegistro());
    }

    @Test
    public void testSeleccionarPreparaRegistroYEstadoModificar() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);
        TestEntity registro = new TestEntity(UUID.randomUUID());

        cut.seleccionar(registro);

        assertEquals(ESTADO_CRUD.MODIFICAR, cut.getEstado());
        assertSame(registro, cut.getRegistro());
    }

    @Test
    public void testGuardarCrearInvocaDAOYRecargaRegistros() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);
        TestEntity registro = new TestEntity(UUID.randomUUID());

        Mockito.when(dao.contar()).thenReturn(1L);

        cut.setRegistro(registro);
        cut.setEstado(ESTADO_CRUD.CREAR);

        cut.guardar();

        Mockito.verify(dao).crear(registro);
        Mockito.verify(dao).contar();
        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
        assertNotNull(cut.getModelo());
    }

    @Test
    public void testGuardarModificarInvocaDAOYRecargaRegistros() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);
        TestEntity registro = new TestEntity(UUID.randomUUID());
        TestEntity modificado = new TestEntity(registro.id);

        Mockito.when(dao.modificar(registro)).thenReturn(modificado);
        Mockito.when(dao.contar()).thenReturn(1L);

        cut.setRegistro(registro);
        cut.setEstado(ESTADO_CRUD.MODIFICAR);

        cut.guardar();

        Mockito.verify(dao).modificar(registro);
        Mockito.verify(dao).contar();
        assertSame(modificado, cut.getRegistro());
        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
        assertNotNull(cut.getModelo());
    }

    @Test
    public void testGuardarSinOperacionNoInvocaDAO() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);

        cut.setRegistro(new TestEntity(UUID.randomUUID()));

        cut.guardar();

        Mockito.verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
    }

    @Test
    public void testCancelarRestableceEstado() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);
        TestEntity registro = new TestEntity(UUID.randomUUID());

        cut.setRegistro(registro);
        cut.setEstado(ESTADO_CRUD.CREAR);
        cut.cancelar();

        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
        assertSame(registro, cut.getRegistro());
    }

    @Test
    public void testContarDevuelveConteoDelDAOComoInt() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);

        Mockito.when(dao.contar()).thenReturn(7L);

        TestModel cut = new TestModel(dao);

        assertEquals(7, cut.contar());
        Mockito.verify(dao).contar();
    }

    @Test
    public void testCantidadRegistrosPorDefectoYSetter() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);

        assertEquals(50, cut.getCantidadRegistros());

        cut.setCantidadRegistros(25);

        assertEquals(25, cut.getCantidadRegistros());
    }

    @Test
    public void testLazyCountUsaDAOContar() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        Mockito.when(dao.contar()).thenReturn(9L);

        TestModel cut = new TestModel(dao);
        cut.inicializarRegistros();

        int resultado = cut.getModelo().count(Map.of());

        assertEquals(9, resultado);
        Mockito.verify(dao, Mockito.times(2)).contar();
    }

    @Test
    public void testRowKeyUsaIdDelRegistro() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);
        UUID id = UUID.randomUUID();
        TestEntity registro = new TestEntity(id);

        assertEquals(id.toString(), cut.getRowKey(registro));
    }

    @Test
    public void testRowDataBuscaPorId() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);
        UUID id = UUID.randomUUID();
        TestEntity esperado = new TestEntity(id);

        Mockito.when(dao.findById(id)).thenReturn(esperado);

        assertSame(esperado, cut.getRowData(id.toString()));
        Mockito.verify(dao).findById(id);
    }

    @Test
    public void testRowDataRowKeyInvalido() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.getRowData("no-es-uuid")
        );
    }

    @Test
    public void testSeleccionarRegistroDesdeSelectEvent() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);
        TestEntity registro = new TestEntity(UUID.randomUUID());

        @SuppressWarnings("unchecked")
        SelectEvent<TestEntity> event = Mockito.mock(SelectEvent.class);
        Mockito.when(event.getObject()).thenReturn(registro);

        cut.seleccionarRegistro(event);

        assertSame(registro, cut.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, cut.getEstado());
    }
}
