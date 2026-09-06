package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    }

    @Test
    public void testInicializarPreparaEstadoListasYRegistros() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        List<TestEntity> registros = List.of(
                new TestEntity(UUID.randomUUID()),
                new TestEntity(UUID.randomUUID())
        );

        Mockito.when(dao.contar()).thenReturn(2L);
        Mockito.when(dao.findRange(0, 2)).thenReturn(registros);

        TestModel cut = new TestModel(dao);

        cut.inicializar();

        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
        assertNull(cut.getRegistro());
        assertEquals(registros, cut.getRegistros());
        assertTrue(cut.listasInicializadas);
        Mockito.verify(dao).contar();
        Mockito.verify(dao).findRange(0, 2);
    }

    @Test
    public void testInicializarRegistrosSinDatosNoConsultaRangoVacio() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);

        Mockito.when(dao.contar()).thenReturn(0L);

        TestModel cut = new TestModel(dao);

        cut.inicializarRegistros();

        assertEquals(List.of(), cut.getRegistros());
        Mockito.verify(dao).contar();
        Mockito.verify(dao, Mockito.never()).findRange(Mockito.anyInt(), Mockito.anyInt());
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
        List<TestEntity> registros = List.of(registro);

        Mockito.when(dao.contar()).thenReturn(1L);
        Mockito.when(dao.findRange(0, 1)).thenReturn(registros);

        cut.setRegistro(registro);
        cut.setEstado(ESTADO_CRUD.CREAR);

        cut.guardar();

        Mockito.verify(dao).crear(registro);
        Mockito.verify(dao).contar();
        Mockito.verify(dao).findRange(0, 1);
        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
        assertEquals(registros, cut.getRegistros());
    }

    @Test
    public void testGuardarModificarInvocaDAOYRecargaRegistros() {
        @SuppressWarnings("unchecked")
        DAOInterface<TestEntity> dao = Mockito.mock(DAOInterface.class);
        TestModel cut = new TestModel(dao);
        TestEntity registro = new TestEntity(UUID.randomUUID());
        TestEntity modificado = new TestEntity(registro.id);
        List<TestEntity> registros = List.of(modificado);

        Mockito.when(dao.modificar(registro)).thenReturn(modificado);
        Mockito.when(dao.contar()).thenReturn(1L);
        Mockito.when(dao.findRange(0, 1)).thenReturn(registros);

        cut.setRegistro(registro);
        cut.setEstado(ESTADO_CRUD.MODIFICAR);

        cut.guardar();

        Mockito.verify(dao).modificar(registro);
        Mockito.verify(dao).contar();
        Mockito.verify(dao).findRange(0, 1);
        assertSame(modificado, cut.getRegistro());
        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
        assertEquals(registros, cut.getRegistros());
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
}
