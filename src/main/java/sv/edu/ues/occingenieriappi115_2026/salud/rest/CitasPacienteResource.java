package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import sv.edu.ues.occingenieriappi115_2026.salud.control.CitasPacienteService;

@RequestScoped
@Path("v2/citas_paciente")
public class CitasPacienteResource {
  @EJB CitasPacienteService citas;
  @Inject ApiSettings settings;
  @Context HttpHeaders headers;
  @Context UriInfo uri;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response buscar(
      @QueryParam("idClinica") String clinica,
      @QueryParam("fechaDesde") String desde,
      @QueryParam("fechaHasta") String hasta,
      @QueryParam("first") String first,
      @QueryParam("max") String max) {
    settings.requireManagement();
    if (!java.util.Set.of("idClinica", "fechaDesde", "fechaHasta", "first", "max")
        .containsAll(uri.getQueryParameters().keySet()))
      throw new ApiException(422, "Parámetro no permitido");
    var page = RestParameters.page(first, max, settings);
    var result =
        citas.buscar(
            headers.getHeaderString("Authorization"),
            RestParameters.uuid(clinica, "idClinica"),
            desde,
            hasta,
            settings.getClinicZone(),
            page.first(),
            page.max());
    return Response.ok(result.citas()).header("Total-registros", result.total()).build();
  }

  @GET
  @Path("{id}/ticket")
  @Produces("application/pdf")
  public Response ticket(@PathParam("id") String id) {
    settings.requireManagement();
    byte[] bytes =
        citas.ticket(
            headers.getHeaderString("Authorization"),
            RestParameters.uuid(id, "idConsulta"),
            settings.getClinicZone());
    return Response.ok(bytes, "application/pdf")
        .header("Content-Disposition", "attachment; filename=\"cita-" + id + ".pdf\"")
        .header("Cache-Control", "no-store")
        .build();
  }
}
