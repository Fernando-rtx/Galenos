package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;

/** DAO EJB de {@link Documento}; Liberty administra su ciclo de vida y las transacciones heredadas. */
@Stateless
@LocalBean
public class DocumentoDAO extends DefaultDAO<Documento> {

    /** Indica a {@link DefaultDAO} que administra documentos. */
    public DocumentoDAO() {
        super(Documento.class);
    }

    /**
     * Comprueba globalmente la combinación de tipo documental y valor.
     *
     * @param idTipoDocumento UUID del tipo de documento
     * @param valor valor normalizado del documento
     * @return {@code true} si ya existe esa combinación en PostgreSQL
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean existeDocumento(UUID idTipoDocumento, String valor) {
        return existeDocumento(idTipoDocumento, valor, null);
    }

    /**
     * Comprueba la combinación global excluyendo, si corresponde, el registro
     * que se está editando.
     *
     * @param idTipoDocumento UUID del tipo de documento
     * @param valor valor normalizado del documento
     * @param idDocumentoExcluir UUID del documento actual, o {@code null} al crear
     * @return {@code true} si otro registro ya tiene esa combinación
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean existeDocumento(UUID idTipoDocumento, String valor, UUID idDocumentoExcluir) {
        if (idTipoDocumento == null || valor == null) {
            return false;
        }
        List<FiltroDAO> filtros = new ArrayList<>(List.of(
                new FiltroDAO("idTipoDocumento.idTipoDocumento", OperadorFiltro.IGUAL, idTipoDocumento),
                new FiltroDAO("valor", OperadorFiltro.IGUAL, valor)));
        if (idDocumentoExcluir != null) {
            filtros.add(new FiltroDAO("idDocumento", OperadorFiltro.DISTINTO, idDocumentoExcluir));
        }
        return !obtenerPagina(0, 1, filtros, List.of()).isEmpty();
    }
}
