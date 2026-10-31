package sv.edu.ues.occingenieriappi115_2026.salud.rest;

/** Error público sin stack trace, SQL, entidades ni detalles internos. */
public class ApiError {
    private final String mensaje;

    public ApiError(String mensaje) { this.mensaje = mensaje; }
    public String getMensaje() { return mensaje; }
}
