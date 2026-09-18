package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

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

class ProcedimientoPasoModelTest {

    @Test
    void nuevoCreaPasoYCambiaEstadoACreacion() {
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                mock(ProcedimientoPasoDAO.class),
                mock(ProcedimientoDAO.class),
                mock(RolDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                mock(ProcedimientoPasoDAO.class),
                mock(ProcedimientoDAO.class),
                mock(RolDAO.class));
        ProcedimientoPaso paso = new ProcedimientoPaso();

        model.seleccionar(paso);

        assertSame(paso, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        model.nuevo();
        ProcedimientoPaso seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        ProcedimientoPaso paso = new ProcedimientoPaso();
        ProcedimientoPaso actualizado = new ProcedimientoPaso();
        model.seleccionar(paso);
        when(dao.actualizar(paso)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(paso);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                mock(ProcedimientoPasoDAO.class),
                mock(ProcedimientoDAO.class),
                mock(RolDAO.class));
        model.seleccionar(new ProcedimientoPaso());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaProcedimientosYRoles() {
        ProcedimientoDAO procDao = mock(ProcedimientoDAO.class);
        RolDAO rolDao = mock(RolDAO.class);
        when(procDao.obtenerTodos()).thenReturn(List.of(new Procedimiento()));
        when(rolDao.obtenerTodos()).thenReturn(List.of(new Rol()));

        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                mock(ProcedimientoPasoDAO.class), procDao, rolDao);

        assertEquals(1, model.getProcedimientos().size());
        assertEquals(1, model.getRoles().size());
    }
}
