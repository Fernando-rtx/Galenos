package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DuiMayoriaEdadModelTest {
    private static final String MENOR = "Esta persona no es mayor de edad. Asígnele un carné de minoridad.";

    private static LocalDate hoy() {
        return LocalDate.now(DocumentoModel.ZONA_CLINICA);
    }

    private static Date fecha(LocalDate dia) {
        return dia == null ? null : Date.from(dia.atStartOfDay(DocumentoModel.ZONA_CLINICA).toInstant());
    }

    private static TipoDocumento tipo(String nombre) {
        TipoDocumento tipo = new TipoDocumento(UUID.randomUUID());
        tipo.setNombre(nombre);
        tipo.setActivo(true);
        tipo.setExpresionRegular("^[0-9]{8}-[0-9]$");
        return tipo;
    }

    private static FacesContext contexto(String campo) {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg"))
                .thenReturn(ResourceBundle.getBundle("i18n.messages", Locale.forLanguageTag("es")));
        UIViewRoot raiz = mock(UIViewRoot.class);
        UIInput entrada = mock(UIInput.class);
        when(entrada.getId()).thenReturn(campo);
        when(entrada.getClientId(contexto)).thenReturn("layoutForm:personaTabs:" + campo);
        when(entrada.getFacetsAndChildren()).thenAnswer(i -> List.<jakarta.faces.component.UIComponent>of().iterator());
        UIInput valor = mock(UIInput.class);
        when(valor.getId()).thenReturn("valor");
        when(valor.getClientId(contexto)).thenReturn("layoutForm:personaTabs:valor");
        when(valor.getFacetsAndChildren()).thenAnswer(i -> List.<jakarta.faces.component.UIComponent>of().iterator());
        when(raiz.getFacetsAndChildren()).thenAnswer(i -> List.<jakarta.faces.component.UIComponent>of(entrada, valor).iterator());
        when(contexto.getViewRoot()).thenReturn(raiz);
        return contexto;
    }

    private static void inyectarContexto(DocumentoModel modelo, FacesContext contexto) throws Exception {
        Field campo = DocumentoModel.class.getDeclaredField("facesContext");
        campo.setAccessible(true);
        campo.set(modelo, contexto);
    }

    private static void unicoError(FacesContext contexto, String campo, String texto) {
        verify(contexto).validationFailed();
        verify(contexto, times(1)).addMessage(eq("layoutForm:personaTabs:" + campo), argThat(m ->
                FacesMessage.SEVERITY_ERROR.equals(m.getSeverity()) && texto.equals(m.getSummary())));
        verify(contexto, times(1)).addMessage(any(), any(FacesMessage.class));
    }

    private static class Escenario {
        final DocumentoDAO documentos = mock(DocumentoDAO.class);
        final PersonaDAO personas = mock(PersonaDAO.class);
        final TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        final Persona persona = new Persona(UUID.randomUUID());
        final TipoDocumento dui = tipo("DUI");
        final DocumentoModel modelo = new DocumentoModel(documentos, personas, tipos);
        final Documento borrador;
        FacesContext contexto = contexto("tipo");

        Escenario(LocalDate nacimiento) throws Exception {
            persona.setFechaNacimiento(fecha(nacimiento));
            when(personas.buscarPorId(persona.getIdPersona())).thenReturn(persona);
            when(tipos.buscarPorId(dui.getIdTipoDocumento())).thenReturn(dui);
            modelo.nuevoParaPersona(persona);
            modelo.setTipoDocumentoSeleccionadoId(dui.getIdTipoDocumento().toString());
            borrador = modelo.getSeleccionado();
            borrador.setValor("01234567-8");
            borrador.setRutaFisica("/documentos/dui.pdf");
            inyectarContexto(modelo, contexto);
        }

        void nuevaPeticion() throws Exception {
            contexto = contexto("tipo");
            inyectarContexto(modelo, contexto);
        }

        void comprobarRechazo(String texto) {
            assertSame(borrador, modelo.getSeleccionado());
            assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
            verify(documentos, never()).guardar(any());
            unicoError(contexto, "tipo", texto);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 17})
    void menoresNoPuedenGuardarDuiAunqueNoEjecutenElAjax(int edad) throws Exception {
        Escenario e = new Escenario(hoy().minusYears(edad));
        e.borrador.setValor(" 01234567-8 ");
        e.borrador.setRutaFisica(" /documentos/dui.pdf ");

        e.modelo.guardar();

        e.comprobarRechazo(MENOR);
        assertEquals(" 01234567-8 ", e.borrador.getValor());
        assertEquals(" /documentos/dui.pdf ", e.borrador.getRutaFisica());
    }

    @Test
    void cumpleDieciochoMananaTodaviaNoPuedeGuardarDui() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(18).plusDays(1));
        e.modelo.guardar();
        e.comprobarRechazo(MENOR);
    }

    @ParameterizedTest
    @ValueSource(ints = {18, 20})
    void cumpleDieciochoHoyOYaEsMayorPuedeGuardar(int edad) throws Exception {
        Escenario e = new Escenario(hoy().minusYears(edad));
        e.modelo.guardar();
        verify(e.documentos).guardar(e.borrador);
        assertEquals(ESTADO_CRUD.LISTADO, e.modelo.getEstado());
        assertNull(e.modelo.getSeleccionado());
        verify(e.contexto, never()).validationFailed();
        verify(e.documentos).existeDocumento(e.dui.getIdTipoDocumento(), "01234567-8", null);
    }

    @Test
    void eventoDelComboMuestraElAvisoSinGuardarNiCambiarDatos() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(16));
        e.modelo.validarEdadDuiSeleccionado();
        e.comprobarRechazo(MENOR);
        assertSame(e.dui, e.borrador.getIdTipoDocumento());
        assertEquals("01234567-8", e.borrador.getValor());
        verify(e.tipos, never()).guardar(any());
    }

    @Test
    void cambiarAPasaporteQuitaElAvisoYPermiteGuardarParaMenor() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(16));
        e.modelo.validarEdadDuiSeleccionado();
        unicoError(e.contexto, "tipo", MENOR);
        TipoDocumento pasaporte = tipo("Pasaporte");
        when(e.tipos.buscarPorId(pasaporte.getIdTipoDocumento())).thenReturn(pasaporte);
        e.nuevaPeticion();

        e.modelo.setTipoDocumentoSeleccionadoId(pasaporte.getIdTipoDocumento().toString());
        e.modelo.validarEdadDuiSeleccionado();

        verifyNoInteractions(e.contexto);
        assertEquals("01234567-8", e.borrador.getValor());
        e.modelo.guardar();
        verify(e.documentos).guardar(e.borrador);
        verify(e.contexto, never()).validationFailed();
    }

    @Test
    void corregirFechaRegistradaQuitaElAvisoYPermiteGuardarDui() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(17));
        e.modelo.guardar();
        e.comprobarRechazo(MENOR);
        e.persona.setFechaNacimiento(fecha(hoy().minusYears(18)));
        e.nuevaPeticion();

        e.modelo.validarEdadDuiSeleccionado();
        verifyNoInteractions(e.contexto);
        e.modelo.guardar();

        verify(e.documentos).guardar(e.borrador);
        verify(e.contexto, never()).validationFailed();
    }

    @Test
    void fechaAusenteImpideAsignarDuiSinExcepciones() throws Exception {
        Escenario e = new Escenario(null);
        assertDoesNotThrow(e.modelo::guardar);
        e.comprobarRechazo("Complete o corrija la fecha de nacimiento de esta persona antes de asignarle un DUI.");
    }

    @Test
    void fechaFuturaNoPermiteAsignarDui() throws Exception {
        Escenario e = new Escenario(hoy().plusDays(1));
        assertDoesNotThrow(e.modelo::guardar);
        e.comprobarRechazo("Complete o corrija la fecha de nacimiento de esta persona antes de asignarle un DUI.");
    }

    @Test
    void nacimientoAnteriorAlRangoPermitidoNoPermiteDui() throws Exception {
        Escenario e = new Escenario(LocalDate.of(1899, 12, 31));
        e.modelo.guardar();
        e.comprobarRechazo("Complete o corrija la fecha de nacimiento de esta persona antes de asignarle un DUI.");
    }

    @Test
    void nombreNormalizadoDelCatalogoIdentificaDuiSinUuidFijo() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(16));
        e.dui.setNombre("  dUi  ");
        e.modelo.guardar();
        e.comprobarRechazo(MENOR);
        assertFalse(DocumentoModel.esDui(tipo("DNI")));
        assertFalse(DocumentoModel.esDui(tipo("Pasaporte")));
    }

    @Test
    void nombreFalsificadoEnElBorradorNoEvadeLaReglaDelCatalogo() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(17));
        TipoDocumento falsificado = new TipoDocumento(e.dui.getIdTipoDocumento());
        falsificado.setNombre("Pasaporte");
        e.borrador.setIdTipoDocumento(falsificado);

        e.modelo.guardar();

        e.comprobarRechazo(MENOR);
        assertSame(e.dui, e.borrador.getIdTipoDocumento());
    }

    @Test
    void fechaFalsificadaEnLaPersonaDelBorradorNoEvadeLaFechaRegistrada() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(17));
        Persona falsificada = new Persona(e.persona.getIdPersona());
        falsificada.setFechaNacimiento(fecha(hoy().minusYears(20)));
        e.borrador.setIdPersona(falsificada);

        e.modelo.guardar();

        e.comprobarRechazo(MENOR);
        verify(e.personas).buscarPorId(e.persona.getIdPersona());
    }

    @Test
    void tipoDuiInactivoNoPuedeAsignarseAunqueSeaMayor() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(20));
        e.dui.setActivo(false);
        e.modelo.guardar();
        e.comprobarRechazo("El tipo de documento seleccionado está inactivo.");
    }

    @Test
    void tipoSinIdDelCatalogoNoPuedeEvadirLasValidaciones() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(16));
        TipoDocumento falsificado = new TipoDocumento();
        falsificado.setNombre("Pasaporte");
        e.borrador.setIdTipoDocumento(falsificado);
        e.modelo.guardar();
        e.comprobarRechazo("El tipo de documento es obligatorio.");
    }

    @Test
    void tipoInexistenteEnCatalogoSeRechaza() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(20));
        e.borrador.setIdTipoDocumento(new TipoDocumento(UUID.randomUUID()));
        e.modelo.guardar();
        e.comprobarRechazo("El tipo de documento es obligatorio.");
    }

    @Test
    void serMayorNoEvitaValidarLaExpresionRegular() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(20));
        e.borrador.setValor("no-es-un-dui");
        e.modelo.guardar();
        assertEquals(ESTADO_CRUD.CREACION, e.modelo.getEstado());
        verify(e.documentos, never()).guardar(any());
        // El campo Valor es el destino de la validación de formato, no el selector Tipo.
        unicoError(e.contexto, "valor", "El valor no cumple el formato definido para este tipo de documento.");
    }

    @Test
    void serMayorNoEvitaLaUnicidadGlobal() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(20));
        when(e.documentos.existeDocumento(e.dui.getIdTipoDocumento(), "01234567-8", null)).thenReturn(true);
        e.modelo.guardar();
        assertEquals(ESTADO_CRUD.CREACION, e.modelo.getEstado());
        verify(e.documentos, never()).guardar(any());
        unicoError(e.contexto, "valor", "Este tipo de documento y valor ya están registrados.");
    }

    @Test
    void enEdicionElTipoDelDocumentoSigueSiendoInmutable() throws Exception {
        Escenario e = new Escenario(hoy().minusYears(20));
        e.borrador.setIdDocumento(UUID.randomUUID());
        e.modelo.seleccionar(e.borrador);
        TipoDocumento pasaporte = tipo("Pasaporte");
        when(e.tipos.buscarPorId(pasaporte.getIdTipoDocumento())).thenReturn(pasaporte);

        e.modelo.setTipoDocumentoSeleccionadoId(pasaporte.getIdTipoDocumento().toString());
        assertSame(e.dui, e.borrador.getIdTipoDocumento());
        e.borrador.setIdTipoDocumento(pasaporte);
        e.modelo.guardar();

        verify(e.documentos, never()).actualizar(any());
        assertSame(e.dui, e.borrador.getIdTipoDocumento());
        assertEquals(ESTADO_CRUD.EDICION, e.modelo.getEstado());
        unicoError(e.contexto, "tipo", "No se puede cambiar el tipo de documento al editar.");
    }

    @Test
    void calculoUsaMesDiaYZonaDeElSalvador() {
        LocalDate hoy = LocalDate.of(2026, 10, 6);
        assertEquals("documento.duiMenorEdad", DocumentoModel.errorFechaParaDui(fecha(LocalDate.of(2008, 10, 7)), hoy));
        assertNull(DocumentoModel.errorFechaParaDui(fecha(LocalDate.of(2008, 10, 6)), hoy));
        assertEquals("documento.duiMenorEdad", DocumentoModel.errorFechaParaDui(fecha(LocalDate.of(2008, 11, 1)), hoy));
        assertNull(DocumentoModel.errorFechaParaDui(fecha(LocalDate.of(2008, 9, 30)), hoy));
        // El instante UTC cae el día 7; en El Salvador corresponde al nacimiento del día 6.
        Date nacimiento = Date.from(java.time.Instant.parse("2008-10-07T01:00:00Z"));
        assertNull(DocumentoModel.errorFechaParaDui(nacimiento, hoy));
        assertNull(DocumentoModel.errorFechaParaDui(new java.sql.Date(fecha(LocalDate.of(2008, 10, 6)).getTime()), hoy));
    }

    @Test
    void nacimientoBisiestoSeEvaluaConPeriodCompleto() {
        Date nacimiento = fecha(LocalDate.of(2008, 2, 29));
        assertEquals("documento.duiMenorEdad", DocumentoModel.errorFechaParaDui(nacimiento, LocalDate.of(2026, 2, 28)));
        assertNull(DocumentoModel.errorFechaParaDui(nacimiento, LocalDate.of(2026, 3, 1)));
    }

    @Test
    void editarFechaDePersonaConDuiNoPuedeConvertirlaEnMenor() {
        PersonaDAO personas = mock(PersonaDAO.class);
        DocumentoDAO documentos = mock(DocumentoDAO.class);
        PersonaModel modelo = new PersonaModel(personas, documentos);
        Persona persona = persona(hoy().minusYears(20));
        modelo.seleccionar(persona);
        persona.setFechaNacimiento(fecha(hoy().minusYears(17)));
        // La colección de la entidad puede no incluir el DUI recién agregado; manda el DAO.
        persona.setDocumentoCollection(List.of());
        Documento dui = new Documento(UUID.randomUUID());
        dui.setIdTipoDocumento(tipo(" dui "));
        when(documentos.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList())).thenReturn(List.of(dui));
        modelo.facesContext = contexto("fechaNacimiento");

        modelo.guardar();

        verify(personas, never()).actualizar(any());
        verify(documentos).obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL, persona.getIdPersona())), List.of());
        unicoError(modelo.facesContext, "fechaNacimiento",
                "La persona ya tiene un DUI. La fecha de nacimiento debe corresponder a una persona con 18 años cumplidos.");
        assertSame(persona, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
        assertEquals(fecha(hoy().minusYears(17)), persona.getFechaNacimiento());
        verify(documentos, never()).eliminar(any());
        verify(documentos, never()).actualizar(any());

        persona.setFechaNacimiento(fecha(hoy().minusYears(18)));
        modelo.facesContext = contexto("fechaNacimiento");
        when(personas.actualizar(persona)).thenReturn(persona);
        modelo.guardar();
        verify(personas).actualizar(persona);
        verify(modelo.facesContext, never()).validationFailed();
        verify(modelo.facesContext).addMessage(isNull(), argThat(m -> FacesMessage.SEVERITY_INFO.equals(m.getSeverity())));
    }

    @Test
    void fechaDeMenorSinDuiSigueSiendoPermitida() {
        PersonaDAO personas = mock(PersonaDAO.class);
        DocumentoDAO documentos = mock(DocumentoDAO.class);
        PersonaModel modelo = new PersonaModel(personas, documentos);
        Persona persona = persona(hoy().minusYears(20));
        modelo.seleccionar(persona);
        persona.setFechaNacimiento(fecha(hoy().minusYears(16)));
        Documento pasaporte = new Documento(UUID.randomUUID());
        pasaporte.setIdTipoDocumento(tipo("Pasaporte"));
        when(documentos.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList())).thenReturn(List.of(pasaporte));
        when(personas.actualizar(persona)).thenReturn(persona);
        modelo.facesContext = contexto("fechaNacimiento");

        modelo.guardar();

        verify(personas).actualizar(persona);
        verify(modelo.facesContext, never()).validationFailed();
    }

    private static Persona persona(LocalDate nacimiento) {
        Persona persona = new Persona(UUID.randomUUID());
        persona.setNombres("Ana");
        persona.setApellidos("López");
        persona.setFechaNacimiento(fecha(nacimiento));
        return persona;
    }
}
