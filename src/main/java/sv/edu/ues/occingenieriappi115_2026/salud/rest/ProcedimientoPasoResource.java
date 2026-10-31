package sv.edu.ues.occingenieriappi115_2026.salud.rest;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

/** Configuración del flujo existente: crear paso con origen, rol y exámenes. */
@RequestScoped @Path("v2/procedimiento_paso") @Produces(MediaType.APPLICATION_JSON)
public class ProcedimientoPasoResource {
    @EJB ProcedimientoFlujoService service;
    @Inject ApiSettings settings;
    @Context HttpServletResponse response;
    @Context UriInfo uri;
    @JsonbNillable public record Datos(String idProcedimientoPaso,String idProcedimiento,String idRol,String nombre,Boolean indicaFin) {
        static Datos from(ProcedimientoPaso p) {return new Datos(p.getIdProcedimientoPaso().toString(),p.getIdProcedimiento().getIdProcedimiento().toString(),p.getIdRol().getIdRol().toString(),p.getNombre(),p.getIndicaFin());}
    }
    @GET public List<Datos> listar(@QueryParam("idProcedimiento")String parent,@QueryParam("first")String first,@QueryParam("max")String max) {
        settings.requireManagement();var id=RestParameters.uuid(parent,"idProcedimiento");var page=RestParameters.page(first,max,settings);
        response.setHeader("Total-registros",Long.toString(service.contarPasos(id)));return service.pasos(id,page.first(),page.max()).stream().map(Datos::from).toList();
    }
    @GET @Path("{id}") public Datos detalle(@PathParam("id")String id) {
        settings.requireManagement();var value=service.buscarPaso(RestParameters.uuid(id,"id"));if(value==null)throw new ApiException(404,"Paso no encontrado");return Datos.from(value);
    }
    @POST @Consumes(MediaType.APPLICATION_JSON) public Response crear(JsonObject body) {
        settings.requireManagement();var input=new RestInput(body,"idProcedimiento","idPasoOrigen","nombre","idRol","indicaFin","tipoSecuencia","idsExamenes");
        var origin=input.text("idPasoOrigen");var value=service.crearPaso(input.uuid("idProcedimiento"),origin==null?null:RestParameters.uuid(origin,"idPasoOrigen"),input.text("nombre"),input.uuid("idRol"),Boolean.TRUE.equals(input.bool("indicaFin")),input.text("tipoSecuencia"),input.uuidList("idsExamenes"));
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdProcedimientoPaso().toString()).build()).entity(Datos.from(value)).build();
    }
    @PUT @Path("{id}") @Consumes(MediaType.APPLICATION_JSON) public Datos actualizar(@PathParam("id")String id,JsonObject body) {
        settings.requireManagement();var uuid=RestParameters.uuid(id,"id");var current=service.buscarPaso(uuid);if(current==null)throw new ApiException(404,"Paso no encontrado");
        var input=new RestInput(body,"nombre","idRol","indicaFin");
        service.actualizarPaso(uuid,input.text("nombre"),input.uuid("idRol"),Boolean.TRUE.equals(input.bool("indicaFin")),service.examenesPaso(uuid));
        return Datos.from(service.buscarPaso(uuid));
    }
    @DELETE @Path("{id}") public Response eliminar(@PathParam("id")String id) {
        settings.requireManagement();var uuid=RestParameters.uuid(id,"id");if(service.buscarPaso(uuid)==null)throw new ApiException(404,"Paso no encontrado");service.eliminarPaso(uuid);return Response.noContent().build();
    }
}
