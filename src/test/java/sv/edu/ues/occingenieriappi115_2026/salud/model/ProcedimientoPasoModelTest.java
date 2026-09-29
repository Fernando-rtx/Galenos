package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProcedimientoPasoModelTest {

    @Test
    void nuevoCreaPasoYCambiaEstadoACreacion() {
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                mock(ProcedimientoPasoDAO.class),
                mock(ProcedimientoDAO.class),
                mock(RolDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                mock(ProcedimientoPasoDAO.class),
                mock(ProcedimientoDAO.class),
                mock(RolDAO.class));
        ProcedimientoPaso paso = new ProcedimientoPaso();

        model.seleccionar(paso);

        assertSame(paso, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("Preparación");
        ProcedimientoPaso seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        ProcedimientoPaso paso = new ProcedimientoPaso();
        paso.setNombre("Preparación");
        ProcedimientoPaso actualizado = new ProcedimientoPaso();
        model.seleccionar(paso);
        when(dao.actualizar(paso)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(paso);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                mock(ProcedimientoPasoDAO.class),
                mock(ProcedimientoDAO.class),
                mock(RolDAO.class));
        model.seleccionar(new ProcedimientoPaso());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaProcedimientosYRoles() {
        ProcedimientoDAO procDao = mock(ProcedimientoDAO.class);
        RolDAO rolDao = mock(RolDAO.class);
        when(procDao.obtenerTodos()).thenReturn(List.of(new Procedimiento()));
        when(rolDao.obtenerTodos()).thenReturn(List.of(new Rol()));

        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                mock(ProcedimientoPasoDAO.class), procDao, rolDao);

        assertEquals(1, model.getProcedimientos().size());
        assertEquals(1, model.getRoles().size());
    }

    @Test
    void guardarRecortaEspaciosDelNombre() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("  Preparación  ");

        model.guardar();

        assertEquals("Preparación", model.getSeleccionado().getNombre());
        verify(dao).guardar(model.getSeleccionado());
    }

    @Test
    void guardarSinNombreNoDelegaNiCambiaEstado() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarConNombreDeUnSoloCaracterNoDelega() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("A");

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarConNombreDeMasDe255CaracteresNoDelega() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("x".repeat(256));

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarEnListadoNoPersisteElPaso() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        model.nuevo();
        model.getSeleccionado().setNombre("Preparación");
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConContextoPublicaErrorDeNombre() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(
                dao, mock(ProcedimientoDAO.class), mock(RolDAO.class));
        FacesContext contexto = contexto("procedimientoPaso.nombreRequerido");
        model.facesContext = contexto;
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        verificarError(contexto, "procedimientoPaso.nombreRequerido");
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
