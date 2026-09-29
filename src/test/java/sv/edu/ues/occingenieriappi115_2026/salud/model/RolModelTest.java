package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

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

class RolModelTest {

    @Test
    void nuevoCreaRolYCambiaEstadoACreacion() {
        RolModel model = new RolModel(
                mock(RolDAO.class),
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertTrue(model.getSeleccionado().getActivo());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        RolModel model = new RolModel(
                mock(RolDAO.class),
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        Rol rol = new Rol();

        model.seleccionar(rol);

        assertSame(rol, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("Doctor");
        Rol seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        Rol rol = new Rol();
        rol.setNombre("Doctor");
        Rol actualizado = new Rol();
        model.seleccionar(rol);
        when(dao.actualizar(rol)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(rol);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        RolModel model = new RolModel(
                mock(RolDAO.class),
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.seleccionar(new Rol());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void guardarRecortaEspaciosDelNombre() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("  Enfermera  ");

        model.guardar();

        assertEquals("Enfermera", model.getSeleccionado().getNombre());
        verify(dao).guardar(model.getSeleccionado());
    }

    @Test
    void guardarSinNombreNoDelegaNiCambiaEstado() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarConNombreDeUnSoloCaracterNoDelega() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("A");

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarConNombreDeMasDe255CaracteresNoDelega() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("x".repeat(256));

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarEnListadoActualizaElRol() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("Doctor");
        Rol seleccionado = model.getSeleccionado();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verify(dao).actualizar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConContextoPublicaErrorDeNombre() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        FacesContext contexto = contexto("rol.nombreRequerido");
        model.facesContext = contexto;
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        verificarError(contexto, "rol.nombreRequerido");
    }

    @Test
    void eliminarSinSeleccionadoNoDelega() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));

        assertDoesNotThrow(model::eliminar);

        verifyNoInteractions(dao);
    }

    @Test
    void eliminarConSeleccionadoSinIdNoDelega() {
        RolDAO dao = mock(RolDAO.class);
        RolModel model = new RolModel(
                dao,
                mock(PersonaRolDAO.class),
                mock(ProcedimientoPasoDAO.class));
        model.seleccionar(new Rol());

        assertDoesNotThrow(model::eliminar);

        verifyNoInteractions(dao);
    }

    @Test
    void eliminarConPersonasAsignadasBloqueaEliminacion() {
        RolDAO dao = mock(RolDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        RolModel model = new RolModel(
                dao, personaRolDao, mock(ProcedimientoPasoDAO.class));
        UUID id = UUID.randomUUID();
        Rol seleccionado = new Rol();
        seleccionado.setIdRol(id);
        model.seleccionar(seleccionado);
        Rol referencia = new Rol();
        referencia.setIdRol(id);
        PersonaRol asignacion = new PersonaRol();
        asignacion.setIdRol(referencia);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of(asignacion));

        model.eliminar();

        verify(dao, never()).eliminar(any());
        assertSame(seleccionado, model.getSeleccionado());
    }

    @Test
    void eliminarConPasosQueLoUsanBloqueaEliminacion() {
        RolDAO dao = mock(RolDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        RolModel model = new RolModel(dao, personaRolDao, pasoDao);
        UUID id = UUID.randomUUID();
        Rol seleccionado = new Rol();
        seleccionado.setIdRol(id);
        model.seleccionar(seleccionado);
        Rol referencia = new Rol();
        referencia.setIdRol(id);
        ProcedimientoPaso paso = new ProcedimientoPaso();
        paso.setIdRol(referencia);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of());
        when(pasoDao.obtenerTodos()).thenReturn(List.of(paso));

        model.eliminar();

        verify(dao, never()).eliminar(any());
        assertSame(seleccionado, model.getSeleccionado());
    }

    @Test
    void eliminarSinReferenciasEliminaYVuelveAListado() {
        RolDAO dao = mock(RolDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        RolModel model = new RolModel(dao, personaRolDao, pasoDao);
        UUID id = UUID.randomUUID();
        Rol seleccionado = new Rol();
        seleccionado.setIdRol(id);
        model.seleccionar(seleccionado);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of());
        when(pasoDao.obtenerTodos()).thenReturn(List.of());
        when(dao.eliminar(id)).thenReturn(true);

        model.eliminar();

        verify(dao).eliminar(id);
        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void eliminarCuandoDaoRetornaFalsoConservaSeleccionado() {
        RolDAO dao = mock(RolDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        RolModel model = new RolModel(dao, personaRolDao, pasoDao);
        UUID id = UUID.randomUUID();
        Rol seleccionado = new Rol();
        seleccionado.setIdRol(id);
        model.seleccionar(seleccionado);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of());
        when(pasoDao.obtenerTodos()).thenReturn(List.of());
        when(dao.eliminar(id)).thenReturn(false);

        model.eliminar();

        assertSame(seleccionado, model.getSeleccionado());
    }

    @Test
    void eliminarConErrorDeDaoNoPropaga() {
        RolDAO dao = mock(RolDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        ProcedimientoPasoDAO pasoDao = mock(ProcedimientoPasoDAO.class);
        RolModel model = new RolModel(dao, personaRolDao, pasoDao);
        UUID id = UUID.randomUUID();
        Rol seleccionado = new Rol();
        seleccionado.setIdRol(id);
        model.seleccionar(seleccionado);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of());
        when(pasoDao.obtenerTodos()).thenReturn(List.of());
        doThrow(new IllegalStateException("fallo")).when(dao).eliminar(id);

        assertDoesNotThrow(model::eliminar);

        assertSame(seleccionado, model.getSeleccionado());
    }

    @Test
    void eliminarConContextoPublicaErrorDeBloqueo() {
        RolDAO dao = mock(RolDAO.class);
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        RolModel model = new RolModel(
                dao, personaRolDao, mock(ProcedimientoPasoDAO.class));
        FacesContext contexto = contexto("rol.eliminacionBloqueada");
        model.facesContext = contexto;
        UUID id = UUID.randomUUID();
        Rol seleccionado = new Rol();
        seleccionado.setIdRol(id);
        model.seleccionar(seleccionado);
        Rol referencia = new Rol();
        referencia.setIdRol(id);
        PersonaRol asignacion = new PersonaRol();
        asignacion.setIdRol(referencia);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of(asignacion));

        model.eliminar();

        verify(dao, never()).eliminar(any());
        verificarError(contexto, "rol.eliminacionBloqueada");
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
