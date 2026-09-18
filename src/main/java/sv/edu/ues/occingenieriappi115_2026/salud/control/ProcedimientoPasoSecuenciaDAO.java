package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;

/**
 * DAO concreto para ProcedimientoPasoSecuencia.
 *
 * <p>Esta entidad modela la secuencia entre pasos y por eso tiene DAO propio
 * aunque no tenga consultas especificas todavia.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoPasoSecuenciaDAO
        extends DefaultDAO<ProcedimientoPasoSecuencia> {

    /** Construye el DAO indicando que administra ProcedimientoPasoSecuencia. */
    public ProcedimientoPasoSecuenciaDAO() {
        super(ProcedimientoPasoSecuencia.class);
    }
}
