package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;

/** Describe el contexto académico que devuelve el contrato v2. */
@RequestScoped
@Path("v2/contexto")
@Produces(MediaType.APPLICATION_JSON)
public class ContextoResource {
    @Inject ApiSettings settings;
    @EJB ConsultaFlujoService service;
    @GET public Map<String, Object> get() {
        settings.requireManagement();
        var clinics = service.clinicasDisponibles(0, Integer.MAX_VALUE).stream()
                .map(clinic -> clinic.getIdClinica().toString()).sorted().toList();
        return Map.of("actor", "academico-local", "puedeEscribir", true, "clinicas", clinics);
    }
}
