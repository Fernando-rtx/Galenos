package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;

class ProcedimientoPasoInicialTest {
    private ProcedimientoPaso paso() { return new ProcedimientoPaso(UUID.randomUUID()); }
    private ProcedimientoPasoSecuencia enlace(ProcedimientoPaso a, ProcedimientoPaso b) {
        var s = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        s.setIdProcedimientoPaso(a); s.setIdProcedimientoPasoReferencia(b.getIdProcedimientoPaso());
        s.setTipoSecuencia("SIGUIENTE"); return s;
    }
    @Test void cadenaDevuelveRaizAunqueLosPasosEstanDesordenados() {
        var a = paso(); var b = paso(); var c = paso();
        assertSame(a, ProcedimientoPasoDAO.determinarPasoInicial(List.of(c,b,a), List.of(enlace(a,b),enlace(b,c))));
    }
    @Test void rechazaVacioYVariasRaices() {
        assertThrows(FlujoConsultaException.class, () -> ProcedimientoPasoDAO.determinarPasoInicial(List.of(),List.of()));
        assertThrows(FlujoConsultaException.class, () -> ProcedimientoPasoDAO.determinarPasoInicial(List.of(paso(),paso()),List.of()));
    }
    @Test void rechazaCicloDesconectadoAunqueHayUnaRaiz() {
        var a = paso(); var b = paso(); var c = paso();
        assertThrows(FlujoConsultaException.class, () -> ProcedimientoPasoDAO.determinarPasoInicial(List.of(a,b,c),List.of(enlace(b,c),enlace(c,b))));
    }
    @Test void rechazaDestinoDeOtroProcedimientoYCicloConRaiz() {
        var a = paso(); var b = paso(); var c = paso();
        assertThrows(FlujoConsultaException.class, () -> ProcedimientoPasoDAO.determinarPasoInicial(List.of(a,b),List.of(enlace(a,c))));
        assertThrows(FlujoConsultaException.class, () -> ProcedimientoPasoDAO.determinarPasoInicial(List.of(a,b,c),List.of(enlace(a,b),enlace(b,c),enlace(c,b))));
    }
}
