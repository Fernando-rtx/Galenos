package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ConsultaProcedimientoPasoDAOTest {
    @Test void transicionConsultaLaFilaConBloqueoDeEscritura() {
        EntityManager em = mock(EntityManager.class);
        var dao = new ConsultaProcedimientoPasoDAO() {
            @Override protected EntityManager getEntityManager() { return em; }
        };
        UUID id = UUID.randomUUID();
        var paso = new ConsultaProcedimientoPaso(id);
        when(em.find(ConsultaProcedimientoPaso.class, id, LockModeType.PESSIMISTIC_WRITE)).thenReturn(paso);
        assertSame(paso, dao.buscarParaActualizar(id));
        verify(em).find(ConsultaProcedimientoPaso.class, id, LockModeType.PESSIMISTIC_WRITE);
    }
}
