package sv.edu.ues.occingenieriappi115_2026.salud.model;

import org.junit.jupiter.api.Test;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import java.util.ResourceBundle;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoMedioContactoDAO;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;
import static org.junit.jupiter.api.Assertions.*;
import static sv.edu.ues.occingenieriappi115_2026.salud.model.TipoMedioContactoModel.FormatoContacto.*;

class TipoMedioContactoModelTest {
    private TipoMedioContactoModel nuevo() {
        TipoMedioContactoModel model = new TipoMedioContactoModel();
        model.nuevo();
        return model;
    }

    @Test
    void nuevoSinFormatoEspecialNoOfreceFormatoPersonalizado() {
        var model = nuevo();
        assertNull(model.getFormatoContacto());
        assertFalse(model.getFormatosContacto().contains(ACTUAL));
    }

    @Test
    void telefonoValidaLongitudYPrimerDigito() {
        var model = nuevo();
        model.setFormatoContacto(TELEFONO);
        String regla = model.getSeleccionado().getExpresionRegular();
        assertTrue("73456789".matches(regla));
        assertFalse("2".matches(regla));
        assertFalse("13456789".matches(regla));
        assertEquals(TELEFONO, model.getFormatoContacto());
    }

    @Test
    void residencialSoloAceptaInicioDos() {
        var model = nuevo();
        model.setFormatoContacto(RESIDENCIAL);
        String regla = model.getSeleccionado().getExpresionRegular();
        assertTrue("23456789".matches(regla));
        assertFalse("73456789".matches(regla));
    }

    @Test
    void correoNoAceptaEspaciosNiDireccionesIncompletas() {
        var model = nuevo();
        model.setFormatoContacto(CORREO);
        String regla = model.getSeleccionado().getExpresionRegular();
        assertTrue("ana@example.test".matches(regla));
        assertFalse("ana example@test.com".matches(regla));
        assertFalse("ana@example".matches(regla));
    }

    @Test
    void editarConservaExactamenteReglaPersonalizadaExistente() {
        var model = nuevo();
        var tipo = new TipoMedioContacto();
        String regla = "^[^@[:space:]]+@[^@[:space:]]+[.][^@[:space:]]+$";
        tipo.setExpresionRegular(regla);
        model.seleccionar(tipo);
        assertEquals(ACTUAL, model.getFormatoContacto());
        assertTrue(model.getFormatosContacto().contains(ACTUAL));
        model.setFormatoContacto(ACTUAL);
        assertEquals(regla, tipo.getExpresionRegular());
        model.setFormatoContacto(CORREO);
        assertEquals(CORREO, model.getFormatoContacto());
        assertNotEquals(regla, tipo.getExpresionRegular());
    }

    @Test
    void quitarFormatoEsExplicito() {
        var model = nuevo();
        model.setFormatoContacto(TELEFONO);
        model.setFormatoContacto(null);
        assertNull(model.getSeleccionado().getExpresionRegular());
        model.cancelar();
        assertDoesNotThrow(() -> model.setFormatoContacto(CORREO));
    }

    private TipoMedioContactoModel conContexto(TipoMedioContactoDAO dao) {
        var model = new TipoMedioContactoModel(dao);
        model.facesContext = mock(FacesContext.class);
        var application = mock(Application.class);
        when(model.facesContext.getApplication()).thenReturn(application);
        when(application.getResourceBundle(model.facesContext, "msg"))
                .thenReturn(ResourceBundle.getBundle("i18n.messages", java.util.Locale.forLanguageTag("es")));
        model.nuevo();
        return model;
    }

    private void verificarError(TipoMedioContactoModel model, TipoMedioContactoDAO dao,
            String campo, String texto) {
        model.guardar();
        verify(model.facesContext).addMessage(eq(campo),
                argThat(m -> texto.equals(m.getSummary())));
        verify(model.facesContext, times(1)).addMessage(any(), any());
        verify(model.facesContext).validationFailed();
        verifyNoInteractions(dao);
        assertNotEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void todoVacioSoloNombre() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        model.getSeleccionado().setNombre("   ");
        verificarError(model, dao, "nombre", "El nombre es requerido.");
    }

    @Test
    void nombreCompletoSoloIndicaciones() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        model.getSeleccionado().setNombre("Teléfono");
        model.getSeleccionado().setIndicaciones("  ");
        verificarError(model, dao, "indicaciones", "Las indicaciones son requeridas.");
    }

    @Test
    void nombreEIndicacionesSoloFormato() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        model.getSeleccionado().setNombre("Teléfono");
        model.getSeleccionado().setIndicaciones("Número de contacto");
        verificarError(model, dao, "formatoContacto", "El formato del contacto es requerido.");
    }

    @Test
    void completoGuarda() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        model.getSeleccionado().setNombre(" Teléfono ");
        model.getSeleccionado().setIndicaciones(" Número de contacto ");
        model.setFormatoContacto(TELEFONO);
        var tipo = model.getSeleccionado();
        model.guardar();
        verify(dao).guardar(tipo);
        verify(model.facesContext, never()).validationFailed();
        assertEquals("Teléfono", tipo.getNombre());
        assertEquals("Número de contacto", tipo.getIndicaciones());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void edicionNoActualizaAlBorrarNombre() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        model.getSeleccionado().setIndicaciones("Número de contacto");
        model.setFormatoContacto(TELEFONO);
        model.seleccionar(model.getSeleccionado());
        verificarError(model, dao, "nombre", "El nombre es requerido.");
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void edicionCompletaConservaReglaPersonalizada() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        var tipo = model.getSeleccionado();
        tipo.setNombre("Contacto");
        tipo.setIndicaciones("Instrucciones");
        tipo.setExpresionRegular("^[0-9]{8}$");
        model.seleccionar(tipo);
        when(dao.actualizar(tipo)).thenReturn(tipo);
        model.setFormatoContacto(ACTUAL);
        model.guardar();
        verify(dao).actualizar(tipo);
        assertEquals("^[0-9]{8}$", tipo.getExpresionRegular());
        verify(model.facesContext, never()).validationFailed();
    }
}
