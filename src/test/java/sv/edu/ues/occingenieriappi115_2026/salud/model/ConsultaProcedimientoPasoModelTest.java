package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConsultaProcedimientoPasoModelTest {

    @Test
    void nuevoCreaPasoYCambiaEstadoACreacion() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();

        model.seleccionar(paso);

        assertSame(paso, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));
        model.nuevo();
        ConsultaProcedimientoPaso seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        ConsultaProcedimientoPaso actualizado = new ConsultaProcedimientoPaso();
        model.seleccionar(paso);
        when(dao.actualizar(paso)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(paso);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        model.seleccionar(new ConsultaProcedimientoPaso());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaProcedimientosYPersonasRoles() {
        ConsultaProcedimientoDAO procDao = mock(ConsultaProcedimientoDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        when(procDao.obtenerTodos()).thenReturn(List.of(new ConsultaProcedimiento()));
        when(personaRolDao.obtenerTodos()).thenReturn(List.of(new PersonaRol()));

        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class), procDao, personaRolDao);

        assertEquals(1, model.getConsultaProcedimientos().size());
        assertEquals(1, model.getPersonasRoles().size());
    }
}
