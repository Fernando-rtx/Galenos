package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Context;
import jakarta.servlet.http.HttpServletRequest;
import sv.edu.ues.occingenieriappi115_2026.salud.control.IdentidadAcademicaService;

/** No existe política administrativa implícita: gestión heredada cerrada por defecto. */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AccesoAcademicoFilter implements ContainerRequestFilter {
  @EJB IdentidadAcademicaService identidad;
  @jakarta.inject.Inject ApiSettings settings;
  @Context HttpServletRequest servlet;

  @Override
  public void filter(ContainerRequestContext request) {
    String path = request.getUriInfo().getPath().replaceFirst("^/+", "");
    if (!path.startsWith("v2/")) return;
    if (settings.isCrudDemo()) {
      boolean read = servlet.isUserInRole("galenos-demo-read") || servlet.isUserInRole("galenos-demo-write");
      boolean write = servlet.isUserInRole("galenos-demo-write");
      if (!read) throw new ApiException(403, "No tiene permiso de lectura");
      if (!java.util.Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod()) && !write)
        throw new ApiException(403, "No tiene permiso de escritura");
      return;
    }
    if (request.getMethod().equals("OPTIONS")
        || path.startsWith("v2/identidad_academica/")
        || path.equals("v2/citas_paciente")
        || path.startsWith("v2/citas_paciente/")) return;
    String bearer = request.getHeaderString("Authorization");
    identidad.exigir(bearer, "EMPLEADO");
    if (request.getMethod().equals("GET")
        && (path.equals("v2/clinica_disponible") || path.equals("v2/contexto"))) return;
    boolean lecturaClinica =
        path.equals("v2/consulta")
            || path.matches("v2/consulta/[0-9a-fA-F-]+")
            || path.equals("v2/persona_rol/pacientes")
            || path.matches("v2/consulta_procedimiento(?:/[0-9a-fA-F-]+)?")
            || path.matches("v2/consulta_procedimiento_paso(?:/[0-9a-fA-F-]+)?");
    if (request.getMethod().equals("GET") && lecturaClinica) {
      var params = request.getUriInfo().getQueryParameters();
      identidad.exigirEmpleado(
          bearer,
          RestParameters.uuid(request.getHeaderString("X-Persona-Rol"), "X-Persona-Rol"),
          RestParameters.uuid(params.getFirst("idClinica"), "idClinica"));
      return;
    }
    throw new ApiException(403, "Operación sin política de acceso habilitada");
  }
}
