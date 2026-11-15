package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import jakarta.ejb.EJBException;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.Test;
import jakarta.ws.rs.core.Response;
import org.mockito.ArgumentCaptor;

/** Mapeo HTTP unitario: no provoca bloqueos reales en PostgreSQL. */
class AtencionConflictoHttpTest {
    @Test void conflictosJpaDirectosOEnvueltosConservan409SinDetallesInternos() {
        var mapper = new ApiExceptionMapper();
        for (var error : new Exception[] { new LockTimeoutException("SQL privado"),
                new PessimisticLockException("SQL privado"), new OptimisticLockException("SQL privado"),
                new EJBException(new LockTimeoutException("SQL privado")) }) {
            // El proveedor de Response lo pone Liberty; aquí se observa el status y la entidad.
            try (var responses = mockStatic(Response.class)) {
                var builder = mock(Response.ResponseBuilder.class, RETURNS_SELF);
                var response = mock(Response.class);
                responses.when(() -> Response.status(409)).thenReturn(builder);
                when(builder.build()).thenReturn(response);
                assertSame(response, mapper.toResponse(error));
                responses.verify(() -> Response.status(409));
                var payload = ArgumentCaptor.forClass(ApiError.class);
                verify(builder).entity(payload.capture());
                assertEquals("La consulta está siendo modificada. Actualiza el paso antes de continuar.", payload.getValue().getMensaje());
            }
        }
    }
}
