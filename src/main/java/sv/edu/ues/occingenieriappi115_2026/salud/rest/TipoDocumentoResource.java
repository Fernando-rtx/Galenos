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
        var pagination = RestParameters.page(first, max, settings);
        List<TipoDocumentoDto> page = dao.obtenerPagina(pagination.first(), pagination.max(), List.of(),
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
        var uuid = RestParameters.uuid(id, "id");
        var entity = dao.buscarPorId(uuid);
        if (entity == null) throw new ApiException(404, "Tipo de documento no encontrado");
        response.setHeader("Cache-Control", "no-store");
        return new TipoDocumentoDto(entity);
    }

}
