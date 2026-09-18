package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;

/** DAO EJB del catálogo {@link TipoMedioContacto}, con infraestructura heredada y sin EntityManager duplicado. */
@Stateless
@LocalBean
public class TipoMedioContactoDAO extends DefaultDAO<TipoMedioContacto> {

    /** Configura el DAO genérico para el tipo de medio de contacto. */
    public TipoMedioContactoDAO() {
        super(TipoMedioContacto.class);
    }
}
