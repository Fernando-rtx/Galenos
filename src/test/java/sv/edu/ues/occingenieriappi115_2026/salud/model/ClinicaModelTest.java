package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

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

class ClinicaModelTest {

    @Test
    void nuevoCreaClinicaYCambiaEstadoACreacion() {
        ClinicaModel model = new ClinicaModel(
                mock(ClinicaDAO.class), mock(PersonaRolDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertTrue(model.getSeleccionado().getActivo());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ClinicaModel model = new ClinicaModel(
                mock(ClinicaDAO.class), mock(PersonaRolDAO.class));
        Clinica clinica = new Clinica();

        model.seleccionar(clinica);

        assertSame(clinica, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("Clínica Central");
        Clinica seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        Clinica clinica = new Clinica();
        clinica.setNombre("Clínica Central");
        Clinica actualizado = new Clinica();
        model.seleccionar(clinica);
        when(dao.actualizar(clinica)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(clinica);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ClinicaModel model = new ClinicaModel(
                mock(ClinicaDAO.class), mock(PersonaRolDAO.class));
        model.seleccionar(new Clinica());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void guardarRecortaEspaciosDelNombre() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("  Clínica Norte  ");

        model.guardar();

        assertEquals("Clínica Norte", model.getSeleccionado().getNombre());
        verify(dao).guardar(model.getSeleccionado());
    }

    @Test
    void guardarSinNombreNoDelegaNiCambiaEstado() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarConNombreDeUnSoloCaracterNoDelega() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("A");

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarConNombreDeMasDe255CaracteresNoDelega() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("x".repeat(256));

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarEnListadoActualizaLaClinica() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("Clínica Central");
        Clinica seleccionada = model.getSeleccionado();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verify(dao).actualizar(seleccionada);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConContextoPublicaErrorDeNombre() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        FacesContext contexto = contexto("clinica.nombreRequerido");
        model.facesContext = contexto;
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        verificarError(contexto, "clinica.nombreRequerido");
    }

    @Test
    void eliminarSinSeleccionadoNoDelega() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));

        assertDoesNotThrow(model::eliminar);

        verifyNoInteractions(dao);
    }

    @Test
    void eliminarConSeleccionadoSinIdNoDelega() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        model.seleccionar(new Clinica());

        assertDoesNotThrow(model::eliminar);

        verifyNoInteractions(dao);
    }

    @Test
    void eliminarConPersonasAsignadasBloqueaEliminacion() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ClinicaModel model = new ClinicaModel(dao, personaRolDao);
        UUID id = UUID.randomUUID();
        Clinica seleccionada = new Clinica();
        seleccionada.setIdClinica(id);
        model.seleccionar(seleccionada);
        Clinica referencia = new Clinica();
        referencia.setIdClinica(id);
        PersonaRol asignacion = new PersonaRol();
        asignacion.setIdClinica(referencia);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of(asignacion));

        model.eliminar();

        verify(dao, never()).eliminar(any());
        assertSame(seleccionada, model.getSeleccionado());
    }

    @Test
    void eliminarSinReferenciasEliminaYVuelveAListado() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ClinicaModel model = new ClinicaModel(dao, personaRolDao);
        UUID id = UUID.randomUUID();
        Clinica seleccionada = new Clinica();
        seleccionada.setIdClinica(id);
        model.seleccionar(seleccionada);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of());
        when(dao.eliminar(id)).thenReturn(true);

        model.eliminar();

        verify(dao).eliminar(id);
        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void eliminarCuandoDaoRetornaFalsoConservaSeleccionado() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ClinicaModel model = new ClinicaModel(dao, personaRolDao);
        UUID id = UUID.randomUUID();
        Clinica seleccionada = new Clinica();
        seleccionada.setIdClinica(id);
        model.seleccionar(seleccionada);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of());
        when(dao.eliminar(id)).thenReturn(false);

        model.eliminar();

        assertSame(seleccionada, model.getSeleccionado());
    }

    @Test
    void eliminarConErrorDeDaoNoPropaga() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ClinicaModel model = new ClinicaModel(dao, personaRolDao);
        UUID id = UUID.randomUUID();
        Clinica seleccionada = new Clinica();
        seleccionada.setIdClinica(id);
        model.seleccionar(seleccionada);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of());
        doThrow(new IllegalStateException("fallo")).when(dao).eliminar(id);

        assertDoesNotThrow(model::eliminar);

        assertSame(seleccionada, model.getSeleccionado());
    }

    @Test
    void eliminarConContextoPublicaErrorDeBloqueo() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ClinicaModel model = new ClinicaModel(dao, personaRolDao);
        FacesContext contexto = contexto("clinica.eliminacionBloqueada");
        model.facesContext = contexto;
        UUID id = UUID.randomUUID();
        Clinica seleccionada = new Clinica();
        seleccionada.setIdClinica(id);
        model.seleccionar(seleccionada);
        Clinica referencia = new Clinica();
        referencia.setIdClinica(id);
        PersonaRol asignacion = new PersonaRol();
        asignacion.setIdClinica(referencia);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of(asignacion));

        model.eliminar();

        verify(dao, never()).eliminar(any());
        verificarError(contexto, "clinica.eliminacionBloqueada");
    }

    @Test
    void guardarConNombreDuplicadoNoDelega() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        Clinica existente = new Clinica();
        existente.setNombre("CLINICA CENTRAL");
        when(dao.obtenerTodos()).thenReturn(List.of(existente));
        model.nuevo();
        model.getSeleccionado().setNombre("Clinica Central");

        model.guardar();

        verify(dao, never()).guardar(any());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarConErrorDeDaoPublicaErrorGuardarYConservaEstado() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        FacesContext contexto = contexto("clinica.errorGuardar");
        model.facesContext = contexto;
        model.nuevo();
        model.getSeleccionado().setNombre("Clinica Central");
        Clinica seleccionado = model.getSeleccionado();
        doThrow(new IllegalStateException("fallo")).when(dao).guardar(seleccionado);

        model.guardar();

        assertSame(seleccionado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        verificarError(contexto, "clinica.errorGuardar");
    }

    @Test
    void guardarEnCreacionPublicaMensajeDeExito() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao, mock(PersonaRolDAO.class));
        FacesContext contexto = contexto("clinica.creadoCorrectamente");
        model.facesContext = contexto;
        model.nuevo();
        model.getSeleccionado().setNombre("Clinica Central");

        model.guardar();

        verify(contexto, never()).validationFailed();
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                FacesMessage.SEVERITY_INFO.equals(mensaje.getSeverity())
                && ("Mensaje clinica.creadoCorrectamente").equals(mensaje.getSummary())));
    }

    @Test
    void cancelarPublicaMensajeDeCancelacion() {
        ClinicaModel model = new ClinicaModel(
                mock(ClinicaDAO.class), mock(PersonaRolDAO.class));
        FacesContext contexto = contexto("clinica.cancelado");
        model.facesContext = contexto;
        model.nuevo();

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
        verify(contexto, never()).validationFailed();
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                FacesMessage.SEVERITY_INFO.equals(mensaje.getSeverity())
                && ("Mensaje clinica.cancelado").equals(mensaje.getSummary())));
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
