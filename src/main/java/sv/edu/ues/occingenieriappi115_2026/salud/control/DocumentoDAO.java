package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;

/**
 * DAO concreto para Documento.
 *
 * <p>DefaultDAO implementa las operaciones comunes para esta entidad.</p>
 */
@Stateless
@LocalBean
public class DocumentoDAO extends DefaultDAO<Documento> {

    /** EntityManager inyectado mediante GalenoPU. */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /** Construye el DAO indicando que administra Documento. */
    public DocumentoDAO() {
        super(Documento.class);
    }

    /**
     * Provee el EntityManager para las operaciones heredadas.
     *
     * @return EntityManager de Jakarta Persistence.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
