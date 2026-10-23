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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DireccionOrden;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenDAO;

/** Lectura exclusivamente académica sobre datos sintéticos, con clínica obligatoria. */
@RequestScoped
@Path("v1/consulta")
@Produces(MediaType.APPLICATION_JSON)
public class ConsultaResource {
    @EJB ConsultaDAO dao;
    @EJB ClinicaDAO clinicaDao;
    @Inject ApiSettings settings;
    @Context HttpServletResponse response;

    @GET
    public List<ConsultaDto> findRange(@QueryParam("first") String first,
            @QueryParam("max") String max, @QueryParam("idClinica") String idClinica,
            @QueryParam("fechaDesde") String fechaDesde, @QueryParam("fechaHasta") String fechaHasta,
            @QueryParam("idPersonaRol") String idPersonaRol) {
        settings.requireDemo();
        var page = RestParameters.page(first, max, settings);
        var filters = clinicFilters(RestParameters.uuid(idClinica, "idClinica"));
        LocalDate from = date(fechaDesde, "fechaDesde");
        LocalDate until = date(fechaHasta, "fechaHasta");
        if (from != null && until != null && from.isAfter(until)) {
            throw new ApiException(422, "fechaDesde no puede ser posterior a fechaHasta");
        }
        if (from != null) filters.add(new FiltroDAO("fechaInicio", OperadorFiltro.MAYOR_O_IGUAL,
                Date.from(from.atStartOfDay(settings.getClinicZone()).toInstant())));
        if (until != null) filters.add(new FiltroDAO("fechaInicio", OperadorFiltro.MENOR_QUE,
                Date.from(until.plusDays(1).atStartOfDay(settings.getClinicZone()).toInstant())));
        if (idPersonaRol != null) filters.add(new FiltroDAO("idPersonaRol.idPersonaRol", OperadorFiltro.IGUAL,
                RestParameters.uuid(idPersonaRol, "idPersonaRol")));
        var result = dao.obtenerPagina(page.first(), page.max(), filters,
                List.of(new OrdenDAO("idConsulta", DireccionOrden.ASCENDENTE)))
                .stream().map(ConsultaDto::new).toList();
        response.setHeader("Total-registros", Long.toString(dao.contar(filters)));
        return result;
    }

    @GET
    @Path("{id}")
    public ConsultaDto findById(@PathParam("id") String id, @QueryParam("idClinica") String idClinica) {
        settings.requireDemo();
        UUID uuid = RestParameters.uuid(id, "id");
        var filters = clinicFilters(RestParameters.uuid(idClinica, "idClinica"));
        filters.add(new FiltroDAO("idConsulta", OperadorFiltro.IGUAL, uuid));
        var result = dao.obtenerPagina(0, 1, filters, List.of());
        if (result.isEmpty()) throw new ApiException(404, "Consulta no encontrada en la clínica seleccionada");
        return new ConsultaDto(result.getFirst());
    }

    private List<FiltroDAO> clinicFilters(UUID idClinica) {
        var clinic = clinicaDao.buscarPorId(idClinica);
        if (clinic == null || !Boolean.TRUE.equals(clinic.getActivo())) {
            throw new ApiException(404, "Clínica no disponible");
        }
        return new ArrayList<>(List.of(new FiltroDAO("idPersonaRol.idClinica.idClinica",
                OperadorFiltro.IGUAL, idClinica)));
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
