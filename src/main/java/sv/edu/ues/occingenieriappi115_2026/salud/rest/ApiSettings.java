package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Configuración pública de paginación y CORS; nunca contiene credenciales. */
@ApplicationScoped
public class ApiSettings {
    private final int pageSize;
    private final int maxPageSize;
    private final Set<String> allowedOrigins;
    private final boolean demoMode;
    private final ZoneId clinicZone;
    private final boolean crudDemo;
    private final Map<String, Set<java.util.UUID>> actorClinics;

    public ApiSettings() {
        this(System.getenv());
    }

    ApiSettings(Map<String, String> environment) {
        pageSize = positiveInt(environment, "GALENOS_API_PAGE_SIZE", "20");
        maxPageSize = positiveInt(environment, "GALENOS_API_MAX_PAGE_SIZE", "100");
        if (pageSize > maxPageSize) {
            throw new IllegalArgumentException("GALENOS_API_PAGE_SIZE excede GALENOS_API_MAX_PAGE_SIZE");
        }
        String origins = environment.getOrDefault("GALENOS_CORS_ORIGINS", "");
        allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::strip).filter(value -> !value.isEmpty())
                .map(ApiSettings::validateOrigin).collect(Collectors.toUnmodifiableSet());
        String demo = environment.getOrDefault("GALENOS_DEMO_MODE", "false");
        if (!Set.of("true", "false").contains(demo)) {
            throw new IllegalArgumentException("GALENOS_DEMO_MODE debe ser true o false");
        }
        demoMode = Boolean.parseBoolean(demo);
        if (demoMode && !environment.getOrDefault("POSTGRESQL_DB_NAME", "")
                .matches("galenos_(demo|rest_test)_[a-z0-9_]+")) {
            throw new IllegalArgumentException("La demostración requiere una base galenos_demo_* o galenos_rest_test_*");
        }
        String crud = environment.getOrDefault("GALENOS_CRUD_DEMO", "false");
        if (!Set.of("true", "false").contains(crud)) throw new IllegalArgumentException("GALENOS_CRUD_DEMO debe ser true o false");
        crudDemo = Boolean.parseBoolean(crud);
        if (crudDemo && (!demoMode || !"galenos_demo_crud".equals(environment.get("POSTGRESQL_DB_NAME")))) {
            throw new IllegalArgumentException("CRUD requiere la base sintetica galenos_demo_crud y modo demo");
        }
        Map<String, Set<java.util.UUID>> scopes = new java.util.HashMap<>();
        if (crudDemo) {
            for (String item : environment.getOrDefault("GALENOS_CRUD_ACTOR_CLINICS", "").split(";")) {
                if (item.isBlank()) continue;
                String[] entry = item.split("=", -1);
                if (entry.length != 2 || entry[0].isBlank() || scopes.containsKey(entry[0])) {
                    throw new IllegalArgumentException("Asignacion de clinicas CRUD invalida");
                }
                Set<java.util.UUID> ids = Arrays.stream(entry[1].split(","))
                        .map(java.util.UUID::fromString).collect(Collectors.toUnmodifiableSet());
                scopes.put(entry[0], ids);
            }
        }
        actorClinics = Map.copyOf(scopes);
        clinicZone = ZoneId.of(environment.getOrDefault("GALENOS_CLINIC_TIME_ZONE", "America/El_Salvador"));
    }

    public int getPageSize() { return pageSize; }
    public int getMaxPageSize() { return maxPageSize; }
    public boolean allowsOrigin(String origin) { return allowedOrigins.contains(origin); }
    public ZoneId getClinicZone() { return clinicZone; }

    public Set<java.util.UUID> clinicsFor(String actor) { return actorClinics.getOrDefault(actor, Set.of()); }
    public void requireClinic(String actor, java.util.UUID id) {
        if (!clinicsFor(actor).contains(id)) throw new ApiException(403, "No tiene acceso a la clínica solicitada");
    }

    public boolean isCrudDemo() { return crudDemo; }
    public void requireCrudDemo() {
        if (!crudDemo) throw new ApiException(404, "Recurso de gestión no disponible");
    }

    public void requireDemo() {
        if (!demoMode) throw new ApiException(404, "Recurso de demostración no disponible");
    }

    private static int positiveInt(Map<String, String> environment, String key, String fallback) {
        try {
            int value = Integer.parseInt(environment.getOrDefault(key, fallback));
            if (value > 0) return value;
        } catch (NumberFormatException ignored) {
            // El mensaje identifica la configuración y no revela su contenido.
        }
        throw new IllegalArgumentException(key + " debe ser un entero positivo");
    }

    private static String validateOrigin(String origin) {
        URI uri = URI.create(origin);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null
                || !uri.getRawPath().isEmpty() || uri.getRawQuery() != null
                || uri.getRawFragment() != null || uri.getPort() > 65535
                || uri.getPort() == 0) {
            throw new IllegalArgumentException("GALENOS_CORS_ORIGINS requiere orígenes HTTP(S) exactos");
        }
        return origin;
    }
}
