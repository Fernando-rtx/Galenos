package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

@ApplicationScoped
public class RolDAO extends DefaultDAO<Rol> {

    public RolDAO() {
        super(Rol.class);
    }
}
