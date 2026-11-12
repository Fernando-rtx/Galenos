package sv.edu.ues.occingenieriappi115_2026.salud.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

class IdentidadAcademicaServiceTest {
  IdentidadAcademicaService s;
  Persona persona;
  TipoDocumento tipo;
  Rol paciente, empleado;
  Clinica clinica;
  PersonaRol pr;

  @BeforeEach
  void setup() {
    s = new IdentidadAcademicaService();
    s.tipos = mock(TipoDocumentoDAO.class);
    s.documentos = mock(DocumentoDAO.class);
    s.personas = mock(PersonaDAO.class);
    s.asignaciones = mock(PersonaRolDAO.class);
    s.roles = mock(RolDAO.class);
    s.sesiones = new SesionAcademicaService();
    persona = new Persona(UUID.randomUUID());
    persona.setNombres("Ana");
    persona.setApellidos("Ficticia");
    tipo = new TipoDocumento(UUID.randomUUID());
    tipo.setActivo(true);
    tipo.setExpresionRegular("^[A-Z]{3}$");
    paciente = new Rol(UUID.randomUUID());
    paciente.setNombre("Paciente");
    paciente.setActivo(true);
    empleado = new Rol(UUID.randomUUID());
    empleado.setNombre("Recepción");
    empleado.setActivo(true);
    clinica = new Clinica(UUID.randomUUID());
    clinica.setNombre("Clínica sintética");
    clinica.setActivo(true);
    pr = asignacion(paciente, clinica);
    var d = new Documento(UUID.randomUUID());
    d.setIdPersona(persona);
    when(s.tipos.buscarPorId(tipo.getIdTipoDocumento())).thenReturn(tipo);
    when(s.documentos.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
        .thenReturn(List.of(d));
    when(s.personas.buscarPorId(persona.getIdPersona())).thenReturn(persona);
    when(s.asignaciones.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
        .thenReturn(List.of(pr));
    s.rolesConfigurados = empleado.getIdRol().toString();
    when(s.roles.buscarPorId(empleado.getIdRol())).thenReturn(empleado);
  }

  PersonaRol asignacion(Rol rol, Clinica c) {
    var a = new PersonaRol(UUID.randomUUID());
    a.setIdPersona(persona);
    a.setIdRol(rol);
    a.setIdClinica(c);
    return a;
  }

  void error(int status, Runnable call) {
    assertEquals(status, assertThrows(IdentificacionException.class, call::run).getStatus());
  }

  IdentidadAcademicaService.Identificacion identificar(String portal) {
    return s.identificar(tipo.getIdTipoDocumento(), "ABC", portal, "America/El_Salvador");
  }

  @Test
  void pacienteValidoYContextoMinimo() {
    var r = identificar("PACIENTE");
    assertEquals("Ana Ficticia", r.contexto().personaNombre());
    assertEquals(
        pr.getIdPersonaRol().toString(), r.contexto().asignaciones().getFirst().idPersonaRol());
    assertEquals(43, r.token().length());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "abc", "ABC123"})
  void formatoInvalido(String valor) {
    error(422, () -> s.identificar(tipo.getIdTipoDocumento(), valor, "PACIENTE", "UTC"));
    verifyNoInteractions(s.documentos);
  }

  @Test
  void tipoAusenteEInactivo() {
    error(404, () -> s.identificar(UUID.randomUUID(), "ABC", "PACIENTE", "UTC"));
    tipo.setActivo(false);
    error(422, () -> identificar("PACIENTE"));
  }

  @Test
  void documentoInexistente() {
    when(s.documentos.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
        .thenReturn(List.of());
    error(404, () -> identificar("PACIENTE"));
  }

  @Test
  void personaSinPaciente() {
    pr.setIdRol(empleado);
    error(403, () -> identificar("PACIENTE"));
  }

  @Test
  void soloPacienteNoEmpleado() {
    error(403, () -> identificar("EMPLEADO"));
  }

  @Test
  void empleadoConVariosRolesYClinicas() {
    var otra = new Clinica(UUID.randomUUID());
    otra.setActivo(true);
    otra.setNombre("Otra");
    var rol = new Rol(UUID.randomUUID());
    rol.setNombre("Médico");
    rol.setActivo(true);
    s.rolesConfigurados += "," + rol.getIdRol();
    when(s.roles.buscarPorId(rol.getIdRol())).thenReturn(rol);
    var a = asignacion(empleado, clinica);
    var b = asignacion(rol, otra);
    when(s.asignaciones.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
        .thenReturn(List.of(a, b, pr));
    var result = identificar("EMPLEADO");
    assertEquals(2, result.contexto().asignaciones().size());
    assertEquals(
        a,
        s.exigirEmpleado("Bearer " + result.token(), a.getIdPersonaRol(), clinica.getIdClinica()));
    error(
        403,
        () ->
            s.exigirEmpleado(
                "Bearer " + result.token(), b.getIdPersonaRol(), clinica.getIdClinica()));
  }

  @Test
  void rolYClinicaInactivosRechazados() {
    paciente.setActivo(false);
    error(403, () -> identificar("PACIENTE"));
    paciente.setActivo(true);
    clinica.setActivo(false);
    error(403, () -> identificar("PACIENTE"));
  }

  @Test
  void nuncaIncluyeAsignacionAjena() {
    pr.setIdPersona(new Persona(UUID.randomUUID()));
    error(403, () -> identificar("PACIENTE"));
  }

  @Test
  void variasClinicasPaciente() {
    var c = new Clinica(UUID.randomUUID());
    c.setActivo(true);
    when(s.asignaciones.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
        .thenReturn(List.of(pr, asignacion(paciente, c)));
    assertEquals(2, identificar("PACIENTE").contexto().asignaciones().size());
  }

  @Test
  void cierreYProposito() {
    var r = identificar("PACIENTE");
    String token = "Bearer " + r.token();
    error(403, () -> s.exigir(token, "EMPLEADO"));
    s.cerrar(token, "PACIENTE");
    error(401, () -> s.exigir(token, "PACIENTE"));
    assertDoesNotThrow(() -> s.cerrar(token, "PACIENTE"));
  }

  @Test
  void revocacionPorCambioDeRol() {
    var r = identificar("PACIENTE");
    paciente.setActivo(false);
    error(403, () -> s.contexto("Bearer " + r.token(), "PACIENTE", "UTC"));
  }

  @Test
  void falloDAOSePropaga() {
    when(s.documentos.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
        .thenThrow(new jakarta.persistence.PersistenceException("sintético"));
    assertThrows(jakarta.persistence.PersistenceException.class, () -> identificar("PACIENTE"));
  }

  @Test
  void configuracionNoConviertePacienteEnEmpleado() {
    s.rolesConfigurados = paciente.getIdRol().toString();
    when(s.roles.buscarPorId(paciente.getIdRol())).thenReturn(paciente);
    assertThrows(IllegalStateException.class, () -> identificar("EMPLEADO"));
  }

  @Test
  void empleadoConRolOClinicaInactivosNoIniciaSesion() {
    pr.setIdRol(empleado);
    empleado.setActivo(false);
    error(403, () -> identificar("EMPLEADO"));
    empleado.setActivo(true);
    clinica.setActivo(false);
    error(403, () -> identificar("EMPLEADO"));
  }
}
