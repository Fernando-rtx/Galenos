package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import java.util.UUID;

/** Validaciones comunes de los recursos; conserva el contrato de TipoDocumento. */
final class RestParameters {
    private RestParameters() { }

    record Page(int first, int max) { }

    static Page page(String first, String max, ApiSettings settings) {
        int offset = integer(first, 0, "first");
        int limit = integer(max, settings.getPageSize(), "max");
        if (limit < 1 || limit > settings.getMaxPageSize()) {
            throw new ApiException(422, "first debe ser >= 0 y max debe estar entre 1 y "
                    + settings.getMaxPageSize());
        }
        return new Page(offset, limit);
    }

    static UUID uuid(String raw, String parameter) {
        try {
            if (raw == null) throw new IllegalArgumentException();
            UUID value = UUID.fromString(raw);
            if (!value.toString().equalsIgnoreCase(raw)) throw new IllegalArgumentException();
            return value;
        } catch (IllegalArgumentException exception) {
            throw new ApiException(422, parameter + " debe ser un UUID canónico");
        }
    }

    private static int integer(String raw, int fallback, String parameter) {
        if (raw == null) return fallback;
        try {
            if (!raw.matches("[0-9]+")) throw new NumberFormatException();
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            throw new ApiException(422, parameter + " debe ser un entero no negativo válido");
        }
    }
}
