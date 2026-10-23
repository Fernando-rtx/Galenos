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
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DireccionOrden;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoDocumentoDAO;

/** Lectura del catálogo sin documentos ni datos de pacientes. */
@RequestScoped
@Path("v1/tipo_documento")
@Produces(MediaType.APPLICATION_JSON)
public class TipoDocumentoResource {
    @EJB
    TipoDocumentoDAO dao;
    @Inject
    ApiSettings settings;
    @Context
    HttpServletResponse response;

    @GET
    public List<TipoDocumentoDto> findRange(@QueryParam("first") String first,
            @QueryParam("max") String max) {
        int offset = parseInteger(first, 0, "first");
        int limit = parseInteger(max, settings.getPageSize(), "max");
        if (offset < 0 || limit < 1 || limit > settings.getMaxPageSize()) {
            throw new ApiException(422, "first debe ser >= 0 y max debe estar entre 1 y "
                    + settings.getMaxPageSize());
        }
        List<TipoDocumentoDto> page = dao.obtenerPagina(offset, limit, List.of(),
                List.of(new OrdenDAO("idTipoDocumento", DireccionOrden.ASCENDENTE)))
                .stream().map(TipoDocumentoDto::new).toList();
        long total = dao.contar();
        response.setHeader("Total-registros", Long.toString(total));
        response.setHeader("Cache-Control", "no-store");
        return page;
    }

    @GET
    @Path("{id}")
    public TipoDocumentoDto findById(@PathParam("id") String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
            if (!uuid.toString().equalsIgnoreCase(id)) throw new IllegalArgumentException();
        } catch (IllegalArgumentException exception) {
            throw new ApiException(422, "id debe ser un UUID canónico");
        }
        var entity = dao.buscarPorId(uuid);
        if (entity == null) throw new ApiException(404, "Tipo de documento no encontrado");
        response.setHeader("Cache-Control", "no-store");
        return new TipoDocumentoDto(entity);
    }

    private static int parseInteger(String raw, int fallback, String parameter) {
        if (raw == null) return fallback;
        try {
            if (!raw.matches("[0-9]+")) throw new NumberFormatException();
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            throw new ApiException(422, parameter + " debe ser un entero no negativo válido");
        }
    }
}
