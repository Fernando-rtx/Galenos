package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/** Lectura tipada del JSON de entrada; no deserializa entidades ni relaciones JPA. */
final class RestInput {
    private final JsonObject data;
    RestInput(JsonObject data, String... campos) {
        if (data == null || !Set.of(campos).containsAll(data.keySet())) {
            throw new ApiException(422, "El cuerpo contiene campos ausentes o no publicados");
        }
        this.data = data;
    }
    String text(String campo) {
        JsonValue value = data.get(campo);
        if (value == null || value == JsonValue.NULL) return null;
        if (!(value instanceof JsonString text)) throw new ApiException(422, campo + " debe ser texto o null");
        return text.getString();
    }
    Boolean bool(String campo) {
        JsonValue value = data.get(campo);
        if (value == null || value == JsonValue.NULL) return null;
        if (value == JsonValue.TRUE) return true;
        if (value == JsonValue.FALSE) return false;
        throw new ApiException(422, campo + " debe ser booleano o null");
    }
    UUID uuid(String campo) { return RestParameters.uuid(text(campo), campo); }
    Date date(String campo) {
        String value = text(campo);
        if (value == null) return null;
        try { return Date.from(Instant.parse(value)); }
        catch (DateTimeParseException ex) { throw new ApiException(422, campo + " debe ser un instante ISO-8601 con zona"); }
    }
}
