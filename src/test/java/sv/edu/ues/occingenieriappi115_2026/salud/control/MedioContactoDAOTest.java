package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MedioContactoDAOTest {

    @Test
    void buscaPersonaTipoYValorExactosConUnaFilaMaxima() {
        MedioContactoDAO dao = spy(new MedioContactoDAO());
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        UUID idExcluir = UUID.randomUUID();
        String valor = "72105157";
        doReturn(List.of(new MedioContacto(UUID.randomUUID())))
                .when(dao).obtenerPagina(eq(0), eq(1), anyList(), anyList());

        assertTrue(dao.existeContacto(idPersona, idTipo, valor, idExcluir));

        verify(dao).obtenerPagina(eq(0), eq(1),
                eq(List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL, idPersona),
                        new FiltroDAO("idTipoMedioContacto.idTipoMedioContacto", OperadorFiltro.IGUAL, idTipo),
                        new FiltroDAO("valor", OperadorFiltro.IGUAL, valor),
                        new FiltroDAO("idMedioContacto", OperadorFiltro.DISTINTO, idExcluir))),
                eq(List.of()));
    }

    @Test
    void consultaSinExclusionSirveParaCrear() {
        MedioContactoDAO dao = spy(new MedioContactoDAO());
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        doReturn(List.of()).when(dao).obtenerPagina(eq(0), eq(1), anyList(), anyList());

        assertFalse(dao.existeContacto(idPersona, idTipo, "77778888", null));

        verify(dao).obtenerPagina(eq(0), eq(1),
                eq(List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL, idPersona),
                        new FiltroDAO("idTipoMedioContacto.idTipoMedioContacto", OperadorFiltro.IGUAL, idTipo),
                        new FiltroDAO("valor", OperadorFiltro.IGUAL, "77778888"))),
                eq(List.of()));
    }

    @Test
    void valorNoExistenteYArgumentosNulosNoSonDuplicados() {
        MedioContactoDAO dao = spy(new MedioContactoDAO());
        UUID idPersona = UUID.randomUUID();
        UUID idTipo = UUID.randomUUID();
        doReturn(List.of()).when(dao).obtenerPagina(eq(0), eq(1), anyList(), anyList());

        assertFalse(dao.existeContacto(idPersona, idTipo, "77778888", null));
        assertFalse(dao.existeContacto(null, idTipo, "77778888", null));

        verify(dao, times(1)).obtenerPagina(eq(0), eq(1), anyList(), anyList());
    }
}
