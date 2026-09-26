package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
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

class ConsultaModelTest {

    @Test
    void nuevoCreaConsultaYCambiaEstadoACreacion() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();

        model.seleccionar(consulta);

        assertSame(consulta, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(
                dao, mock(PersonaRolDAO.class));
        model.nuevo();
        Consulta seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(
                dao, mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        Consulta actualizada = new Consulta();
        model.seleccionar(consulta);
        when(dao.actualizar(consulta)).thenReturn(actualizada);

        model.guardar();

        verify(dao).actualizar(consulta);
        assertSame(actualizada, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(
                dao, mock(PersonaRolDAO.class));
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        model.seleccionar(new Consulta());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(
                dao, mock(PersonaRolDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaPersonasRoles() {
        PersonaRolDAO personaRolDAO = mock(PersonaRolDAO.class);
        when(personaRolDAO.obtenerTodos()).thenReturn(List.of(new PersonaRol()));

        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class), personaRolDAO);

        assertEquals(1, model.getPersonasRoles().size());
    }

    @Test
    void editarSeleccionadoCambiaEstadoAEdicionConLaSeleccionEnlazada() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        model.setSeleccionado(consulta);

        model.editarSeleccionado();

        assertSame(consulta, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void editarSeleccionadoSinSeleccionNoRompeNiCambiaElEstado() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));

        assertDoesNotThrow(model::editarSeleccionado);

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }
}
