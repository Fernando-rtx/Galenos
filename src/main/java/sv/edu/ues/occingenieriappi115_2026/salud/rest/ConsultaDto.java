package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.json.bind.annotation.JsonbNillable;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;

/** Proyección de demostración: sin observaciones, referencias, documentos o colecciones. */
@JsonbNillable
public class ConsultaDto {
    private final String idConsulta;
    private final String fechaInicio;
    private final String fechaFin;
    private final String idPersonaRol;
    private final String personaNombre;
    private final String rolNombre;
    private final String idClinica;
    private final String clinicaNombre;

    public ConsultaDto(Consulta source) {
        idConsulta = source.getIdConsulta().toString();
        fechaInicio = instant(source.getFechaInicio());
        fechaFin = instant(source.getFechaFin());
        var assignment = source.getIdPersonaRol();
        idPersonaRol = assignment == null || assignment.getIdPersonaRol() == null
                ? null : assignment.getIdPersonaRol().toString();
        var person = assignment == null ? null : assignment.getIdPersona();
        String name = person == null ? "" : Stream.of(person.getNombres(), person.getApellidos())
                .filter(value -> value != null && !value.isBlank()).map(String::strip)
                .collect(Collectors.joining(" "));
        personaNombre = name.isEmpty() ? null : name;
        var role = assignment == null ? null : assignment.getIdRol();
        rolNombre = role == null ? null : role.getNombre();
        var clinic = assignment == null ? null : assignment.getIdClinica();
        idClinica = clinic == null || clinic.getIdClinica() == null ? null : clinic.getIdClinica().toString();
        clinicaNombre = clinic == null ? null : clinic.getNombre();
    }

    private static String instant(Date value) { return value == null ? null : value.toInstant().toString(); }
    public String getIdConsulta() { return idConsulta; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public String getIdPersonaRol() { return idPersonaRol; }
    public String getPersonaNombre() { return personaNombre; }
    public String getRolNombre() { return rolNombre; }
    public String getIdClinica() { return idClinica; }
    public String getClinicaNombre() { return clinicaNombre; }
}
