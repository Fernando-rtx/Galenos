package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

/**
 * DAO concreto para TipoDocumento.
 *
 * <p>DefaultDAO provee crear, modificar, eliminar, buscar, paginar y contar.</p>
 */
@Stateless
@LocalBean
public class TipoDocumentoDAO extends DefaultDAO<TipoDocumento> {

    /** EntityManager inyectado por la unidad GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra TipoDocumento. */
    public TipoDocumentoDAO() {
        super(TipoDocumento.class);
    }

    /**
     * Provee el EntityManager para la logica generica heredada.
     *
     * @return EntityManager de persistencia.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
