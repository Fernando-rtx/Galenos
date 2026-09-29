package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.primefaces.event.SelectEvent;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExamenTipoExamenModelTest {

    @Test
    void dobleClicAbreCopiaYCancelarConservaFilaOriginal() {
        ExamenTipoExamenModel model = model(mock(ExamenTipoExamenDAO.class));
        Examen examen = examenPersistido();
        ExamenTipoExamen fila = asociacionPersistida(examen);
        fila.setObservaciones("Original");
        model.cargarPorExamen(examen);

        model.seleccionarAsociacion(evento(fila));

        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
        assertEquals(fila.getIdTipoExamen().getIdTipoExamen().toString(), model.getIdTipoExamenSeleccionado());
        model.getSeleccionado().setObservaciones("Borrador");
        model.cancelarAsociacion();
        assertEquals("Original", fila.getObservaciones());
        assertNull(model.getSeleccionado());
        assertNull(model.getFilaSeleccionada());
    }

    @Test
    void editarMismaAsociacionActualizaSinContarseComoDuplicada() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        TipoExamenDAO tipos = mock(TipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(dao, mock(ExamenDAO.class), tipos);
        Examen examen = examenPersistido();
        ExamenTipoExamen fila = asociacionPersistida(examen);
        TipoExamen tipo = fila.getIdTipoExamen();
        when(tipos.buscarPorId(tipo.getIdTipoExamen())).thenReturn(tipo);
        when(dao.buscarPorId(fila.getIdExamenTipoExamen())).thenReturn(fila);
        when(dao.countByIdExamenAndIdTipoExamen(examen.getIdExamen(), tipo.getIdTipoExamen())).thenReturn(1L);
        model.cargarPorExamen(examen);
        model.seleccionarAsociacion(evento(fila));
        model.getSeleccionado().setObservaciones(" Cambio ");

        model.guardarAsociacion();

        verify(dao).actualizar(argThat(a -> a.getIdExamenTipoExamen().equals(fila.getIdExamenTipoExamen())
                && "Cambio".equals(a.getObservaciones())));
        verify(dao, never()).guardar(any());
        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void editarTipoRechazaDuplicadoYPermiteTipoSinAsociacion() {
        for (long existentes : List.of(0L, 1L)) {
            ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
            TipoExamenDAO tipos = mock(TipoExamenDAO.class);
            ExamenTipoExamenModel model = new ExamenTipoExamenModel(dao, mock(ExamenDAO.class), tipos);
            Examen examen = examenPersistido();
            ExamenTipoExamen fila = asociacionPersistida(examen);
            TipoExamen nuevoTipo = new TipoExamen(UUID.randomUUID());
            when(tipos.buscarPorId(nuevoTipo.getIdTipoExamen())).thenReturn(nuevoTipo);
            when(dao.buscarPorId(fila.getIdExamenTipoExamen())).thenReturn(fila);
            when(dao.countByIdExamenAndIdTipoExamen(examen.getIdExamen(), nuevoTipo.getIdTipoExamen()))
                    .thenReturn(existentes);
            model.cargarPorExamen(examen);
            model.seleccionarAsociacion(evento(fila));
            model.setIdTipoExamenSeleccionado(nuevoTipo.getIdTipoExamen().toString());
            model.facesContext = contexto();

            model.guardarAsociacion();

            if (existentes == 0) {
                verify(dao).actualizar(argThat(a -> a.getIdTipoExamen() == nuevoTipo));
                verify(model.facesContext, never()).validationFailed();
            } else {
                verify(dao, never()).actualizar(any());
                verify(model.facesContext).validationFailed();
                assertNotNull(model.getSeleccionado());
            }
            verify(dao, never()).guardar(any());
        }
    }

    @Test
    void quitarAsociacionEditadaCierraFormularioYLimpiaSeleccion() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = model(dao);
        Examen examen = examenPersistido();
        ExamenTipoExamen fila = asociacionPersistida(examen);
        when(dao.buscarPorId(fila.getIdExamenTipoExamen())).thenReturn(fila);
        when(dao.eliminar(fila.getIdExamenTipoExamen())).thenReturn(true);
        model.cargarPorExamen(examen);
        model.seleccionarAsociacion(evento(fila));

        model.quitarAsociacion(model.getSeleccionado(), examen);

        verify(dao).eliminar(fila.getIdExamenTipoExamen());
        assertNull(model.getSeleccionado());
        assertNull(model.getFilaSeleccionada());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    private ExamenTipoExamen asociacionPersistida(Examen examen) {
        ExamenTipoExamen fila = new ExamenTipoExamen(UUID.randomUUID());
        fila.setIdExamen(examen);
        fila.setIdTipoExamen(new TipoExamen(UUID.randomUUID()));
        return fila;
    }

    @SuppressWarnings("unchecked")
    private SelectEvent<ExamenTipoExamen> evento(ExamenTipoExamen fila) {
        SelectEvent<ExamenTipoExamen> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(fila);
        return evento;
    }

    @Test
    void quitarAsociacionEliminaSoloVinculoYConservaBorrador() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenDAO examenes = mock(ExamenDAO.class);
        TipoExamenDAO tipos = mock(TipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(dao, examenes, tipos);
        Examen examen = examenPersistido();
        ExamenTipoExamen asociacion = new ExamenTipoExamen(UUID.randomUUID());
        asociacion.setIdExamen(examen);
        when(dao.buscarPorId(asociacion.getIdExamenTipoExamen())).thenReturn(asociacion);
        when(dao.eliminar(asociacion.getIdExamenTipoExamen())).thenReturn(true);
        List<ExamenTipoExamen> restantes = List.of(new ExamenTipoExamen(UUID.randomUUID()));
        when(dao.findByIdExamen(examen.getIdExamen(), 0, Integer.MAX_VALUE)).thenReturn(restantes);
        model.nuevaAsociacion(examen);
        ExamenTipoExamen borrador = model.getSeleccionado();
        borrador.setObservaciones("Conservar estos datos");
        model.facesContext = contexto();

        model.quitarAsociacion(asociacion, examen);

        verify(dao).eliminar(asociacion.getIdExamenTipoExamen());
        verify(examenes, never()).eliminar(any());
        verify(tipos, never()).eliminar(any());
        assertSame(restantes, model.getAsociaciones());
        assertSame(borrador, model.getSeleccionado());
        assertEquals("Conservar estos datos", borrador.getObservaciones());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        verify(model.facesContext).addMessage(isNull(), argThat(m ->
                "Asociacion quitada".equals(m.getSummary())));
        verify(model.facesContext, never()).validationFailed();
    }

    @Test
    void quitarAsociacionRechazaVinculoPersistidoDeOtroExamen() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = model(dao);
        Examen examen = examenPersistido();
        model.cargarPorExamen(examen);
        ExamenTipoExamen enviada = new ExamenTipoExamen(UUID.randomUUID());
        enviada.setIdExamen(examen);
        ExamenTipoExamen persistida = new ExamenTipoExamen(enviada.getIdExamenTipoExamen());
        persistida.setIdExamen(examenPersistido());
        when(dao.buscarPorId(enviada.getIdExamenTipoExamen())).thenReturn(persistida);
        model.facesContext = contexto();

        model.quitarAsociacion(enviada, examen);

        verify(dao, never()).eliminar(any());
        verify(model.facesContext).validationFailed();
    }

    @Test
    void quitarAsociacionRechazaCambioDeExamenAbierto() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = model(dao);
        model.cargarPorExamen(examenPersistido());
        model.facesContext = contexto();

        model.quitarAsociacion(new ExamenTipoExamen(UUID.randomUUID()), examenPersistido());

        verify(dao, never()).buscarPorId(any());
        verify(dao, never()).eliminar(any());
        verify(model.facesContext).validationFailed();
    }

    @Test
    void quitarAsociacionInexistenteNoElimina() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = model(dao);
        Examen examen = examenPersistido();
        model.cargarPorExamen(examen);
        model.facesContext = contexto();

        model.quitarAsociacion(new ExamenTipoExamen(UUID.randomUUID()), examen);

        verify(dao, never()).eliminar(any());
        verify(model.facesContext).validationFailed();
    }

    @Test
    void quitarAsociacionManejaFalloDelDaoSinMostrarExito() {
        for (boolean lanzarExcepcion : List.of(false, true)) {
            ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
            ExamenTipoExamenModel model = model(dao);
            Examen examen = examenPersistido();
            ExamenTipoExamen asociacion = new ExamenTipoExamen(UUID.randomUUID());
            asociacion.setIdExamen(examen);
            List<ExamenTipoExamen> originales = List.of(asociacion);
            when(dao.findByIdExamen(examen.getIdExamen(), 0, Integer.MAX_VALUE)).thenReturn(originales);
            when(dao.buscarPorId(asociacion.getIdExamenTipoExamen())).thenReturn(asociacion);
            if (lanzarExcepcion) {
                when(dao.eliminar(asociacion.getIdExamenTipoExamen())).thenThrow(new IllegalStateException());
            }
            model.cargarPorExamen(examen);
            model.facesContext = contexto();

            assertDoesNotThrow(() -> model.quitarAsociacion(asociacion, examen));

            assertSame(originales, model.getAsociaciones());
            verify(model.facesContext).validationFailed();
            verify(model.facesContext).addMessage(isNull(), argThat(m ->
                    "Error al quitar".equals(m.getSummary())));
        }
    }

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
        FacesContext contexto = contexto();
        model.facesContext = contexto;
        model.nuevaAsociacion(examen);
        model.getSeleccionado().setIdTipoExamen(tipo);
        when(dao.countByIdExamenAndIdTipoExamen(examen.getIdExamen(), tipo.getIdTipoExamen()))
                .thenReturn(1L);

        model.guardarAsociacion();

        verify(dao, never()).guardar(any());
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                "Asociacion duplicada".equals(mensaje.getSummary())));
        verify(contexto).validationFailed();
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarAsociacionSinTipoNoPersisteYMarcaValidacionFallida() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = model(dao);
        FacesContext contexto = contexto();
        model.facesContext = contexto;
        model.nuevaAsociacion(examenPersistido());

        model.guardarAsociacion();

        verify(dao, never()).guardar(any());
        verify(dao, never()).countByIdExamenAndIdTipoExamen(any(), any());
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                "Tipo requerido".equals(mensaje.getSummary())));
        verify(contexto).validationFailed();
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
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

    private FacesContext contexto() {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        ResourceBundle mensajes = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                    {"examen.errorQuitarAsociacion", "Error al quitar"},
                    {"examen.asociacionQuitada", "Asociacion quitada"},
                    {"examen.tipoDuplicado", "Asociacion duplicada"},
                    {"examen.seleccioneExamen", "Examen requerido"},
                    {"examen.seleccioneTipo", "Tipo requerido"}
                };
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(mensajes);
        return contexto;
    }
}
