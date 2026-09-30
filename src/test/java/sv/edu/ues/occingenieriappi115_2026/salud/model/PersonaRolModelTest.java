package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonaRolModelTest {

    @Test
    void guardarSinRelacionSeleccionadaNoLlamaAlDao() {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        PersonaRolModel modelo = model(dao, mock(PersonaDAO.class), mock(RolDAO.class),
                mock(ClinicaDAO.class));

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void relacionIncompletaEnEdicionNoActualizaNiCambiaEstado() {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        PersonaRolModel modelo = model(dao, mock(PersonaDAO.class), mock(RolDAO.class),
                mock(ClinicaDAO.class));
        PersonaRol relacion = new PersonaRol();
        relacion.setIdPersona(new Persona());
        relacion.setIdRol(new Rol());
        modelo.seleccionar(relacion);

        modelo.guardar();

        verifyNoInteractions(dao);
        assertSame(relacion, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }

    @Test
    void cargaPersonasRolesYClinicasUnaSolaVez() {
        PersonaDAO personaDAO = mock(PersonaDAO.class);
        RolDAO rolDAO = mock(RolDAO.class);
        ClinicaDAO clinicaDAO = mock(ClinicaDAO.class);
        java.util.List<Persona> personas = java.util.List.of(new Persona());
        java.util.List<Rol> roles = java.util.List.of(new Rol());
        java.util.List<Clinica> clinicas = java.util.List.of(new Clinica());
        when(personaDAO.obtenerTodos()).thenReturn(personas);
        when(rolDAO.obtenerTodos()).thenReturn(roles);
        when(clinicaDAO.obtenerTodos()).thenReturn(clinicas);
        PersonaRolModel modelo = model(mock(PersonaRolDAO.class), personaDAO, rolDAO, clinicaDAO);

        assertSame(personas, modelo.getPersonas());
        assertSame(personas, modelo.getPersonas());
        assertSame(roles, modelo.getRoles());
        assertSame(roles, modelo.getRoles());
        assertSame(clinicas, modelo.getClinicas());
        assertSame(clinicas, modelo.getClinicas());
        verify(personaDAO).obtenerTodos();
        verify(rolDAO).obtenerTodos();
        verify(clinicaDAO).obtenerTodos();
    }

    @Test
    void idDeRolInvalidoNoAsignaRelacion() {
        RolDAO rolDAO = mock(RolDAO.class);
        PersonaRolModel modelo = model(mock(PersonaRolDAO.class), mock(PersonaDAO.class),
                rolDAO, mock(ClinicaDAO.class));
        modelo.nuevo();

        modelo.setRolSeleccionadoId("id-invalido");

        assertNull(modelo.getSeleccionado().getIdRol());
        verifyNoInteractions(rolDAO);
    }

    @Test
    void idDeClinicaInvalidoNoAsignaRelacion() {
        ClinicaDAO clinicaDAO = mock(ClinicaDAO.class);
        PersonaRolModel modelo = model(mock(PersonaRolDAO.class), mock(PersonaDAO.class),
                mock(RolDAO.class), clinicaDAO);
        modelo.nuevo();

        modelo.setClinicaSeleccionadaId("id-invalido");

        assertNull(modelo.getSeleccionado().getIdClinica());
        verifyNoInteractions(clinicaDAO);
    }

    @Test
    void errorDelDaoAlGuardarConservaRelacionYEstado() {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        ClinicaDAO clinicaDAO = mock(ClinicaDAO.class);
        PersonaRolModel modelo = model(dao, mock(PersonaDAO.class), mock(RolDAO.class),
                clinicaDAO);
        modelo.nuevo();
        PersonaRol relacion = modelo.getSeleccionado();
        relacion.setIdPersona(new Persona());
        relacion.setIdRol(new Rol());
        Clinica clinica = new Clinica(UUID.randomUUID(), "Centro de salud");
        clinica.setActivo(true);
        relacion.setIdClinica(clinica);
        when(clinicaDAO.buscarPorId(clinica.getIdClinica())).thenReturn(clinica);
        doThrow(new IllegalStateException("Error de persistencia")).when(dao).guardar(relacion);

        assertThrows(IllegalStateException.class, modelo::guardar);

        assertSame(relacion, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    private PersonaRolModel model(PersonaRolDAO d, PersonaDAO p, RolDAO r, ClinicaDAO c) {
        return new PersonaRolModel(d, p, r, c);
    }

    @Test
    void nuevoInicializaFecha() {
        PersonaRolModel m = model(mock(PersonaRolDAO.class), mock(PersonaDAO.class), mock(RolDAO.class), mock(ClinicaDAO.class));
        m.nuevo();
        assertNotNull(m.getSeleccionado().getFechaCreacion());
        assertEquals(ESTADO_CRUD.CREACION, m.getEstado());
    }

    @Test
    void seleccionarYCancelar() {
        PersonaRolModel m = model(mock(PersonaRolDAO.class), mock(PersonaDAO.class), mock(RolDAO.class), mock(ClinicaDAO.class));
        PersonaRol pr = new PersonaRol();
        m.seleccionar(pr);
        assertSame(pr, m.getSeleccionado());
        m.cancelar();
        assertNull(m.getSeleccionado());
    }

    @Test
    void guardarCreacionYEdicion() {
        PersonaRolDAO d = mock(PersonaRolDAO.class);
        ClinicaDAO clinicaDAO = mock(ClinicaDAO.class);
        PersonaRolModel m = model(d, mock(PersonaDAO.class), mock(RolDAO.class), clinicaDAO);
        m.nuevo();
        PersonaRol n = m.getSeleccionado();
        n.setIdPersona(new Persona());
        n.setIdRol(new Rol());
        Clinica clinica = new Clinica(UUID.randomUUID(), "Clínica central");
        clinica.setActivo(true);
        n.setIdClinica(clinica);
        when(clinicaDAO.buscarPorId(clinica.getIdClinica())).thenReturn(clinica);
        m.guardar();
        verify(d).guardar(n);
        PersonaRol a = new PersonaRol();
        when(d.actualizar(n)).thenReturn(a);
        m.seleccionar(n);
        m.guardar();
        assertSame(a, m.getSeleccionado());
    }

    @Test
    void asignaTresRelaciones() {
        PersonaDAO p = mock(PersonaDAO.class);
        RolDAO r = mock(RolDAO.class);
        ClinicaDAO c = mock(ClinicaDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        Rol rol = new Rol(UUID.randomUUID());
        Clinica clinica = new Clinica(UUID.randomUUID());
        when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);
        when(r.buscarPorId(rol.getIdRol())).thenReturn(rol);
        when(c.buscarPorId(clinica.getIdClinica())).thenReturn(clinica);
        PersonaRolModel m = model(mock(PersonaRolDAO.class), p, r, c);
        m.nuevo();
        m.setPersonaSeleccionadaId(persona.getIdPersona().toString());
        m.setRolSeleccionadoId(rol.getIdRol().toString());
        m.setClinicaSeleccionadaId(clinica.getIdClinica().toString());
        assertSame(persona, m.getSeleccionado().getIdPersona());
        assertSame(rol, m.getSeleccionado().getIdRol());
        assertSame(clinica, m.getSeleccionado().getIdClinica());
    }

    @Test
    void contextoPreselecciona() {
        PersonaDAO p = mock(PersonaDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);
        PersonaRolModel m = model(mock(PersonaRolDAO.class), p, mock(RolDAO.class), mock(ClinicaDAO.class));
        m.setIdPersonaContexto(persona.getIdPersona().toString());
        m.nuevo();
        assertSame(persona, m.getSeleccionado().getIdPersona());
    }

    @Test
    void personaNullImpideGuardar() {
        PersonaRolDAO d = mock(PersonaRolDAO.class);
        PersonaRolModel m = model(d, mock(PersonaDAO.class), mock(RolDAO.class), mock(ClinicaDAO.class));
        m.nuevo();
        m.getSeleccionado().setIdRol(new Rol());
        m.getSeleccionado().setIdClinica(new Clinica());
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void rolNullImpideGuardar() {
        PersonaRolDAO d = mock(PersonaRolDAO.class);
        PersonaRolModel m = model(d, mock(PersonaDAO.class), mock(RolDAO.class), mock(ClinicaDAO.class));
        m.nuevo();
        m.getSeleccionado().setIdPersona(new Persona());
        m.getSeleccionado().setIdClinica(new Clinica());
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void clinicaNullImpideGuardar() {
        PersonaRolDAO d = mock(PersonaRolDAO.class);
        PersonaRolModel m = model(d, mock(PersonaDAO.class), mock(RolDAO.class), mock(ClinicaDAO.class));
        m.nuevo();
        m.getSeleccionado().setIdPersona(new Persona());
        m.getSeleccionado().setIdRol(new Rol());
        m.guardar();
        verifyNoInteractions(d);
    }

    @Test
    void clinicasInactivasNoEstanDisponiblesEnUnaAsignacionNueva() {
        Clinica activa = new Clinica(UUID.randomUUID(), "Clínica activa");
        activa.setActivo(true);
        Clinica inactiva = new Clinica(UUID.randomUUID(), "Clínica inactiva");
        inactiva.setActivo(false);
        ClinicaDAO clinicaDAO = mock(ClinicaDAO.class);
        when(clinicaDAO.obtenerTodos()).thenReturn(List.of(activa, inactiva));
        PersonaRolModel modelo = model(mock(PersonaRolDAO.class), mock(PersonaDAO.class),
                mock(RolDAO.class), clinicaDAO);
        modelo.nuevo();

        assertEquals(List.of(activa), modelo.getClinicasDisponibles());
    }

    @Test
    void rechazaAsignarUnaClinicaInactivaNueva() {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        ClinicaDAO clinicaDAO = mock(ClinicaDAO.class);
        PersonaRolModel modelo = model(dao, mock(PersonaDAO.class), mock(RolDAO.class), clinicaDAO);
        modelo.nuevo();
        Clinica inactiva = new Clinica(UUID.randomUUID(), "Clínica inactiva");
        inactiva.setActivo(false);
        modelo.getSeleccionado().setIdPersona(new Persona(UUID.randomUUID()));
        modelo.getSeleccionado().setIdRol(new Rol(UUID.randomUUID()));
        modelo.getSeleccionado().setIdClinica(inactiva);
        when(clinicaDAO.buscarPorId(inactiva.getIdClinica())).thenReturn(inactiva);

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test
    void conservaLaClinicaInactivaDeLaAsignacionActualAlEditar() {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        ClinicaDAO clinicaDAO = mock(ClinicaDAO.class);
        Clinica inactiva = new Clinica(UUID.randomUUID(), "Clínica anterior");
        inactiva.setActivo(false);
        Clinica activa = new Clinica(UUID.randomUUID(), "Clínica activa");
        activa.setActivo(true);
        when(clinicaDAO.obtenerTodos()).thenReturn(List.of(activa, inactiva));
        when(clinicaDAO.buscarPorId(inactiva.getIdClinica())).thenReturn(inactiva);
        PersonaRol relacion = new PersonaRol(UUID.randomUUID());
        relacion.setIdPersona(new Persona(UUID.randomUUID()));
        relacion.setIdRol(new Rol(UUID.randomUUID()));
        relacion.setIdClinica(inactiva);
        when(dao.actualizar(relacion)).thenReturn(relacion);
        PersonaRolModel modelo = model(dao, mock(PersonaDAO.class), mock(RolDAO.class), clinicaDAO);
        modelo.seleccionar(relacion);

        assertEquals(List.of(activa, inactiva), modelo.getClinicasDisponibles());
        modelo.guardar();

        verify(dao).actualizar(relacion);
    }

    @Test
    void personaContextualFiltraRolesYPreasignaLaRelacion() {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        List<PersonaRol> esperados = List.of(new PersonaRol(UUID.randomUUID()));
        when(dao.obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList()))
                .thenReturn(esperados);
        PersonaRolModel modelo = model(dao, mock(PersonaDAO.class), mock(RolDAO.class),
                mock(ClinicaDAO.class));

        assertSame(esperados, modelo.getRolesPorPersona(persona));
        verify(dao).obtenerPagina(eq(0), eq(Integer.MAX_VALUE),
                eq(List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL,
                        persona.getIdPersona()))),
                eq(List.of()));

        modelo.nuevoParaPersona(persona);

        assertSame(persona, modelo.getSeleccionado().getIdPersona());
        assertTrue(modelo.isSeleccionadoParaPersona(persona));
    }

    @Test
    void eliminaRolSeleccionadoDeLaPersona() {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        PersonaRol personaRol = new PersonaRol(UUID.randomUUID());
        personaRol.setIdPersona(persona);
        when(dao.eliminar(personaRol.getIdPersonaRol())).thenReturn(true);
        PersonaRolModel modelo = model(dao, mock(PersonaDAO.class), mock(RolDAO.class),
                mock(ClinicaDAO.class));
        modelo.seleccionar(personaRol);

        modelo.eliminarSeleccionadoParaPersona(persona);

        verify(dao).eliminar(personaRol.getIdPersonaRol());
        assertNull(modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test
    void errorAlEliminarRolEnUsoConservaLaSeleccion() {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        PersonaRol personaRol = new PersonaRol(UUID.randomUUID());
        personaRol.setIdPersona(persona);
        doThrow(new IllegalStateException("Rol en uso"))
                .when(dao).eliminar(personaRol.getIdPersonaRol());
        PersonaRolModel modelo = model(dao, mock(PersonaDAO.class), mock(RolDAO.class),
                mock(ClinicaDAO.class));
        modelo.seleccionar(personaRol);

        modelo.eliminarSeleccionadoParaPersona(persona);

        assertSame(personaRol, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, modelo.getEstado());
    }
}
