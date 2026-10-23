package sv.edu.ues.occingenieriappi115_2026.salud.rest;

/** Fallo esperado con un mensaje apto para el consumidor de la API. */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() { return status; }
}
