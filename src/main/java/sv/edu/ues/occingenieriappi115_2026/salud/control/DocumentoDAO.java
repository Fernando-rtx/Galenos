package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;

/** DAO EJB de {@link Documento}; Liberty administra su ciclo de vida y las transacciones heredadas. */
@Stateless
@LocalBean
public class DocumentoDAO extends DefaultDAO<Documento> {

    /** Indica a {@link DefaultDAO} que administra documentos. */
    public DocumentoDAO() {
        super(Documento.class);
    }
}
