package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.ApplicationException;

/** Conserva el contrato IllegalArgumentException y exige rollback a través de EJB. */
@ApplicationException(rollback = true)
public class FlujoProcedimientoException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    public FlujoProcedimientoException(String clave) { super(clave); }
}
