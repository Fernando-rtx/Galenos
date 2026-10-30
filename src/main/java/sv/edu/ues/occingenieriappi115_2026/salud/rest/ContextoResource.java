package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

/** Contexto comprobado por Liberty; no admite identidades enviadas como selector. */
@RequestScoped
@Path("v2/contexto")
@Produces(MediaType.APPLICATION_JSON)
public class ContextoResource {
    @Inject ApiSettings settings;
    @Context HttpServletRequest request;
    @GET public Map<String, Object> get() {
        settings.requireCrudDemo();
        String actor = request.getUserPrincipal().getName();
        return Map.of("actor", actor, "puedeEscribir", request.isUserInRole("galenos-demo-write"),
                "clinicas", settings.clinicsFor(actor).stream().map(Object::toString).sorted().toList());
    }
}
