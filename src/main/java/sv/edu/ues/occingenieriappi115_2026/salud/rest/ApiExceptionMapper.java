package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ApiException expected) {
            return Response.status(expected.getStatus()).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ApiError(expected.getMessage())).build();
        }
        if (exception instanceof WebApplicationException http) {
            int status = http.getResponse().getStatus();
            // Conserva cabeceras del runtime como Allow para 405.
            return Response.fromResponse(http.getResponse()).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ApiError(status >= 500 ? "Error interno del servidor"
                            : "Solicitud HTTP no disponible (" + status + ")")).build();
        }
        LOG.severe("Fallo REST de tipo " + exception.getClass().getName());
        return Response.serverError().type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiError("Error interno del servidor")).build();
    }
}
