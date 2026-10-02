package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClinicaActualBeanTest {
    @Test
    void falloDeConexionNoRompeVistaYPermiteRecuperarse() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        when(dao.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
                .thenThrow(new IllegalStateException("conexión no disponible"))
                .thenReturn(java.util.List.of());
        ClinicaActualBean bean = new ClinicaActualBean(dao);
        assertTrue(bean.getClinicas().isEmpty());
        assertFalse(bean.isCatalogoDisponible());
        assertTrue(bean.getClinicas().isEmpty());
        assertTrue(bean.isCatalogoDisponible());
    }

    @Test
    void sesionesIndependientesYCambiosInvalidanFormularios() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        for (UUID id : new UUID[]{a, b}) {
            Clinica c = new Clinica(id);
            c.setActivo(true);
            when(dao.buscarPorId(id)).thenReturn(c);
        }
        ClinicaActualBean uno = new ClinicaActualBean(dao), dos = new ClinicaActualBean(dao);
        uno.seleccionarClinica(a);
        dos.seleccionarClinica(b);
        uno.seleccionarClinica(a);
        assertEquals(1, uno.getRevision());
        uno.seleccionarClinica(b);
        assertEquals(2, uno.getRevision());
        assertEquals(b, dos.getIdClinicaActual());
        assertEquals(1, dos.getRevision());
    }

    @Test
    void noAceptaClinicaInactivaInexistenteONula() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        UUID id = UUID.randomUUID();
        Clinica c = new Clinica(id);
        c.setActivo(false);
        when(dao.buscarPorId(id)).thenReturn(c);
        ClinicaActualBean bean = new ClinicaActualBean(dao);
        assertThrows(IllegalArgumentException.class, () -> bean.seleccionarClinica(id));
        assertThrows(IllegalArgumentException.class, () -> bean.seleccionarClinica(UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> bean.seleccionarClinica(null));
        assertFalse(bean.isSeleccionada());
    }
}
