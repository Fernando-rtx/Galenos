package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FlujoConsultaException;

/** Lectura exclusivamente académica sobre datos sintéticos, con clínica obligatoria. */
@RequestScoped
@Path("v1/consulta")
@Produces(MediaType.APPLICATION_JSON)
public class ConsultaResource {
    @EJB ConsultaFlujoService flujoService;
    @Inject ApiSettings settings;
    @Context HttpServletResponse response;

    @GET
    public List<ConsultaDto> findRange(@QueryParam("first") String first,
            @QueryParam("max") String max, @QueryParam("idClinica") String idClinica,
            @QueryParam("fechaDesde") String fechaDesde, @QueryParam("fechaHasta") String fechaHasta,
            @QueryParam("idPersonaRol") String idPersonaRol) {
        settings.requireDemo();
        var page = RestParameters.page(first, max, settings);
        UUID clinica = RestParameters.uuid(idClinica, "idClinica");
        LocalDate from = date(fechaDesde, "fechaDesde");
        LocalDate until = date(fechaHasta, "fechaHasta");
        UUID persona = idPersonaRol == null ? null : RestParameters.uuid(idPersonaRol, "idPersonaRol");
        try {
            var result = flujoService.buscarConsultas(clinica, from, until, settings.getClinicZone(),
                    persona, page.first(), page.max()).stream().map(ConsultaDto::new).toList();
            long total = flujoService.contarConsultas(clinica, from, until, settings.getClinicZone(), persona);
            response.setHeader("Total-registros", Long.toString(total));
            return result;
        } catch (FlujoConsultaException ex) {
            throw traducir(ex);
        }
    }

    @GET
    @Path("{id}")
    public ConsultaDto findById(@PathParam("id") String id, @QueryParam("idClinica") String idClinica) {
        settings.requireDemo();
        UUID uuid = RestParameters.uuid(id, "id");
        UUID clinica = RestParameters.uuid(idClinica, "idClinica");
        try {
            var result = flujoService.buscarConsulta(uuid, clinica);
            if (result == null) throw new ApiException(404, "Consulta no encontrada en la clínica seleccionada");
            return new ConsultaDto(result);
        } catch (FlujoConsultaException ex) {
            throw traducir(ex);
        }
    }

    private static RuntimeException traducir(FlujoConsultaException ex) {
        return switch (ex.getMessage()) {
            case "clinicaActual.noDisponible" -> new ApiException(404, "Clínica no disponible");
            case "consulta.rangoInvalido" -> new ApiException(422, "fechaDesde no puede ser posterior a fechaHasta");
            default -> ex;
        };
    }

    private static LocalDate date(String raw, String parameter) {
        if (raw == null) return null;
        try {
            if (!raw.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw new DateTimeParseException("Formato", raw, 0);
            var value = LocalDate.parse(raw);
            if (value.getYear() == 0) throw new DateTimeParseException("Año", raw, 0);
            return value;
        } catch (DateTimeParseException exception) {
            throw new ApiException(422, parameter + " debe ser una fecha válida YYYY-MM-DD");
        }
    }
}
