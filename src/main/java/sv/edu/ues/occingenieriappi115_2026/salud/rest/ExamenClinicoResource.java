package sv.edu.ues.occingenieriappi115_2026.salud.rest;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.servlet.http.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Órdenes y resultados dentro de una clínica autorizada; sin entidades JPA en HTTP. */
@RequestScoped @Path("v2") @Produces(MediaType.APPLICATION_JSON)
public class ExamenClinicoResource {
    @EJB ConsultaFlujoService service;
    @Inject ApiSettings settings;
    @Context HttpServletRequest request;
    @Context HttpServletResponse response;
    @Context UriInfo uri;
    private UUID clinic(String value) {
        settings.requireManagement();var id=RestParameters.uuid(value,"idClinica");
        settings.requireClinic(request.getUserPrincipal().getName(),id);return id;
    }
    private static String instant(Date value) { return value==null?null:value.toInstant().toString(); }
    @JsonbNillable public record OrdenDatos(String idOrdenExamen, String idConsultaProcedimientoPaso, String fechaCreacion, String indicaciones) {
        static OrdenDatos from(OrdenExamen value) { return new OrdenDatos(value.getIdOrdenExamen().toString(),value.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso().toString(),instant(value.getFechaCreacion()),value.getIndicaciones()); }
    }
    @GET @Path("orden_examen")
    public List<OrdenDatos> listarOrden(@QueryParam("idClinica") String c,@QueryParam("idPaso") String parent,@QueryParam("first") String first,@QueryParam("max") String max) {
        var clinic=clinic(c);var id=RestParameters.uuid(parent,"idPaso");var page=RestParameters.page(first,max,settings);
        response.setHeader("Total-registros",Long.toString(service.contarOrden(id,clinic)));
        return service.listarOrden(id,clinic,page.first(),page.max()).stream().map(OrdenDatos::from).toList();
    }
    @GET @Path("orden_examen/{id}")
    public OrdenDatos detalleOrden(@PathParam("id") String id,@QueryParam("idClinica") String c) {
        var value=service.buscarOrden(RestParameters.uuid(id,"id"),clinic(c));if(value==null)throw new ApiException(404,"Registro no encontrado");return OrdenDatos.from(value);
    }
    private OrdenExamen leerOrden(JsonObject body,UUID id) {
        var input=new RestInput(body,"idConsultaProcedimientoPaso","fechaCreacion","indicaciones");var value=new OrdenExamen(id);
        value.setIdConsultaProcedimientoPaso(new ConsultaProcedimientoPaso(input.uuid("idConsultaProcedimientoPaso")));value.setFechaCreacion(input.date("fechaCreacion"));
        if(value.getFechaCreacion()==null)throw new ApiException(422,"La fecha de creación es requerida");
        value.setIndicaciones(input.text("indicaciones"));
        return value;
    }
    @POST @Path("orden_examen") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearOrden(@QueryParam("idClinica") String c,JsonObject body) {
        var clinic=clinic(c);var value=service.guardarOrden(leerOrden(body,null),clinic,false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdOrdenExamen().toString()).queryParam("idClinica",clinic).build()).entity(OrdenDatos.from(value)).build();
    }
    @PUT @Path("orden_examen/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public OrdenDatos actualizarOrden(@PathParam("id") String id,@QueryParam("idClinica") String c,JsonObject body) {
        return OrdenDatos.from(service.guardarOrden(leerOrden(body,RestParameters.uuid(id,"id")),clinic(c),true));
    }
    @JsonbNillable public record ResultadoDatos(String idExamenResultado, String idOrdenExamen, String fechaCreacion, String resultado, String interpretacion, String rutaAtestado) {
        static ResultadoDatos from(ExamenResultado value) { return new ResultadoDatos(value.getIdExamenResultado().toString(),value.getIdOrdenExamen().getIdOrdenExamen().toString(),instant(value.getFechaCreacion()),value.getResultado(),value.getInterpretacion(),value.getRutaAtestado()); }
    }
    @GET @Path("examen_resultado")
    public List<ResultadoDatos> listarResultado(@QueryParam("idClinica") String c,@QueryParam("idOrden") String parent,@QueryParam("first") String first,@QueryParam("max") String max) {
        var clinic=clinic(c);var id=RestParameters.uuid(parent,"idOrden");var page=RestParameters.page(first,max,settings);
        response.setHeader("Total-registros",Long.toString(service.contarResultado(id,clinic)));
        return service.listarResultado(id,clinic,page.first(),page.max()).stream().map(ResultadoDatos::from).toList();
    }
    @GET @Path("examen_resultado/{id}")
    public ResultadoDatos detalleResultado(@PathParam("id") String id,@QueryParam("idClinica") String c) {
        var value=service.buscarResultado(RestParameters.uuid(id,"id"),clinic(c));if(value==null)throw new ApiException(404,"Registro no encontrado");return ResultadoDatos.from(value);
    }
    private ExamenResultado leerResultado(JsonObject body,UUID id) {
        var input=new RestInput(body,"idOrdenExamen","fechaCreacion","resultado","interpretacion","rutaAtestado");var value=new ExamenResultado(id);
        value.setIdOrdenExamen(new OrdenExamen(input.uuid("idOrdenExamen")));value.setFechaCreacion(input.date("fechaCreacion"));
        if(value.getFechaCreacion()==null)throw new ApiException(422,"La fecha de creación es requerida");
        value.setResultado(input.text("resultado"));value.setInterpretacion(input.text("interpretacion"));value.setRutaAtestado(input.text("rutaAtestado"));
        return value;
    }
    @POST @Path("examen_resultado") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearResultado(@QueryParam("idClinica") String c,JsonObject body) {
        var clinic=clinic(c);var value=service.guardarResultado(leerResultado(body,null),clinic,false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdExamenResultado().toString()).queryParam("idClinica",clinic).build()).entity(ResultadoDatos.from(value)).build();
    }
    @PUT @Path("examen_resultado/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public ResultadoDatos actualizarResultado(@PathParam("id") String id,@QueryParam("idClinica") String c,JsonObject body) {
        return ResultadoDatos.from(service.guardarResultado(leerResultado(body,RestParameters.uuid(id,"id")),clinic(c),true));
    }
}
