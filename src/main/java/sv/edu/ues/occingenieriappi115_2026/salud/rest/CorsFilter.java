package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/** CORS solo para la API: orígenes exactos, lectura y sin credenciales. */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Set<String> METHODS = Set.of("GET", "HEAD");
    private static final Set<String> HEADERS = Set.of("accept", "content-type");
    private static final String APPROVED_ORIGIN = CorsFilter.class.getName() + ".origin";
    @Inject
    ApiSettings settings;

    @Override
    public void filter(ContainerRequestContext request) {
        String origin = request.getHeaderString("Origin");
        if (origin == null) return;
        if (!settings.allowsOrigin(origin)) {
            reject(request);
            return;
        }
        request.setProperty(APPROVED_ORIGIN, origin);
        String requestedMethod = request.getHeaderString("Access-Control-Request-Method");
        if ("OPTIONS".equals(request.getMethod()) && requestedMethod != null) {
            String requestedHeaders = request.getHeaderString("Access-Control-Request-Headers");
            boolean headersAllowed = requestedHeaders == null || Arrays.stream(requestedHeaders.split(","))
                    .map(String::strip).map(value -> value.toLowerCase(Locale.ROOT))
                    .allMatch(HEADERS::contains);
            if (!METHODS.contains(requestedMethod) || !headersAllowed) {
                reject(request);
                return;
            }
            request.abortWith(Response.noContent()
                    .header("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Accept, Content-Type")
                    .build());
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        // Evita reutilizar entre orígenes una respuesta guardada por un proxy.
        response.getHeaders().add("Vary", "Origin");
        response.getHeaders().add("Vary", "Access-Control-Request-Method");
        response.getHeaders().add("Vary", "Access-Control-Request-Headers");
        response.getHeaders().putSingle("Cache-Control", "no-store");
        Object origin = request.getProperty(APPROVED_ORIGIN);
        if (origin != null) {
            response.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            response.getHeaders().putSingle("Access-Control-Expose-Headers", "Total-registros");
        }
    }

    private static void reject(ContainerRequestContext request) {
        request.abortWith(Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiError("Origen o preflight CORS no permitido")).build());
    }
}
