package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenTipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExamenTipoExamenModelTest {

    @Test
    void nuevoCreaEntidadYCambiaEstadoACreacion() {
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                mock(ExamenTipoExamenDAO.class),
                mock(ExamenDAO.class),
                mock(TipoExamenDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                mock(ExamenTipoExamenDAO.class),
                mock(ExamenDAO.class),
                mock(TipoExamenDAO.class));
        ExamenTipoExamen entidad = new ExamenTipoExamen();

        model.seleccionar(entidad);

        assertSame(entidad, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                dao, mock(ExamenDAO.class), mock(TipoExamenDAO.class));
        model.nuevo();
        ExamenTipoExamen seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                dao, mock(ExamenDAO.class), mock(TipoExamenDAO.class));
        ExamenTipoExamen entidad = new ExamenTipoExamen();
        ExamenTipoExamen actualizado = new ExamenTipoExamen();
        model.seleccionar(entidad);
        when(dao.actualizar(entidad)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(entidad);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                dao, mock(ExamenDAO.class), mock(TipoExamenDAO.class));
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                mock(ExamenTipoExamenDAO.class),
                mock(ExamenDAO.class),
                mock(TipoExamenDAO.class));
        model.seleccionar(new ExamenTipoExamen());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                dao, mock(ExamenDAO.class), mock(TipoExamenDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaExamenesYTipos() {
        ExamenDAO examenDao = mock(ExamenDAO.class);
        TipoExamenDAO tipoExamenDao = mock(TipoExamenDAO.class);
        when(examenDao.obtenerTodos()).thenReturn(List.of(new Examen()));
        when(tipoExamenDao.obtenerTodos()).thenReturn(List.of(new TipoExamen()));

        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                mock(ExamenTipoExamenDAO.class), examenDao, tipoExamenDao);

        assertEquals(1, model.getExamenes().size());
        assertEquals(1, model.getTipoExamenes().size());
    }
}
