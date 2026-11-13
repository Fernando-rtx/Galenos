package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.ApplicationException;

/** Rechazo esperado de identificación/acceso; EJB lo conserva sin envolverlo. */
@ApplicationException(rollback = true)
public class IdentificacionException extends RuntimeException {
  private final int status;

  public IdentificacionException(int status, String mensaje) {
    super(mensaje);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
