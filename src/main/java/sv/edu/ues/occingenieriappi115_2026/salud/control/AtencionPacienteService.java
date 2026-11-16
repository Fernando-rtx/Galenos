package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.Date;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Alta atómica de un paciente desde recepción; no permite elegir su rol. */
@Stateless @LocalBean @TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AtencionPacienteService {
    @EJB private PersonaFlujoService personas;
    @EJB private RolDAO roles;
    @EJB private ClinicaDAO clinicas;
    @EJB private ConsultaFlujoService consultas;

    public PersonaRol registrar(String nombres, String apellidos, Date nacimiento,
            UUID tipoDocumento, String documento, UUID clinica) {
        Clinica sede = clinicas.buscarPorId(clinica);
        if (sede == null || !Boolean.TRUE.equals(sede.getActivo()))
            throw new FlujoConsultaException("consultaFlujo.clinicaInvalida");
        Rol paciente = roles.obtenerTodos().stream()
                .filter(r -> Boolean.TRUE.equals(r.getActivo()) && r.getNombre() != null
                        && "paciente".equalsIgnoreCase(r.getNombre().trim()))
                .findFirst().orElseThrow(() -> new FlujoConsultaException("consultaFlujo.pacienteInvalido"));

        Persona persona = new Persona();
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        persona.setFechaNacimiento(nacimiento);
        persona.setFechaCreacion(new Date());
        persona = personas.guardarPersona(persona, false);

        Documento identidad = new Documento();
        identidad.setIdPersona(persona);
        identidad.setIdTipoDocumento(new TipoDocumento(tipoDocumento));
        identidad.setValor(documento);
        personas.guardarDocumento(identidad, false, null, null);

        PersonaRol asignacion = new PersonaRol();
        asignacion.setIdPersona(persona);
        asignacion.setIdRol(paciente);
        asignacion.setIdClinica(sede);
        asignacion.setFechaCreacion(new Date());
        return personas.guardarAsignacion(asignacion, false, null, null, null);
    }

    public Consulta crearCita(UUID paciente, UUID procedimiento, Date inicio, Date fin,
            String referencia, String observaciones, UUID clinica, PersonaRol recepcion) {
        Consulta datos = new Consulta();
        datos.setIdPersonaRol(new PersonaRol(paciente));
        datos.setFechaInicio(inicio);
        datos.setFechaFin(fin);
        datos.setReferenciaExterna(referencia);
        datos.setObservaciones(observaciones);
        Consulta cita = consultas.guardarConsulta(datos, clinica, false);
        ConsultaProcedimiento flujo = new ConsultaProcedimiento();
        flujo.setIdConsulta(cita);
        flujo.setIdProcedimiento(procedimiento);
        flujo.setFechaInicio(inicio);
        flujo.setObservaciones(observaciones);
        consultas.crearProcedimiento(flujo, clinica, recepcion);
        return cita;
    }
}
