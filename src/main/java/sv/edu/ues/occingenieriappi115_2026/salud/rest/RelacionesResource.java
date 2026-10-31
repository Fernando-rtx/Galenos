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
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Relaciones de catálogo: las reglas y transacciones pertenecen a los servicios JSF compartidos. */
@RequestScoped @Path("v2") @Produces(MediaType.APPLICATION_JSON)
public class RelacionesResource {
    @EJB CatalogoService catalogo;
    @EJB ProcedimientoFlujoService flujo;
    @Inject ApiSettings settings;
    @Context HttpServletResponse response;
    @Context UriInfo uri;
    private static String instant(Date value){return value==null?null:value.toInstant().toString();}
    @JsonbNillable public record ExamenTipoExamenDatos(String idExamenTipoExamen,String idExamen,String idTipoExamen,String fechaCreacion,String observaciones) {
        static ExamenTipoExamenDatos from(ExamenTipoExamen p){return new ExamenTipoExamenDatos(p.getIdExamenTipoExamen().toString(),p.getIdExamen().getIdExamen().toString(),p.getIdTipoExamen().getIdTipoExamen().toString(),instant(p.getFechaCreacion()),p.getObservaciones());}
    }
    @GET @Path("examen_tipo_examen") public List<ExamenTipoExamenDatos> listarExamenTipoExamen(@QueryParam("idExamen")String id,@QueryParam("first")String first,@QueryParam("max")String max) {
        settings.requireManagement();var parent=RestParameters.uuid(id,"idExamen");var page=RestParameters.page(first,max,settings);
        response.setHeader("Total-registros",Long.toString(catalogo.contarRelacionesExamen(parent)));
        return catalogo.relacionesExamen(parent,page.first(),page.max()).stream().map(ExamenTipoExamenDatos::from).toList();
    }
    @GET @Path("examen_tipo_examen/{id}") public ExamenTipoExamenDatos detalleExamenTipoExamen(@PathParam("id")String id) {
        settings.requireManagement();var value=catalogo.buscarRelacionExamen(RestParameters.uuid(id,"id"));if(value==null)throw new ApiException(404,"Relación no encontrada");return ExamenTipoExamenDatos.from(value);
    }
    private ExamenTipoExamen guardarExamenTipoExamen(JsonObject body,UUID id) {
        var input=new RestInput(body,"idExamen","idTipoExamen","observaciones");var old=id==null?null:catalogo.buscarRelacionExamen(id);
        if(id!=null&&old==null)throw new ApiException(404,"Relación no encontrada");var value=new ExamenTipoExamen(id);var parent=catalogo.buscarExamen(input.uuid("idExamen"));if(parent==null)throw new ApiException(404,"Registro padre no encontrado");value.setIdExamen(parent);value.setIdTipoExamen(new TipoExamen(input.uuid("idTipoExamen")));value.setFechaCreacion(old==null?new Date():old.getFechaCreacion());value.setObservaciones(input.text("observaciones"));
        return catalogo.guardarRelacionExamen(value,id!=null,old==null?null:old.getIdExamen().getIdExamen(),old==null?null:old.getIdTipoExamen().getIdTipoExamen());
    }
    @POST @Path("examen_tipo_examen") @Consumes(MediaType.APPLICATION_JSON) public Response crearExamenTipoExamen(JsonObject body) {
        settings.requireManagement();var value=guardarExamenTipoExamen(body,null);return Response.created(uri.getAbsolutePathBuilder().path(value.getIdExamenTipoExamen().toString()).build()).entity(ExamenTipoExamenDatos.from(value)).build();
    }
    @PUT @Path("examen_tipo_examen/{id}") @Consumes(MediaType.APPLICATION_JSON) public ExamenTipoExamenDatos actualizarExamenTipoExamen(@PathParam("id")String id,JsonObject body) {
        settings.requireManagement();return ExamenTipoExamenDatos.from(guardarExamenTipoExamen(body,RestParameters.uuid(id,"id")));
    }
    @DELETE @Path("examen_tipo_examen/{id}") public Response eliminarExamenTipoExamen(@PathParam("id")String id) {
        settings.requireManagement();var uuid=RestParameters.uuid(id,"id");if(catalogo.buscarRelacionExamen(uuid)==null)throw new ApiException(404,"Relación no encontrada");catalogo.eliminarRelacionExamen(uuid);return Response.noContent().build();
    }
    @JsonbNillable public record ProcedimientoPasoExamenDatos(String idProcedimientoPasoExamen,String idProcedimientoPaso,String idExamen,String fechaCreacion,String observaciones,Boolean activo) {
        static ProcedimientoPasoExamenDatos from(ProcedimientoPasoExamen p){return new ProcedimientoPasoExamenDatos(p.getIdProcedimientoPasoExamen().toString(),p.getIdProcedimientoPaso().getIdProcedimientoPaso().toString(),p.getIdExamen().getIdExamen().toString(),instant(p.getFechaCreacion()),p.getObservaciones(),p.getActivo());}
    }
    @GET @Path("procedimiento_paso_examen") public List<ProcedimientoPasoExamenDatos> listarProcedimientoPasoExamen(@QueryParam("idProcedimientoPaso")String id,@QueryParam("first")String first,@QueryParam("max")String max) {
        settings.requireManagement();var parent=RestParameters.uuid(id,"idProcedimientoPaso");var page=RestParameters.page(first,max,settings);
        response.setHeader("Total-registros",Long.toString(flujo.contarRelacionesPaso(parent)));
        return flujo.relacionesPaso(parent,page.first(),page.max()).stream().map(ProcedimientoPasoExamenDatos::from).toList();
    }
    @GET @Path("procedimiento_paso_examen/{id}") public ProcedimientoPasoExamenDatos detalleProcedimientoPasoExamen(@PathParam("id")String id) {
        settings.requireManagement();var value=flujo.buscarRelacionPaso(RestParameters.uuid(id,"id"));if(value==null)throw new ApiException(404,"Relación no encontrada");return ProcedimientoPasoExamenDatos.from(value);
    }
    private ProcedimientoPasoExamen guardarProcedimientoPasoExamen(JsonObject body,UUID id) {
        var input=new RestInput(body,"idProcedimientoPaso","idExamen","observaciones","activo");var old=id==null?null:flujo.buscarRelacionPaso(id);
        if(id!=null&&old==null)throw new ApiException(404,"Relación no encontrada");var value=new ProcedimientoPasoExamen(id);var parent=flujo.buscarPaso(input.uuid("idProcedimientoPaso"));if(parent==null)throw new ApiException(404,"Registro padre no encontrado");value.setIdProcedimientoPaso(parent);value.setIdExamen(new Examen(input.uuid("idExamen")));value.setFechaCreacion(old==null?new Date():old.getFechaCreacion());value.setObservaciones(input.text("observaciones"));value.setActivo(input.bool("activo"));
        return flujo.guardarPasoExamen(value,id!=null,old==null?null:old.getIdProcedimientoPaso().getIdProcedimientoPaso(),old==null?null:old.getIdExamen().getIdExamen(),parent.getIdProcedimiento().getIdProcedimiento());
    }
    @POST @Path("procedimiento_paso_examen") @Consumes(MediaType.APPLICATION_JSON) public Response crearProcedimientoPasoExamen(JsonObject body) {
        settings.requireManagement();var value=guardarProcedimientoPasoExamen(body,null);return Response.created(uri.getAbsolutePathBuilder().path(value.getIdProcedimientoPasoExamen().toString()).build()).entity(ProcedimientoPasoExamenDatos.from(value)).build();
    }
    @PUT @Path("procedimiento_paso_examen/{id}") @Consumes(MediaType.APPLICATION_JSON) public ProcedimientoPasoExamenDatos actualizarProcedimientoPasoExamen(@PathParam("id")String id,JsonObject body) {
        settings.requireManagement();return ProcedimientoPasoExamenDatos.from(guardarProcedimientoPasoExamen(body,RestParameters.uuid(id,"id")));
    }
    @DELETE @Path("procedimiento_paso_examen/{id}") public Response eliminarProcedimientoPasoExamen(@PathParam("id")String id) {
        settings.requireManagement();var uuid=RestParameters.uuid(id,"id");if(flujo.buscarRelacionPaso(uuid)==null)throw new ApiException(404,"Relación no encontrada");flujo.eliminarRelacionPaso(uuid);return Response.noContent().build();
    }
    @JsonbNillable public record ProcedimientoPasoSecuenciaDatos(String idProcedimientoPasoSecuencia,String idProcedimientoPaso,String idProcedimientoPasoReferencia,String tipoSecuencia) {
        static ProcedimientoPasoSecuenciaDatos from(ProcedimientoPasoSecuencia p){return new ProcedimientoPasoSecuenciaDatos(p.getIdProcedimientoPasoSecuencia().toString(),p.getIdProcedimientoPaso().getIdProcedimientoPaso().toString(),p.getIdProcedimientoPasoReferencia().toString(),p.getTipoSecuencia());}
    }
    @GET @Path("procedimiento_paso_secuencia") public List<ProcedimientoPasoSecuenciaDatos> listarProcedimientoPasoSecuencia(@QueryParam("idProcedimientoPaso")String id,@QueryParam("first")String first,@QueryParam("max")String max) {
        settings.requireManagement();var parent=RestParameters.uuid(id,"idProcedimientoPaso");var page=RestParameters.page(first,max,settings);
        response.setHeader("Total-registros",Long.toString(flujo.contarSecuenciasPaso(parent)));
        return flujo.secuenciasPaso(parent,page.first(),page.max()).stream().map(ProcedimientoPasoSecuenciaDatos::from).toList();
    }
    @GET @Path("procedimiento_paso_secuencia/{id}") public ProcedimientoPasoSecuenciaDatos detalleProcedimientoPasoSecuencia(@PathParam("id")String id) {
        settings.requireManagement();var value=flujo.buscarSecuencia(RestParameters.uuid(id,"id"));if(value==null)throw new ApiException(404,"Relación no encontrada");return ProcedimientoPasoSecuenciaDatos.from(value);
    }
    private ProcedimientoPasoSecuencia guardarProcedimientoPasoSecuencia(JsonObject body,UUID id) {
        var input=new RestInput(body,"idProcedimientoPaso","idProcedimientoPasoReferencia","tipoSecuencia");var old=id==null?null:flujo.buscarSecuencia(id);
        if(id!=null&&old==null)throw new ApiException(404,"Relación no encontrada");var value=new ProcedimientoPasoSecuencia(id);var parent=flujo.buscarPaso(input.uuid("idProcedimientoPaso"));if(parent==null)throw new ApiException(404,"Registro padre no encontrado");value.setIdProcedimientoPaso(parent);value.setIdProcedimientoPasoReferencia(input.uuid("idProcedimientoPasoReferencia"));value.setTipoSecuencia(input.text("tipoSecuencia"));
        return flujo.guardarSecuencia(value,id!=null,old==null?null:old.getIdProcedimientoPaso().getIdProcedimientoPaso(),old==null?null:old.getIdProcedimientoPasoReferencia(),parent.getIdProcedimiento().getIdProcedimiento());
    }
    @POST @Path("procedimiento_paso_secuencia") @Consumes(MediaType.APPLICATION_JSON) public Response crearProcedimientoPasoSecuencia(JsonObject body) {
        settings.requireManagement();var value=guardarProcedimientoPasoSecuencia(body,null);return Response.created(uri.getAbsolutePathBuilder().path(value.getIdProcedimientoPasoSecuencia().toString()).build()).entity(ProcedimientoPasoSecuenciaDatos.from(value)).build();
    }
    @PUT @Path("procedimiento_paso_secuencia/{id}") @Consumes(MediaType.APPLICATION_JSON) public ProcedimientoPasoSecuenciaDatos actualizarProcedimientoPasoSecuencia(@PathParam("id")String id,JsonObject body) {
        settings.requireManagement();return ProcedimientoPasoSecuenciaDatos.from(guardarProcedimientoPasoSecuencia(body,RestParameters.uuid(id,"id")));
    }
    @DELETE @Path("procedimiento_paso_secuencia/{id}") public Response eliminarProcedimientoPasoSecuencia(@PathParam("id")String id) {
        settings.requireManagement();var uuid=RestParameters.uuid(id,"id");if(flujo.buscarSecuencia(uuid)==null)throw new ApiException(404,"Relación no encontrada");flujo.eliminarSecuencia(uuid);return Response.noContent().build();
    }
}
