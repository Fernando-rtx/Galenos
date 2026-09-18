package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

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

class ProcedimientoPasoExamenModelTest {

    @Test
    void nuevoCreaPPEYCambiaEstadoACreacion() {
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                mock(ProcedimientoPasoExamenDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ExamenDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado().getFechaCreacion());
        assertEquals(true, model.getSeleccionado().getActivo());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                mock(ProcedimientoPasoExamenDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ExamenDAO.class));
        ProcedimientoPasoExamen ppe = new ProcedimientoPasoExamen();

        model.seleccionar(ppe);

        assertSame(ppe, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, mock(ProcedimientoPasoDAO.class), mock(ExamenDAO.class));
        model.nuevo();
        ProcedimientoPasoExamen seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, mock(ProcedimientoPasoDAO.class), mock(ExamenDAO.class));
        ProcedimientoPasoExamen ppe = new ProcedimientoPasoExamen();
        ProcedimientoPasoExamen actualizado = new ProcedimientoPasoExamen();
        model.seleccionar(ppe);
        when(dao.actualizar(ppe)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(ppe);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                mock(ProcedimientoPasoExamenDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ExamenDAO.class));
        model.seleccionar(new ProcedimientoPasoExamen());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, mock(ProcedimientoPasoDAO.class), mock(ExamenDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaExamenesYProcedimientosPaso() {
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenDao = mock(ExamenDAO.class);
        when(ppDao.obtenerTodos()).thenReturn(List.of(new ProcedimientoPaso()));
        when(examenDao.obtenerTodos()).thenReturn(List.of(new Examen()));

        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                mock(ProcedimientoPasoExamenDAO.class), ppDao, examenDao);

        assertEquals(1, model.getProcedimientosPaso().size());
        assertEquals(1, model.getExamenes().size());
    }
}
