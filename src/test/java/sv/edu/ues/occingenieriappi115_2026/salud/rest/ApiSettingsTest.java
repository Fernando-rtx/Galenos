package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ApiSettingsTest {
    @Test
    void sinConfiguracionNoPermiteOrigenesCruzados() {
        var settings = new ApiSettings(Map.of());
        assertFalse(settings.allowsOrigin("http://localhost:3000"));
        assertEquals(20, settings.getPageSize());
        assertEquals(100, settings.getMaxPageSize());
    }

    @Test
    void allowlistComparaOrigenCompleto() {
        var settings = new ApiSettings(Map.of("GALENOS_CORS_ORIGINS",
                "http://localhost:3000, https://ejemplo.test"));
        assertTrue(settings.allowsOrigin("http://localhost:3000"));
        assertTrue(settings.allowsOrigin("https://ejemplo.test"));
        assertFalse(settings.allowsOrigin("http://127.0.0.1:3000"));
        assertFalse(settings.allowsOrigin("http://localhost:3001"));
        assertFalse(settings.allowsOrigin("http://localhost:3000.ejemplo.test"));
        assertFalse(settings.allowsOrigin("null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"*", "null", "file:///tmp", "https://ejemplo.test/otro",
            "http://usuario:clave@ejemplo.test", "https://ejemplo.test?x=1",
            "https://ejemplo.test/", "http://localhost:65536", "http://localhost:0"})
    void rechazaAllowlistInvalida(String origin) {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiSettings(Map.of("GALENOS_CORS_ORIGINS", origin)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "2147483648", ""})
    void limitesInvalidosFallanDeFormaExplicita(String value) {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiSettings(Map.of("GALENOS_API_MAX_PAGE_SIZE", value)));
    }

    @Test
    void paginaPorDefectoNoPuedeSuperarElMaximo() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiSettings(Map.of("GALENOS_API_MAX_PAGE_SIZE", "10")));
    }
}
