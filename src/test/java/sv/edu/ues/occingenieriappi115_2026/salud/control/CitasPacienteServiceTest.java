package sv.edu.ues.occingenieriappi115_2026.salud.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

class CitasPacienteServiceTest {
  CitasPacienteService s;
  PersonaRol pr;
  Consulta c;
  UUID persona = UUID.randomUUID(), clinica = UUID.randomUUID();
  ZoneId zona = ZoneId.of("America/El_Salvador");

  @BeforeEach
  void setup() {
    s = new CitasPacienteService();
    s.identidad = mock(IdentidadAcademicaService.class);
    s.consultas = mock(ConsultaDAO.class);
    s.asociaciones = mock(ConsultaProcedimientoDAO.class);
    s.procedimientos = mock(ProcedimientoDAO.class);
    pr = new PersonaRol(UUID.randomUUID());
    var p = new Persona(persona);
    p.setNombres("Ana");
    pr.setIdPersona(p);
    pr.setIdClinica(new Clinica(clinica));
    c = new Consulta(UUID.randomUUID());
    c.setIdPersonaRol(pr);
    c.setFechaInicio(Date.from(Instant.parse("2026-11-11T14:00:00Z")));
    when(s.identidad.exigir("token", "PACIENTE"))
        .thenReturn(
            new SesionAcademicaService.Sesion(persona, "PACIENTE", Instant.now().plusSeconds(60)));
    when(s.identidad.asignaciones(persona, "PACIENTE")).thenReturn(List.of(pr));
    when(s.asociaciones.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
        .thenReturn(List.of());
  }

  @ParameterizedTest
  @CsvSource({"2026-02-30,2026-03-02", "2026-11-12,2026-11-11", "2026-1-01,2026-11-11"})
  void rangoInvalido(String a, String b) {
    assertEquals(
        422,
        assertThrows(IdentificacionException.class, () -> CitasPacienteService.rango(a, b, zona))
            .getStatus());
  }

  @Test
  void limitesLocalesInclusivos() {
    var rango = CitasPacienteService.rango("2026-11-11", "2026-11-11", zona);
    assertEquals(Instant.parse("2026-11-11T06:00:00Z"), rango[0].toInstant());
    assertEquals(Instant.parse("2026-11-12T06:00:00Z"), rango[1].toInstant());
  }

  @Test
  void clinicaAjenaNoConsultaDAO() {
    assertEquals(
        403,
        assertThrows(
                IdentificacionException.class,
                () -> s.buscar("token", UUID.randomUUID(), "2026-11-11", "2026-11-11", zona, 0, 20))
            .getStatus());
    verifyNoInteractions(s.consultas);
  }

  @Test
  void vacioYMultiplesResultados() {
    when(s.consultas.buscar(anyList(), any(), any(), anyInt(), anyInt())).thenReturn(List.of());
    assertTrue(
        s.buscar("token", clinica, "2026-11-11", "2026-11-11", zona, 0, 20).citas().isEmpty());
    when(s.consultas.buscar(anyList(), any(), any(), anyInt(), anyInt())).thenReturn(List.of(c, c));
    when(s.consultas.contar(anyList(), any(), any())).thenReturn(2L);
    var page = s.buscar("token", clinica, "2026-11-11", "2026-11-11", zona, 0, 20);
    assertEquals(2, page.citas().size());
    assertEquals(2, page.total());
  }

  @Test
  void ticketInexistenteOAjeno() {
    assertEquals(
        404,
        assertThrows(
                IdentificacionException.class, () -> s.ticket("token", c.getIdConsulta(), zona))
            .getStatus());
    c.setIdPersonaRol(new PersonaRol(UUID.randomUUID()));
    when(s.consultas.buscarPorId(c.getIdConsulta())).thenReturn(c);
    assertEquals(
        404,
        assertThrows(
                IdentificacionException.class, () -> s.ticket("token", c.getIdConsulta(), zona))
            .getStatus());
  }

  @Test
  void pdfBinarioConMultipaginaYTextoAcentuado() {
    var lines = new ArrayList<String>();
    for (int i = 0; i < 90; i++) lines.add("GalenosSV Clínica José — Cita ficticia " + i);
    var bytes = TicketPdf.generar(lines);
    String pdf = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
    assertTrue(pdf.startsWith("%PDF-1.4"));
    assertTrue(pdf.contains("/Count 3"));
    assertTrue(pdf.contains("/Subtype /Image"));
    assertTrue(pdf.endsWith("%%EOF\n"));
  }
}
