package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.ApplicationException;

/** Error de negocio que revierte toda la operación, incluida la asociación. */
@ApplicationException(rollback = true)
public class FlujoConsultaException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public FlujoConsultaException(String clave) { super(clave); }
}
