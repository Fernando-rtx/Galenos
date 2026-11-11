package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.*;
import jakarta.inject.Inject;
import java.util.*;
import java.util.regex.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Identificación académica por documento y revalidación de pertenencia. */
@Stateless
@LocalBean
public class IdentidadAcademicaService {
  @EJB TipoDocumentoDAO tipos;
  @EJB DocumentoDAO documentos;
  @EJB PersonaRolDAO asignaciones;
  @EJB PersonaDAO personas;
  @EJB RolDAO roles;
  @Inject SesionAcademicaService sesiones;

  public record Asignacion(
      String idPersonaRol,
      String idRol,
      String rolNombre,
      String idClinica,
      String clinicaNombre) {}

  public record Contexto(
      String tipo,
      String idPersona,
      String personaNombre,
      List<Asignacion> asignaciones,
      String zonaHoraria) {}

  public record Identificacion(String token, String expiraEn, Contexto contexto) {}

  String rolesConfigurados = System.getenv().getOrDefault("GALENOS_EMPLEADO_ROLES", "");

  private Set<UUID> rolesEmpleado() {
    Set<UUID> ids = new HashSet<>();
    for (String raw : rolesConfigurados.split(",")) {
      if (raw.isBlank()) continue;
      UUID id = UUID.fromString(raw.strip());
      Rol rol = roles.buscarPorId(id);
      if (rol == null || paciente(rol))
        throw new IllegalStateException("Configuración de empleado inválida");
      ids.add(id);
    }
    return ids;
  }

  public static boolean paciente(Rol r) {
    return r != null && r.getNombre() != null && "paciente".equalsIgnoreCase(r.getNombre().trim());
  }

  public static boolean utilizable(PersonaRol pr, UUID persona) {
    return pr != null
        && pr.getIdPersonaRol() != null
        && pr.getIdPersona() != null
        && persona.equals(pr.getIdPersona().getIdPersona())
        && pr.getIdRol() != null
        && Boolean.TRUE.equals(pr.getIdRol().getActivo())
        && pr.getIdClinica() != null
        && Boolean.TRUE.equals(pr.getIdClinica().getActivo());
  }

  public List<PersonaRol> asignaciones(UUID persona, String tipo) {
    Set<UUID> permitidos = "EMPLEADO".equals(tipo) ? rolesEmpleado() : Set.of();
    return asignaciones
        .obtenerPagina(
            0,
            Integer.MAX_VALUE,
            List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL, persona)),
            List.of(new OrdenDAO("idPersonaRol", DireccionOrden.ASCENDENTE)))
        .stream()
        .filter(pr -> utilizable(pr, persona))
        .filter(
            pr ->
                "PACIENTE".equals(tipo)
                    ? paciente(pr.getIdRol())
                    : !paciente(pr.getIdRol()) && permitidos.contains(pr.getIdRol().getIdRol()))
        .toList();
  }

  public static String nombre(Persona persona) {
    String name =
        java.util.stream.Stream.of(persona.getNombres(), persona.getApellidos())
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .collect(java.util.stream.Collectors.joining(" "));
    return name.isEmpty() ? "Persona sin nombre" : name;
  }

  private Contexto contexto(UUID id, String tipo, String zona) {
    Persona persona = personas.buscarPorId(id);
    var rows = asignaciones(id, tipo);
    if (persona == null || rows.isEmpty())
      throw new IdentificacionException(403, "No tiene asignaciones disponibles para este portal");
    return new Contexto(
        tipo,
        id.toString(),
        nombre(persona),
        rows.stream()
            .map(
                pr ->
                    new Asignacion(
                        pr.getIdPersonaRol().toString(),
                        pr.getIdRol().getIdRol().toString(),
                        pr.getIdRol().getNombre(),
                        pr.getIdClinica().getIdClinica().toString(),
                        pr.getIdClinica().getNombre()))
            .toList(),
        zona);
  }

  public Identificacion identificar(UUID idTipo, String valor, String tipo, String zona) {
    TipoDocumento t = tipos.buscarPorId(idTipo);
    if (t == null) throw new IdentificacionException(404, "Tipo de documento no encontrado");
    if (!Boolean.TRUE.equals(t.getActivo()))
      throw new IdentificacionException(422, "Tipo de documento no disponible");
    if (valor == null || valor.isBlank())
      throw new IdentificacionException(422, "Ingrese el número de documento");
    String normalized = valor.trim();
    if (t.getExpresionRegular() != null
        && !t.getExpresionRegular().isBlank()
        && !Pattern.compile(t.getExpresionRegular()).matcher(normalized).matches())
      throw new IdentificacionException(422, "Formato de documento inválido");
    var rows =
        documentos.obtenerPagina(
            0,
            Integer.MAX_VALUE,
            List.of(
                new FiltroDAO("idTipoDocumento.idTipoDocumento", OperadorFiltro.IGUAL, idTipo),
                new FiltroDAO("valor", OperadorFiltro.IGUAL, normalized)),
            List.of());
    Set<UUID> ids = new HashSet<>();
    for (var d : rows) if (d.getIdPersona() != null) ids.add(d.getIdPersona().getIdPersona());
    if (ids.isEmpty()) throw new IdentificacionException(404, "Persona no encontrada");
    if (ids.size() != 1)
      throw new IdentificacionException(409, "Documento ambiguo; contacte a la clínica");
    UUID id = ids.iterator().next();
    Contexto context = contexto(id, tipo, zona);
    var session = sesiones.crear(id, tipo);
    return new Identificacion(session.token(), session.expira().toString(), context);
  }

  public SesionAcademicaService.Sesion exigir(String bearer, String tipo) {
    var sesion = sesiones.buscar(bearer);
    if (sesion == null) throw new IdentificacionException(401, "Identifíquese para continuar");
    if (!tipo.equals(sesion.tipo()))
      throw new IdentificacionException(403, "Sesión no válida para este portal");
    return sesion;
  }

  public Contexto contexto(String bearer, String tipo, String zona) {
    return contexto(exigir(bearer, tipo).persona(), tipo, zona);
  }

  public void cerrar(String bearer, String tipo) {
    var sesion = sesiones.buscar(bearer);
    if (sesion != null && !tipo.equals(sesion.tipo()))
      throw new IdentificacionException(403, "Sesión no válida para este portal");
    sesiones.cerrar(bearer);
  }

  public PersonaRol exigirEmpleado(String bearer, UUID asignacion, UUID clinica) {
    UUID persona = exigir(bearer, "EMPLEADO").persona();
    return asignaciones(persona, "EMPLEADO").stream()
        .filter(
            pr ->
                pr.getIdPersonaRol().equals(asignacion)
                    && pr.getIdClinica().getIdClinica().equals(clinica))
        .findFirst()
        .orElseThrow(() -> new IdentificacionException(403, "Asignación no disponible"));
  }
}
