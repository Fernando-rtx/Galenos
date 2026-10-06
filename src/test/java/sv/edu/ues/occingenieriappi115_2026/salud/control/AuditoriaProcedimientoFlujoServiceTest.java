package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditoriaProcedimientoFlujoServiceTest {
    private final ProcedimientoPasoDAO pasos = mock(ProcedimientoPasoDAO.class);
    private final ProcedimientoPasoExamenDAO asociaciones = mock(ProcedimientoPasoExamenDAO.class);
    private final ProcedimientoPasoSecuenciaDAO secuencias = mock(ProcedimientoPasoSecuenciaDAO.class);
    private final RolDAO roles = mock(RolDAO.class);
    private final ExamenDAO examenes = mock(ExamenDAO.class);
    private final ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
    private final Rol rol = new Rol(UUID.randomUUID());
    private final Examen examen = new Examen(UUID.randomUUID());

    private ProcedimientoFlujoService servicio() throws Exception {
        ProcedimientoFlujoService service = new ProcedimientoFlujoService();
        for (Object[] par : new Object[][]{{"pasoDAO", pasos}, {"pasoExamenDAO", asociaciones},
            {"secuenciaDAO", secuencias}, {"rolDAO", roles}, {"examenDAO", examenes}}) {
            Field campo = service.getClass().getDeclaredField((String) par[0]);
            campo.setAccessible(true); campo.set(service, par[1]);
        }
        paso.setIdProcedimiento(new Procedimiento(UUID.randomUUID()));
        paso.setIdRol(rol);
        paso.setNombre("Antes");
        paso.setIndicaFin(false);
        // Una relación histórica puede apuntar a catálogos que ahora están inactivos.
        rol.setActivo(false); examen.setActivo(false);
        ProcedimientoPasoExamen asociacion = new ProcedimientoPasoExamen(UUID.randomUUID());
        asociacion.setIdProcedimientoPaso(paso); asociacion.setIdExamen(examen);
        when(pasos.buscarPorId(paso.getIdProcedimientoPaso())).thenReturn(paso);
        when(pasos.obtenerTodos()).thenReturn(List.of(paso));
        when(asociaciones.obtenerTodos()).thenReturn(List.of(asociacion));
        when(roles.buscarPorId(rol.getIdRol())).thenReturn(rol);
        when(examenes.buscarPorId(examen.getIdExamen())).thenReturn(examen);
        return service;
    }

    @Test
    void peticionConOtroRolNoActualizaPasoNiAsociaciones() throws Exception {
        ProcedimientoFlujoService service = servicio();
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                service.actualizarPaso(paso.getIdProcedimientoPaso(), "Después", UUID.randomUUID(), false,
                        List.of(examen.getIdExamen())));
        assertEquals("relaciones.noEditables", error.getMessage());
        assertEquals("Antes", paso.getNombre());
        verify(pasos, never()).actualizar(any());
        verifyNoInteractions(secuencias);
        verify(asociaciones, never()).guardar(any());
        verify(asociaciones, never()).eliminar(any());
    }

    @Test
    void peticionConOtrosExamenesNoReemplazaRelaciones() throws Exception {
        ProcedimientoFlujoService service = servicio();
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                service.actualizarPaso(paso.getIdProcedimientoPaso(), "Después", rol.getIdRol(), false,
                        List.of(UUID.randomUUID())));
        assertEquals("relaciones.noEditables", error.getMessage());
        verify(pasos, never()).actualizar(any());
        verify(asociaciones, never()).guardar(any());
        verify(asociaciones, never()).eliminar(any());
    }

    @Test
    void quitarExamenDuranteEdicionSeRechaza() throws Exception {
        ProcedimientoFlujoService service = servicio();
        assertThrows(IllegalArgumentException.class, () ->
                service.actualizarPaso(paso.getIdProcedimientoPaso(), "Después", rol.getIdRol(), false, List.of()));
        verify(pasos, never()).actualizar(any());
        verify(asociaciones, never()).eliminar(any());
    }

    @Test
    void conservaRelacionesInactivasYActualizaNombreYFin() throws Exception {
        ProcedimientoFlujoService service = servicio();
        service.actualizarPaso(paso.getIdProcedimientoPaso(), " Después ", rol.getIdRol(), true,
                List.of(examen.getIdExamen()));
        verify(pasos).actualizar(paso);
        assertEquals("Después", paso.getNombre());
        assertTrue(paso.getIndicaFin());
        assertSame(rol, paso.getIdRol());
        verify(asociaciones, never()).guardar(any());
        verify(asociaciones, never()).actualizar(any());
        verify(asociaciones, never()).eliminar(any());
    }
}
