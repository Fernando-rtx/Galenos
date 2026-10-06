package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.boundary.ClinicaActualBean;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Dependencias de sesión/EJB explícitas para las pruebas de Models aislados. */
final class ContextoConsultaFixture {
    static final UUID CLINICA = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static void configurar(Object model) {
        ClinicaActualBean contexto = mock(ClinicaActualBean.class);
        when(contexto.getIdClinicaActual()).thenReturn(CLINICA);
        when(contexto.isSeleccionada()).thenReturn(true);
        ConsultaFlujoService service = mock(ConsultaFlujoService.class);
        when(service.guardarConsulta(any(), any(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(service.crearProcedimiento(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(service.actualizarProcedimiento(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(service.actualizarPaso(any(), any())).thenAnswer(i -> i.getArgument(0));
        campo(model, "clinicaActualBean", contexto);
        campo(model, "flujoService", service);
    }
    static void campo(Object object, String name, Object value) {
        try {
            var field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(object, value);
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
    static ConsultaFlujoService servicio(Object model) {
        try {
            var field = model.getClass().getDeclaredField("flujoService");
            field.setAccessible(true);
            return (ConsultaFlujoService) field.get(model);
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
    static Clinica clinica() {
        Clinica c = new Clinica(CLINICA); c.setActivo(true); return c;
    }
    static Consulta consulta() {
        PersonaRol paciente = new PersonaRol(UUID.randomUUID());
        paciente.setIdClinica(clinica());
        paciente.setIdPersona(new Persona(UUID.randomUUID()));
        Rol rol = new Rol(UUID.randomUUID()); rol.setActivo(true); rol.setNombre("Paciente");
        paciente.setIdRol(rol);
        Consulta c = new Consulta(); c.setIdPersonaRol(paciente); return c;
    }
    static Procedimiento procedimientoActivo() {
        Procedimiento p = new Procedimiento(); p.setActivo(true); return p;
    }
}
