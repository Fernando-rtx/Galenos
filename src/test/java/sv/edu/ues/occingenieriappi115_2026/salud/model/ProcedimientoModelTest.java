package sv.edu.ues.occingenieriappi115_2026.salud.model;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProcedimientoModelTest {

    @Test
    void nuevoCreaProcedimientoYCambiaEstadoACreacion() {
        ProcedimientoModel model = new ProcedimientoModel(mock(ProcedimientoDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ProcedimientoModel model = new ProcedimientoModel(mock(ProcedimientoDAO.class));
        Procedimiento procedimiento = new Procedimiento();

        model.seleccionar(procedimiento);

        assertSame(procedimiento, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(dao);
        model.nuevo();
        Procedimiento seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(dao);
        Procedimiento procedimiento = new Procedimiento();
        Procedimiento actualizado = new Procedimiento();
        model.seleccionar(procedimiento);
        when(dao.actualizar(procedimiento)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(procedimiento);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ProcedimientoModel model = new ProcedimientoModel(mock(ProcedimientoDAO.class));
        model.seleccionar(new Procedimiento());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(dao);

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }
}
