package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ApiSettingsTest {
    @Test
    void demoDesactivadaPorDefecto() {
        assertEquals(404, assertThrows(ApiException.class,
                () -> new ApiSettings(Map.of()).requireDemo()).getStatus());
    }

    @Test
    void demoSoloSeHabilitaConNombreDeBaseAislada() {
        assertDoesNotThrow(() -> new ApiSettings(Map.of("GALENOS_DEMO_MODE", "true",
                "POSTGRESQL_DB_NAME", "galenos_demo_unit")).requireDemo());
        assertDoesNotThrow(() -> new ApiSettings(Map.of("GALENOS_DEMO_MODE", "true",
                "POSTGRESQL_DB_NAME", "galenos_rest_test_unit")).requireDemo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"clinica", "universidad", "", "galenos_demo_", "otra_galenos_demo_prueba"})
    void demoRechazaBaseNoIdentificadaComoPrueba(String database) {
        assertThrows(IllegalArgumentException.class, () -> new ApiSettings(Map.of(
                "GALENOS_DEMO_MODE", "true", "POSTGRESQL_DB_NAME", database)));
    }

    @Test
    void demoRechazaBooleanoYZonaHorariaInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiSettings(Map.of("GALENOS_DEMO_MODE", "yes")));
        assertThrows(java.time.DateTimeException.class,
                () -> new ApiSettings(Map.of("GALENOS_CLINIC_TIME_ZONE", "Zona/NoExiste")));
    }

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

    @Test
    void crudDesactivadoPorDefectoYRestringidoALaBaseExacta() {
        assertEquals(404, assertThrows(ApiException.class, () -> new ApiSettings(Map.of()).requireCrudDemo()).getStatus());
        for (String database : java.util.List.of("clinica", "galenos_demo_local", "galenos_rest_test_unit")) {
            assertThrows(IllegalArgumentException.class, () -> new ApiSettings(Map.of(
                    "GALENOS_CRUD_DEMO", "true", "GALENOS_DEMO_MODE", "true", "POSTGRESQL_DB_NAME", database)));
        }
        assertThrows(IllegalArgumentException.class, () -> new ApiSettings(Map.of(
                "GALENOS_CRUD_DEMO", "true", "POSTGRESQL_DB_NAME", "galenos_demo_crud")));
    }

    @Test
    void permisosClinicaSeDerivanDeIdentidadYFaltantesRechazan() {
        String clinic = "00000000-0000-4000-8000-000000000011";
        var settings = new ApiSettings(Map.of("GALENOS_CRUD_DEMO", "true", "GALENOS_DEMO_MODE", "true",
                "POSTGRESQL_DB_NAME", "galenos_demo_crud", "GALENOS_CRUD_ACTOR_CLINICS", "sintetico=" + clinic));
        assertDoesNotThrow(() -> settings.requireClinic("sintetico", java.util.UUID.fromString(clinic)));
        assertEquals(403, assertThrows(ApiException.class, () -> settings.requireClinic("otro", java.util.UUID.fromString(clinic))).getStatus());
        assertEquals(403, assertThrows(ApiException.class, () -> settings.requireClinic("sintetico", java.util.UUID.randomUUID())).getStatus());
    }

    @Test
    void desarrolloRequiereClinicaSinModoDemoYNoOtorgaClinicasPorDefecto() {
        var settings = new ApiSettings(Map.of("GALENOS_API_MODE", "development", "POSTGRESQL_DB_NAME", "clinica"));
        assertTrue(settings.isManagementEnabled());
        assertDoesNotThrow(settings::requireManagement);
        assertThrows(ApiException.class, settings::requireDemo);
        assertTrue(settings.clinicsFor("sin-asignacion").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new ApiSettings(Map.of("GALENOS_API_MODE", "development", "POSTGRESQL_DB_NAME", "galenos_demo_crud")));
        assertThrows(IllegalArgumentException.class, () -> new ApiSettings(Map.of("GALENOS_API_MODE", "development", "POSTGRESQL_DB_NAME", "clinica", "GALENOS_DEMO_MODE", "true")));
    }
}
