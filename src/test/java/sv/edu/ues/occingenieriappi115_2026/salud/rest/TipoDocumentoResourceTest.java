package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DireccionOrden;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoDocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TipoDocumentoResourceTest {
    private TipoDocumentoResource resource;
    private TipoDocumentoDAO dao;
    private HttpServletResponse response;

    @BeforeEach
    void setup() {
        dao = mock(TipoDocumentoDAO.class);
        response = mock(HttpServletResponse.class);
        resource = new TipoDocumentoResource();
        resource.dao = dao;
        resource.response = response;
        resource.settings = new ApiSettings(Map.of());
    }

    @Test
    void paginaConOrdenEstableYConteoRealSinLeerRelaciones() {
        TipoDocumento entity = mock(TipoDocumento.class);
        UUID id = UUID.randomUUID();
        when(entity.getIdTipoDocumento()).thenReturn(id);
        when(entity.getNombre()).thenReturn("Documento sintético");
        when(entity.getActivo()).thenReturn(true);
        when(dao.obtenerPagina(2, 3, List.of(),
                List.of(new OrdenDAO("idTipoDocumento", DireccionOrden.ASCENDENTE))))
                .thenReturn(List.of(entity));
        when(dao.contar()).thenReturn(17L);

        var page = resource.findRange("2", "3");

        assertEquals(1, page.size());
        assertEquals(id.toString(), page.getFirst().getIdTipoDocumento());
        assertEquals("Documento sintético", page.getFirst().getNombre());
        assertTrue(page.getFirst().getActivo());
        assertNull(page.getFirst().getIndicaciones());
        verify(response).setHeader("Total-registros", "17");
        verify(entity, never()).getDocumentoCollection();
        verify(dao, never()).obtenerTodos();
    }

    @Test
    void usaTamanoConfiguradoYDevuelvePaginaVaciaConTotal() {
        resource.settings = new ApiSettings(Map.of("GALENOS_API_PAGE_SIZE", "7"));
        when(dao.obtenerPagina(eq(0), eq(7), anyList(), anyList())).thenReturn(List.of());
        when(dao.contar()).thenReturn(0L);

        assertTrue(resource.findRange(null, null).isEmpty());
        verify(response).setHeader("Total-registros", "0");
    }

    @ParameterizedTest
    @CsvSource(value = {"-1,2", "0,0", "0,101", "abc,1", "0,1.2", "2147483648,1", "0,-1"})
    void rechazaPaginacionInvalidaSinConsultarDao(String first, String max) {
        ApiException error = assertThrows(ApiException.class, () -> resource.findRange(first, max));
        assertEquals(422, error.getStatus());
        verifyNoInteractions(dao, response);
    }

    @Test
    void rechazaParametroVacio() {
        assertEquals(422, assertThrows(ApiException.class, () -> resource.findRange("", "1")).getStatus());
        verifyNoInteractions(dao);
    }

    @Test
    void detalleUsaUuidYRespetaCamposNulos() {
        UUID id = UUID.randomUUID();
        when(dao.buscarPorId(id)).thenReturn(new TipoDocumento(id));
        var dto = resource.findById(id.toString());
        assertEquals(id.toString(), dto.getIdTipoDocumento());
        assertNull(dto.getNombre());
        assertNull(dto.getActivo());
    }

    @Test
    void detalleAusenteEs404() {
        UUID id = UUID.randomUUID();
        assertEquals(404, assertThrows(ApiException.class, () -> resource.findById(id.toString())).getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"no-es-uuid", "1-1-1-1-1", "", "00000000-0000-0000-0000-00000000000z"})
    void uuidInvalidoEs422SinConsultarDao(String id) {
        assertEquals(422, assertThrows(ApiException.class, () -> resource.findById(id)).getStatus());
        verifyNoInteractions(dao);
    }

    @Test
    void noDisfrazaFalloDePersistenciaComoResultadoVacio() {
        when(dao.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
                .thenThrow(new IllegalStateException("fallo de prueba"));
        assertThrows(IllegalStateException.class, () -> resource.findRange(null, null));
        verifyNoInteractions(response);
    }
}
