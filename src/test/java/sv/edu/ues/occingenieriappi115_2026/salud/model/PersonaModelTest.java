package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonaModelTest {

    @Test
    void guardarSinPersonaSeleccionadaNoLlamaAlDao() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void idDeContextoInvalidoLimpiaLaSeleccion() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        modelo.seleccionar(new Persona());

        modelo.setIdPersonaContexto("id-invalido");

        assertNull(modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
        verifyNoInteractions(dao);
    }

    @Test
    void guardarRecortaNombresYApellidos() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        modelo.nuevo();
        Persona persona = modelo.getSeleccionado();
        persona.setNombres("  Ana  ");
        persona.setApellidos("  López  ");
        persona.setFechaNacimiento(fechaValida());

        modelo.guardar();

        assertEquals("Ana", persona.getNombres());
        assertEquals("López", persona.getApellidos());
        verify(dao).guardar(persona);
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }

    @Test
    void guardarCreacionMuestraMensajeDeExito() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        FacesContext contexto = contexto();
        modelo.facesContext = contexto;
        modelo.nuevo();
        completarPersona(modelo.getSeleccionado());

        modelo.guardar();

        verify(dao).guardar(modelo.getSeleccionado());
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                "Persona creada correctamente.".equals(mensaje.getSummary())));
        verify(contexto, never()).validationFailed();
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }

    @Test
    void guardarEdicionMuestraMensajeDeExito() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        FacesContext contexto = contexto();
        modelo.facesContext = contexto;
        Persona persona = new Persona(UUID.randomUUID());
        completarPersona(persona);
        modelo.seleccionar(persona);
        when(dao.actualizar(persona)).thenReturn(persona);

        modelo.guardar();

        verify(dao).actualizar(persona);
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                "Persona actualizada correctamente.".equals(mensaje.getSummary())));
        verify(contexto, never()).validationFailed();
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }

    @Test
    void errorDePersistenciaMarcaValidacionYConservaCreacion() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        FacesContext contexto = contexto();
        modelo.facesContext = contexto;
        modelo.nuevo();
        Persona persona = modelo.getSeleccionado();
        completarPersona(persona);
        doThrow(new IllegalStateException("Error de persistencia")).when(dao).guardar(persona);

        assertDoesNotThrow(modelo::guardar);

        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                "No fue posible guardar la persona. Intente nuevamente.".equals(mensaje.getSummary())));
        verify(contexto).validationFailed();
        assertSame(persona, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void errorDelDaoAlGuardarConservaEstadoDeCreacion() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        modelo.nuevo();
        Persona persona = modelo.getSeleccionado();
        completarPersona(persona);
        doThrow(new IllegalStateException("Error de persistencia")).when(dao).guardar(persona);

        assertThrows(IllegalStateException.class, modelo::guardar);

        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
        assertSame(persona, modelo.getSeleccionado());
    }

    @Test
    void camposObligatoriosVaciosSonRechazados() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        FacesContext contexto = contexto();

        assertAll(
                () -> assertThrows(ValidatorException.class,
                        () -> modelo.validarNombre(contexto, componente("nombres"), null)),
                () -> assertThrows(ValidatorException.class,
                        () -> modelo.validarNombre(contexto, componente("apellidos"), "   ")),
                () -> assertThrows(ValidatorException.class,
                        () -> modelo.validarFechaNacimiento(contexto,
                                componente("fechaNacimiento"), null)));
    }

    @Test
    void guardarVacioSoloMuestraErrorDeNombres() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        FacesContext contexto = contexto();
        modelo.facesContext = contexto;
        modelo.nuevo();

        modelo.guardar();

        verificarUnicoError(contexto, "nombres", "El nombre es requerido.");
        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void guardarConNombresSoloMuestraErrorDeApellidos() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        FacesContext contexto = contexto();
        modelo.facesContext = contexto;
        modelo.nuevo();
        modelo.getSeleccionado().setNombres("Ana");

        modelo.guardar();

        verificarUnicoError(contexto, "apellidos", "El apellido es requerido.");
        verifyNoInteractions(dao);
    }

    @Test
    void guardarConNombresYApellidosSoloMuestraErrorDeFecha() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        FacesContext contexto = contexto();
        modelo.facesContext = contexto;
        modelo.nuevo();
        modelo.getSeleccionado().setNombres("Ana");
        modelo.getSeleccionado().setApellidos("López");

        modelo.guardar();

        verificarUnicoError(contexto, "fechaNacimiento", "La fecha de nacimiento es requerida.");
        verifyNoInteractions(dao);
    }

    @Test
    void guardarConFechaFuturaNoPersisteYMuestraSoloErrorDeFecha() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        FacesContext contexto = contexto();
        modelo.facesContext = contexto;
        modelo.nuevo();
        completarPersona(modelo.getSeleccionado());
        modelo.getSeleccionado().setFechaNacimiento(
                new Date(System.currentTimeMillis() + 86_400_000L));

        modelo.guardar();

        verificarUnicoError(contexto, "fechaNacimiento", "Fecha futura");
        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void guardarEdicionSinApellidosNoActualiza() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        FacesContext contexto = contexto();
        modelo.facesContext = contexto;
        Persona persona = new Persona(UUID.randomUUID());
        persona.setNombres("Ana");
        persona.setFechaNacimiento(fechaValida());
        modelo.seleccionar(persona);

        modelo.guardar();

        verificarUnicoError(contexto, "apellidos", "El apellido es requerido.");
        verify(dao, never()).actualizar(any());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
        assertSame(persona, modelo.getSeleccionado());
    }

    @Test
    void nombreDeUnaLetraEsRechazado() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        assertThrows(ValidatorException.class,
                () -> modelo.validarNombre(contexto(), componente("nombres"), "A"));
    }

    @Test
    void nombreDeMasDe255CaracteresEsRechazado() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        String nombreLargo = "A".repeat(256);
        assertThrows(ValidatorException.class,
                () -> modelo.validarNombre(contexto(), componente("nombres"), nombreLargo));
    }

    @Test
    void fechaMinimaEsAceptada() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        assertDoesNotThrow(() -> modelo.validarFechaNacimiento(
                null, componente("fechaNacimiento"), modelo.getFechaMinimaNacimiento()));
    }

    @Test
    void nuevoInicializaFecha() {
        PersonaModel m = new PersonaModel(mock(PersonaDAO.class));
        m.nuevo();
        assertNotNull(m.getSeleccionado().getFechaCreacion());
        assertEquals(ESTADO_CRUD.CREACION, m.getEstado());
    }

    @Test
    void nuevoDespuesDeEditarCreaUnaPersonaLimpia() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        Persona anterior = new Persona();
        anterior.setNombres("Alexander");
        anterior.setApellidos("Itzep");
        anterior.setFechaNacimiento(new Date());
        modelo.seleccionar(anterior);

        modelo.nuevo();

        assertNotSame(anterior, modelo.getSeleccionado());
        assertNull(modelo.getSeleccionado().getNombres());
        assertNull(modelo.getSeleccionado().getApellidos());
        assertNull(modelo.getSeleccionado().getFechaNacimiento());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void nuevaPersonaLimpiaLasRelacionesContextualesAnteriores() {
        Persona anterior = new Persona(UUID.randomUUID());
        var documentos = new DocumentoModel(mock(sv.edu.ues.occingenieriappi115_2026.salud.control.DocumentoDAO.class),
                mock(PersonaDAO.class), mock(sv.edu.ues.occingenieriappi115_2026.salud.control.TipoDocumentoDAO.class));
        var contactos = new MedioContactoModel(mock(sv.edu.ues.occingenieriappi115_2026.salud.control.MedioContactoDAO.class),
                mock(PersonaDAO.class), mock(sv.edu.ues.occingenieriappi115_2026.salud.control.TipoMedioContactoDAO.class));
        var roles = new PersonaRolModel(mock(sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO.class),
                mock(PersonaDAO.class), mock(sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO.class),
                mock(sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO.class));
        documentos.nuevoParaPersona(anterior);
        contactos.nuevoParaPersona(anterior);
        roles.nuevoParaPersona(anterior);
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));

        modelo.nuevaPersona(documentos, contactos, roles);

        assertNull(documentos.getSeleccionado());
        assertNull(documentos.getPersonaContexto());
        assertNull(contactos.getSeleccionado());
        assertNull(contactos.getPersonaContexto());
        assertNull(roles.getSeleccionado());
        assertNull(roles.getPersonaContexto());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void seleccionarEdita() {
        PersonaModel m = new PersonaModel(mock(PersonaDAO.class));
        Persona p = new Persona();
        m.seleccionar(p);
        assertSame(p, m.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, m.getEstado());
    }

    @Test
    void dobleClicSeleccionaPersonaDelEvento() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        Persona persona = new Persona();
        SelectEvent<Persona> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(persona);

        modelo.seleccionarFila(evento);

        assertSame(persona, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }

    @Test
    void guardarCreacionPermiteUuidDelDao() {
        PersonaDAO d = mock(PersonaDAO.class);
        doAnswer(i -> {
            Persona p = i.getArgument(0);
            p.setIdPersona(UUID.randomUUID());
            return p;
        }).when(d).guardar(any());
        PersonaModel m = new PersonaModel(d);
        m.nuevo();
        completarPersona(m.getSeleccionado());
        m.guardar();
        assertNotNull(m.getSeleccionado().getIdPersona());
        verify(d).guardar(m.getSeleccionado());
    }

    @Test
    void guardarEdicionUsaResultado() {
        PersonaDAO d = mock(PersonaDAO.class);
        Persona a = new Persona(), b = new Persona();
        completarPersona(a);
        when(d.actualizar(a)).thenReturn(b);
        PersonaModel m = new PersonaModel(d);
        m.seleccionar(a);
        m.guardar();
        assertSame(b, m.getSeleccionado());
    }

    @Test
    void cancelarLimpia() {
        PersonaModel m = new PersonaModel(mock(PersonaDAO.class));
        m.nuevo();
        m.cancelar();
        assertNull(m.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, m.getEstado());
    }

    @Test
    void cerrarEdicionLimpiaPersonaYModelosDependientes() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        DocumentoModel documento = mock(DocumentoModel.class);
        MedioContactoModel contacto = mock(MedioContactoModel.class);
        PersonaRolModel personaRol = mock(PersonaRolModel.class);
        modelo.nuevo();

        modelo.cerrarEdicion(documento, contacto, personaRol);

        verify(documento).cancelar();
        verify(contacto).cancelar();
        verify(personaRol).cancelar();
        assertNull(modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void recibeIdPersonaYSeleccionaContexto() {
        PersonaDAO d = mock(PersonaDAO.class);
        Persona p = new Persona(UUID.randomUUID());
        when(d.buscarPorId(p.getIdPersona())).thenReturn(p);
        PersonaModel m = new PersonaModel(d);
        m.setIdPersonaContexto(p.getIdPersona().toString());
        assertSame(p, m.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, m.getEstado());
    }

    @Test
    void nombreValidoEsAceptado() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        assertDoesNotThrow(() -> model.validarNombre(null, componente("nombres"), "José María"));
    }

    @Test
    void nombreConNumerosEsRechazado() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        assertThrows(ValidatorException.class,
                () -> model.validarNombre(contexto(), componente("nombres"), "Alexander123"));
    }

    @Test
    void apellidoValidoConTildeEsAceptado() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        assertDoesNotThrow(() -> model.validarNombre(null, componente("apellidos"), "Álvarez-López"));
    }

    @Test
    void fechaFuturaEsRechazada() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        Date futura = new Date(System.currentTimeMillis() + 86_400_000L);
        assertThrows(ValidatorException.class,
                () -> model.validarFechaNacimiento(contexto(), componente("fechaNacimiento"), futura));
    }

    @Test
    void fechasHistoricasDelParcialSonAceptadas() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        assertAll(
                () -> assertDoesNotThrow(() -> model.validarFechaNacimiento(contexto(), componente("fechaNacimiento"),
                        fecha("2005-05-19"))),
                () -> assertDoesNotThrow(() -> model.validarFechaNacimiento(contexto(), componente("fechaNacimiento"),
                        fecha("1990-12-31"))),
                () -> assertDoesNotThrow(() -> model.validarFechaNacimiento(contexto(), componente("fechaNacimiento"),
                        fecha("1960-01-01"))));
    }

    @Test
    void fechaDeHoyEsAceptada() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        assertDoesNotThrow(() -> model.validarFechaNacimiento(
                contexto(), componente("fechaNacimiento"), new Date()));
    }

    @Test
    void rangoDelCalendarioTerminaEnElAnioActual() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        assertEquals("1900:" + LocalDate.now().getYear(), model.getRangoAniosNacimiento());
    }

    @Test
    void fechaDelAnio0200EsRechazada() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        Date antigua = new Date(-55_853_280_000_000L);
        assertThrows(ValidatorException.class,
                () -> model.validarFechaNacimiento(contexto(), componente("fechaNacimiento"), antigua));
    }

    @Test
    void fechaValidaEsAceptada() {
        PersonaModel model = new PersonaModel(mock(PersonaDAO.class));
        assertDoesNotThrow(() -> model.validarFechaNacimiento(
                null, componente("fechaNacimiento"), new Date(946_684_800_000L)));
    }

    private UIComponent componente(String id) {
        UIComponent componente = mock(UIComponent.class);
        when(componente.getId()).thenReturn(id);
        return componente;
    }

    private static void completarPersona(Persona persona) {
        persona.setNombres("Ana");
        persona.setApellidos("López");
        persona.setFechaNacimiento(fechaValida());
    }

    private static Date fechaValida() {
        return new Date(946_684_800_000L);
    }

    private static Date fecha(String iso) {
        return Date.from(LocalDate.parse(iso).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void verificarUnicoError(FacesContext contexto, String clientId, String resumen) {
        verify(contexto).addMessage(eq(clientId), argThat(mensaje ->
                resumen.equals(mensaje.getSummary())));
        verify(contexto).validationFailed();
        verify(contexto, times(1)).addMessage(anyString(), any(FacesMessage.class));
    }

    private FacesContext contexto() {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                    {"persona.nombresFormato", "Formato invalido"},
                    {"persona.nombresRequeridos", "El nombre es requerido."},
                    {"persona.nombresMinimo", "Nombre demasiado corto"},
                    {"persona.nombresMaximo", "Nombre demasiado largo"},
                    {"persona.apellidosRequeridos", "El apellido es requerido."},
                    {"persona.apellidosMinimo", "Apellido demasiado corto"},
                    {"persona.apellidosMaximo", "Apellido demasiado largo"},
                    {"persona.apellidosFormato", "Formato de apellido invalido"},
                    {"persona.fechaNacimientoRequerida", "La fecha de nacimiento es requerida."},
                    {"persona.fechaNacimientoMinima", "Fecha antigua"},
                    {"persona.fechaNacimientoFutura", "Fecha futura"},
                    {"persona.creadaCorrectamente", "Persona creada correctamente."},
                    {"persona.actualizadaCorrectamente", "Persona actualizada correctamente."},
                    {"persona.errorGuardar", "No fue posible guardar la persona. Intente nuevamente."}
                };
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(bundle);
        return contexto;
    }
}
