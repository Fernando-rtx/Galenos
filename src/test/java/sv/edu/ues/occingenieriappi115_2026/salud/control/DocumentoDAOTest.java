package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class DocumentoDAOTest {

    @Test
    void buscaLaCombinacionGlobalPorTipoYValor() {
        DocumentoDAO dao = spy(new DocumentoDAO());
        UUID tipoId = UUID.randomUUID();
        String valor = "06887861-1";
        doReturn(List.of(new Documento())).when(dao)
                .obtenerPagina(eq(0), eq(1), anyList(), anyList());

        assertTrue(dao.existeDocumento(tipoId, valor));

        verify(dao).obtenerPagina(eq(0), eq(1), eq(List.of(
                new FiltroDAO("idTipoDocumento.idTipoDocumento", OperadorFiltro.IGUAL, tipoId),
                new FiltroDAO("valor", OperadorFiltro.IGUAL, valor))), eq(List.of()));
    }

    @Test
    void excluyeElUuidActualAlBuscarDuplicadosDuranteEdicion() {
        DocumentoDAO dao = spy(new DocumentoDAO());
        UUID tipoId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        String valor = "06887861-1";
        doReturn(List.of()).when(dao).obtenerPagina(eq(0), eq(1), anyList(), anyList());

        dao.existeDocumento(tipoId, valor, documentoId);

        verify(dao).obtenerPagina(eq(0), eq(1), eq(List.of(
                new FiltroDAO("idTipoDocumento.idTipoDocumento", OperadorFiltro.IGUAL, tipoId),
                new FiltroDAO("valor", OperadorFiltro.IGUAL, valor),
                new FiltroDAO("idDocumento", OperadorFiltro.DISTINTO, documentoId))), eq(List.of()));
    }
}
