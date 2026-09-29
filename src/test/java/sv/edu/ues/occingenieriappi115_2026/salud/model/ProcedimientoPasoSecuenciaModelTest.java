package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProcedimientoPasoSecuenciaModelTest {

    @Test
    void nuevoCreaSecuenciaYCambiaEstadoACreacion() {
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class),
                mock(ProcedimientoPasoDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class),
                mock(ProcedimientoPasoDAO.class));
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();

        model.seleccionar(secuencia);

        assertSame(secuencia, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));
        model.getSeleccionado().setTipoSecuencia("ANTES");
        ProcedimientoPasoSecuencia seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        secuencia.setIdProcedimientoPaso(new ProcedimientoPaso(UUID.randomUUID()));
        secuencia.setTipoSecuencia("ANTES");
        ProcedimientoPasoSecuencia actualizado = new ProcedimientoPasoSecuencia();
        model.seleccionar(secuencia);
        when(dao.actualizar(secuencia)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(secuencia);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.seleccionar(new ProcedimientoPasoSecuencia());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaProcedimientosPaso() {
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        when(ppDao.obtenerTodos()).thenReturn(List.of(new ProcedimientoPaso()));

        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class), ppDao);

        assertEquals(1, model.getProcedimientosPaso().size());
    }

    @Test
    void guardarSinPasoOrigenNoDelegaNiCambiaEstado() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarConDestinoInexistenteNoDelega() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));
        model.getSeleccionado().setIdProcedimientoPasoReferencia(UUID.randomUUID());
        model.getSeleccionado().setTipoSecuencia("ANTES");

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarConDestinoValidoDelega() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, ppDao);
        model.nuevo();
        UUID idDestino = UUID.randomUUID();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));
        model.getSeleccionado().setIdProcedimientoPasoReferencia(idDestino);
        model.getSeleccionado().setTipoSecuencia("ANTES");
        when(ppDao.buscarPorId(idDestino)).thenReturn(new ProcedimientoPaso(idDestino));

        model.guardar();

        verify(dao).guardar(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void rechazaPasosDeProcedimientosDistintos() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, ppDao);
        model.nuevo();
        ProcedimientoPaso origen = new ProcedimientoPaso(UUID.randomUUID());
        origen.setIdProcedimiento(new Procedimiento(UUID.randomUUID()));
        ProcedimientoPaso destino = new ProcedimientoPaso(UUID.randomUUID());
        destino.setIdProcedimiento(new Procedimiento(UUID.randomUUID()));
        model.getSeleccionado().setIdProcedimientoPaso(origen);
        model.getSeleccionado().setIdProcedimientoPasoReferencia(
                destino.getIdProcedimientoPaso());
        model.getSeleccionado().setTipoSecuencia("ANTES");
        when(ppDao.buscarPorId(destino.getIdProcedimientoPaso())).thenReturn(destino);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void rechazaEnlaceDuplicado() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, ppDao);
        model.nuevo();
        UUID idOrigen = UUID.randomUUID();
        UUID idDestino = UUID.randomUUID();
        ProcedimientoPaso origen = new ProcedimientoPaso(idOrigen);
        ProcedimientoPaso destino = new ProcedimientoPaso(idDestino);
        model.getSeleccionado().setIdProcedimientoPaso(origen);
        model.getSeleccionado().setIdProcedimientoPasoReferencia(idDestino);
        model.getSeleccionado().setTipoSecuencia("ANTES");
        when(ppDao.buscarPorId(idDestino)).thenReturn(destino);
        ProcedimientoPasoSecuencia existente = new ProcedimientoPasoSecuencia();
        existente.setIdProcedimientoPaso(origen);
        existente.setIdProcedimientoPasoReferencia(idDestino);
        when(dao.obtenerTodos()).thenReturn(List.of(existente));

        model.guardar();

        verify(dao, never()).guardar(any());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void permiteEditarElRegistroPropioConElMismoDestino() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, ppDao);
        UUID idPropio = UUID.randomUUID();
        UUID idDestino = UUID.randomUUID();
        ProcedimientoPaso origen = new ProcedimientoPaso(UUID.randomUUID());
        ProcedimientoPaso destino = new ProcedimientoPaso(idDestino);
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        secuencia.setIdProcedimientoPasoSecuencia(idPropio);
        secuencia.setIdProcedimientoPaso(origen);
        secuencia.setIdProcedimientoPasoReferencia(idDestino);
        secuencia.setTipoSecuencia("ANTES");
        ProcedimientoPasoSecuencia existente = new ProcedimientoPasoSecuencia();
        existente.setIdProcedimientoPasoSecuencia(idPropio);
        existente.setIdProcedimientoPaso(origen);
        existente.setIdProcedimientoPasoReferencia(idDestino);
        when(ppDao.buscarPorId(idDestino)).thenReturn(destino);
        when(dao.obtenerTodos()).thenReturn(List.of(existente));
        model.seleccionar(secuencia);
        when(dao.actualizar(secuencia)).thenReturn(secuencia);

        model.guardar();

        verify(dao).actualizar(secuencia);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void rechazaMismoOrdenParaElMismoOrigen() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, ppDao);
        model.nuevo();
        UUID idOrigen = UUID.randomUUID();
        UUID idDestino = UUID.randomUUID();
        ProcedimientoPaso origen = new ProcedimientoPaso(idOrigen);
        ProcedimientoPaso destino = new ProcedimientoPaso(idDestino);
        model.getSeleccionado().setIdProcedimientoPaso(origen);
        model.getSeleccionado().setIdProcedimientoPasoReferencia(idDestino);
        model.getSeleccionado().setTipoSecuencia("DESPUES");
        when(ppDao.buscarPorId(idDestino)).thenReturn(destino);
        ProcedimientoPasoSecuencia existente = new ProcedimientoPasoSecuencia();
        existente.setIdProcedimientoPaso(origen);
        existente.setIdProcedimientoPasoReferencia(UUID.randomUUID());
        existente.setTipoSecuencia("despues");
        when(dao.obtenerTodos()).thenReturn(List.of(existente));

        model.guardar();

        verify(dao, never()).guardar(any());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void detectaCicloEnLaSecuencia() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, ppDao);
        model.nuevo();
        UUID idOrigen = UUID.randomUUID();
        UUID idDestino = UUID.randomUUID();
        ProcedimientoPaso origen = new ProcedimientoPaso(idOrigen);
        ProcedimientoPaso destino = new ProcedimientoPaso(idDestino);
        model.getSeleccionado().setIdProcedimientoPaso(origen);
        model.getSeleccionado().setIdProcedimientoPasoReferencia(idDestino);
        model.getSeleccionado().setTipoSecuencia("DESPUES");
        when(ppDao.buscarPorId(idDestino)).thenReturn(destino);
        ProcedimientoPasoSecuencia inversa = new ProcedimientoPasoSecuencia();
        inversa.setIdProcedimientoPaso(destino);
        inversa.setIdProcedimientoPasoReferencia(idOrigen);
        inversa.setTipoSecuencia("ANTES");
        when(dao.obtenerTodos()).thenReturn(List.of(inversa));

        model.guardar();

        verify(dao, never()).guardar(any());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void rechazaReferenciaAlPasoOrigen() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, ppDao);
        model.nuevo();
        UUID idOrigen = UUID.randomUUID();
        ProcedimientoPaso origen = new ProcedimientoPaso(idOrigen);
        model.getSeleccionado().setIdProcedimientoPaso(origen);
        model.getSeleccionado().setIdProcedimientoPasoReferencia(idOrigen);
        model.getSeleccionado().setTipoSecuencia("ANTES");
        when(ppDao.buscarPorId(idOrigen)).thenReturn(origen);

        model.guardar();

        verify(dao, never()).guardar(any());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarEnListadoActualizaLaSecuencia() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));
        model.getSeleccionado().setTipoSecuencia("ANTES");
        ProcedimientoPasoSecuencia seleccionado = model.getSeleccionado();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verify(dao).actualizar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConContextoPublicaErrorDeOrigen() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        FacesContext contexto = contexto("secuencia.pasoOrigenRequerido");
        model.facesContext = contexto;
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        verificarError(contexto, "secuencia.pasoOrigenRequerido");
    }

    @Test
    void guardarSinTipoSecuenciaNoDelega() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        FacesContext contexto = contexto("secuencia.tipoRequerido");
        model.facesContext = contexto;
        model.nuevo();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));

        model.guardar();

        verifyNoInteractions(dao);
        verificarError(contexto, "secuencia.tipoRequerido");
    }

    @Test
    void guardarConTipoDeMasDe20CaracteresNoDelega() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        FacesContext contexto = contexto("secuencia.tipoMaximo");
        model.facesContext = contexto;
        model.nuevo();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));
        model.getSeleccionado().setTipoSecuencia("x".repeat(21));

        model.guardar();

        verifyNoInteractions(dao);
        verificarError(contexto, "secuencia.tipoMaximo");
    }

    @Test
    void quitarLaSecuenciaSeleccionadaNoTocaLosPasos() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(dao, ppDao);
        UUID id = UUID.randomUUID();
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        secuencia.setIdProcedimientoPasoSecuencia(id);
        model.seleccionar(secuencia);
        when(dao.buscarPorId(id)).thenReturn(secuencia);
        when(dao.eliminar(id)).thenReturn(true);

        model.quitar(secuencia);

        verify(dao).eliminar(id);
        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
        verify(ppDao, never()).eliminar(any());
    }

    @Test
    void quitarConRegistroInexistenteNoElimina() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        secuencia.setIdProcedimientoPasoSecuencia(UUID.randomUUID());
        when(dao.buscarPorId(secuencia.getIdProcedimientoPasoSecuencia())).thenReturn(null);

        model.quitar(secuencia);

        verify(dao, never()).eliminar(any());
    }

    @Test
    void quitarConErrorDeDaoNoPropaga() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        secuencia.setIdProcedimientoPasoSecuencia(UUID.randomUUID());
        when(dao.buscarPorId(secuencia.getIdProcedimientoPasoSecuencia()))
                .thenThrow(new IllegalStateException("fallo"));

        assertDoesNotThrow(() -> model.quitar(secuencia));

        verify(dao, never()).eliminar(any());
    }

    @Test
    void guardarConErrorDeDaoPublicaErrorGuardarYConservaEstado() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        FacesContext contexto = contexto("secuencia.errorGuardar");
        model.facesContext = contexto;
        model.nuevo();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));
        model.getSeleccionado().setTipoSecuencia("ANTES");
        ProcedimientoPasoSecuencia seleccionado = model.getSeleccionado();
        doThrow(new IllegalStateException("fallo")).when(dao).guardar(seleccionado);

        model.guardar();

        assertSame(seleccionado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        verificarError(contexto, "secuencia.errorGuardar");
    }

    @Test
    void guardarEnCreacionPublicaMensajeDeExito() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                dao, mock(ProcedimientoPasoDAO.class));
        FacesContext contexto = contexto("secuencia.creadaCorrectamente");
        model.facesContext = contexto;
        model.nuevo();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));
        model.getSeleccionado().setTipoSecuencia("ANTES");

        model.guardar();

        verify(contexto, never()).validationFailed();
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                FacesMessage.SEVERITY_INFO.equals(mensaje.getSeverity())
                && ("Mensaje secuencia.creadaCorrectamente").equals(mensaje.getSummary())));
    }

    @Test
    void cancelarPublicaMensajeDeCancelacion() {
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(
                mock(ProcedimientoPasoSecuenciaDAO.class),
                mock(ProcedimientoPasoDAO.class));
        FacesContext contexto = contexto("secuencia.cancelado");
        model.facesContext = contexto;
        model.nuevo();

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
        verify(contexto, never()).validationFailed();
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                FacesMessage.SEVERITY_INFO.equals(mensaje.getSeverity())
                && ("Mensaje secuencia.cancelado").equals(mensaje.getSummary())));
    }

    private FacesContext contexto(String... claves) {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                Object[][] contenidos = new Object[claves.length][2];
                for (int i = 0; i < claves.length; i++) {
                    contenidos[i] = new Object[]{claves[i], "Mensaje " + claves[i]};
                }
                return contenidos;
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(bundle);
        return contexto;
    }

    private void verificarError(FacesContext contexto, String clave) {
        verify(contexto).validationFailed();
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                FacesMessage.SEVERITY_ERROR.equals(mensaje.getSeverity())
                && ("Mensaje " + clave).equals(mensaje.getSummary())));
    }
}
