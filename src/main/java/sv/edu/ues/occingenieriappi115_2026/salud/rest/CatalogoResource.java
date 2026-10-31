package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.CatalogoService;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Catálogos de gestión; la autenticación de contenedor protege /v2. */
@RequestScoped
@Path("v2")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogoResource {
    @EJB CatalogoService service;
    @Inject ApiSettings settings;
    @Context HttpServletResponse response;
    @Context UriInfo uri;
    @jakarta.json.bind.annotation.JsonbNillable
    public record TipoDocumentoDatos(String idTipoDocumento, String nombre, String indicaciones, String expresionRegular, Boolean activo) {
        static TipoDocumentoDatos from(TipoDocumento e) { return new TipoDocumentoDatos(e.getIdTipoDocumento().toString(), e.getNombre(), e.getIndicaciones(), e.getExpresionRegular(), e.getActivo()); }
    }
    @GET @Path("tipo_documento")
    public List<TipoDocumentoDatos> listarTipoDocumento(@QueryParam("first") String first, @QueryParam("max") String max,
            @QueryParam("nombre") String nombre) {
        settings.requireManagement();
        var page = RestParameters.page(first, max, settings);
        var result = service.listarTipoDocumento(page.first(), page.max(), nombre).stream().map(TipoDocumentoDatos::from).toList();
        response.setHeader("Total-registros", Long.toString(service.contarTipoDocumento(nombre)));
        return result;
    }
    @GET @Path("tipo_documento/{id}")
    public TipoDocumentoDatos detalleTipoDocumento(@PathParam("id") String id) {
        settings.requireManagement();
        var value = service.buscarTipoDocumento(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return TipoDocumentoDatos.from(value);
    }
    @POST @Path("tipo_documento") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearTipoDocumento(JsonObject body) {
        settings.requireManagement();
        var value = leerTipoDocumento(body, new TipoDocumento());
        value = service.guardarTipoDocumento(value, false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdTipoDocumento().toString()).build())
                .entity(TipoDocumentoDatos.from(value)).build();
    }
    @PUT @Path("tipo_documento/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public TipoDocumentoDatos actualizarTipoDocumento(@PathParam("id") String id, JsonObject body) {
        settings.requireManagement();
        var value = service.buscarTipoDocumento(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return TipoDocumentoDatos.from(service.guardarTipoDocumento(leerTipoDocumento(body, value), true));
    }
    private static TipoDocumento leerTipoDocumento(JsonObject body, TipoDocumento value) {
        var input = new RestInput(body, "nombre", "indicaciones", "expresionRegular", "activo");
        value.setNombre(input.text("nombre"));
        value.setIndicaciones(input.text("indicaciones"));
        value.setExpresionRegular(input.text("expresionRegular"));
        value.setActivo(input.bool("activo"));
        return value;
    }
    @jakarta.json.bind.annotation.JsonbNillable
    public record TipoMedioContactoDatos(String idTipoMedioContacto, String nombre, String indicaciones, String expresionRegular, Boolean activo) {
        static TipoMedioContactoDatos from(TipoMedioContacto e) { return new TipoMedioContactoDatos(e.getIdTipoMedioContacto().toString(), e.getNombre(), e.getIndicaciones(), e.getExpresionRegular(), e.getActivo()); }
    }
    @GET @Path("tipo_medio_contacto")
    public List<TipoMedioContactoDatos> listarTipoMedioContacto(@QueryParam("first") String first, @QueryParam("max") String max,
            @QueryParam("nombre") String nombre) {
        settings.requireManagement();
        var page = RestParameters.page(first, max, settings);
        var result = service.listarTipoMedioContacto(page.first(), page.max(), nombre).stream().map(TipoMedioContactoDatos::from).toList();
        response.setHeader("Total-registros", Long.toString(service.contarTipoMedioContacto(nombre)));
        return result;
    }
    @GET @Path("tipo_medio_contacto/{id}")
    public TipoMedioContactoDatos detalleTipoMedioContacto(@PathParam("id") String id) {
        settings.requireManagement();
        var value = service.buscarTipoMedioContacto(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return TipoMedioContactoDatos.from(value);
    }
    @POST @Path("tipo_medio_contacto") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearTipoMedioContacto(JsonObject body) {
        settings.requireManagement();
        var value = leerTipoMedioContacto(body, new TipoMedioContacto());
        value = service.guardarTipoMedioContacto(value, false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdTipoMedioContacto().toString()).build())
                .entity(TipoMedioContactoDatos.from(value)).build();
    }
    @PUT @Path("tipo_medio_contacto/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public TipoMedioContactoDatos actualizarTipoMedioContacto(@PathParam("id") String id, JsonObject body) {
        settings.requireManagement();
        var value = service.buscarTipoMedioContacto(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return TipoMedioContactoDatos.from(service.guardarTipoMedioContacto(leerTipoMedioContacto(body, value), true));
    }
    private static TipoMedioContacto leerTipoMedioContacto(JsonObject body, TipoMedioContacto value) {
        var input = new RestInput(body, "nombre", "indicaciones", "expresionRegular", "activo");
        value.setNombre(input.text("nombre"));
        value.setIndicaciones(input.text("indicaciones"));
        value.setExpresionRegular(input.text("expresionRegular"));
        value.setActivo(input.bool("activo"));
        return value;
    }
    @jakarta.json.bind.annotation.JsonbNillable
    public record TipoExamenDatos(String idTipoExamen, String nombre, String observaciones, Boolean activo) {
        static TipoExamenDatos from(TipoExamen e) { return new TipoExamenDatos(e.getIdTipoExamen().toString(), e.getNombre(), e.getObservaciones(), e.getActivo()); }
    }
    @GET @Path("tipo_examen")
    public List<TipoExamenDatos> listarTipoExamen(@QueryParam("first") String first, @QueryParam("max") String max,
            @QueryParam("nombre") String nombre) {
        settings.requireManagement();
        var page = RestParameters.page(first, max, settings);
        var result = service.listarTipoExamen(page.first(), page.max(), nombre).stream().map(TipoExamenDatos::from).toList();
        response.setHeader("Total-registros", Long.toString(service.contarTipoExamen(nombre)));
        return result;
    }
    @GET @Path("tipo_examen/{id}")
    public TipoExamenDatos detalleTipoExamen(@PathParam("id") String id) {
        settings.requireManagement();
        var value = service.buscarTipoExamen(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return TipoExamenDatos.from(value);
    }
    @POST @Path("tipo_examen") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearTipoExamen(JsonObject body) {
        settings.requireManagement();
        var value = leerTipoExamen(body, new TipoExamen());
        value = service.guardarTipoExamen(value, false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdTipoExamen().toString()).build())
                .entity(TipoExamenDatos.from(value)).build();
    }
    @PUT @Path("tipo_examen/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public TipoExamenDatos actualizarTipoExamen(@PathParam("id") String id, JsonObject body) {
        settings.requireManagement();
        var value = service.buscarTipoExamen(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return TipoExamenDatos.from(service.guardarTipoExamen(leerTipoExamen(body, value), true));
    }
    private static TipoExamen leerTipoExamen(JsonObject body, TipoExamen value) {
        var input = new RestInput(body, "nombre", "observaciones", "activo");
        value.setNombre(input.text("nombre"));
        value.setObservaciones(input.text("observaciones"));
        value.setActivo(input.bool("activo"));
        return value;
    }
    @jakarta.json.bind.annotation.JsonbNillable
    public record ClinicaDatos(String idClinica, String nombre, String tipo, String comentarios, Boolean activo) {
        static ClinicaDatos from(Clinica e) { return new ClinicaDatos(e.getIdClinica().toString(), e.getNombre(), e.getTipo(), e.getComentarios(), e.getActivo()); }
    }
    @GET @Path("clinica")
    public List<ClinicaDatos> listarClinica(@QueryParam("first") String first, @QueryParam("max") String max,
            @QueryParam("nombre") String nombre) {
        settings.requireManagement();
        var page = RestParameters.page(first, max, settings);
        var result = service.listarClinica(page.first(), page.max(), nombre).stream().map(ClinicaDatos::from).toList();
        response.setHeader("Total-registros", Long.toString(service.contarClinica(nombre)));
        return result;
    }
    @GET @Path("clinica/{id}")
    public ClinicaDatos detalleClinica(@PathParam("id") String id) {
        settings.requireManagement();
        var value = service.buscarClinica(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return ClinicaDatos.from(value);
    }
    @POST @Path("clinica") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearClinica(JsonObject body) {
        settings.requireManagement();
        var value = leerClinica(body, new Clinica());
        value = service.guardarClinica(value, false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdClinica().toString()).build())
                .entity(ClinicaDatos.from(value)).build();
    }
    @PUT @Path("clinica/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public ClinicaDatos actualizarClinica(@PathParam("id") String id, JsonObject body) {
        settings.requireManagement();
        var value = service.buscarClinica(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return ClinicaDatos.from(service.guardarClinica(leerClinica(body, value), true));
    }
    private static Clinica leerClinica(JsonObject body, Clinica value) {
        var input = new RestInput(body, "nombre", "tipo", "comentarios", "activo");
        value.setNombre(input.text("nombre"));
        value.setTipo(input.text("tipo"));
        value.setComentarios(input.text("comentarios"));
        value.setActivo(input.bool("activo"));
        return value;
    }
    @DELETE @Path("clinica/{id}")
    public Response eliminarClinica(@PathParam("id") String id) {
        settings.requireManagement();
        service.eliminarClinica(RestParameters.uuid(id,"id"));
        return Response.noContent().build();
    }
    @jakarta.json.bind.annotation.JsonbNillable
    public record RolDatos(String idRol, String nombre, String observaciones, Boolean activo) {
        static RolDatos from(Rol e) { return new RolDatos(e.getIdRol().toString(), e.getNombre(), e.getObservaciones(), e.getActivo()); }
    }
    @GET @Path("rol")
    public List<RolDatos> listarRol(@QueryParam("first") String first, @QueryParam("max") String max,
            @QueryParam("nombre") String nombre) {
        settings.requireManagement();
        var page = RestParameters.page(first, max, settings);
        var result = service.listarRol(page.first(), page.max(), nombre).stream().map(RolDatos::from).toList();
        response.setHeader("Total-registros", Long.toString(service.contarRol(nombre)));
        return result;
    }
    @GET @Path("rol/{id}")
    public RolDatos detalleRol(@PathParam("id") String id) {
        settings.requireManagement();
        var value = service.buscarRol(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return RolDatos.from(value);
    }
    @POST @Path("rol") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearRol(JsonObject body) {
        settings.requireManagement();
        var value = leerRol(body, new Rol());
        value = service.guardarRol(value, false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdRol().toString()).build())
                .entity(RolDatos.from(value)).build();
    }
    @PUT @Path("rol/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public RolDatos actualizarRol(@PathParam("id") String id, JsonObject body) {
        settings.requireManagement();
        var value = service.buscarRol(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return RolDatos.from(service.guardarRol(leerRol(body, value), true));
    }
    private static Rol leerRol(JsonObject body, Rol value) {
        var input = new RestInput(body, "nombre", "observaciones", "activo");
        value.setNombre(input.text("nombre"));
        value.setObservaciones(input.text("observaciones"));
        value.setActivo(input.bool("activo"));
        return value;
    }
    @DELETE @Path("rol/{id}")
    public Response eliminarRol(@PathParam("id") String id) {
        settings.requireManagement();
        service.eliminarRol(RestParameters.uuid(id,"id"));
        return Response.noContent().build();
    }
    @jakarta.json.bind.annotation.JsonbNillable
    public record ExamenDatos(String idExamen, String nombre, String observaciones, Boolean activo) {
        static ExamenDatos from(Examen e) { return new ExamenDatos(e.getIdExamen().toString(), e.getNombre(), e.getObservaciones(), e.getActivo()); }
    }
    @GET @Path("examen")
    public List<ExamenDatos> listarExamen(@QueryParam("first") String first, @QueryParam("max") String max,
            @QueryParam("nombre") String nombre) {
        settings.requireManagement();
        var page = RestParameters.page(first, max, settings);
        var result = service.listarExamen(page.first(), page.max(), nombre).stream().map(ExamenDatos::from).toList();
        response.setHeader("Total-registros", Long.toString(service.contarExamen(nombre)));
        return result;
    }
    @GET @Path("examen/{id}")
    public ExamenDatos detalleExamen(@PathParam("id") String id) {
        settings.requireManagement();
        var value = service.buscarExamen(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return ExamenDatos.from(value);
    }
    @POST @Path("examen") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearExamen(JsonObject body) {
        settings.requireManagement();
        var value = leerExamen(body, new Examen());
        value = service.guardarExamen(value, false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdExamen().toString()).build())
                .entity(ExamenDatos.from(value)).build();
    }
    @PUT @Path("examen/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public ExamenDatos actualizarExamen(@PathParam("id") String id, JsonObject body) {
        settings.requireManagement();
        var value = service.buscarExamen(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return ExamenDatos.from(service.guardarExamen(leerExamen(body, value), true));
    }
    private static Examen leerExamen(JsonObject body, Examen value) {
        var input = new RestInput(body, "nombre", "observaciones", "activo");
        value.setNombre(input.text("nombre"));
        value.setObservaciones(input.text("observaciones"));
        value.setActivo(input.bool("activo"));
        return value;
    }
    @jakarta.json.bind.annotation.JsonbNillable
    public record ProcedimientoDatos(String idProcedimiento, String nombre, String observaciones, Boolean activo) {
        static ProcedimientoDatos from(Procedimiento e) { return new ProcedimientoDatos(e.getIdProcedimiento().toString(), e.getNombre(), e.getObservaciones(), e.getActivo()); }
    }
    @GET @Path("procedimiento")
    public List<ProcedimientoDatos> listarProcedimiento(@QueryParam("first") String first, @QueryParam("max") String max,
            @QueryParam("nombre") String nombre, @QueryParam("activo") String activo) {
        settings.requireManagement();
        var page = RestParameters.page(first, max, settings);
        var result = service.listarProcedimiento(page.first(), page.max(), nombre, booleano(activo)).stream().map(ProcedimientoDatos::from).toList();
        response.setHeader("Total-registros", Long.toString(service.contarProcedimiento(nombre, booleano(activo))));
        return result;
    }
    @GET @Path("procedimiento/{id}")
    public ProcedimientoDatos detalleProcedimiento(@PathParam("id") String id) {
        settings.requireManagement();
        var value = service.buscarProcedimiento(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return ProcedimientoDatos.from(value);
    }
    @POST @Path("procedimiento") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearProcedimiento(JsonObject body) {
        settings.requireManagement();
        var value = leerProcedimiento(body, new Procedimiento());
        value = service.guardarProcedimiento(value, false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdProcedimiento().toString()).build())
                .entity(ProcedimientoDatos.from(value)).build();
    }
    @PUT @Path("procedimiento/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public ProcedimientoDatos actualizarProcedimiento(@PathParam("id") String id, JsonObject body) {
        settings.requireManagement();
        var value = service.buscarProcedimiento(RestParameters.uuid(id,"id"));
        if (value == null) throw new ApiException(404,"Registro no encontrado");
        return ProcedimientoDatos.from(service.guardarProcedimiento(leerProcedimiento(body, value), true));
    }
    private static Procedimiento leerProcedimiento(JsonObject body, Procedimiento value) {
        var input = new RestInput(body, "nombre", "observaciones", "activo");
        value.setNombre(input.text("nombre"));
        value.setObservaciones(input.text("observaciones"));
        value.setActivo(input.bool("activo"));
        return value;
    }
    @DELETE @Path("procedimiento/{id}")
    public Response eliminarProcedimiento(@PathParam("id") String id) {
        settings.requireManagement();
        service.eliminarProcedimiento(RestParameters.uuid(id,"id"));
        return Response.noContent().build();
    }
    private static Boolean booleano(String value) {
        if (value == null) return null;
        if (!value.equals("true") && !value.equals("false")) throw new ApiException(422, "activo debe ser true o false");
        return Boolean.valueOf(value);
    }
}
