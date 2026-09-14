package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenTipoExamenDAOInterface;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExamenModelTest {

    private void inyectarDAO(ExamenModel model, ExamenDAO dao)
            throws Exception {
        Field daoField = ExamenModel.class.getDeclaredField("examenDAO");
        daoField.setAccessible(true);
        daoField.set(model, dao);
    }

    private void inyectarDetalleDAO(
            ExamenModel model,
            ExamenTipoExamenDAOInterface dao)
            throws Exception {
        Field daoField = ExamenModel.class.getDeclaredField(
                "examenTipoExamenDAO"
        );
        daoField.setAccessible(true);
        daoField.set(model, dao);
    }

    private void inyectarTipoExamenDAO(ExamenModel model, TipoExamenDAO dao)
            throws Exception {
        Field daoField = ExamenModel.class.getDeclaredField("tipoExamenDAO");
        daoField.setAccessible(true);
        daoField.set(model, dao);
    }

    @Test
    public void testDeclaraNamedYViewScoped() {
        assertTrue(ExamenModel.class.isAnnotationPresent(Named.class));
        assertTrue(ExamenModel.class.isAnnotationPresent(ViewScoped.class));
    }

    @Test
    public void testGetDAORetornaDAOInyectado()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenDAO dao = Mockito.mock(ExamenDAO.class);

        inyectarDAO(cut, dao);

        assertSame(dao, cut.getDAO());
    }

    @Test
    public void testInstanciaRegistroGeneraUUIDActivo() {
        ExamenModel cut = new ExamenModel();

        Examen registro = cut.instanciaRegistro();

        assertNotNull(registro);
        assertNotNull(registro.getIdExamen());
        assertEquals(Boolean.TRUE, registro.getActivo());
    }

    @Test
    public void testGetIdByRegistroRetornaIdExamen() {
        ExamenModel cut = new ExamenModel();
        UUID id = UUID.randomUUID();
        Examen registro = new Examen(id);

        assertEquals(id, cut.getIdByRegistro(registro));
    }

    @Test
    public void testGetIdByRowKeyConvierteUUID() {
        ExamenModel cut = new ExamenModel();
        UUID id = UUID.randomUUID();

        assertEquals(id, cut.getIdByRowKey(id.toString()));
    }

    @Test
    public void testGetIdByRowKeyInvalido() {
        ExamenModel cut = new ExamenModel();

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.getIdByRowKey("uuid-invalido")
        );
    }

    @Test
    public void testGetRowDataUsaDAOFindById()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenDAO dao = Mockito.mock(ExamenDAO.class);
        UUID id = UUID.randomUUID();
        Examen esperado = new Examen(id);

        inyectarDAO(cut, dao);
        Mockito.when(dao.findById(id)).thenReturn(esperado);

        assertSame(esperado, cut.getRowData(id.toString()));
        Mockito.verify(dao).findById(id);
    }

    @Test
    public void testSeleccionarExamenDesdeEventoInicializaDetalle()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);

        inyectarDetalleDAO(cut, detalleDAO);
        Mockito.when(detalleDAO.countByIdExamen(idExamen)).thenReturn(4L);

        @SuppressWarnings("unchecked")
        SelectEvent<Examen> event = Mockito.mock(SelectEvent.class);
        Mockito.when(event.getObject()).thenReturn(examen);

        cut.seleccionarExamen(event);

        assertSame(examen, cut.getExamenSeleccionado());
        assertEquals(idExamen, cut.getIdExamenSeleccionado());
        assertNotNull(cut.getModeloDetalle());
        assertEquals(4, cut.getModeloDetalle().getRowCount());
        Mockito.verify(detalleDAO).countByIdExamen(idExamen);
    }

    @Test
    public void testDetalleSinSeleccionNoConsultaDAO()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);

        inyectarDetalleDAO(cut, detalleDAO);
        cut.inicializarDetalle();

        List<ExamenTipoExamen> resultado = cut.getModeloDetalle().load(
                0,
                10,
                Map.<String, SortMeta>of(),
                Map.<String, FilterMeta>of()
        );

        assertEquals(0, cut.getModeloDetalle().getRowCount());
        assertTrue(resultado.isEmpty());
        Mockito.verifyNoInteractions(detalleDAO);
    }

    @Test
    public void testLazyDetalleUsaFindByIdExamenYCount()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);
        List<ExamenTipoExamen> esperado = List.of(
                new ExamenTipoExamen(UUID.randomUUID())
        );

        inyectarDetalleDAO(cut, detalleDAO);
        Mockito.when(detalleDAO.countByIdExamen(idExamen)).thenReturn(1L);
        Mockito.when(detalleDAO.findByIdExamen(idExamen, 5, 10))
                .thenReturn(esperado);

        cut.setExamenSeleccionado(examen);
        Mockito.clearInvocations(detalleDAO);

        List<ExamenTipoExamen> resultado = cut.getModeloDetalle().load(
                5,
                10,
                Map.<String, SortMeta>of(),
                Map.<String, FilterMeta>of()
        );

        assertSame(esperado, resultado);
        assertEquals(1, cut.getModeloDetalle().getRowCount());
        Mockito.verify(detalleDAO).countByIdExamen(idExamen);
        Mockito.verify(detalleDAO).findByIdExamen(idExamen, 5, 10);
    }

    @Test
    public void testLazyTiposExamenUsaTipoExamenDAO()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        TipoExamenDAO tipoExamenDAO = Mockito.mock(TipoExamenDAO.class);
        List<TipoExamen> esperado = List.of(
                new TipoExamen(UUID.randomUUID())
        );

        inyectarTipoExamenDAO(cut, tipoExamenDAO);
        Mockito.when(tipoExamenDAO.contar()).thenReturn(1L);
        Mockito.when(tipoExamenDAO.findRange(0, 10)).thenReturn(esperado);

        cut.inicializarTiposExamen();
        Mockito.clearInvocations(tipoExamenDAO);

        List<TipoExamen> resultado = cut.getModeloTiposExamen().load(
                0,
                10,
                Map.<String, SortMeta>of(),
                Map.<String, FilterMeta>of()
        );

        assertSame(esperado, resultado);
        assertEquals(1, cut.getModeloTiposExamen().getRowCount());
        Mockito.verify(tipoExamenDAO).contar();
        Mockito.verify(tipoExamenDAO).findRange(0, 10);
    }

    @Test
    public void testContarDetalleUsaCountByIdExamen()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idExamen = UUID.randomUUID();

        inyectarDetalleDAO(cut, detalleDAO);
        Mockito.when(detalleDAO.countByIdExamen(idExamen)).thenReturn(7L);

        cut.setExamenSeleccionado(new Examen(idExamen));
        Mockito.clearInvocations(detalleDAO);

        assertEquals(7, cut.contarDetalle());
        Mockito.verify(detalleDAO).countByIdExamen(idExamen);
    }

    @Test
    public void testCambioDeSeleccionUsaNuevoId()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idAnterior = UUID.randomUUID();
        UUID idNuevo = UUID.randomUUID();

        inyectarDetalleDAO(cut, detalleDAO);
        Mockito.when(detalleDAO.countByIdExamen(idNuevo)).thenReturn(1L);
        Mockito.when(detalleDAO.findByIdExamen(idNuevo, 0, 10))
                .thenReturn(List.of(new ExamenTipoExamen(UUID.randomUUID())));

        cut.setExamenSeleccionado(new Examen(idAnterior));
        cut.setExamenSeleccionado(new Examen(idNuevo));
        Mockito.clearInvocations(detalleDAO);

        cut.getModeloDetalle().load(
                0,
                10,
                Map.<String, SortMeta>of(),
                Map.<String, FilterMeta>of()
        );

        Mockito.verify(detalleDAO).countByIdExamen(idNuevo);
        Mockito.verify(detalleDAO).findByIdExamen(idNuevo, 0, 10);
        Mockito.verify(detalleDAO, Mockito.never())
                .findByIdExamen(Mockito.eq(idAnterior), Mockito.anyInt(),
                        Mockito.anyInt());
    }

    @Test
    public void testSeleccionarParaEditarActualizaRegistroEstadoYDetalle()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);

        inyectarDetalleDAO(cut, detalleDAO);

        cut.seleccionar(examen);

        assertSame(examen, cut.getRegistro());
        assertSame(examen, cut.getExamenSeleccionado());
        assertEquals(ESTADO_CRUD.MODIFICAR, cut.getEstado());
        Mockito.verify(detalleDAO).countByIdExamen(idExamen);
    }

    @Test
    public void testNuevoLimpiaSeleccionYDetalle()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);

        inyectarDetalleDAO(cut, detalleDAO);
        cut.setExamenSeleccionado(new Examen(UUID.randomUUID()));
        Mockito.clearInvocations(detalleDAO);

        cut.nuevo();

        assertEquals(ESTADO_CRUD.CREAR, cut.getEstado());
        assertNotNull(cut.getRegistro());
        assertNull(cut.getExamenSeleccionado());
        assertEquals(0, cut.getModeloDetalle().getRowCount());
        Mockito.verifyNoInteractions(detalleDAO);
    }

    @Test
    public void testPrepararAsociacionLimpiaTipoEInicializaModelo()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        TipoExamenDAO tipoExamenDAO = Mockito.mock(TipoExamenDAO.class);

        inyectarTipoExamenDAO(cut, tipoExamenDAO);
        Mockito.when(tipoExamenDAO.contar()).thenReturn(3L);

        cut.setTipoExamenSeleccionado(new TipoExamen(UUID.randomUUID()));

        cut.prepararAsociacion();

        assertNull(cut.getTipoExamenSeleccionado());
        assertNotNull(cut.getModeloTiposExamen());
        assertEquals(3, cut.getModeloTiposExamen().getRowCount());
        Mockito.verify(tipoExamenDAO).contar();
    }

    @Test
    public void testAsociarTipoCreaExamenTipoExamenYRefrescaDetalle()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idExamen = UUID.randomUUID();
        UUID idTipoExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);
        TipoExamen tipoExamen = new TipoExamen(idTipoExamen);

        inyectarDetalleDAO(cut, detalleDAO);
        cut.setExamenSeleccionado(examen);
        cut.setTipoExamenSeleccionado(tipoExamen);
        Mockito.clearInvocations(detalleDAO);

        Mockito.when(detalleDAO.countByIdExamenAndIdTipoExamen(
                idExamen,
                idTipoExamen
        )).thenReturn(0L);
        Mockito.when(detalleDAO.countByIdExamen(idExamen)).thenReturn(1L);

        cut.asociarTipoExamen();

        ArgumentCaptor<ExamenTipoExamen> captor =
                ArgumentCaptor.forClass(ExamenTipoExamen.class);

        Mockito.verify(detalleDAO).countByIdExamenAndIdTipoExamen(
                idExamen,
                idTipoExamen
        );
        Mockito.verify(detalleDAO).crear(captor.capture());
        Mockito.verify(detalleDAO).countByIdExamen(idExamen);

        ExamenTipoExamen creado = captor.getValue();
        assertNotNull(creado.getIdExamenTipoExamen());
        assertSame(examen, creado.getIdExamen());
        assertSame(tipoExamen, creado.getIdTipoExamen());
        assertNotNull(creado.getFechaCreacion());
        assertNull(cut.getTipoExamenSeleccionado());
        assertEquals(1, cut.getModeloDetalle().getRowCount());
    }

    @Test
    public void testAsociarTipoSinExamenNoInvocaDAO()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);

        inyectarDetalleDAO(cut, detalleDAO);
        cut.setTipoExamenSeleccionado(new TipoExamen(UUID.randomUUID()));

        cut.asociarTipoExamen();

        Mockito.verifyNoInteractions(detalleDAO);
    }

    @Test
    public void testAsociarTipoSinTipoNoInvocaDAO()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);

        inyectarDetalleDAO(cut, detalleDAO);
        cut.setExamenSeleccionado(new Examen(UUID.randomUUID()));
        Mockito.clearInvocations(detalleDAO);

        cut.asociarTipoExamen();

        Mockito.verifyNoInteractions(detalleDAO);
    }

    @Test
    public void testAsociarTipoDuplicadoNoCrea()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idExamen = UUID.randomUUID();
        UUID idTipoExamen = UUID.randomUUID();

        inyectarDetalleDAO(cut, detalleDAO);
        cut.setExamenSeleccionado(new Examen(idExamen));
        cut.setTipoExamenSeleccionado(new TipoExamen(idTipoExamen));
        Mockito.clearInvocations(detalleDAO);

        Mockito.when(detalleDAO.countByIdExamenAndIdTipoExamen(
                idExamen,
                idTipoExamen
        )).thenReturn(1L);

        cut.asociarTipoExamen();

        Mockito.verify(detalleDAO).countByIdExamenAndIdTipoExamen(
                idExamen,
                idTipoExamen
        );
        Mockito.verify(detalleDAO, Mockito.never())
                .crear(Mockito.any(ExamenTipoExamen.class));
        Mockito.verify(detalleDAO, Mockito.never())
                .countByIdExamen(Mockito.any());
    }

    @Test
    public void testEliminarInvocaDAOYLimpiaSeleccion()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenDAO dao = Mockito.mock(ExamenDAO.class);
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idExamen = UUID.randomUUID();
        Examen examen = new Examen(idExamen);

        inyectarDAO(cut, dao);
        inyectarDetalleDAO(cut, detalleDAO);
        Mockito.when(dao.contar()).thenReturn(0L);

        cut.setRegistro(examen);
        cut.setEstado(ESTADO_CRUD.MODIFICAR);
        cut.setExamenSeleccionado(examen);
        Mockito.clearInvocations(dao, detalleDAO);

        cut.eliminar(examen);

        Mockito.verify(dao).eliminar(idExamen);
        assertNull(cut.getRegistro());
        assertNull(cut.getExamenSeleccionado());
        assertEquals(ESTADO_CRUD.NINGUNO, cut.getEstado());
        assertEquals(0, cut.getModeloDetalle().getRowCount());
    }

    @Test
    public void testQuitarAsociacionEliminaSoloRegistroPuenteYRefrescaDetalle()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenDAO examenDAO = Mockito.mock(ExamenDAO.class);
        TipoExamenDAO tipoExamenDAO = Mockito.mock(TipoExamenDAO.class);
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID idExamen = UUID.randomUUID();
        UUID idAsociacion = UUID.randomUUID();
        ExamenTipoExamen asociacion =
                new ExamenTipoExamen(idAsociacion);

        inyectarDAO(cut, examenDAO);
        inyectarTipoExamenDAO(cut, tipoExamenDAO);
        inyectarDetalleDAO(cut, detalleDAO);
        cut.setExamenSeleccionado(new Examen(idExamen));
        Mockito.clearInvocations(examenDAO, tipoExamenDAO, detalleDAO);

        Mockito.when(detalleDAO.countByIdExamen(idExamen)).thenReturn(2L);

        cut.quitarAsociacion(asociacion);

        Mockito.verify(detalleDAO).eliminar(idAsociacion);
        Mockito.verify(detalleDAO).countByIdExamen(idExamen);
        Mockito.verifyNoInteractions(examenDAO);
        Mockito.verifyNoInteractions(tipoExamenDAO);
        assertEquals(2, cut.getModeloDetalle().getRowCount());
    }

    @Test
    public void testGetRowDataDetalleUsaDAOFindById()
            throws Exception {
        ExamenModel cut = new ExamenModel();
        ExamenTipoExamenDAOInterface detalleDAO =
                Mockito.mock(ExamenTipoExamenDAOInterface.class);
        UUID id = UUID.randomUUID();
        ExamenTipoExamen esperado = new ExamenTipoExamen(id);

        inyectarDetalleDAO(cut, detalleDAO);
        Mockito.when(detalleDAO.findById(id)).thenReturn(esperado);

        assertSame(esperado, cut.getRowDataDetalle(id.toString()));
        Mockito.verify(detalleDAO).findById(id);
    }

    @Test
    public void testNombreModelo() {
        ExamenModel cut = new ExamenModel();

        assertEquals("Examen", cut.getNombreModelo());
    }
}
