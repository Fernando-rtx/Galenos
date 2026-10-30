package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.ApplicationException;

/** Fallo de negocio compartido por Faces y REST, con rollback del caso de uso. */
@ApplicationException(rollback = true)
public class ReglaNegocioException extends RuntimeException {
    public enum Motivo { VALIDACION, DUPLICADO, REFERENCIADO, AUSENTE }
    private static final long serialVersionUID = 1L;
    private final Motivo motivo;
    private final String campo;

    public ReglaNegocioException(Motivo motivo, String campo, String clave) {
        super(clave);
        this.motivo = motivo;
        this.campo = campo;
    }
    public ReglaNegocioException(Motivo motivo, String campo, String clave, Throwable causa) {
        super(clave, causa);
        this.motivo = motivo;
        this.campo = campo;
    }
    public Motivo getMotivo() { return motivo; }
    public String getCampo() { return campo; }
}
