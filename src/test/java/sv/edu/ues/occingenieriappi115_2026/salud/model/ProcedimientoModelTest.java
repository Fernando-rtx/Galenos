package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProcedimientoModelTest {

    @Test
    void nuevoCreaProcedimientoYCambiaEstadoACreacion() {
        ProcedimientoModel model = new ProcedimientoModel(
                mock(ProcedimientoDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertTrue(model.getSeleccionado().getActivo());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ProcedimientoModel model = new ProcedimientoModel(
                mock(ProcedimientoDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        Procedimiento procedimiento = new Procedimiento();

        model.seleccionar(procedimiento);

        assertSame(procedimiento, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("Biopsia");
        Procedimiento seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        Procedimiento procedimiento = new Procedimiento();
        procedimiento.setNombre("Biopsia");
        Procedimiento actualizado = new Procedimiento();
        model.seleccionar(procedimiento);
        when(dao.actualizar(procedimiento)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(procedimiento);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ProcedimientoModel model = new ProcedimientoModel(
                mock(ProcedimientoDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        model.seleccionar(new Procedimiento());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void guardarRecortaEspaciosDelNombre() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("  Biopsia  ");

        model.guardar();

        assertEquals("Biopsia", model.getSeleccionado().getNombre());
        verify(dao).guardar(model.getSeleccionado());
    }

    @Test
    void guardarSinNombreNoDelegaNiCambiaEstado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarConNombreDeUnSoloCaracterNoDelega() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("A");

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarConNombreDeMasDe255CaracteresNoDelega() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("x".repeat(256));

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarEnListadoActualizaElProcedimiento() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("Biopsia");
        Procedimiento seleccionado = model.getSeleccionado();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verify(dao).actualizar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConContextoPublicaErrorDeNombre() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        FacesContext contexto = contexto("procedimiento.nombreRequerido");
        model.facesContext = contexto;
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        verificarError(contexto, "procedimiento.nombreRequerido");
    }

    @Test
    void eliminarSinSeleccionadoNoDelega() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));

        assertDoesNotThrow(model::eliminar);

        verifyNoInteractions(dao);
    }

    @Test
    void eliminarConSeleccionadoSinIdNoDelega() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        model.seleccionar(new Procedimiento());

        assertDoesNotThrow(model::eliminar);

        verifyNoInteractions(dao);
    }

    @Test
    void eliminarConPasoReferenciadoBloqueaEliminacion() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao, pasoDao, mock(ConsultaProcedimientoDAO.class));
        UUID id = UUID.randomUUID();
        Procedimiento seleccionado = new Procedimiento(id);
        model.seleccionar(seleccionado);
        ProcedimientoPaso paso = new ProcedimientoPaso();
        paso.setIdProcedimiento(new Procedimiento(id));
        when(pasoDao.obtenerTodos()).thenReturn(List.of(paso));

        model.eliminar();

        verify(dao, never()).eliminar(any());
        assertSame(seleccionado, model.getSeleccionado());
    }

    @Test
    void eliminarConConsultaReferenciadaBloqueaEliminacion() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        ConsultaProcedimientoDAO consultaDao = mock(ConsultaProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(dao, pasoDao, consultaDao);
        UUID id = UUID.randomUUID();
        Procedimiento seleccionado = new Procedimiento(id);
        model.seleccionar(seleccionado);
        ConsultaProcedimiento consulta = new ConsultaProcedimiento();
        consulta.setIdProcedimiento(id);
        when(pasoDao.obtenerTodos()).thenReturn(List.of());
        when(consultaDao.obtenerTodos()).thenReturn(List.of(consulta));

        model.eliminar();

        verify(dao, never()).eliminar(any());
        assertSame(seleccionado, model.getSeleccionado());
    }

    @Test
    void eliminarSinReferenciasEliminaYVuelveAListado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        ConsultaProcedimientoDAO consultaDao = mock(ConsultaProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(dao, pasoDao, consultaDao);
        UUID id = UUID.randomUUID();
        model.seleccionar(new Procedimiento(id));
        when(pasoDao.obtenerTodos()).thenReturn(List.of());
        when(consultaDao.obtenerTodos()).thenReturn(List.of());
        when(dao.eliminar(id)).thenReturn(true);

        model.eliminar();

        verify(dao).eliminar(id);
        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void eliminarCuandoDaoRetornaFalsoConservaSeleccionado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        ConsultaProcedimientoDAO consultaDao = mock(ConsultaProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(dao, pasoDao, consultaDao);
        UUID id = UUID.randomUUID();
        Procedimiento seleccionado = new Procedimiento(id);
        model.seleccionar(seleccionado);
        when(pasoDao.obtenerTodos()).thenReturn(List.of());
        when(consultaDao.obtenerTodos()).thenReturn(List.of());
        when(dao.eliminar(id)).thenReturn(false);

        model.eliminar();

        assertSame(seleccionado, model.getSeleccionado());
    }

    @Test
    void eliminarConErrorDeDaoNoPropaga() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        ConsultaProcedimientoDAO consultaDao = mock(ConsultaProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(dao, pasoDao, consultaDao);
        UUID id = UUID.randomUUID();
        Procedimiento seleccionado = new Procedimiento(id);
        model.seleccionar(seleccionado);
        when(pasoDao.obtenerTodos()).thenReturn(List.of());
        when(consultaDao.obtenerTodos()).thenReturn(List.of());
        doThrow(new IllegalStateException("fallo")).when(dao).eliminar(id);

        assertDoesNotThrow(model::eliminar);

        assertSame(seleccionado, model.getSeleccionado());
    }

    @Test
    void eliminarConContextoPublicaErrorDeBloqueo() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao, pasoDao, mock(ConsultaProcedimientoDAO.class));
        FacesContext contexto = contexto("procedimiento.eliminacionBloqueada");
        model.facesContext = contexto;
        UUID id = UUID.randomUUID();
        model.seleccionar(new Procedimiento(id));
        ProcedimientoPaso paso = new ProcedimientoPaso();
        paso.setIdProcedimiento(new Procedimiento(id));
        when(pasoDao.obtenerTodos()).thenReturn(List.of(paso));

        model.eliminar();

        verify(dao, never()).eliminar(any());
        verificarError(contexto, "procedimiento.eliminacionBloqueada");
    }

    @Test
    void guardarConNombreDuplicadoNoDelega() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        Procedimiento existente = new Procedimiento(UUID.randomUUID());
        existente.setNombre("  biopsia  ");
        when(dao.obtenerTodos()).thenReturn(List.of(existente));
        model.nuevo();
        model.getSeleccionado().setNombre("Biopsia");

        model.guardar();

        verify(dao, never()).guardar(any());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarAlEditarElMismoRegistroNoSeConsideraDuplicado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        Procedimiento existente = new Procedimiento(UUID.randomUUID());
        existente.setNombre("Biopsia");
        when(dao.obtenerTodos()).thenReturn(List.of(existente));
        when(dao.actualizar(existente)).thenReturn(existente);
        model.seleccionar(existente);

        model.guardar();

        verify(dao).actualizar(existente);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConErrorDeDaoPublicaErrorGuardarYConservaEstado() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        FacesContext contexto = contexto("procedimiento.errorGuardar");
        model.facesContext = contexto;
        model.nuevo();
        model.getSeleccionado().setNombre("Biopsia");
        Procedimiento seleccionado = model.getSeleccionado();
        doThrow(new IllegalStateException("fallo")).when(dao).guardar(seleccionado);

        model.guardar();

        assertSame(seleccionado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        verificarError(contexto, "procedimiento.errorGuardar");
    }

    @Test
    void guardarEnCreacionPublicaMensajeDeExito() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        ProcedimientoModel model = new ProcedimientoModel(
                dao,
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        FacesContext contexto = contexto("procedimiento.creadoCorrectamente");
        model.facesContext = contexto;
        model.nuevo();
        model.getSeleccionado().setNombre("Biopsia");

        model.guardar();

        verify(contexto, never()).validationFailed();
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                FacesMessage.SEVERITY_INFO.equals(mensaje.getSeverity())
                && ("Mensaje procedimiento.creadoCorrectamente").equals(mensaje.getSummary())));
    }

    @Test
    void cancelarPublicaMensajeDeCancelacion() {
        ProcedimientoModel model = new ProcedimientoModel(
                mock(ProcedimientoDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class));
        FacesContext contexto = contexto("procedimiento.cancelado");
        model.facesContext = contexto;
        model.nuevo();

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
        verify(contexto, never()).validationFailed();
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                FacesMessage.SEVERITY_INFO.equals(mensaje.getSeverity())
                && ("Mensaje procedimiento.cancelado").equals(mensaje.getSummary())));
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
