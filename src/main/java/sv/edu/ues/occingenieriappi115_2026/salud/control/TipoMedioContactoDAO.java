package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;

@ApplicationScoped
public class TipoMedioContactoDAO extends DefaultDAO<TipoMedioContacto> {

    public TipoMedioContactoDAO() {
        super(TipoMedioContacto.class);
    }
}