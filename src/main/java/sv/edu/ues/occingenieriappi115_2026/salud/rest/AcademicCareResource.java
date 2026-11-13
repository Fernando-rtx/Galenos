package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.LocalDate;
import java.util.*;
import sv.edu.ues.occingenieriappi115_2026.salud.control.AcademicCareService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.IdentidadAcademicaService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.SesionAcademicaService;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FlujoConsultaException;

/** HU1-HU4 REST facade. Real use is bound to an academic portal session. */
@RequestScoped @Path("v2") @Produces(MediaType.APPLICATION_JSON)
public class AcademicCareResource {
    @EJB AcademicCareService service;
    @EJB IdentidadAcademicaService identidad;
    @Inject ApiSettings settings;
    @Context HttpServletRequest request;
    @Context HttpHeaders headers;

    @JsonbNillable public record Ticket(String paciente, String clinica, String fechaInicio,
                                       String procedimiento, String pasos) {}

    private String actor() {
        settings.requireCrudDemo();
        if (request.getUserPrincipal() == null) throw new ApiException(401, "Autenticación requerida");
        return request.getUserPrincipal().getName();
    }
    private UUID employee(String raw) {
        UUID expected = settings.personFor(actor());
        if (expected == null) throw new ApiException(403, "La identidad de demostración no tiene persona asignada");
        if (raw != null && !expected.equals(RestParameters.uuid(raw, "idPersona")))
            throw new ApiException(403, "La identidad no corresponde al empleado solicitado");
        return expected;
    }
    private String bearer() { return headers.getHeaderString(HttpHeaders.AUTHORIZATION); }
    private UUID sessionEmployee(UUID clinic, UUID role) {
        identidad.exigir(bearer(), "EMPLEADO");
        PersonaRol assignment = identidad.exigirEmpleado(bearer(),
                RestParameters.uuid(headers.getHeaderString("X-Persona-Rol"), "X-Persona-Rol"), clinic);
        UUID person = assignment.getIdPersona().getIdPersona();
        if (!role.equals(assignment.getIdRol().getIdRol()))
            throw new ApiException(403, "El rol solicitado no corresponde a la asignación activa");
        return person;
    }
    private SesionAcademicaService.Sesion patientSession() { return identidad.exigir(bearer(), "PACIENTE"); }
    private void requirePatientClinic(UUID person, UUID clinic) {
        var context = identidad.contexto(bearer(), "PACIENTE", settings.getClinicZone().getId());
        if (!person.equals(UUID.fromString(context.idPersona()))
                || context.asignaciones().stream().noneMatch(a -> clinic.toString().equals(a.idClinica())))
            throw new ApiException(403, "La clínica no pertenece a la sesión del paciente");
    }
    private AcademicCareService.Identity sessionIdentity(String portal) {
        var context = identidad.contexto(bearer(), portal, settings.getClinicZone().getId());
        var clinics = context.asignaciones().stream().collect(java.util.stream.Collectors.toMap(
                IdentidadAcademicaService.Asignacion::idClinica,
                a -> new AcademicCareService.Clinic(a.idClinica(), a.clinicaNombre()), (a, b) -> a,
                java.util.LinkedHashMap::new)).values().stream().toList();
        var roles = context.asignaciones().stream().map(a -> new AcademicCareService.Role(
                a.idPersonaRol(), a.idRol(), a.rolNombre(), a.idClinica(), a.clinicaNombre())).toList();
        String[] name = context.personaNombre().split(" ", 2);
        return new AcademicCareService.Identity(context.idPersona(), name[0], name.length > 1 ? name[1] : "", clinics, roles);
    }
    private AcademicCareService.Identity staffIdentity(String type, String number) {
        if (!settings.isCrudDemo()) return sessionIdentity("EMPLEADO");
        String actor = actor();
        UUID expected = settings.personFor(actor);
        if (expected == null) throw new ApiException(403, "La identidad de demostración no tiene persona asignada");
        var identity = service.employee(RestParameters.uuid(type, "idTipoDocumento"), number, expected);
        if (identity == null) throw new ApiException(401, "No se pudo comprobar la identidad del empleado");
        return scoped(identity, actor);
    }
    private AcademicCareService.Identity patientIdentity(String type, String number) {
        if (!settings.isCrudDemo()) return sessionIdentity("PACIENTE");
        actor();
        var identity = service.patient(RestParameters.uuid(type, "idTipoDocumento"), number);
        if (identity == null) throw new ApiException(404, "No se encontró un paciente registrado con ese documento");
        return scoped(identity, request.getUserPrincipal().getName());
    }
    private AcademicCareService.Identity scoped(AcademicCareService.Identity identity, String actor) {
        Set<String> allowed = settings.clinicsFor(actor).stream().map(UUID::toString).collect(java.util.stream.Collectors.toSet());
        List<AcademicCareService.Role> roles = identity.roles().stream().filter(role -> allowed.contains(role.idClinica())).toList();
        if (roles.isEmpty()) throw new ApiException(403, "La identidad no tiene una clínica de demostración asignada");
        List<AcademicCareService.Clinic> clinics = identity.clinicas().stream().filter(clinic -> allowed.contains(clinic.idClinica())).toList();
        return new AcademicCareService.Identity(identity.idPersona(), identity.nombres(), identity.apellidos(), clinics, roles);
    }
    private static LocalDate date(String raw, String key) { return ConsultaResource.date(raw, key); }
    private static void dates(LocalDate from, LocalDate until) {
        if (from != null && until != null && from.isAfter(until))
            throw new ApiException(422, "fechaDesde no puede ser posterior a fechaHasta");
    }
    private void clinic(UUID id) { if (settings.isCrudDemo()) settings.requireClinic(actor(), id); }

    @GET @Path("empleado/identidad")
    public AcademicCareService.Identity verifyEmployee(@QueryParam("idTipoDocumento") String type,
                                                        @QueryParam("numero") String number) {
        if (!settings.isCrudDemo()) identidad.exigir(bearer(), "EMPLEADO");
        return staffIdentity(type, number);
    }

    @GET @Path("paciente/clinicas")
    public AcademicCareService.Identity patientClinics(@QueryParam("idTipoDocumento") String type,
                                                        @QueryParam("numero") String number) {
        if (!settings.isCrudDemo()) patientSession();
        return patientIdentity(type, number);
    }

    @GET @Path("paciente/citas")
    public List<AcademicCareService.Appointment> patientAppointments(
            @QueryParam("idTipoDocumento") String type, @QueryParam("numero") String number,
            @QueryParam("idClinica") String rawClinic, @QueryParam("fechaDesde") String rawFrom,
            @QueryParam("fechaHasta") String rawUntil, @QueryParam("first") String first,
            @QueryParam("max") String max, @Context HttpServletResponse response) {
        UUID sessionPerson = settings.isCrudDemo() ? null : patientSession().persona();
        UUID clinic = RestParameters.uuid(rawClinic, "idClinica");
        UUID person;
        if (settings.isCrudDemo()) {
            var demoPatient = patientIdentity(type, number);
            clinic(clinic);
            person = UUID.fromString(demoPatient.idPersona());
        } else {
            person = sessionPerson;
            requirePatientClinic(person, clinic);
        }
        LocalDate from = date(rawFrom, "fechaDesde"), until = date(rawUntil, "fechaHasta"); dates(from, until);
        var page = RestParameters.page(first, max, settings);
        List<AcademicCareService.Appointment> rows = settings.isCrudDemo()
                ? service.patientAppointments(RestParameters.uuid(type, "idTipoDocumento"), number, clinic, from, until, page.first(), page.max())
                : service.patientAppointments(person, clinic, from, until, page.first(), page.max());
        response.setHeader("Total-registros", Long.toString(settings.isCrudDemo()
                ? service.patientAppointmentCount(RestParameters.uuid(type, "idTipoDocumento"), number, clinic, from, until)
                : service.patientAppointmentCount(person, clinic, from, until)));
        return rows;
    }

    @GET @Path("paciente/citas/{id}/ticket")
    @Produces("application/pdf")
    public Response ticket(@PathParam("id") String rawId, @QueryParam("idTipoDocumento") String type,
                           @QueryParam("numero") String number, @QueryParam("idClinica") String rawClinic) {
        UUID sessionPerson = settings.isCrudDemo() ? null : patientSession().persona();
        UUID clinic = RestParameters.uuid(rawClinic, "idClinica"); clinic(clinic);
        UUID person;
        if (settings.isCrudDemo()) { patientIdentity(type, number); person = null; }
        else { person = sessionPerson; requirePatientClinic(person, clinic); }
        UUID id = RestParameters.uuid(rawId, "id");
        List<AcademicCareService.Appointment> rows = settings.isCrudDemo()
                ? service.patientAppointments(RestParameters.uuid(type, "idTipoDocumento"), number, clinic, null, null, 0, 1000)
                : service.patientAppointments(person, clinic, null, null, 0, 1000);
        AcademicCareService.Appointment appt = rows.stream().filter(row -> row.idConsulta().equals(id.toString()))
                .findFirst().orElseThrow(() -> new ApiException(404, "Cita no encontrada"));
        String steps = appt.historial().stream().map(AcademicCareService.History::nombre).distinct()
                .reduce((a, b) -> a + ", " + b).orElse("Sin procedimientos registrados");
        byte[] pdf = TicketPdf.generate(List.of("GalenosSV - Ticket de cita", "Paciente: " + appt.paciente(),
                "Clinica: " + appt.clinica(), "Fecha: " + Objects.toString(appt.fechaInicio(), "Pendiente"),
                "Procedimiento: " + appt.procedimiento(), "Pasos: " + steps, "Consulta: " + appt.idConsulta()));
        return Response.ok(pdf, "application/pdf").header("Content-Disposition", "attachment; filename=galenos-ticket.pdf").build();
    }

    @GET @Path("empleado/consultas")
    public List<AcademicCareService.Appointment> employeeAppointments(
            @QueryParam("idPersona") String rawPerson, @QueryParam("idClinica") String rawClinic,
            @QueryParam("idRol") String rawRole, @QueryParam("fechaDesde") String rawFrom,
            @QueryParam("fechaHasta") String rawUntil, @QueryParam("first") String first,
            @QueryParam("max") String max, @Context HttpServletResponse response) {
        if (!settings.isCrudDemo()) identidad.exigir(bearer(), "EMPLEADO");
        UUID clinic = RestParameters.uuid(rawClinic, "idClinica"), role = RestParameters.uuid(rawRole, "idRol"); clinic(clinic);
        UUID person = settings.isCrudDemo() ? employee(rawPerson) : sessionEmployee(clinic, role);
        if (rawPerson != null && !person.equals(RestParameters.uuid(rawPerson, "idPersona")))
            throw new ApiException(403, "La identidad no corresponde al empleado solicitado");
        LocalDate from = date(rawFrom, "fechaDesde"), until = date(rawUntil, "fechaHasta");
        if (from == null && until == null) from = until = LocalDate.now(java.time.ZoneId.of("America/El_Salvador"));
        dates(from, until);
        var page = RestParameters.page(first, max, settings);
        List<AcademicCareService.Appointment> rows = service.employeeAppointments(person, clinic, role, from, until, page.first(), page.max());
        response.setHeader("Total-registros", Long.toString(service.employeeAppointmentCount(person, clinic, role, from, until))); return rows;
    }

    @GET @Path("atencion/consulta/{id}")
    public AcademicCareService.CareDetail detail(@PathParam("id") String rawId,
                                                  @QueryParam("idPersona") String rawPerson,
                                                  @QueryParam("idClinica") String rawClinic,
                                                  @QueryParam("idRol") String rawRole) {
        if (!settings.isCrudDemo()) identidad.exigir(bearer(), "EMPLEADO");
        UUID clinic = RestParameters.uuid(rawClinic, "idClinica"), role = RestParameters.uuid(rawRole, "idRol"); clinic(clinic);
        UUID person = settings.isCrudDemo() ? employee(rawPerson) : sessionEmployee(clinic, role);
        if (rawPerson != null && !person.equals(RestParameters.uuid(rawPerson, "idPersona")))
            throw new ApiException(403, "La identidad no corresponde al empleado solicitado");
        return service.detail(person, clinic, role, RestParameters.uuid(rawId, "id"));
    }

    @POST @Path("atencion/consulta/{id}/registrar") @Consumes(MediaType.APPLICATION_JSON)
    public AcademicCareService.CareResult register(@PathParam("id") String rawId,
            @QueryParam("idPersona") String rawPerson, @QueryParam("idClinica") String rawClinic,
            @QueryParam("idRol") String rawRole, JsonObject body) {
        if (!settings.isCrudDemo()) identidad.exigir(bearer(), "EMPLEADO");
        UUID clinic = RestParameters.uuid(rawClinic, "idClinica"), role = RestParameters.uuid(rawRole, "idRol"); clinic(clinic);
        UUID person = settings.isCrudDemo() ? employee(rawPerson) : sessionEmployee(clinic, role);
        if (rawPerson != null && !person.equals(RestParameters.uuid(rawPerson, "idPersona")))
            throw new ApiException(403, "La identidad no corresponde al empleado solicitado");
        RestInput input = new RestInput(body, "accion", "idConsultaProcedimientoPaso", "valor", "idPasoSiguiente", "examenes");
        UUID step = RestParameters.uuid(input.text("idConsultaProcedimientoPaso"), "idConsultaProcedimientoPaso");
        String target = input.text("idPasoSiguiente");
        return service.register(person, clinic, role, RestParameters.uuid(rawId, "id"), step,
                input.text("accion"), input.text("valor"), target == null ? null : RestParameters.uuid(target, "idPasoSiguiente"), input.uuidList("examenes"));
    }

    @POST @Path("atencion/registrar") @Consumes(MediaType.APPLICATION_JSON)
    public AcademicCareService.CareResult registerAcademic(JsonObject body) {
        RestInput input = new RestInput(body, "idConsulta", "idPersona", "idClinica", "idRol", "accion",
                "idConsultaProcedimientoPaso", "valor", "idPasoSiguiente", "examenes");
        if (!settings.isCrudDemo()) identidad.exigir(bearer(), "EMPLEADO");
        UUID clinic = RestParameters.uuid(input.text("idClinica"), "idClinica"); clinic(clinic);
        UUID role = RestParameters.uuid(input.text("idRol"), "idRol");
        UUID person = settings.isCrudDemo() ? employee(input.text("idPersona")) : sessionEmployee(clinic, role);
        if (input.text("idPersona") != null && !person.equals(RestParameters.uuid(input.text("idPersona"), "idPersona")))
            throw new ApiException(403, "La identidad no corresponde al empleado solicitado");
        UUID consultation = RestParameters.uuid(input.text("idConsulta"), "idConsulta");
        String next = input.text("idPasoSiguiente");
        return service.register(person, clinic, role, consultation,
                RestParameters.uuid(input.text("idConsultaProcedimientoPaso"), "idConsultaProcedimientoPaso"),
                input.text("accion"), input.text("valor"), next == null ? null : RestParameters.uuid(next, "idPasoSiguiente"),
                input.uuidList("examenes"));
    }
}
