package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrdenExamenModelTest {

    @Test
    void nuevoCreaConsultaYCambiaEstadoACreacion() {
        OrdenExamenModel model = new OrdenExamenModel(
                mock(OrdenExamenDAO.class),
                mock(ConsultaProcedimientoPasoDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        OrdenExamenModel model = new OrdenExamenModel(
                mock(OrdenExamenDAO.class),
                mock(ConsultaProcedimientoPasoDAO.class));
        OrdenExamen ordenExamen = new OrdenExamen();

        model.seleccionar(ordenExamen);

        assertSame(ordenExamen, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
        OrdenExamenModel model = new OrdenExamenModel(
                dao, mock(ConsultaProcedimientoPasoDAO.class));
        model.nuevo();
        OrdenExamen seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
        OrdenExamenModel model = new OrdenExamenModel(
                dao, mock(ConsultaProcedimientoPasoDAO.class));
        OrdenExamen ordenExamen = new OrdenExamen();
        OrdenExamen actualizada = new OrdenExamen();
        model.seleccionar(ordenExamen);
        when(dao.actualizar(ordenExamen)).thenReturn(actualizada);

        model.guardar();

        verify(dao).actualizar(ordenExamen);
        assertSame(actualizada, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
        OrdenExamenModel model = new OrdenExamenModel(
                dao, mock(ConsultaProcedimientoPasoDAO.class));
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        OrdenExamenModel model = new OrdenExamenModel(
                mock(OrdenExamenDAO.class),
                mock(ConsultaProcedimientoPasoDAO.class));
        model.seleccionar(new OrdenExamen());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
        OrdenExamenModel model = new OrdenExamenModel(
                dao, mock(ConsultaProcedimientoPasoDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaPasos() {
        ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO = mock(ConsultaProcedimientoPasoDAO.class);
        when(consultaProcedimientoPasoDAO.obtenerTodos()).thenReturn(List.of(new ConsultaProcedimientoPaso()));

        OrdenExamenModel model = new OrdenExamenModel(
                mock(OrdenExamenDAO.class), consultaProcedimientoPasoDAO);

        assertEquals(1, model.getConsultaProcedimientoPasos().size());
    }

    @Test
    void idPasoContextoValidoFiltraLaListaDePasos() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());
        when(dao.buscarPorId(paso.getIdConsultaProcedimientoPaso())).thenReturn(paso);
        OrdenExamenModel model = new OrdenExamenModel(mock(OrdenExamenDAO.class), dao);

        model.setIdPasoContexto(paso.getIdConsultaProcedimientoPaso().toString());

        assertEquals(paso.getIdConsultaProcedimientoPaso().toString(), model.getIdPasoContexto());
        assertEquals(List.of(paso), model.getConsultaProcedimientoPasos());
    }

    @Test
    void idPasoContextoExponeRoundTripParaViewParam() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());
        when(dao.obtenerTodos()).thenReturn(List.of(paso));
        when(dao.buscarPorId(paso.getIdConsultaProcedimientoPaso())).thenReturn(paso);
        OrdenExamenModel model = new OrdenExamenModel(mock(OrdenExamenDAO.class), dao);

        model.setIdPasoContexto(paso.getIdConsultaProcedimientoPaso().toString());

        assertEquals(paso.getIdConsultaProcedimientoPaso().toString(), model.getIdPasoContexto());
        assertEquals(List.of(paso), model.getConsultaProcedimientoPasos());
    }

    @Test
    void idPasoContextoInvalidoConservaLaListaCompleta() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());
        when(dao.obtenerTodos()).thenReturn(List.of(paso));
        OrdenExamenModel model = new OrdenExamenModel(mock(OrdenExamenDAO.class), dao);

        model.setIdPasoContexto("no-es-uuid");

        assertEquals(List.of(paso), model.getConsultaProcedimientoPasos());
    }

    @Test
    void nuevoConContextoPreseleccionaElPaso() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());
        when(dao.buscarPorId(paso.getIdConsultaProcedimientoPaso())).thenReturn(paso);
        OrdenExamenModel model = new OrdenExamenModel(mock(OrdenExamenDAO.class), dao);
        model.setIdPasoContexto(paso.getIdConsultaProcedimientoPaso().toString());

        model.nuevo();

        assertSame(paso, model.getSeleccionado().getIdConsultaProcedimientoPaso());
    }
}
