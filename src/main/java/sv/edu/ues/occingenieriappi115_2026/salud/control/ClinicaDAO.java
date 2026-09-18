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
}
