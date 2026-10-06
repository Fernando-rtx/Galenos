package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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

    /**
     * Busca globalmente un nombre sin distinguir mayúsculas ni espacios
     * exteriores, excluyendo opcionalmente el registro que se está editando.
     *
     * @param nombre nombre recibido desde el formulario
     * @param idTipoDocumentoExcluir UUID actual en edición, o {@code null} al crear
     * @return {@code true} cuando ya existe un registro con ese nombre lógico
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean existeNombreNormalizado(String nombre, UUID idTipoDocumentoExcluir) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        String nombreNormalizado = nombre.strip().toLowerCase(Locale.ROOT);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<TipoDocumento> consulta = builder.createQuery(TipoDocumento.class);
        Root<TipoDocumento> tipo = consulta.from(TipoDocumento.class);
        List<Predicate> condiciones = new ArrayList<>();
        condiciones.add(builder.equal(
                builder.lower(builder.trim(tipo.<String>get("nombre"))), nombreNormalizado));
        if (idTipoDocumentoExcluir != null) {
            condiciones.add(builder.notEqual(tipo.get("idTipoDocumento"), idTipoDocumentoExcluir));
        }
        consulta.select(tipo).where(condiciones.toArray(Predicate[]::new));
        return !getEntityManager().createQuery(consulta).setMaxResults(1).getResultList().isEmpty();
    }
}
