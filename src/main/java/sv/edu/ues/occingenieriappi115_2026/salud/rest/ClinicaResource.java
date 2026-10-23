package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DireccionOrden;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenDAO;

@RequestScoped
@Path("v1/clinica")
@Produces(MediaType.APPLICATION_JSON)
public class ClinicaResource {
    @EJB ClinicaDAO dao;
    @Inject ApiSettings settings;
    @Context HttpServletResponse response;

    @GET
    public List<ClinicaDto> findRange(@QueryParam("first") String first, @QueryParam("max") String max) {
        settings.requireDemo();
        var page = RestParameters.page(first, max, settings);
        var filters = List.of(new FiltroDAO("activo", OperadorFiltro.IGUAL, true));
        var result = dao.obtenerPagina(page.first(), page.max(), filters,
                List.of(new OrdenDAO("idClinica", DireccionOrden.ASCENDENTE)))
                .stream().map(ClinicaDto::new).toList();
        response.setHeader("Total-registros", Long.toString(dao.contar(filters)));
        return result;
    }
}
