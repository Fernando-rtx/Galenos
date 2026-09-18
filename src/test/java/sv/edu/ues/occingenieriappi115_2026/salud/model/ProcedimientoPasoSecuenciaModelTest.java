package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProcedimientoPasoSecuenciaModelTest {

    @Test
    void nuevoCreaSecuenciaYCambiaEstadoACreacion() {
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class),
                mock(ProcedimientoPasoDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class),
                mock(ProcedimientoPasoDAO.class));
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();

        model.seleccionar(secuencia);

        assertSame(secuencia, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        ProcedimientoPasoSecuencia seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        ProcedimientoPasoSecuencia actualizado = new ProcedimientoPasoSecuencia();
        model.seleccionar(secuencia);
        when(dao.actualizar(secuencia)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(secuencia);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.seleccionar(new ProcedimientoPasoSecuencia());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaProcedimientosPaso() {
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        when(ppDao.obtenerTodos()).thenReturn(List.of(new ProcedimientoPaso()));

        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class), ppDao);

        assertEquals(1, model.getProcedimientosPaso().size());
    }
}
