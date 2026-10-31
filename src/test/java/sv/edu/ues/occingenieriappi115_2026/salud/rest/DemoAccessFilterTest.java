package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import java.security.Principal;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DemoAccessFilterTest {
    @ParameterizedTest @ValueSource(strings={"v2/tipo_documento", "/v2/tipo_documento"})
    void escrituraSinRolEsRechazadaConAmbasFormasDeRuta(String path) {
        var filter = new DemoAccessFilter();
        filter.settings = new ApiSettings(Map.of("GALENOS_CRUD_DEMO","true","GALENOS_DEMO_MODE","true","POSTGRESQL_DB_NAME","galenos_demo_crud"));
        filter.servlet = mock(HttpServletRequest.class);
        when(filter.servlet.getUserPrincipal()).thenReturn(mock(Principal.class));
        when(filter.servlet.getHeader("Authorization")).thenReturn("Basic test-sintetico");
        var request = mock(ContainerRequestContext.class);var uri = mock(UriInfo.class);
        when(request.getUriInfo()).thenReturn(uri);when(uri.getPath()).thenReturn(path);when(request.getMethod()).thenReturn("POST");
        assertEquals(403, assertThrows(ApiException.class, () -> filter.filter(request)).getStatus());
    }

    @ParameterizedTest @ValueSource(strings={"/v2/persona", "/v2/documento", "/v2/medio_contacto", "/v2/persona_rol"})
    void lecturaPersonalRequiereAdministradorEnClinica(String path) {
        var filter=new DemoAccessFilter();
        filter.settings=new ApiSettings(Map.of("GALENOS_API_MODE","development","POSTGRESQL_DB_NAME","clinica"));
        filter.servlet=mock(HttpServletRequest.class);
        when(filter.servlet.getUserPrincipal()).thenReturn(()->"lector");when(filter.servlet.getHeader("Authorization")).thenReturn("Basic prueba-unitaria");
        var request=mock(ContainerRequestContext.class);var uri=mock(UriInfo.class);
        when(request.getUriInfo()).thenReturn(uri);when(uri.getPath()).thenReturn(path);when(request.getMethod()).thenReturn("GET");
        assertEquals(403,assertThrows(ApiException.class,()->filter.filter(request)).getStatus());
    }
}
