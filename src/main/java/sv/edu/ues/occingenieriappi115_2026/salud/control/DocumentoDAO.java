package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;

@ApplicationScoped
public class DocumentoDAO extends DefaultDAO<Documento> {

    public DocumentoDAO() {
        super(Documento.class);
    }
}
