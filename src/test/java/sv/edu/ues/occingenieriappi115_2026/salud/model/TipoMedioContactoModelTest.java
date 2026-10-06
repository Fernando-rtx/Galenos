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

class TipoMedioContactoModelTest {
    private TipoMedioContactoModel nuevo() {
        TipoMedioContactoModel model = new TipoMedioContactoModel();
        model.nuevo();
        return model;
    }

    @Test
    void nuevoIniciaEnPersonalizado() {
        var model = nuevo();
        assertEquals(FormatoRegexSugerido.PERSONALIZADO, model.getFormatoSugerido());
        assertTrue(model.getFormatosSugeridos().contains(FormatoRegexSugerido.PERSONALIZADO));
    }

    @Test
    void telefonoValidaLongitudYPrimerDigito() {
        var model = nuevo();
        model.setFormatoSugerido(FormatoRegexSugerido.TELEFONO_SV);
        String regla = model.getSeleccionado().getExpresionRegular();
        assertTrue("73456789".matches(regla));
        assertFalse("2".matches(regla));
        assertFalse("13456789".matches(regla));
        assertEquals("^(2|6|7)[0-9]{7}$", regla);
        assertEquals(FormatoRegexSugerido.TELEFONO_SV, model.getFormatoSugerido());
    }

    @Test
    void residencialSoloAceptaInicioDos() {
        var model = nuevo();
        model.setFormatoSugerido(FormatoRegexSugerido.RESIDENCIAL_SV);
        String regla = model.getSeleccionado().getExpresionRegular();
        assertTrue("23456789".matches(regla));
        assertFalse("73456789".matches(regla));
    }

    @Test
    void correoNoAceptaEspaciosNiDireccionesIncompletas() {
        var model = nuevo();
        model.setFormatoSugerido(FormatoRegexSugerido.CORREO);
        String regla = model.getSeleccionado().getExpresionRegular();
        assertTrue("ana@example.test".matches(regla));
        assertFalse("ana example@test.com".matches(regla));
        assertFalse("ana@example".matches(regla));
        assertEquals("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", regla);
    }

    @Test
    void editarConservaExactamenteReglaPersonalizadaExistente() {
        var model = nuevo();
        var tipo = new TipoMedioContacto();
        String regla = "^[^@[:space:]]+@[^@[:space:]]+[.][^@[:space:]]+$";
        tipo.setExpresionRegular(regla);
        model.seleccionar(tipo);
        assertEquals(FormatoRegexSugerido.PERSONALIZADO, model.getFormatoSugerido());
        model.setFormatoSugerido(FormatoRegexSugerido.PERSONALIZADO);
        assertEquals(regla, tipo.getExpresionRegular());
        model.setFormatoSugerido(FormatoRegexSugerido.CORREO);
        assertEquals(FormatoRegexSugerido.CORREO, model.getFormatoSugerido());
        assertNotEquals(regla, tipo.getExpresionRegular());
    }

    @Test
    void cambiarAPersonalizadoPermiteEditarLaExpresionManual() {
        var model = nuevo();
        model.setFormatoSugerido(FormatoRegexSugerido.TELEFONO_SV);
        model.setFormatoSugerido(FormatoRegexSugerido.PERSONALIZADO);
        model.getSeleccionado().setExpresionRegular("^[A-Z]{2}[0-9]{7}$");
        assertEquals("^[A-Z]{2}[0-9]{7}$", model.getSeleccionado().getExpresionRegular());
        model.cancelar();
        assertDoesNotThrow(() -> model.setFormatoSugerido(FormatoRegexSugerido.CORREO));
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
    void expresionRegularPersonalizadaEsOpcionalComoAntes() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        model.getSeleccionado().setNombre("Teléfono");
        model.getSeleccionado().setIndicaciones("Número de contacto");

        model.guardar();

        verify(dao).guardar(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void completoGuarda() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        model.getSeleccionado().setNombre(" Teléfono ");
        model.getSeleccionado().setIndicaciones(" Número de contacto ");
        model.setFormatoSugerido(FormatoRegexSugerido.TELEFONO_SV);
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
        model.setFormatoSugerido(FormatoRegexSugerido.TELEFONO_SV);
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
        model.setFormatoSugerido(FormatoRegexSugerido.PERSONALIZADO);
        model.guardar();
        verify(dao).actualizar(tipo);
        assertEquals("^[0-9]{8}$", tipo.getExpresionRegular());
        verify(model.facesContext, never()).validationFailed();
    }

    @Test
    void editarRegexConocidaSeleccionaElPresetSinCambiarElValorPersistido() {
        var model = nuevo();
        var tipo = new TipoMedioContacto();
        tipo.setExpresionRegular("^(2|6|7)[0-9]{7}$");

        model.seleccionar(tipo);

        assertEquals(FormatoRegexSugerido.TELEFONO_SV, model.getFormatoSugerido());
        assertEquals("^(2|6|7)[0-9]{7}$", tipo.getExpresionRegular());
    }

    @Test
    void editarRegexDesconocidaUsaPersonalizadoYLaConserva() {
        var model = nuevo();
        var tipo = new TipoMedioContacto();
        tipo.setExpresionRegular("^[A-Z]{2}[0-9]{7}$");

        model.seleccionar(tipo);

        assertEquals(FormatoRegexSugerido.PERSONALIZADO, model.getFormatoSugerido());
        assertEquals("^[A-Z]{2}[0-9]{7}$", tipo.getExpresionRegular());
    }

    @Test
    void expresionRegularSintacticamenteInvalidaImpideGuardar() {
        var dao = mock(TipoMedioContactoDAO.class);
        var model = conContexto(dao);
        model.getSeleccionado().setNombre("Teléfono");
        model.getSeleccionado().setIndicaciones("Número de contacto");
        model.getSeleccionado().setExpresionRegular("^[0-9{");

        verificarError(model, dao, "expresionRegular", "La expresión regular no es válida.");
        assertEquals("^[0-9{", model.getSeleccionado().getExpresionRegular());
    }
}
