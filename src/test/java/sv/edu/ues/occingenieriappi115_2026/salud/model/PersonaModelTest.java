package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import java.util.Date;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;
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

        modelo.guardar();

        assertEquals("Ana", persona.getNombres());
        assertEquals("López", persona.getApellidos());
        verify(dao).guardar(persona);
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void errorDelDaoAlGuardarConservaEstadoDeCreacion() {
        PersonaDAO dao = mock(PersonaDAO.class);
        PersonaModel modelo = new PersonaModel(dao);
        modelo.nuevo();
        Persona persona = modelo.getSeleccionado();
        doThrow(new IllegalStateException("Error de persistencia")).when(dao).guardar(persona);

        assertThrows(IllegalStateException.class, modelo::guardar);

        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
        assertSame(persona, modelo.getSeleccionado());
    }

    @Test
    void nombreNuloEsRechazado() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        assertThrows(ValidatorException.class,
                () -> modelo.validarNombre(contexto(), componente("nombres"), null));
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
    void fechaNulaEsRechazada() {
        PersonaModel modelo = new PersonaModel(mock(PersonaDAO.class));
        assertThrows(ValidatorException.class,
                () -> modelo.validarFechaNacimiento(contexto(), componente("fechaNacimiento"), null));
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
    void seleccionarEdita() {
        PersonaModel m = new PersonaModel(mock(PersonaDAO.class));
        Persona p = new Persona();
        m.seleccionar(p);
        assertSame(p, m.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, m.getEstado());
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
        m.guardar();
        assertNotNull(m.getSeleccionado().getIdPersona());
        verify(d).guardar(m.getSeleccionado());
    }

    @Test
    void guardarEdicionUsaResultado() {
        PersonaDAO d = mock(PersonaDAO.class);
        Persona a = new Persona(), b = new Persona();
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

    private FacesContext contexto() {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                    {"persona.nombresFormato", "Formato invalido"},
                    {"persona.nombresRequeridos", "Nombre requerido"},
                    {"persona.nombresMinimo", "Nombre demasiado corto"},
                    {"persona.nombresMaximo", "Nombre demasiado largo"},
                    {"persona.fechaNacimientoRequerida", "Fecha requerida"},
                    {"persona.fechaNacimientoMinima", "Fecha antigua"},
                    {"persona.fechaNacimientoFutura", "Fecha futura"}
                };
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(bundle);
        return contexto;
    }
}
