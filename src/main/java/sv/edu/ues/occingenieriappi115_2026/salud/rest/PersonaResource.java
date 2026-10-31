package sv.edu.ues.occingenieriappi115_2026.salud.rest;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.servlet.http.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.Instant;
import java.util.*;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Personas y asociaciones: DTO explícitos, nunca colecciones JPA. Solo demo autenticado. */
@RequestScoped @Path("v2") @Produces(MediaType.APPLICATION_JSON)
public class PersonaResource {
 @EJB PersonaFlujoService service;
 @Inject ApiSettings settings;
 @Context HttpServletResponse response;
 @Context HttpServletRequest request;
 @Context UriInfo uri;
 private void enabled(){settings.requireManagement();}
 private Persona persona(UUID id){var p=service.buscarPersona(id);if(p==null)throw new ApiException(404,"Persona no encontrada");return p;}
 private void clinic(UUID id){settings.requireClinic(request.getUserPrincipal().getName(),id);}
 private static String instant(Date value){return value==null?null:Instant.ofEpochMilli(value.getTime()).toString();}
 @JsonbNillable public record PersonaDatos(String idPersona,String nombres,String apellidos,String fechaNacimiento,String fechaCreacion){
  static PersonaDatos from(Persona p){return new PersonaDatos(p.getIdPersona().toString(),p.getNombres(),p.getApellidos(),instant(p.getFechaNacimiento()),instant(p.getFechaCreacion()));}
 }
 @GET @Path("persona")
 public List<PersonaDatos> listar(@QueryParam("first")String first,@QueryParam("max")String max,@QueryParam("nombres")String nombres,@QueryParam("apellidos")String apellidos){
  enabled();var page=RestParameters.page(first,max,settings);response.setHeader("Total-registros",Long.toString(service.contarPersonas(nombres,apellidos)));
  return service.listarPersonas(page.first(),page.max(),nombres,apellidos).stream().map(PersonaDatos::from).toList();
 }
 @GET @Path("persona/{id}") public PersonaDatos detalle(@PathParam("id")String id){enabled();return PersonaDatos.from(persona(RestParameters.uuid(id,"id")));}
 private Persona leerPersona(JsonObject body,UUID id){
  var input=new RestInput(body,"nombres","apellidos","fechaNacimiento");var p=new Persona(id);
  p.setNombres(input.text("nombres"));p.setApellidos(input.text("apellidos"));p.setFechaNacimiento(input.date("fechaNacimiento"));
  p.setFechaCreacion(id==null?new Date():persona(id).getFechaCreacion());return p;
 }
 @POST @Path("persona") @Consumes(MediaType.APPLICATION_JSON)
 public Response crear(JsonObject body){enabled();var value=service.guardarPersona(leerPersona(body,null),false);
  return Response.created(uri.getAbsolutePathBuilder().path(value.getIdPersona().toString()).build()).entity(PersonaDatos.from(value)).build();}
 @PUT @Path("persona/{id}") @Consumes(MediaType.APPLICATION_JSON)
 public PersonaDatos actualizar(@PathParam("id")String id,JsonObject body){enabled();return PersonaDatos.from(service.guardarPersona(leerPersona(body,RestParameters.uuid(id,"id")),true));}

 @JsonbNillable public record DocumentoDatos(String idDocumento,String idPersona,String idTipoDocumento,String valor,String rutaFisica) {
  static DocumentoDatos from(Documento p){return new DocumentoDatos(p.getIdDocumento().toString(),p.getIdPersona().getIdPersona().toString(),p.getIdTipoDocumento().getIdTipoDocumento().toString(),p.getValor(),p.getRutaFisica());}
 }
 @GET @Path("documento")
 public List<DocumentoDatos> listarDocumento(@QueryParam("idPersona")String id,@QueryParam("first")String first,@QueryParam("max")String max){
  enabled();var persona=RestParameters.uuid(id,"idPersona");var page=RestParameters.page(first,max,settings);
  response.setHeader("Total-registros",Long.toString(service.contarDocumentos(persona)));
  return service.documentos(persona,page.first(),page.max()).stream().map(DocumentoDatos::from).toList();
 }
 @GET @Path("documento/{id}")
 public DocumentoDatos detalleDocumento(@PathParam("id")String id){enabled();var value=service.buscarDocumento(RestParameters.uuid(id,"id"));
  if(value==null)throw new ApiException(404,"Registro no encontrado");return DocumentoDatos.from(value);}
 private Documento guardarDocumento(JsonObject body,UUID id){
  var input=new RestInput(body,"idPersona","idTipoDocumento","valor","rutaFisica");var old=id==null?null:service.buscarDocumento(id);
  if(id!=null&&old==null)throw new ApiException(404,"Registro no encontrado");
  var p=new Documento(id);p.setIdPersona(persona(input.uuid("idPersona")));p.setIdTipoDocumento(new TipoDocumento(input.uuid("idTipoDocumento")));p.setValor(input.text("valor"));p.setRutaFisica(input.text("rutaFisica"));
  return service.guardarDocumento(p,id!=null,old==null?null:old.getIdPersona().getIdPersona(),old==null?null:old.getIdTipoDocumento().getIdTipoDocumento());
 }
 @POST @Path("documento") @Consumes(MediaType.APPLICATION_JSON)
 public Response crearDocumento(JsonObject body){enabled();var value=guardarDocumento(body,null);
  return Response.created(uri.getAbsolutePathBuilder().path(value.getIdDocumento().toString()).build()).entity(DocumentoDatos.from(value)).build();}
 @PUT @Path("documento/{id}") @Consumes(MediaType.APPLICATION_JSON)
 public DocumentoDatos actualizarDocumento(@PathParam("id")String id,JsonObject body){enabled();return DocumentoDatos.from(guardarDocumento(body,RestParameters.uuid(id,"id")));}
 @DELETE @Path("documento/{id}")
 public Response eliminarDocumento(@PathParam("id")String id){enabled();var value=service.buscarDocumento(RestParameters.uuid(id,"id"));
  if(value==null)throw new ApiException(404,"Registro no encontrado");
  service.eliminarDocumento(value.getIdDocumento());return Response.noContent().build();}

 @JsonbNillable public record MedioContactoDatos(String idMedioContacto,String idPersona,String idTipoMedioContacto,String valor,String fechaCreacion) {
  static MedioContactoDatos from(MedioContacto p){return new MedioContactoDatos(p.getIdMedioContacto().toString(),p.getIdPersona().getIdPersona().toString(),p.getIdTipoMedioContacto().getIdTipoMedioContacto().toString(),p.getValor(),instant(p.getFechaCreacion()));}
 }
 @GET @Path("medio_contacto")
 public List<MedioContactoDatos> listarMedioContacto(@QueryParam("idPersona")String id,@QueryParam("first")String first,@QueryParam("max")String max){
  enabled();var persona=RestParameters.uuid(id,"idPersona");var page=RestParameters.page(first,max,settings);
  response.setHeader("Total-registros",Long.toString(service.contarContactos(persona)));
  return service.contactos(persona,page.first(),page.max()).stream().map(MedioContactoDatos::from).toList();
 }
 @GET @Path("medio_contacto/{id}")
 public MedioContactoDatos detalleMedioContacto(@PathParam("id")String id){enabled();var value=service.buscarContacto(RestParameters.uuid(id,"id"));
  if(value==null)throw new ApiException(404,"Registro no encontrado");return MedioContactoDatos.from(value);}
 private MedioContacto guardarMedioContacto(JsonObject body,UUID id){
  var input=new RestInput(body,"idPersona","idTipoMedioContacto","valor");var old=id==null?null:service.buscarContacto(id);
  if(id!=null&&old==null)throw new ApiException(404,"Registro no encontrado");
  var p=new MedioContacto(id);p.setIdPersona(persona(input.uuid("idPersona")));p.setIdTipoMedioContacto(new TipoMedioContacto(input.uuid("idTipoMedioContacto")));p.setValor(input.text("valor"));p.setFechaCreacion(old==null?new Date():old.getFechaCreacion());
  return service.guardarContacto(p,id!=null,old==null?null:old.getIdPersona().getIdPersona(),old==null?null:old.getIdTipoMedioContacto());
 }
 @POST @Path("medio_contacto") @Consumes(MediaType.APPLICATION_JSON)
 public Response crearMedioContacto(JsonObject body){enabled();var value=guardarMedioContacto(body,null);
  return Response.created(uri.getAbsolutePathBuilder().path(value.getIdMedioContacto().toString()).build()).entity(MedioContactoDatos.from(value)).build();}
 @PUT @Path("medio_contacto/{id}") @Consumes(MediaType.APPLICATION_JSON)
 public MedioContactoDatos actualizarMedioContacto(@PathParam("id")String id,JsonObject body){enabled();return MedioContactoDatos.from(guardarMedioContacto(body,RestParameters.uuid(id,"id")));}
 @DELETE @Path("medio_contacto/{id}")
 public Response eliminarMedioContacto(@PathParam("id")String id){enabled();var value=service.buscarContacto(RestParameters.uuid(id,"id"));
  if(value==null)throw new ApiException(404,"Registro no encontrado");
  service.eliminarContacto(value.getIdMedioContacto());return Response.noContent().build();}

 @JsonbNillable public record PersonaRolDatos(String idPersonaRol,String idPersona,String idRol,String idClinica,String fechaCreacion) {
  static PersonaRolDatos from(PersonaRol p){return new PersonaRolDatos(p.getIdPersonaRol().toString(),p.getIdPersona().getIdPersona().toString(),p.getIdRol().getIdRol().toString(),p.getIdClinica().getIdClinica().toString(),instant(p.getFechaCreacion()));}
 }
 @GET @Path("persona_rol")
 public List<PersonaRolDatos> listarPersonaRol(@QueryParam("idPersona")String id,@QueryParam("first")String first,@QueryParam("max")String max){
  enabled();var persona=RestParameters.uuid(id,"idPersona");var page=RestParameters.page(first,max,settings);
  response.setHeader("Total-registros",Long.toString(service.contarAsignaciones(persona)));
  return service.asignaciones(persona,page.first(),page.max()).stream().map(PersonaRolDatos::from).toList();
 }
 @GET @Path("persona_rol/{id}")
 public PersonaRolDatos detallePersonaRol(@PathParam("id")String id){enabled();var value=service.buscarAsignacion(RestParameters.uuid(id,"id"));
  if(value==null)throw new ApiException(404,"Registro no encontrado");return PersonaRolDatos.from(value);}
 private PersonaRol guardarPersonaRol(JsonObject body,UUID id){
  var input=new RestInput(body,"idPersona","idRol","idClinica");var old=id==null?null:service.buscarAsignacion(id);
  if(id!=null&&old==null)throw new ApiException(404,"Registro no encontrado");
  var p=new PersonaRol(id);p.setIdPersona(persona(input.uuid("idPersona")));p.setIdRol(new Rol(input.uuid("idRol")));p.setIdClinica(new Clinica(input.uuid("idClinica")));clinic(p.getIdClinica().getIdClinica());p.setFechaCreacion(old==null?new Date():old.getFechaCreacion());
  return service.guardarAsignacion(p,id!=null,old==null?null:old.getIdPersona().getIdPersona(),old==null?null:old.getIdClinica().getIdClinica(),old==null?null:old.getIdRol().getIdRol());
 }
 @POST @Path("persona_rol") @Consumes(MediaType.APPLICATION_JSON)
 public Response crearPersonaRol(JsonObject body){enabled();var value=guardarPersonaRol(body,null);
  return Response.created(uri.getAbsolutePathBuilder().path(value.getIdPersonaRol().toString()).build()).entity(PersonaRolDatos.from(value)).build();}
 @PUT @Path("persona_rol/{id}") @Consumes(MediaType.APPLICATION_JSON)
 public PersonaRolDatos actualizarPersonaRol(@PathParam("id")String id,JsonObject body){enabled();return PersonaRolDatos.from(guardarPersonaRol(body,RestParameters.uuid(id,"id")));}
 @DELETE @Path("persona_rol/{id}")
 public Response eliminarPersonaRol(@PathParam("id")String id){enabled();var value=service.buscarAsignacion(RestParameters.uuid(id,"id"));
  if(value==null)throw new ApiException(404,"Registro no encontrado");clinic(value.getIdClinica().getIdClinica());
  service.eliminarAsignacion(value.getIdPersonaRol());return Response.noContent().build();}
}
