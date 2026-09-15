package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto;

@ApplicationScoped
public class MedioContactoDAO extends DefaultDAO<MedioContacto> {

    public MedioContactoDAO() {
        super(MedioContacto.class);
    }
}
