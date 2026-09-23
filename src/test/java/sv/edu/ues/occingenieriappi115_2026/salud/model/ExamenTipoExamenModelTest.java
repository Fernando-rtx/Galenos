package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExamenTipoExamenModelTest {

    @Test
    void cargarPorExamenFiltraConElDaoEspecializado() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        Examen examen = examenPersistido();
        List<ExamenTipoExamen> esperadas = List.of(new ExamenTipoExamen());
        when(dao.findByIdExamen(examen.getIdExamen(), 0, Integer.MAX_VALUE))
                .thenReturn(esperadas);
        ExamenTipoExamenModel model = model(dao);

        model.cargarPorExamen(examen);

        assertSame(esperadas, model.getAsociaciones());
        verify(dao).findByIdExamen(examen.getIdExamen(), 0, Integer.MAX_VALUE);
    }

    @Test
    void cargarExamenNoPersistidoNoConsultaElDao() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = model(dao);

        model.cargarPorExamen(new Examen());

        assertTrue(model.getAsociaciones().isEmpty());
        verify(dao, never()).findByIdExamen(any(), anyInt(), anyInt());
    }

    @Test
    void nuevaAsociacionFijaExamenFechaYEstado() {
        ExamenTipoExamenModel model = model(mock(ExamenTipoExamenDAO.class));
        Examen examen = examenPersistido();

        model.nuevaAsociacion(examen);

        assertSame(examen, model.getSeleccionado().getIdExamen());
        assertNotNull(model.getSeleccionado().getFechaCreacion());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarAsociacionPersisteYRecargaSoloElExamenPadre() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = model(dao);
        Examen examen = examenPersistido();
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        model.nuevaAsociacion(examen);
        model.getSeleccionado().setIdTipoExamen(tipo);
        model.getSeleccionado().setObservaciones(" observacion ");
        when(dao.countByIdExamenAndIdTipoExamen(examen.getIdExamen(), tipo.getIdTipoExamen()))
                .thenReturn(0L);
        when(dao.findByIdExamen(examen.getIdExamen(), 0, Integer.MAX_VALUE))
                .thenReturn(List.of());
        ExamenTipoExamen asociacion = model.getSeleccionado();

        model.guardarAsociacion();

        assertEquals("observacion", asociacion.getObservaciones());
        verify(dao).guardar(asociacion);
        verify(dao).findByIdExamen(examen.getIdExamen(), 0, Integer.MAX_VALUE);
        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarAsociacionResuelveElTipoSeleccionadoPorUuid() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenDAO examenDAO = mock(ExamenDAO.class);
        TipoExamenDAO tipoExamenDAO = mock(TipoExamenDAO.class);
        Examen examen = examenPersistido();
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        when(examenDAO.obtenerTodos()).thenReturn(List.of());
        when(tipoExamenDAO.obtenerTodos()).thenReturn(List.of(tipo));
        when(tipoExamenDAO.buscarPorId(tipo.getIdTipoExamen())).thenReturn(tipo);
        when(dao.countByIdExamenAndIdTipoExamen(examen.getIdExamen(), tipo.getIdTipoExamen()))
                .thenReturn(0L);
        when(dao.findByIdExamen(examen.getIdExamen(), 0, Integer.MAX_VALUE))
                .thenReturn(List.of());
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(
                dao, examenDAO, tipoExamenDAO);
        model.nuevaAsociacion(examen);
        model.setIdTipoExamenSeleccionado(tipo.getIdTipoExamen().toString());

        model.guardarAsociacion();

        verify(tipoExamenDAO).buscarPorId(tipo.getIdTipoExamen());
        verify(dao).guardar(argThat(asociacion ->
                asociacion.getIdTipoExamen() == tipo
                        && asociacion.getIdExamen() == examen));
    }

    @Test
    void guardarAsociacionDuplicadaNoPersiste() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = model(dao);
        Examen examen = examenPersistido();
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());
        model.nuevaAsociacion(examen);
        model.getSeleccionado().setIdTipoExamen(tipo);
        when(dao.countByIdExamenAndIdTipoExamen(examen.getIdExamen(), tipo.getIdTipoExamen()))
                .thenReturn(1L);

        model.guardarAsociacion();

        verify(dao, never()).guardar(any());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void cancelarAsociacionLimpiaSeleccion() {
        ExamenTipoExamenModel model = model(mock(ExamenTipoExamenDAO.class));
        model.nuevaAsociacion(examenPersistido());

        model.cancelarAsociacion();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

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

    private ExamenTipoExamenModel model(ExamenTipoExamenDAO dao) {
        ExamenDAO examenDAO = mock(ExamenDAO.class);
        TipoExamenDAO tipoExamenDAO = mock(TipoExamenDAO.class);
        when(examenDAO.obtenerTodos()).thenReturn(List.of());
        when(tipoExamenDAO.obtenerTodos()).thenReturn(List.of());
        return new ExamenTipoExamenModel(dao, examenDAO, tipoExamenDAO);
    }

    private Examen examenPersistido() {
        return new Examen(UUID.randomUUID());
    }
}
