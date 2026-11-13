package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.*;
import java.time.*;
import java.util.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

@Stateless
@LocalBean
public class CitasPacienteService {
  @EJB IdentidadAcademicaService identidad;
  @EJB ConsultaDAO consultas;
  @EJB ConsultaProcedimientoDAO asociaciones;
  @EJB ProcedimientoDAO procedimientos;

  public record ProcedimientoDatos(
      String idConsultaProcedimiento, String idProcedimiento, String procedimientoNombre) {}

  @jakarta.json.bind.annotation.JsonbNillable
  public record Cita(
      String idConsulta,
      String idClinica,
      String clinicaNombre,
      String fechaInicio,
      String fechaFin,
      List<ProcedimientoDatos> procedimientos) {}

  public record Pagina(List<Cita> citas, long total) {}

  public static Date[] rango(String desde, String hasta, ZoneId zona) {
    try {
      if (desde == null
          || hasta == null
          || !desde.matches("\\d{4}-\\d{2}-\\d{2}")
          || !hasta.matches("\\d{4}-\\d{2}-\\d{2}")) throw new DateTimeException("Formato");
      LocalDate a = LocalDate.parse(desde), b = LocalDate.parse(hasta);
      if (a.isAfter(b)) throw new DateTimeException("Orden");
      return new Date[] {
        Date.from(a.atStartOfDay(zona).toInstant()),
        Date.from(b.plusDays(1).atStartOfDay(zona).toInstant())
      };
    } catch (DateTimeException | IllegalArgumentException e) {
      throw new IdentificacionException(422, "Ingrese un rango de fechas válido");
    }
  }

  public Pagina buscar(
      String bearer, UUID clinica, String desde, String hasta, ZoneId zona, int first, int max) {
    UUID persona = identidad.exigir(bearer, "PACIENTE").persona();
    var ids =
        identidad.asignaciones(persona, "PACIENTE").stream()
            .filter(pr -> pr.getIdClinica().getIdClinica().equals(clinica))
            .map(PersonaRol::getIdPersonaRol)
            .toList();
    if (ids.isEmpty())
      throw new IdentificacionException(403, "No está registrado como paciente en esta clínica");
    Date[] fechas = rango(desde, hasta, zona);
    return new Pagina(
        consultas.buscar(ids, fechas[0], fechas[1], first, max).stream().map(this::dto).toList(),
        consultas.contar(ids, fechas[0], fechas[1]));
  }

  private Cita dto(Consulta c) {
    var pr = c.getIdPersonaRol();
    var list =
        asociaciones.obtenerPagina(
            0,
            Integer.MAX_VALUE,
            List.of(
                new FiltroDAO("idConsulta.idConsulta", OperadorFiltro.IGUAL, c.getIdConsulta())),
            List.of(new OrdenDAO("idConsultaProcedimiento", DireccionOrden.ASCENDENTE)));
    return new Cita(
        c.getIdConsulta().toString(),
        pr.getIdClinica().getIdClinica().toString(),
        pr.getIdClinica().getNombre(),
        c.getFechaInicio() == null ? null : c.getFechaInicio().toInstant().toString(),
        c.getFechaFin() == null ? null : c.getFechaFin().toInstant().toString(),
        list.stream()
            .map(
                p -> {
                  var proc = procedimientos.buscarPorId(p.getIdProcedimiento());
                  String name =
                      proc == null || proc.getNombre() == null || proc.getNombre().isBlank()
                          ? "Procedimiento sin nombre"
                          : proc.getNombre();
                  return new ProcedimientoDatos(
                      p.getIdConsultaProcedimiento().toString(),
                      p.getIdProcedimiento().toString(),
                      name);
                })
            .toList());
  }

  public byte[] ticket(String bearer, UUID id, ZoneId zona) {
    UUID persona = identidad.exigir(bearer, "PACIENTE").persona();
    var asignaciones = identidad.asignaciones(persona, "PACIENTE");
    if (asignaciones.isEmpty())
      throw new IdentificacionException(403, "Sin asignación paciente disponible");
    var c = consultas.buscarPorId(id);
    if (c == null
        || c.getIdPersonaRol() == null
        || asignaciones.stream()
            .noneMatch(pr -> pr.getIdPersonaRol().equals(c.getIdPersonaRol().getIdPersonaRol())))
      throw new IdentificacionException(404, "Cita no encontrada");
    Cita cita = dto(c);
    var formato = java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withZone(zona);
    List<String> lines =
        new ArrayList<>(
            List.of(
                "GalenosSV",
                "Ticket de cita",
                "Paciente: " + IdentidadAcademicaService.nombre(c.getIdPersonaRol().getIdPersona()),
                "Clínica: " + cita.clinicaNombre(),
                "Fecha y hora: "
                    + (c.getFechaInicio() == null
                        ? "Sin fecha"
                        : formato.format(c.getFechaInicio().toInstant())),
                "Zona: " + zona,
                "Referencia: " + id,
                "Emitido: " + formato.format(Instant.now()),
                "Procedimientos:"));
    if (cita.procedimientos().isEmpty()) lines.add("Sin procedimientos asociados");
    else cita.procedimientos().forEach(p -> lines.add(p.procedimientoNombre()));
    return TicketPdf.generar(lines);
  }
}
