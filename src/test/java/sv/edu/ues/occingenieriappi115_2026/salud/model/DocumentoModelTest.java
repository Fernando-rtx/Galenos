package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentoModelTest {

    @Test
    void guardarSinDocumentoSeleccionadoNoLlamaAlDao() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void tipoDeDocumentoAusenteImpideGuardarYConservaCreacion() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        modelo.nuevo();
        modelo.getSeleccionado().setIdPersona(new Persona());
        modelo.getSeleccionado().setValor("1234");

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void valorQueCumpleExpresionRegularSeGuardaRecortado() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        TipoDocumento tipo = new TipoDocumento();
        tipo.setExpresionRegular("[0-9]{4}");
        modelo.nuevo();
        Documento documento = modelo.getSeleccionado();
        documento.setIdPersona(new Persona());
        documento.setIdTipoDocumento(tipo);
        documento.setValor(" 1234 ");
        documento.setRutaFisica(" /documentos/1234.pdf ");

        modelo.guardar();

        verify(dao).guardar(documento);
        assertEquals("1234", documento.getValor());
        assertEquals("/documentos/1234.pdf", documento.getRutaFisica());
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void idDePersonaInvalidoDejaRelacionSinAsignar() {
        PersonaDAO personaDAO = mock(PersonaDAO.class);
        DocumentoModel modelo = model(mock(DocumentoDAO.class), personaDAO,
                mock(TipoDocumentoDAO.class));
        modelo.nuevo();

        modelo.setPersonaSeleccionadaId("id-invalido");

        assertNull(modelo.getSeleccionado().getIdPersona());
        verifyNoInteractions(personaDAO);
    }

    @Test
    void errorDelDaoAlGuardarConservaDocumentoYEstado() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        modelo.nuevo();
        Documento documento = modelo.getSeleccionado();
        documento.setIdPersona(new Persona());
        documento.setIdTipoDocumento(new TipoDocumento());
        documento.setValor("ABC");
        doThrow(new IllegalStateException("Error de persistencia")).when(dao).guardar(documento);

        assertThrows(IllegalStateException.class, modelo::guardar);

        assertSame(documento, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    private DocumentoModel model(DocumentoDAO d, PersonaDAO p, TipoDocumentoDAO t) {
        return new DocumentoModel(d, p, t);
    }

    @Test
    void nuevoCreaDocumento() {
        DocumentoModel m = model(mock(DocumentoDAO.class), mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        m.nuevo();
        assertNotNull(m.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, m.getEstado());
    }

    @Test
    void seleccionarYCancelar() {
        DocumentoModel m = model(mock(DocumentoDAO.class), mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        Documento d = new Documento();
        m.seleccionar(d);
        assertSame(d, m.getSeleccionado());
        m.cancelar();
        assertNull(m.getSeleccionado());
    }

    @Test
    void guardarCreacionYEdicion() {
        DocumentoDAO d = mock(DocumentoDAO.class);
        DocumentoModel m = model(d, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        m.nuevo();
        Documento nuevo = m.getSeleccionado();
        nuevo.setIdPersona(new Persona());
        nuevo.setIdTipoDocumento(new TipoDocumento());
        nuevo.setValor("ABC");
        m.guardar();
        verify(d).guardar(nuevo);
        Documento actualizado = new Documento();
        when(d.actualizar(nuevo)).thenReturn(actualizado);
        m.seleccionar(nuevo);
        m.guardar();
        assertSame(actualizado, m.getSeleccionado());
    }

    @Test
    void asignaRelacionesPorUuid() {
        PersonaDAO p = mock(PersonaDAO.class);
        TipoDocumentoDAO t = mock(TipoDocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento tipo = new TipoDocumento(UUID.randomUUID());
        when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);
        when(t.buscarPorId(tipo.getIdTipoDocumento())).thenReturn(tipo);
        DocumentoModel m = model(mock(DocumentoDAO.class), p, t);
        m.nuevo();
        m.setPersonaSeleccionadaId(persona.getIdPersona().toString());
        m.setTipoDocumentoSeleccionadoId(tipo.getIdTipoDocumento().toString());
        assertSame(persona, m.getSeleccionado().getIdPersona());
        assertSame(tipo, m.getSeleccionado().getIdTipoDocumento());
    }

    @Test
    void contextoPreseleccionaPersona() {
        PersonaDAO p = mock(PersonaDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);
        DocumentoModel m = model(mock(DocumentoDAO.class), p, mock(TipoDocumentoDAO.class));
        m.setIdPersonaContexto(persona.getIdPersona().toString());
        m.nuevo();
        assertSame(persona, m.getSeleccionado().getIdPersona());
        assertTrue(m.isConPersonaContexto());
    }

    @Test
    void relacionesObligatoriasImpidenGuardar() {
        DocumentoDAO d = mock(DocumentoDAO.class);
        DocumentoModel m = model(d, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        m.nuevo();
        m.getSeleccionado().setValor("ABC");
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void valorVacioImpideGuardar() {
        DocumentoDAO d = mock(DocumentoDAO.class);
        DocumentoModel m = model(d, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        m.nuevo();
        m.getSeleccionado().setIdPersona(new Persona());
        m.getSeleccionado().setIdTipoDocumento(new TipoDocumento());
        m.getSeleccionado().setValor("   ");
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void expresionRegularDelTipoRechazaValorInvalido() {
        DocumentoDAO d = mock(DocumentoDAO.class);
        DocumentoModel m = model(d, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        TipoDocumento tipo = new TipoDocumento();
        tipo.setExpresionRegular("[0-9]{4}");
        m.nuevo();
        m.getSeleccionado().setIdPersona(new Persona());
        m.getSeleccionado().setIdTipoDocumento(tipo);
        m.getSeleccionado().setValor("ABC");
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void personaContextualFiltraDocumentosYPreasignaLaRelacion() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        List<Documento> esperados = List.of(new Documento(UUID.randomUUID()));
        when(dao.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList()))
                .thenReturn(esperados);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));

        assertSame(esperados, modelo.getDocumentosPorPersona(persona));
        verify(dao).obtenerPagina(eq(0), eq(Integer.MAX_VALUE),
                eq(List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL,
                        persona.getIdPersona()))),
                eq(List.of()));

        modelo.nuevoParaPersona(persona);

        assertSame(persona, modelo.getSeleccionado().getIdPersona());
        assertTrue(modelo.isSeleccionadoParaPersona(persona));
    }
}
