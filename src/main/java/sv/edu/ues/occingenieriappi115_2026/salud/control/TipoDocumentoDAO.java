package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

@ApplicationScoped
public class TipoDocumentoDAO extends DefaultDAO<TipoDocumento> {

    public TipoDocumentoDAO() {
        super(TipoDocumento.class);
    }
}
