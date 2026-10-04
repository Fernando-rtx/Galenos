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
    void opcionesDeDocumentoSoloIncluyenTiposActivosAlCrear() {
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        TipoDocumento activo = new TipoDocumento(UUID.randomUUID());
        activo.setActivo(true);
        TipoDocumento inactivo = new TipoDocumento(UUID.randomUUID());
        inactivo.setActivo(false);
        TipoDocumento sinEstado = new TipoDocumento(UUID.randomUUID());
        when(tipos.obtenerTodos()).thenReturn(List.of(activo, inactivo, sinEstado));
        DocumentoModel modelo = model(mock(DocumentoDAO.class), mock(PersonaDAO.class), tipos);
        modelo.nuevo();

        assertEquals(List.of(activo), modelo.getTiposDocumento());
    }

    @Test
    void edicionConservaElTipoInactivoYaAsignado() {
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        TipoDocumento activo = new TipoDocumento(UUID.randomUUID());
        activo.setActivo(true);
        TipoDocumento inactivo = new TipoDocumento(UUID.randomUUID());
        inactivo.setActivo(false);
        when(tipos.obtenerTodos()).thenReturn(List.of(activo, inactivo));
        Documento documento = new Documento();
        documento.setIdTipoDocumento(inactivo);
        DocumentoModel modelo = model(mock(DocumentoDAO.class), mock(PersonaDAO.class), tipos);
        modelo.seleccionar(documento);

        assertEquals(List.of(activo, inactivo), modelo.getTiposDocumento());
    }

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
        verify(d).actualizar(nuevo);
        assertNull(m.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, m.getEstado());
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
    void tipoActivoYValorValidoSeResuelvenDesdeElDaoYSeGuardan() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        TipoDocumento tipo = new TipoDocumento(UUID.randomUUID());
        tipo.setActivo(true);
        tipo.setExpresionRegular("^[0-9]{4}$");
        when(tipos.buscarPorId(tipo.getIdTipoDocumento())).thenReturn(tipo);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.nuevo();
        Documento documento = modelo.getSeleccionado();
        documento.setIdPersona(new Persona());
        documento.setIdTipoDocumento(new TipoDocumento(tipo.getIdTipoDocumento()));
        documento.setValor("1234");

        modelo.guardar();

        verify(dao).guardar(documento);
        assertSame(tipo, documento.getIdTipoDocumento());
    }

    @Test
    void alCrearRecargaLaColeccionDeLaPersonaDesdeElDao() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento tipo = new TipoDocumento(UUID.randomUUID());
        tipo.setActivo(true);
        tipo.setExpresionRegular("^PAS-[0-9]+$");
        when(tipos.buscarPorId(tipo.getIdTipoDocumento())).thenReturn(tipo);
        Documento documento = new Documento(UUID.randomUUID());
        documento.setIdPersona(persona);
        documento.setIdTipoDocumento(tipo);
        documento.setValor("PAS-123");
        when(dao.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList()))
                .thenReturn(List.of(), List.of(documento));
        when(dao.guardar(any(Documento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.nuevoParaPersona(persona);
        modelo.getSeleccionado().setIdTipoDocumento(tipo);
        modelo.getSeleccionado().setValor("PAS-123");

        modelo.guardar();

        assertEquals(List.of(documento), modelo.getDocumentosPorPersona(persona));
        assertNull(modelo.getSeleccionado(), "el formulario debe quedar limpio tras guardar");
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
        verify(dao, times(2)).obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList());
    }

    @Test
    void alEditarRecargaLaColeccionConElValorActualizado() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento tipo = new TipoDocumento(UUID.randomUUID());
        tipo.setActivo(true);
        tipo.setExpresionRegular("^[0-9]+$");
        when(tipos.buscarPorId(tipo.getIdTipoDocumento())).thenReturn(tipo);
        Documento documento = new Documento(UUID.randomUUID());
        documento.setIdPersona(persona);
        documento.setIdTipoDocumento(tipo);
        documento.setValor("123");
        when(dao.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList()))
                .thenReturn(List.of(documento), List.of(documento));
        when(dao.actualizar(documento)).thenReturn(documento);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        assertEquals(List.of(documento), modelo.getDocumentosPorPersona(persona));
        modelo.seleccionar(documento);
        documento.setValor("456");

        modelo.guardar();

        assertEquals("456", modelo.getDocumentosPorPersona(persona).get(0).getValor());
        assertNull(modelo.getSeleccionado());
        verify(dao, times(2)).obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList());
    }

    @Test
    void alLimpiarContextoLaSiguienteAperturaVuelveACargarDocumentos() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        Documento documentoExistente = new Documento(UUID.randomUUID());
        Documento documentoActualizado = new Documento(UUID.randomUUID());
        when(dao.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList()))
                .thenReturn(List.of(documentoExistente), List.of(documentoActualizado));
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));

        assertEquals(List.of(documentoExistente), modelo.getDocumentosPorPersona(persona));
        modelo.limpiarContexto();

        assertEquals(List.of(documentoActualizado), modelo.getDocumentosPorPersona(persona));
        verify(dao, times(2)).obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList());
    }

    @Test
    void tipoInactivoEnviadoDirectamenteSeRechazaAlCrear() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        TipoDocumento inactivo = new TipoDocumento(UUID.randomUUID());
        inactivo.setActivo(false);
        when(tipos.buscarPorId(inactivo.getIdTipoDocumento())).thenReturn(inactivo);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.nuevo();
        modelo.getSeleccionado().setIdPersona(new Persona());
        modelo.getSeleccionado().setIdTipoDocumento(inactivo);
        modelo.getSeleccionado().setValor("1234");

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void cambiarTipoDuranteEdicionSeIgnoraYLaMutacionDirectaSeRechaza() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        TipoDocumento original = new TipoDocumento(UUID.randomUUID());
        original.setActivo(true);
        original.setExpresionRegular("^[0-9]{4}$");
        TipoDocumento alternativo = new TipoDocumento(UUID.randomUUID());
        alternativo.setActivo(true);
        when(tipos.buscarPorId(original.getIdTipoDocumento())).thenReturn(original);
        when(tipos.buscarPorId(alternativo.getIdTipoDocumento())).thenReturn(alternativo);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        Documento documento = new Documento(UUID.randomUUID());
        documento.setIdPersona(new Persona());
        documento.setIdTipoDocumento(original);
        documento.setValor("1234");
        modelo.seleccionar(documento);

        modelo.setTipoDocumentoSeleccionadoId(alternativo.getIdTipoDocumento().toString());
        assertSame(original, documento.getIdTipoDocumento());
        documento.setIdTipoDocumento(alternativo);

        modelo.guardar();

        verify(dao, never()).actualizar(any());
        assertSame(original, documento.getIdTipoDocumento());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
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

    @Test
    void mismaPersonaNoPuedeCrearDeNuevoLaMismaCombinacionTipoYValor() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento dui = new TipoDocumento(UUID.randomUUID());
        Documento existente = new Documento(UUID.randomUUID());
        existente.setIdPersona(persona);
        existente.setIdTipoDocumento(dui);
        existente.setValor("06887861-1");
        dui.setActivo(true);
        dui.setExpresionRegular("^[0-9]{8}-[0-9]$");
        when(tipos.buscarPorId(dui.getIdTipoDocumento())).thenReturn(dui);
        when(dao.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList()))
                .thenReturn(List.of(existente));
        when(dao.existeDocumento(dui.getIdTipoDocumento(), "06887861-1", null)).thenReturn(true);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.nuevoParaPersona(persona);
        modelo.getSeleccionado().setIdTipoDocumento(dui);
        modelo.getSeleccionado().setValor("06887861-1");

        modelo.guardar();

        verify(dao, never()).guardar(any(Documento.class));
        verify(dao).existeDocumento(dui.getIdTipoDocumento(), "06887861-1", null);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
        assertEquals("06887861-1", modelo.getSeleccionado().getValor());
    }

    @Test
    void documentoDuplicadoGlobalSeRechazaAunqueSeaDeOtraPersona() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        Persona personaA = new Persona(UUID.randomUUID());
        Persona personaB = new Persona(UUID.randomUUID());
        TipoDocumento dui = new TipoDocumento(UUID.randomUUID());
        dui.setActivo(true);
        dui.setExpresionRegular("^[0-9]{8}-[0-9]$");
        when(tipos.buscarPorId(dui.getIdTipoDocumento())).thenReturn(dui);
        when(dao.existeDocumento(dui.getIdTipoDocumento(), "06887861-1", null)).thenReturn(true);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.nuevoParaPersona(personaB);
        modelo.getSeleccionado().setIdTipoDocumento(dui);
        modelo.getSeleccionado().setValor("06887861-1");

        modelo.guardar();

        verify(dao, never()).guardar(any(Documento.class));
        verify(dao).existeDocumento(dui.getIdTipoDocumento(), "06887861-1", null);
        assertEquals(personaB, modelo.getSeleccionado().getIdPersona());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
        assertNotEquals(personaA.getIdPersona(), personaB.getIdPersona());
    }

    @Test
    void mismoValorConTipoDiferenteSePermite() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento pasaporte = new TipoDocumento(UUID.randomUUID());
        pasaporte.setActivo(true);
        pasaporte.setExpresionRegular("^[0-9-]+$");
        when(tipos.buscarPorId(pasaporte.getIdTipoDocumento())).thenReturn(pasaporte);
        when(dao.existeDocumento(pasaporte.getIdTipoDocumento(), "12345678-9", null)).thenReturn(false);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.nuevoParaPersona(persona);
        Documento nuevo = modelo.getSeleccionado();
        nuevo.setIdTipoDocumento(pasaporte);
        nuevo.setValor("12345678-9");

        modelo.guardar();

        verify(dao).guardar(nuevo);
        verify(dao).existeDocumento(pasaporte.getIdTipoDocumento(), "12345678-9", null);
    }

    @Test
    void valorDiferenteDelMismoTipoSePermite() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento dui = new TipoDocumento(UUID.randomUUID());
        dui.setActivo(true);
        dui.setExpresionRegular("^[0-9]{8}-[0-9]$");
        when(tipos.buscarPorId(dui.getIdTipoDocumento())).thenReturn(dui);
        when(dao.existeDocumento(dui.getIdTipoDocumento(), "01234567-8", null)).thenReturn(false);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.nuevoParaPersona(persona);
        Documento nuevo = modelo.getSeleccionado();
        nuevo.setIdTipoDocumento(dui);
        nuevo.setValor("01234567-8");

        modelo.guardar();

        verify(dao).guardar(nuevo);
        verify(dao).existeDocumento(dui.getIdTipoDocumento(), "01234567-8", null);
    }

    @Test
    void editarDocumentoConservandoSuTipoNoSeConsideraDuplicado() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento dui = new TipoDocumento(UUID.randomUUID());
        dui.setActivo(true);
        dui.setExpresionRegular("^[0-9-]+$");
        Documento existente = new Documento(UUID.randomUUID());
        existente.setIdPersona(persona);
        existente.setIdTipoDocumento(dui);
        existente.setValor("01234567-8");
        when(dao.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList()))
                .thenReturn(List.of(existente));
        when(tipos.buscarPorId(dui.getIdTipoDocumento())).thenReturn(dui);
        when(dao.actualizar(existente)).thenReturn(existente);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.seleccionar(existente);

        modelo.guardar();

        verify(dao).actualizar(existente);
        verify(dao).existeDocumento(dui.getIdTipoDocumento(), "01234567-8", existente.getIdDocumento());
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void editarHaciaUnaCombinacionDeOtroDocumentoSeRechazaExcluyendoElActual() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        TipoDocumentoDAO tipos = mock(TipoDocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento dui = new TipoDocumento(UUID.randomUUID());
        dui.setActivo(true);
        dui.setExpresionRegular("^[0-9]{8}-[0-9]$");
        Documento documentoB = new Documento(UUID.randomUUID());
        documentoB.setIdPersona(persona);
        documentoB.setIdTipoDocumento(dui);
        documentoB.setValor("01234567-8");
        when(tipos.buscarPorId(dui.getIdTipoDocumento())).thenReturn(dui);
        when(dao.existeDocumento(dui.getIdTipoDocumento(), "06887861-1", documentoB.getIdDocumento()))
                .thenReturn(true);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), tipos);
        modelo.seleccionar(documentoB);
        documentoB.setValor("06887861-1");

        modelo.guardar();

        verify(dao).existeDocumento(dui.getIdTipoDocumento(), "06887861-1", documentoB.getIdDocumento());
        verify(dao, never()).actualizar(any(Documento.class));
        assertSame(documentoB, modelo.getSeleccionado());
        assertEquals("06887861-1", modelo.getSeleccionado().getValor());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }

    @Test
    void eliminaDocumentoSeleccionadoDeLaPersona() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        Documento documento = new Documento(UUID.randomUUID());
        documento.setIdPersona(persona);
        when(dao.eliminar(documento.getIdDocumento())).thenReturn(true);
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        modelo.seleccionar(documento);

        modelo.eliminarSeleccionadoParaPersona(persona);

        verify(dao).eliminar(documento.getIdDocumento());
        assertNull(modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void noEliminaDocumentoDeOtraPersona() {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        Documento documento = new Documento(UUID.randomUUID());
        documento.setIdPersona(new Persona(UUID.randomUUID()));
        DocumentoModel modelo = model(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        modelo.seleccionar(documento);

        modelo.eliminarSeleccionadoParaPersona(new Persona(UUID.randomUUID()));

        verify(dao, never()).eliminar(any());
        assertSame(documento, modelo.getSeleccionado());
    }
}
