package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenResultado;

/** DAO EJB de {@link ExamenResultado}, con CRUD y EntityManager heredados de {@link DefaultDAO}. */
@Stateless
@LocalBean
public class ExamenResultadoDAO extends DefaultDAO<ExamenResultado> {

    /** Configura el tipo de entidad administrado. */
    public ExamenResultadoDAO() {
        super(ExamenResultado.class);
    }
}
