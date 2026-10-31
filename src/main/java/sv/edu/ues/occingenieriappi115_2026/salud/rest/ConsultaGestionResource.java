package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Flujo de Consulta del primer entregable, sobre base sintética y clínica autorizada. */
@RequestScoped @Path("v2") @Produces(MediaType.APPLICATION_JSON)
public class ConsultaGestionResource {
    @EJB ConsultaFlujoService service;
    @Inject ApiSettings settings;
    @Context HttpServletRequest request;
    @Context HttpServletResponse response;
    @Context UriInfo uri;
    private UUID clinic(String raw) {
        settings.requireManagement();
        UUID id = RestParameters.uuid(raw, "idClinica");
        settings.requireClinic(request.getUserPrincipal().getName(), id);
        return id;
    }
    private static String instant(Date date) { return date == null ? null : date.toInstant().toString(); }
    @GET @Path("clinica_disponible")
    public List<ClinicaDto> clinicas(@QueryParam("first") String first, @QueryParam("max") String max) {
        settings.requireManagement();var page=RestParameters.page(first,max,settings);
        var ids=settings.clinicsFor(request.getUserPrincipal().getName());
        response.setHeader("Total-registros",Long.toString(service.contarClinicasDisponibles(ids)));
        return service.clinicasDisponibles(ids,page.first(),page.max()).stream().map(ClinicaDto::new).toList();
    }
    @JsonbNillable
    public record ConsultaDatos(String idConsulta, String idPersonaRol, String idClinica, String personaNombre,
            String rolNombre, String clinicaNombre, String fechaInicio, String fechaFin, String referenciaExterna, String observaciones) {
        static ConsultaDatos from(Consulta value) {
            var dto = new ConsultaDto(value);
            return new ConsultaDatos(dto.getIdConsulta(), dto.getIdPersonaRol(), dto.getIdClinica(), dto.getPersonaNombre(),
                    dto.getRolNombre(), dto.getClinicaNombre(), dto.getFechaInicio(), dto.getFechaFin(), value.getReferenciaExterna(), value.getObservaciones());
        }
    }
    @GET @Path("consulta")
    public List<ConsultaDatos> listar(@QueryParam("idClinica") String c, @QueryParam("first") String first,
            @QueryParam("max") String max, @QueryParam("fechaDesde") String desde, @QueryParam("fechaHasta") String hasta,
            @QueryParam("idPersonaRol") String paciente) {
        var clinic = clinic(c);var page = RestParameters.page(first,max,settings);
        var from = ConsultaResource.date(desde,"fechaDesde");var to = ConsultaResource.date(hasta,"fechaHasta");
        var p = paciente == null ? null : RestParameters.uuid(paciente,"idPersonaRol");
        var rows = service.buscarConsultas(clinic, from, to, settings.getClinicZone(), p, page.first(), page.max());
        response.setHeader("Total-registros", Long.toString(service.contarConsultas(clinic,from,to,settings.getClinicZone(),p)));
        return rows.stream().map(ConsultaDatos::from).toList();
    }
    @GET @Path("consulta/{id}")
    public ConsultaDatos detalle(@PathParam("id") String id, @QueryParam("idClinica") String c) {
        var value = service.buscarConsulta(RestParameters.uuid(id,"id"), clinic(c));
        if (value == null) throw new ApiException(404,"Consulta no encontrada");
        return ConsultaDatos.from(value);
    }
    private Consulta leer(JsonObject body, UUID id) {
        var input = new RestInput(body,"idPersonaRol","fechaInicio","fechaFin","referenciaExterna","observaciones");
        var value = new Consulta();value.setIdConsulta(id);value.setIdPersonaRol(new PersonaRol(input.uuid("idPersonaRol")));
        value.setFechaInicio(input.date("fechaInicio"));value.setFechaFin(input.date("fechaFin"));
        value.setReferenciaExterna(input.text("referenciaExterna"));value.setObservaciones(input.text("observaciones"));return value;
    }
    @POST @Path("consulta") @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(@QueryParam("idClinica") String c, JsonObject body) {
        var clinic = clinic(c);var value = service.guardarConsulta(leer(body,null),clinic,false);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdConsulta().toString()).queryParam("idClinica",clinic).build())
                .entity(ConsultaDatos.from(value)).build();
    }
    @PUT @Path("consulta/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public ConsultaDatos actualizar(@PathParam("id") String id, @QueryParam("idClinica") String c, JsonObject body) {
        return ConsultaDatos.from(service.guardarConsulta(leer(body,RestParameters.uuid(id,"id")),clinic(c),true));
    }
    @JsonbNillable public record PacienteDatos(String idPersonaRol, String idPersona, String idClinica, String nombres, String apellidos, String rolNombre) {
        static PacienteDatos from(PersonaRol value) {
            return new PacienteDatos(value.getIdPersonaRol().toString(), value.getIdPersona().getIdPersona().toString(),
                    value.getIdClinica().getIdClinica().toString(),value.getIdPersona().getNombres(),value.getIdPersona().getApellidos(),value.getIdRol().getNombre());
        }
    }
    @GET @Path("persona_rol/pacientes")
    public List<PacienteDatos> pacientes(@QueryParam("idClinica") String c,@QueryParam("buscar") String buscar) {
        return service.buscarPacientes(clinic(c),buscar).stream().map(PacienteDatos::from).toList();
    }
    @JsonbNillable public record AsociacionDatos(String idConsultaProcedimiento,String idConsulta,String idProcedimiento,String fechaInicio,String fechaFin,String observaciones) {
        static AsociacionDatos from(ConsultaProcedimiento value) {
            return new AsociacionDatos(value.getIdConsultaProcedimiento().toString(),value.getIdConsulta().getIdConsulta().toString(),
                    value.getIdProcedimiento().toString(),instant(value.getFechaInicio()),instant(value.getFechaFin()),value.getObservaciones());
        }
    }
    @GET @Path("consulta_procedimiento")
    public List<AsociacionDatos> asociaciones(@QueryParam("idClinica") String c,@QueryParam("idConsulta") String consulta,
            @QueryParam("first") String first,@QueryParam("max") String max) {
        var clinic=clinic(c);var parent=RestParameters.uuid(consulta,"idConsulta");var page=RestParameters.page(first,max,settings);
        response.setHeader("Total-registros",Long.toString(service.contarProcedimientosConsulta(clinic,parent)));
        return service.procedimientosConsulta(clinic,parent,page.first(),page.max()).stream().map(AsociacionDatos::from).toList();
    }
    @GET @Path("consulta_procedimiento/{id}")
    public AsociacionDatos asociacion(@PathParam("id") String id,@QueryParam("idClinica") String c) {
        var value=service.buscarProcedimientoConsulta(clinic(c),RestParameters.uuid(id,"id"));
        if(value==null)throw new ApiException(404,"Procedimiento de consulta no encontrado");return AsociacionDatos.from(value);
    }
    private ConsultaProcedimiento leerAsociacion(JsonObject body,UUID id) {
        var input=new RestInput(body,"idConsulta","idProcedimiento","fechaInicio","fechaFin","observaciones");
        var value=new ConsultaProcedimiento();value.setIdConsultaProcedimiento(id);value.setIdConsulta(new Consulta(input.uuid("idConsulta")));
        value.setIdProcedimiento(input.uuid("idProcedimiento"));value.setFechaInicio(input.date("fechaInicio"));value.setFechaFin(input.date("fechaFin"));
        value.setObservaciones(input.text("observaciones"));return value;
    }
    @POST @Path("consulta_procedimiento") @Consumes(MediaType.APPLICATION_JSON)
    public Response crearAsociacion(@QueryParam("idClinica") String c,JsonObject body) {
        var clinic=clinic(c);var value=service.crearProcedimiento(leerAsociacion(body,null),clinic);
        return Response.created(uri.getAbsolutePathBuilder().path(value.getIdConsultaProcedimiento().toString()).queryParam("idClinica",clinic).build()).entity(AsociacionDatos.from(value)).build();
    }
    @PUT @Path("consulta_procedimiento/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public AsociacionDatos actualizarAsociacion(@PathParam("id") String id,@QueryParam("idClinica") String c,JsonObject body) {
        return AsociacionDatos.from(service.actualizarProcedimiento(leerAsociacion(body,RestParameters.uuid(id,"id")),clinic(c)));
    }
    @JsonbNillable public record PasoDatos(String idConsultaProcedimientoPaso,String idConsultaProcedimiento,String idPersonaRol,String fechaInicio,String fechaFin,String estado) {
        static PasoDatos from(ConsultaProcedimientoPaso value) {
            return new PasoDatos(value.getIdConsultaProcedimientoPaso().toString(),value.getIdConsultaProcedimiento().getIdConsultaProcedimiento().toString(),
                    value.getIdPersonaRol().getIdPersonaRol().toString(),instant(value.getFechaInicio()),instant(value.getFechaFin()),value.getEstado());
        }
    }
    @GET @Path("consulta_procedimiento_paso")
    public List<PasoDatos> pasos(@QueryParam("idClinica") String c,@QueryParam("idConsulta") String consulta,@QueryParam("first") String first,@QueryParam("max") String max) {
        var clinic=clinic(c);var parent=RestParameters.uuid(consulta,"idConsulta");var page=RestParameters.page(first,max,settings);
        response.setHeader("Total-registros",Long.toString(service.contarPasosConsulta(clinic,parent)));
        return service.pasosConsulta(clinic,parent,page.first(),page.max()).stream().map(PasoDatos::from).toList();
    }
    @GET @Path("consulta_procedimiento_paso/{id}")
    public PasoDatos paso(@PathParam("id") String id,@QueryParam("idClinica") String c) {
        var value=service.buscarPasoConsulta(clinic(c),RestParameters.uuid(id,"id"));
        if(value==null)throw new ApiException(404,"Paso de consulta no encontrado");return PasoDatos.from(value);
    }
    @PUT @Path("consulta_procedimiento_paso/{id}") @Consumes(MediaType.APPLICATION_JSON)
    public PasoDatos actualizarPaso(@PathParam("id") String id,@QueryParam("idClinica") String c,JsonObject body) {
        var clinic=clinic(c);var input=new RestInput(body,"idConsultaProcedimiento","idPersonaRol","fechaInicio","fechaFin","estado");
        var value=new ConsultaProcedimientoPaso();value.setIdConsultaProcedimientoPaso(RestParameters.uuid(id,"id"));
        value.setIdConsultaProcedimiento(new ConsultaProcedimiento(input.uuid("idConsultaProcedimiento")));
        value.setIdPersonaRol(new PersonaRol(input.uuid("idPersonaRol")));value.setFechaInicio(input.date("fechaInicio"));value.setFechaFin(input.date("fechaFin"));value.setEstado(input.text("estado"));
        return PasoDatos.from(service.actualizarPaso(value,clinic));
    }
}
