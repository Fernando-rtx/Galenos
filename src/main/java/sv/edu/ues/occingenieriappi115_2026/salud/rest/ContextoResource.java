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
    @EJB sv.edu.ues.occingenieriappi115_2026.salud.control.IdentidadAcademicaService identidad;
    @jakarta.ws.rs.core.Context jakarta.ws.rs.core.HttpHeaders headers;
    @GET public Map<String, Object> get() {
        settings.requireManagement();
        var context = identidad.contexto(headers.getHeaderString("Authorization"), "EMPLEADO", settings.getClinicZone().getId());
        return Map.of("actor", "academico-empleado", "puedeEscribir", false,
                "clinicas", context.asignaciones().stream().map(a -> a.idClinica()).distinct().sorted().toList());
    }
}
