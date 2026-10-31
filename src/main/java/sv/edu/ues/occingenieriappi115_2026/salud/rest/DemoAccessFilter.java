package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.Set;

/** La identidad la verifica Liberty; el selector de rol o clínica no autentica. */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class DemoAccessFilter implements ContainerRequestFilter {
    @Inject ApiSettings settings;
    @Context HttpServletRequest servlet;
    @Override public void filter(ContainerRequestContext request) {
        String path = request.getUriInfo().getPath().replaceFirst("^/+", "");
        if (!path.equals("v2") && !path.startsWith("v2/")) return;
        settings.requireManagement();
        if (servlet.getUserPrincipal() == null || servlet.getHeader("Authorization") == null) {
            request.abortWith(Response.status(401).header("WWW-Authenticate", "Basic realm=\"GalenosDemo\"")
                    .type(MediaType.APPLICATION_JSON).entity(new ApiError("Autenticación requerida")).build());
            return;
        }
        boolean write = !Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod());
        String resource = path.substring(3).split("/", 2)[0];
        boolean personal = Set.of("persona", "documento", "medio_contacto", "persona_rol").contains(resource)
                && !path.equals("v2/persona_rol/pacientes");
        boolean clinical = Set.of("consulta", "consulta_procedimiento", "consulta_procedimiento_paso", "orden_examen", "examen_resultado").contains(resource);
        if (settings.isDevelopment() && (personal || (write && !clinical)) && !servlet.isUserInRole("galenos-api-admin")) {
            throw new ApiException(403, "Se requiere permiso de administración");
        }
        if (!Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod())
                && !settings.canWrite(servlet)) {
            throw new ApiException(403, "No tiene permiso de escritura");
        }
    }
}
