package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PersonaRolDAOTest {

    @Test
    void buscaAsignacionesPorRolYClinicaConFiltrosCompuestos() {
        PersonaRolDAO dao = spy(new PersonaRolDAO());
        UUID idRol = UUID.randomUUID();
        UUID idClinica = UUID.randomUUID();
        List<PersonaRol> esperadas = List.of(new PersonaRol(UUID.randomUUID()));
        doReturn(esperadas).when(dao).obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList());

        assertEquals(esperadas, dao.buscarPorRolYClinica(idRol, idClinica));

        verify(dao).obtenerPagina(eq(0), eq(Integer.MAX_VALUE),
                eq(List.of(new FiltroDAO("idRol.idRol", OperadorFiltro.IGUAL, idRol),
                        new FiltroDAO("idClinica.idClinica", OperadorFiltro.IGUAL, idClinica),
                        new FiltroDAO("idRol.activo", OperadorFiltro.IGUAL, true),
                        new FiltroDAO("idClinica.activo", OperadorFiltro.IGUAL, true))),
                eq(List.of()));
    }

    @Test
    void idsNulosDevuelvenListaVaciaSinConsultar() {
        PersonaRolDAO dao = spy(new PersonaRolDAO());

        assertEquals(List.of(), dao.buscarPorRolYClinica(null, UUID.randomUUID()));
        assertEquals(List.of(), dao.buscarPorRolYClinica(UUID.randomUUID(), null));

        verify(dao, never()).obtenerPagina(anyInt(), anyInt(), anyList(), anyList());
    }

    @Test
    void buscaAsignacionesValidasPorClinicaSinRestringirElRol() {
        PersonaRolDAO dao = spy(new PersonaRolDAO());
        UUID idClinica = UUID.randomUUID();
        List<PersonaRol> esperadas = List.of(new PersonaRol(UUID.randomUUID()));
        doReturn(esperadas).when(dao).obtenerPagina(eq(0), eq(Integer.MAX_VALUE), anyList(), anyList());

        assertEquals(esperadas, dao.buscarPorClinica(idClinica));

        verify(dao).obtenerPagina(eq(0), eq(Integer.MAX_VALUE),
                eq(List.of(new FiltroDAO("idClinica.idClinica", OperadorFiltro.IGUAL, idClinica),
                        new FiltroDAO("idClinica.activo", OperadorFiltro.IGUAL, true),
                        new FiltroDAO("idRol.activo", OperadorFiltro.IGUAL, true))),
                eq(List.of()));
    }

    @Test
    void clinicaNulaNoConsultaAsignaciones() {
        PersonaRolDAO dao = spy(new PersonaRolDAO());

        assertEquals(List.of(), dao.buscarPorClinica(null));
        verify(dao, never()).obtenerPagina(anyInt(), anyInt(), anyList(), anyList());
    }

    @Test
    void detectaLaCombinacionPersonaClinicaYRolenUnaConsultaLimitada() {
        PersonaRolDAO dao = spy(new PersonaRolDAO());
        UUID idPersona = UUID.randomUUID();
        UUID idClinica = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        doReturn(List.of(new PersonaRol(UUID.randomUUID())))
                .when(dao).obtenerPagina(eq(0), eq(1), anyList(), anyList());

        assertEquals(true, dao.existeAsignacion(idPersona, idClinica, idRol));

        verify(dao).obtenerPagina(eq(0), eq(1),
                eq(List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL, idPersona),
                        new FiltroDAO("idClinica.idClinica", OperadorFiltro.IGUAL, idClinica),
                        new FiltroDAO("idRol.idRol", OperadorFiltro.IGUAL, idRol))),
                eq(List.of()));
    }

    @Test
    void asignacionInexistenteYOIdsNulosNoSeReportanComoDuplicados() {
        PersonaRolDAO dao = spy(new PersonaRolDAO());
        UUID idPersona = UUID.randomUUID();
        UUID idClinica = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();
        doReturn(List.of()).when(dao).obtenerPagina(eq(0), eq(1), anyList(), anyList());

        assertEquals(false, dao.existeAsignacion(idPersona, idClinica, idRol));
        assertEquals(false, dao.existeAsignacion(null, idClinica, idRol));

        verify(dao, times(1)).obtenerPagina(eq(0), eq(1), anyList(), anyList());
    }
}
