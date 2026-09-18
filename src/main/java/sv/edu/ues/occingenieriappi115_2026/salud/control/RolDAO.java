package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/** DAO EJB del catálogo {@link Rol}; no declara un EntityManager duplicado. */
@Stateless
@LocalBean
public class RolDAO extends DefaultDAO<Rol> {

    /** Configura la entidad administrada por el DAO genérico. */
    public RolDAO() {
        super(Rol.class);
    }
}
