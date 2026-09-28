package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenResultadoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenResultado;
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

class ExamenResultadoModelTest {

    @Test
    void nuevoCreaExamenResultadoYCambiaEstadoACreacion() {
        ExamenResultadoModel model = new ExamenResultadoModel(
                mock(ExamenResultadoDAO.class),
                mock(OrdenExamenDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ExamenResultadoModel model = new ExamenResultadoModel(
                mock(ExamenResultadoDAO.class),
                mock(OrdenExamenDAO.class));
        ExamenResultado examenResultado = new ExamenResultado();

        model.seleccionar(examenResultado);

        assertSame(examenResultado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ExamenResultadoDAO dao = mock(ExamenResultadoDAO.class);
        ExamenResultadoModel model = new ExamenResultadoModel(
                dao, mock(OrdenExamenDAO.class));
        model.nuevo();
        ExamenResultado seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ExamenResultadoDAO dao = mock(ExamenResultadoDAO.class);
        ExamenResultadoModel model = new ExamenResultadoModel(
                dao, mock(OrdenExamenDAO.class));
        ExamenResultado examenResultado = new ExamenResultado();
        ExamenResultado actualizado = new ExamenResultado();
        model.seleccionar(examenResultado);
        when(dao.actualizar(examenResultado)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(examenResultado);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        ExamenResultadoDAO dao = mock(ExamenResultadoDAO.class);
        ExamenResultadoModel model = new ExamenResultadoModel(
                dao, mock(OrdenExamenDAO.class));
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ExamenResultadoModel model = new ExamenResultadoModel(
                mock(ExamenResultadoDAO.class),
                mock(OrdenExamenDAO.class));
        model.seleccionar(new ExamenResultado());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ExamenResultadoDAO dao = mock(ExamenResultadoDAO.class);
        ExamenResultadoModel model = new ExamenResultadoModel(
                dao, mock(OrdenExamenDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaOrdenExamenes() {
        OrdenExamenDAO ordenExamenDAO = mock(OrdenExamenDAO.class);
        when(ordenExamenDAO.obtenerTodos()).thenReturn(List.of(new OrdenExamen()));

        ExamenResultadoModel model = new ExamenResultadoModel(
                mock(ExamenResultadoDAO.class), ordenExamenDAO);

        assertEquals(1, model.getOrdenExamenes().size());
    }

    @Test
    void idOrdenContextoValidoFiltraLaListaDeOrdenes() {
        OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
        OrdenExamen orden = new OrdenExamen();
        orden.setIdOrdenExamen(UUID.randomUUID());
        when(dao.buscarPorId(orden.getIdOrdenExamen())).thenReturn(orden);
        ExamenResultadoModel model = new ExamenResultadoModel(mock(ExamenResultadoDAO.class), dao);

        model.setIdOrdenContexto(orden.getIdOrdenExamen().toString());

        assertEquals(orden.getIdOrdenExamen().toString(), model.getIdOrdenContexto());
        assertEquals(List.of(orden), model.getOrdenExamenes());
    }

    @Test
    void idOrdenContextoInvalidoConservaLaListaCompleta() {
        OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
        OrdenExamen orden = new OrdenExamen();
        orden.setIdOrdenExamen(UUID.randomUUID());
        when(dao.obtenerTodos()).thenReturn(List.of(orden));
        ExamenResultadoModel model = new ExamenResultadoModel(mock(ExamenResultadoDAO.class), dao);

        model.setIdOrdenContexto("no-es-uuid");

        assertEquals(List.of(orden), model.getOrdenExamenes());
    }

    @Test
    void nuevoConContextoPreseleccionaLaOrden() {
        OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
        OrdenExamen orden = new OrdenExamen();
        orden.setIdOrdenExamen(UUID.randomUUID());
        when(dao.buscarPorId(orden.getIdOrdenExamen())).thenReturn(orden);
        ExamenResultadoModel model = new ExamenResultadoModel(mock(ExamenResultadoDAO.class), dao);
        model.setIdOrdenContexto(orden.getIdOrdenExamen().toString());

        model.nuevo();

        assertSame(orden, model.getSeleccionado().getIdOrdenExamen());
    }
}
