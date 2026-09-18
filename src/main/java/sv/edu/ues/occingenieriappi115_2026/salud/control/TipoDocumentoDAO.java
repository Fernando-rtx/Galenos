package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

/**
 * DAO encargado de la persistencia de {@link TipoDocumento}.
 *
 * <p>Hereda de {@link DefaultDAO} las operaciones CRUD y el EntityManager
 * común. Como EJB {@code @Stateless}, Liberty administra su ciclo de vida y
 * las transacciones; {@code @LocalBean} permite inyectarlo sin interfaz local.</p>
 */
@Stateless
@LocalBean
public class TipoDocumentoDAO extends DefaultDAO<TipoDocumento> {

    /** Indica al DAO genérico que administra {@link TipoDocumento}. */
    public TipoDocumentoDAO() {
        super(TipoDocumento.class);
    }
}
