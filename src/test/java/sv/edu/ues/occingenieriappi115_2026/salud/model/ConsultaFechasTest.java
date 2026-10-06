package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sv.edu.ues.occingenieriappi115_2026.salud.model.ContextoConsultaFixture.*;

class ConsultaFechasTest {
    private ConsultaModel model(ConsultaDAO dao) {
        var m = new ConsultaModel(dao,mock(PersonaRolDAO.class)); configurar(m); return m;
    }
    private Date inicio(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.of("America/El_Salvador")).toInstant());
    }
    @Test void hastaIncluyeDiaCompletoYConteoComparteIntervalo() {
        var dao = mock(ConsultaDAO.class); var m = model(dao);
        var desde = LocalDate.of(2026,10,1); var hasta = LocalDate.of(2026,10,2);
        m.setFechaDesde(desde); m.setFechaHasta(hasta); m.aplicarFiltroFechas();
        var filtros = List.of(new FiltroDAO("idPersonaRol.idClinica.idClinica",OperadorFiltro.IGUAL,CLINICA),
                new FiltroDAO("fechaInicio",OperadorFiltro.MAYOR_O_IGUAL,inicio(desde)),
                new FiltroDAO("fechaInicio",OperadorFiltro.MENOR_QUE,inicio(hasta.plusDays(1))));
        when(dao.contar(filtros)).thenReturn(11L);
        assertEquals(11,m.count(Map.of()));
        m.load(0,10,Map.of(),Map.of());
        verify(dao).obtenerPagina(0,10,filtros,List.of());
    }
    @Test void rangoInvalidoConservaFiltroAnteriorYLimpiarLoRetira() {
        var m = model(mock(ConsultaDAO.class)); var fecha = LocalDate.of(2026,10,1);
        m.setFechaDesde(fecha); m.aplicarFiltroFechas();
        var anterior = m.filtrosAdicionales();
        m.setFechaHasta(fecha.minusDays(1)); m.aplicarFiltroFechas();
        assertEquals(anterior,m.filtrosAdicionales());
        m.limpiarFiltroFechas();
        assertEquals(1,m.filtrosAdicionales().size()); assertNull(m.getFechaDesde()); assertNull(m.getFechaHasta());
    }
    @Test void permiteSoloHastaYSoloDesde() {
        var m = model(mock(ConsultaDAO.class)); var fecha = LocalDate.of(2026,10,1);
        m.setFechaHasta(fecha); m.aplicarFiltroFechas();
        assertEquals(OperadorFiltro.MENOR_QUE,m.filtrosAdicionales().get(1).operador());
        m.limpiarFiltroFechas(); m.setFechaDesde(fecha); m.aplicarFiltroFechas();
        assertEquals(OperadorFiltro.MAYOR_O_IGUAL,m.filtrosAdicionales().get(1).operador());
    }
}
