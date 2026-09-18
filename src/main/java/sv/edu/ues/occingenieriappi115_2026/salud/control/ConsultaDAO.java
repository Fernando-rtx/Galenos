package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;

/** DAO EJB de {@link Consulta}; hereda CRUD, transacciones y persistencia común de {@link DefaultDAO}. */
@Stateless
@LocalBean
public class ConsultaDAO extends DefaultDAO<Consulta> {

    /** Configura el tipo concreto administrado por el DAO genérico. */
    public ConsultaDAO() {
        super(Consulta.class);
    }
}
