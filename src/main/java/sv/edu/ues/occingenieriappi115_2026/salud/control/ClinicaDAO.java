package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;

/**
 * DAO EJB encargado de persistir {@link Clinica}.
 *
 * <p>Hereda de {@link DefaultDAO} el CRUD, las transacciones y el EntityManager
 * común. Solo identifica la entidad administrada, evitando duplicación.</p>
 */
@Stateless
@LocalBean
public class ClinicaDAO extends DefaultDAO<Clinica> {

    /** Configura el DAO genérico para trabajar con {@link Clinica}. */
    public ClinicaDAO() {
        super(Clinica.class);
    }

    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public java.util.List<Clinica> buscarDisponibles(java.util.Set<java.util.UUID> ids, int primero, int tamano) {
        if (ids.isEmpty()) return java.util.List.of();
        return getEntityManager().createQuery("SELECT c FROM Clinica c WHERE c.activo = TRUE AND c.idClinica IN :ids ORDER BY c.idClinica", Clinica.class)
                .setParameter("ids", ids).setFirstResult(primero).setMaxResults(tamano).getResultList();
    }
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public long contarDisponibles(java.util.Set<java.util.UUID> ids) {
        if (ids.isEmpty()) return 0;
        return getEntityManager().createQuery("SELECT COUNT(c) FROM Clinica c WHERE c.activo = TRUE AND c.idClinica IN :ids", Long.class)
                .setParameter("ids", ids).getSingleResult();
    }
}
