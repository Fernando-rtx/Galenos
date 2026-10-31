package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClinicaResourceTest {
    @Test
    void selectorConsultaYCuentaSoloActivasConLimite() {
        var resource = new ClinicaResource();
        resource.settings = new ApiSettings(Map.of("GALENOS_DEMO_MODE", "true",
                "POSTGRESQL_DB_NAME", "galenos_demo_unit"));
        resource.dao = mock(ClinicaDAO.class);
        resource.response = mock(HttpServletResponse.class);
        var filters = List.of(new FiltroDAO("activo", OperadorFiltro.IGUAL, true));
        var orders = List.of(new OrdenDAO("idClinica", DireccionOrden.ASCENDENTE));
        UUID id = UUID.randomUUID();
        when(resource.dao.obtenerPagina(0, 20, filters, orders))
                .thenReturn(List.of(new Clinica(id, "Clínica ficticia")));
        when(resource.dao.contar(filters)).thenReturn(2L);
        var result = resource.findRange(null, null);
        assertEquals("Clínica ficticia", result.getFirst().getNombre());
        assertEquals(id.toString(), result.getFirst().getIdClinica());
        verify(resource.response).setHeader("Total-registros", "2");
    }

    @Test
    void modoNormalNoLeeClinicas() {
        var resource = new ClinicaResource();
        resource.settings = new ApiSettings(Map.of());
        resource.dao = mock(ClinicaDAO.class);
        assertEquals(404, assertThrows(ApiException.class,
                () -> resource.findRange(null, null)).getStatus());
        verifyNoInteractions(resource.dao);
    }
}
