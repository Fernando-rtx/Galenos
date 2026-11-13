package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import sv.edu.ues.occingenieriappi115_2026.salud.control.IdentidadAcademicaService;

@RequestScoped
@Path("v2/identidad_academica")
@Produces(MediaType.APPLICATION_JSON)
public class IdentidadAcademicaResource {
  @EJB IdentidadAcademicaService identidad;
  @Inject ApiSettings settings;
  @Context HttpHeaders headers;

  private String tipo(String portal) {
    settings.requireManagement();
    return switch (portal) {
      case "paciente" -> "PACIENTE";
      case "empleado" -> "EMPLEADO";
      default -> throw new ApiException(404, "Portal no encontrado");
    };
  }

  @POST
  @Path("{portal}")
  @Consumes(MediaType.APPLICATION_JSON)
  public IdentidadAcademicaService.Identificacion identificar(
      @PathParam("portal") String portal, JsonObject body) {
    String tipo = tipo(portal);
    var input = new RestInput(body, "idTipoDocumento", "valor");
    return identidad.identificar(
        input.uuid("idTipoDocumento"), input.text("valor"), tipo, settings.getClinicZone().getId());
  }

  @GET
  @Path("{portal}")
  public IdentidadAcademicaService.Contexto contexto(@PathParam("portal") String portal) {
    return identidad.contexto(
        headers.getHeaderString("Authorization"), tipo(portal), settings.getClinicZone().getId());
  }

  @DELETE
  @Path("{portal}")
  public Response cerrar(@PathParam("portal") String portal) {
    identidad.cerrar(headers.getHeaderString("Authorization"), tipo(portal));
    return Response.noContent().build();
  }
}
