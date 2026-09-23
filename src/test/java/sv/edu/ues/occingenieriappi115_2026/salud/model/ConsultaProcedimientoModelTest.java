package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConsultaProcedimientoModelTest {

    @Test
    void nuevoCreaConsultaProcedimientoYCambiaEstadoACreacion() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class),
                mock(ConsultaDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class),
                mock(ConsultaDAO.class));
        ConsultaProcedimiento entidad = new ConsultaProcedimiento();

        model.seleccionar(entidad);

        assertSame(entidad, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class));
        model.nuevo();
        ConsultaProcedimiento seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class));
        ConsultaProcedimiento entidad = new ConsultaProcedimiento();
        ConsultaProcedimiento actualizada = new ConsultaProcedimiento();
        model.seleccionar(entidad);
        when(dao.actualizar(entidad)).thenReturn(actualizada);

        model.guardar();

        verify(dao).actualizar(entidad);
        assertSame(actualizada, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class));
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class),
                mock(ConsultaDAO.class));
        model.seleccionar(new ConsultaProcedimiento());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaConsultas() {
        ConsultaDAO consultaDAO = mock(ConsultaDAO.class);
        when(consultaDAO.obtenerTodos()).thenReturn(List.of(new Consulta()));

        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), consultaDAO);

        assertEquals(1, model.getConsultas().size());
    }
}
