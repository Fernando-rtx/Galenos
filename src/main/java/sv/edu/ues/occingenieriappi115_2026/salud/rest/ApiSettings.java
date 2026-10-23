package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
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
    }

    public int getPageSize() { return pageSize; }
    public int getMaxPageSize() { return maxPageSize; }
    public boolean allowsOrigin(String origin) { return allowedOrigins.contains(origin); }

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
