package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto;

/** DAO EJB de {@link MedioContacto}; reutiliza la persistencia genérica centralizada. */
@Stateless
@LocalBean
public class MedioContactoDAO extends DefaultDAO<MedioContacto> {

    /** Configura el DAO genérico para medios de contacto. */
    public MedioContactoDAO() {
        super(MedioContacto.class);
    }
}
