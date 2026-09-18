package sv.edu.ues.occingenieriappi115_2026.salud.model;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RolModelTest {

    @Test
    void nuevoCreaRolYCambiaEstadoACreacion() {
        RolModel model = new RolModel(mock(RolDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        RolModel model = new RolModel(mock(RolDAO.class));
        Rol rol = new Rol();

        model.seleccionar(rol);

        assertSame(rol, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(dao);
        model.nuevo();
        Rol seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(dao);
        Rol rol = new Rol();
        Rol actualizado = new Rol();
        model.seleccionar(rol);
        when(dao.actualizar(rol)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(rol);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        RolModel model = new RolModel(mock(RolDAO.class));
        model.seleccionar(new Rol());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(dao);

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }
}
