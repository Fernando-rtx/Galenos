package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedioContactoModelTest {

    @Test
    void guardarSinContactoSeleccionadoNoLlamaAlDao() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class),
                mock(TipoMedioContactoDAO.class));

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void personaAusenteImpideGuardarYConservaCreacion() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class),
                mock(TipoMedioContactoDAO.class));
        modelo.nuevo();
        modelo.getSeleccionado().setIdTipoMedioContacto(new TipoMedioContacto());
        modelo.getSeleccionado().setValor("12345678");

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void valorQueCumpleExpresionRegularSeGuardaRecortado() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        TipoMedioContacto tipo = tipoActivo(tipoDAO);
        tipo.setExpresionRegular("[0-9]{8}");
        modelo.nuevo();
        MedioContacto contacto = modelo.getSeleccionado();
        contacto.setIdPersona(new Persona());
        contacto.setIdTipoMedioContacto(tipo);
        contacto.setValor(" 12345678 ");

        modelo.guardar();

        verify(dao).guardar(contacto);
        assertEquals("12345678", contacto.getValor());
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
        assertNull(modelo.getSeleccionado());
    }

    @Test
    void correoValidoCumpleFormatoPosixGuardadoEnBase() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        TipoMedioContacto tipo = tipoActivo(tipoDAO);
        tipo.setExpresionRegular("^[^@[:space:]]+@[^@[:space:]]+[.][^@[:space:]]+$");
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        modelo.nuevo();
        MedioContacto contacto = modelo.getSeleccionado();
        contacto.setIdPersona(new Persona());
        contacto.setIdTipoMedioContacto(tipo);
        contacto.setValor("rubenvenabidez@gmail.com");

        modelo.guardar();

        verify(dao).guardar(contacto);
    }

    @Test
    void correoSinArrobaSigueSiendoRechazado() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        TipoMedioContacto tipo = tipoActivo(tipoDAO);
        tipo.setExpresionRegular("^[^@[:space:]]+@[^@[:space:]]+[.][^@[:space:]]+$");
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        modelo.nuevo();
        MedioContacto contacto = modelo.getSeleccionado();
        contacto.setIdPersona(new Persona());
        contacto.setIdTipoMedioContacto(tipo);
        contacto.setValor("rubengmail.com");

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void idDeTipoInvalidoDejaRelacionSinAsignar() {
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        MedioContactoModel modelo = model(mock(MedioContactoDAO.class),
                mock(PersonaDAO.class), tipoDAO);
        modelo.nuevo();

        modelo.setTipoMedioSeleccionadoId("id-invalido");

        assertNull(modelo.getSeleccionado().getIdTipoMedioContacto());
        verifyNoInteractions(tipoDAO);
    }

    @Test
    void errorDelDaoAlGuardarConservaContactoYEstado() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        modelo.nuevo();
        MedioContacto contacto = modelo.getSeleccionado();
        contacto.setIdPersona(new Persona());
        contacto.setIdTipoMedioContacto(tipoActivo(tipoDAO));
        contacto.setValor("contacto");
        doThrow(new IllegalStateException("Error de persistencia")).when(dao).guardar(contacto);

        assertThrows(IllegalStateException.class, modelo::guardar);

        assertSame(contacto, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    private MedioContactoModel model(MedioContactoDAO d, PersonaDAO p, TipoMedioContactoDAO t) {
        return new MedioContactoModel(d, p, t);
    }

    private MedioContactoModel nuevaAsignacion(MedioContactoDAO dao,
            Persona persona, TipoMedioContacto tipo, String valor) {
        PersonaDAO personaDAO = mock(PersonaDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        when(personaDAO.buscarPorId(persona.getIdPersona())).thenReturn(persona);
        when(tipoDAO.buscarPorId(tipo.getIdTipoMedioContacto())).thenReturn(tipo);
        MedioContactoModel modelo = model(dao, personaDAO, tipoDAO);
        modelo.nuevo();
        modelo.getSeleccionado().setIdPersona(persona);
        modelo.getSeleccionado().setIdTipoMedioContacto(tipo);
        modelo.getSeleccionado().setValor(valor);
        return modelo;
    }

    @Test
    void rechazaMismoTipoValorYPersonaDuplicados() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        tipo.setActivo(true);
        when(dao.existeContacto(persona.getIdPersona(), tipo.getIdTipoMedioContacto(), "72105157", null))
                .thenReturn(true);
        MedioContactoModel modelo = nuevaAsignacion(dao, persona, tipo, "72105157");

        modelo.guardar();

        verify(dao).existeContacto(persona.getIdPersona(), tipo.getIdTipoMedioContacto(), "72105157", null);
        verify(dao, never()).guardar(any());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
        assertEquals("72105157", modelo.getSeleccionado().getValor());
    }

    @Test
    void permiteMismoTipoYPersonaConValorDiferente() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        tipo.setActivo(true);
        MedioContactoModel modelo = nuevaAsignacion(dao, persona, tipo, "77778888");
        MedioContacto contacto = modelo.getSeleccionado();

        modelo.guardar();

        verify(dao).existeContacto(persona.getIdPersona(), tipo.getIdTipoMedioContacto(), "77778888", null);
        verify(dao).guardar(contacto);
    }

    @Test
    void permiteTipoDiferenteConValorValidoParaLaMismaPersona() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto tipoCorreo = new TipoMedioContacto(UUID.randomUUID());
        tipoCorreo.setActivo(true);
        tipoCorreo.setExpresionRegular("^[^@]+@[^@]+\\.[^@]+$");
        MedioContactoModel modelo = nuevaAsignacion(dao, persona, tipoCorreo, "correo1@gmail.com");
        MedioContacto contacto = modelo.getSeleccionado();

        modelo.guardar();

        verify(dao).existeContacto(persona.getIdPersona(), tipoCorreo.getIdTipoMedioContacto(),
                "correo1@gmail.com", null);
        verify(dao).guardar(contacto);
    }

    @Test
    void editarContactoRechazaValorQueYaUsaOtroRegistro() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto tipo = tipoActivo(tipoDAO);
        MedioContacto existente = new MedioContacto(UUID.randomUUID());
        existente.setIdPersona(persona);
        existente.setIdTipoMedioContacto(tipo);
        existente.setValor("72105157");
        MedioContacto editado = new MedioContacto(UUID.randomUUID());
        editado.setIdPersona(persona);
        editado.setIdTipoMedioContacto(tipo);
        editado.setValor("72105158");
        when(dao.buscarPorId(editado.getIdMedioContacto())).thenReturn(editado);
        when(dao.existeContacto(persona.getIdPersona(), tipo.getIdTipoMedioContacto(),
                "72105157", editado.getIdMedioContacto())).thenReturn(true);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        modelo.seleccionar(editado);
        editado.setValor("72105157");

        modelo.guardar();

        verify(dao).existeContacto(persona.getIdPersona(), tipo.getIdTipoMedioContacto(),
                "72105157", editado.getIdMedioContacto());
        verify(dao, never()).actualizar(any());
        assertSame(editado, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }

    @Test
    void editarContactoPermiteConservarSuPropioValor() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto tipo = tipoActivo(tipoDAO);
        MedioContacto editado = new MedioContacto(UUID.randomUUID());
        editado.setIdPersona(persona);
        editado.setIdTipoMedioContacto(tipo);
        editado.setValor("72105157");
        when(dao.buscarPorId(editado.getIdMedioContacto())).thenReturn(editado);
        when(dao.existeContacto(persona.getIdPersona(), tipo.getIdTipoMedioContacto(),
                "72105157", editado.getIdMedioContacto())).thenReturn(false);
        when(dao.actualizar(editado)).thenReturn(editado);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        modelo.seleccionar(editado);

        modelo.guardar();

        verify(dao).existeContacto(persona.getIdPersona(), tipo.getIdTipoMedioContacto(),
                "72105157", editado.getIdMedioContacto());
        verify(dao).actualizar(editado);
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void nuevoInicializaFecha() {
        MedioContactoModel m = model(mock(MedioContactoDAO.class), mock(PersonaDAO.class), mock(TipoMedioContactoDAO.class));
        m.nuevo();
        assertNotNull(m.getSeleccionado().getFechaCreacion());
        assertEquals(ESTADO_CRUD.CREACION, m.getEstado());
    }

    @Test
    void seleccionarYCancelar() {
        MedioContactoModel m = model(mock(MedioContactoDAO.class), mock(PersonaDAO.class), mock(TipoMedioContactoDAO.class));
        MedioContacto c = new MedioContacto();
        m.seleccionar(c);
        assertSame(c, m.getSeleccionado());
        m.cancelar();
        assertNull(m.getSeleccionado());
    }

    @Test
    void guardarCreacionYEdicion() {
        MedioContactoDAO d = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        MedioContactoModel m = model(d, mock(PersonaDAO.class), tipoDAO);
        m.nuevo();
        MedioContacto n = m.getSeleccionado();
        n.setIdPersona(new Persona());
        n.setIdTipoMedioContacto(tipoActivo(tipoDAO));
        n.setValor("contacto");
        m.guardar();
        verify(d).guardar(n);
        n.setIdMedioContacto(UUID.randomUUID());
        when(d.buscarPorId(n.getIdMedioContacto())).thenReturn(n);
        MedioContacto a = new MedioContacto();
        when(d.actualizar(n)).thenReturn(a);
        m.seleccionar(n);
        m.guardar();
        verify(d).actualizar(n);
        assertNull(m.getSeleccionado());
    }

    private TipoMedioContacto tipoActivo(TipoMedioContactoDAO dao) {
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        tipo.setActivo(true);
        when(dao.buscarPorId(tipo.getIdTipoMedioContacto())).thenReturn(tipo);
        return tipo;
    }

    @Test
    void tipoInactivoNoSePuedeAsignarAlCrear() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        tipo.setActivo(false);
        when(tipoDAO.buscarPorId(tipo.getIdTipoMedioContacto())).thenReturn(tipo);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        modelo.nuevo();
        modelo.getSeleccionado().setIdPersona(new Persona());
        modelo.getSeleccionado().setIdTipoMedioContacto(tipo);
        modelo.getSeleccionado().setValor("12345678");

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void expresionRegularMalConfiguradaRechazaElContacto() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        TipoMedioContacto tipo = tipoActivo(tipoDAO);
        tipo.setExpresionRegular("[0-9");
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        modelo.nuevo();
        modelo.getSeleccionado().setIdPersona(new Persona());
        modelo.getSeleccionado().setIdTipoMedioContacto(tipo);
        modelo.getSeleccionado().setValor("12345678");

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void tipoDeContactoNoSePuedeCambiarDuranteEdicion() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        TipoMedioContactoDAO tipoDAO = mock(TipoMedioContactoDAO.class);
        TipoMedioContacto original = tipoActivo(tipoDAO);
        TipoMedioContacto nuevoTipo = tipoActivo(tipoDAO);
        MedioContacto contacto = new MedioContacto(UUID.randomUUID());
        contacto.setIdPersona(new Persona());
        contacto.setIdTipoMedioContacto(original);
        contacto.setValor("actualizado");
        when(dao.buscarPorId(contacto.getIdMedioContacto())).thenReturn(contacto);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class), tipoDAO);
        modelo.seleccionar(contacto);

        modelo.setTipoMedioSeleccionadoId(nuevoTipo.getIdTipoMedioContacto().toString());
        assertSame(original, contacto.getIdTipoMedioContacto());
        contacto.setIdTipoMedioContacto(nuevoTipo);
        modelo.guardar();

        assertSame(original, contacto.getIdTipoMedioContacto());
        verify(dao, never()).actualizar(any());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }

    @Test
    void asignaRelaciones() {
        PersonaDAO p = mock(PersonaDAO.class);
        TipoMedioContactoDAO t = mock(TipoMedioContactoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);
        when(t.buscarPorId(tipo.getIdTipoMedioContacto())).thenReturn(tipo);
        MedioContactoModel m = model(mock(MedioContactoDAO.class), p, t);
        m.nuevo();
        m.setPersonaSeleccionadaId(persona.getIdPersona().toString());
        m.setTipoMedioSeleccionadoId(tipo.getIdTipoMedioContacto().toString());
        assertSame(persona, m.getSeleccionado().getIdPersona());
        assertSame(tipo, m.getSeleccionado().getIdTipoMedioContacto());
    }

    @Test
    void contextoPreselecciona() {
        PersonaDAO p = mock(PersonaDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);
        MedioContactoModel m = model(mock(MedioContactoDAO.class), p, mock(TipoMedioContactoDAO.class));
        m.setIdPersonaContexto(persona.getIdPersona().toString());
        m.nuevo();
        assertSame(persona, m.getSeleccionado().getIdPersona());
    }

    @Test
    void relacionesObligatoriasImpidenGuardar() {
        MedioContactoDAO d = mock(MedioContactoDAO.class);
        MedioContactoModel m = model(d, mock(PersonaDAO.class), mock(TipoMedioContactoDAO.class));
        m.nuevo();
        m.getSeleccionado().setValor("contacto");
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void valorVacioImpideGuardar() {
        MedioContactoDAO d = mock(MedioContactoDAO.class);
        MedioContactoModel m = model(d, mock(PersonaDAO.class), mock(TipoMedioContactoDAO.class));
        m.nuevo();
        m.getSeleccionado().setIdPersona(new Persona());
        m.getSeleccionado().setIdTipoMedioContacto(new TipoMedioContacto());
        m.getSeleccionado().setValor("   ");
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void expresionRegularDelTipoRechazaValorInvalido() {
        MedioContactoDAO d = mock(MedioContactoDAO.class);
        MedioContactoModel m = model(d, mock(PersonaDAO.class), mock(TipoMedioContactoDAO.class));
        TipoMedioContacto tipo = new TipoMedioContacto();
        tipo.setExpresionRegular("[0-9]{8}");
        m.nuevo();
        m.getSeleccionado().setIdPersona(new Persona());
        m.getSeleccionado().setIdTipoMedioContacto(tipo);
        m.getSeleccionado().setValor("correo@example.com");
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void personaContextualFiltraContactosYPreasignaLaRelacion() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        List<MedioContacto> esperados = List.of(new MedioContacto(UUID.randomUUID()));
        when(dao.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList()))
                .thenReturn(esperados);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class),
                mock(TipoMedioContactoDAO.class));

        assertSame(esperados, modelo.getContactosPorPersona(persona));
        verify(dao).obtenerPagina(eq(0), eq(Integer.MAX_VALUE),
                eq(List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL,
                        persona.getIdPersona()))),
                eq(List.of()));

        modelo.nuevoParaPersona(persona);

        assertSame(persona, modelo.getSeleccionado().getIdPersona());
        assertTrue(modelo.isSeleccionadoParaPersona(persona));
    }

    @Test
    void eliminaContactoSeleccionadoDeLaPersona() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        MedioContacto contacto = new MedioContacto(UUID.randomUUID());
        contacto.setIdPersona(persona);
        when(dao.eliminar(contacto.getIdMedioContacto())).thenReturn(true);
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class),
                mock(TipoMedioContactoDAO.class));
        modelo.seleccionar(contacto);

        modelo.eliminarSeleccionadoParaPersona(persona);

        verify(dao).eliminar(contacto.getIdMedioContacto());
        assertNull(modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void noEliminaContactoDeOtraPersona() {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        MedioContacto contacto = new MedioContacto(UUID.randomUUID());
        contacto.setIdPersona(new Persona(UUID.randomUUID()));
        MedioContactoModel modelo = model(dao, mock(PersonaDAO.class),
                mock(TipoMedioContactoDAO.class));
        modelo.seleccionar(contacto);

        modelo.eliminarSeleccionadoParaPersona(new Persona(UUID.randomUUID()));

        verify(dao, never()).eliminar(any());
        assertSame(contacto, modelo.getSeleccionado());
    }
}
