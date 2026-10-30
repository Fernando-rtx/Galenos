package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Casos de uso de Consulta: lecturas acotadas por clínica y escrituras atómicas. */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ConsultaFlujoService {
    @EJB private ClinicaDAO clinicaDAO;
    @EJB private PersonaRolDAO personaRolDAO;
    @EJB private ConsultaDAO consultaDAO;
    @EJB private ProcedimientoDAO procedimientoDAO;
    @EJB private ProcedimientoPasoDAO procedimientoPasoDAO;
    @EJB private ConsultaProcedimientoDAO consultaProcedimientoDAO;
    @EJB private ConsultaProcedimientoPasoDAO pasoDAO;

    public ConsultaFlujoService() { }

    /** Criterio compartido por el listado JSF y la lectura REST. */
    public static List<FiltroDAO> filtrosConsulta(UUID clinica, LocalDate desde,
            LocalDate hasta, ZoneId zona, UUID personaRol) {
        if (clinica == null || zona == null) {
            throw new IllegalArgumentException("Clinica y zona son requeridas");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new FlujoConsultaException("consulta.rangoInvalido");
        }
        List<FiltroDAO> filtros = new ArrayList<>();
        filtros.add(new FiltroDAO("idPersonaRol.idClinica.idClinica", OperadorFiltro.IGUAL, clinica));
        if (desde != null) filtros.add(new FiltroDAO("fechaInicio", OperadorFiltro.MAYOR_O_IGUAL,
                Date.from(desde.atStartOfDay(zona).toInstant())));
        if (hasta != null) filtros.add(new FiltroDAO("fechaInicio", OperadorFiltro.MENOR_QUE,
                Date.from(hasta.plusDays(1).atStartOfDay(zona).toInstant())));
        if (personaRol != null) filtros.add(new FiltroDAO("idPersonaRol.idPersonaRol", OperadorFiltro.IGUAL, personaRol));
        return filtros;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Consulta> buscarConsultas(UUID clinica, LocalDate desde, LocalDate hasta,
            ZoneId zona, UUID personaRol, int primero, int tamano) {
        validarClinica(clinica);
        return consultaDAO.obtenerPagina(primero, tamano,
                filtrosConsulta(clinica, desde, hasta, zona, personaRol),
                List.of(new OrdenDAO("idConsulta", DireccionOrden.ASCENDENTE)));
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarConsultas(UUID clinica, LocalDate desde, LocalDate hasta, ZoneId zona, UUID personaRol) {
        validarClinica(clinica);
        return consultaDAO.contar(filtrosConsulta(clinica, desde, hasta, zona, personaRol));
    }

    /** El detalle solo puede resolverse dentro de la clínica activa recibida. */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Consulta buscarConsulta(UUID id, UUID clinica) {
        Objects.requireNonNull(id, "El UUID de consulta es requerido");
        validarClinica(clinica);
        List<FiltroDAO> filtros = filtrosConsulta(clinica, null, null, ZoneId.of("UTC"), null);
        filtros.add(new FiltroDAO("idConsulta", OperadorFiltro.IGUAL, id));
        List<Consulta> resultado = consultaDAO.obtenerPagina(0, 1, filtros, List.of());
        return resultado.isEmpty() ? null : resultado.getFirst();
    }

    public Consulta guardarConsulta(Consulta datos, UUID clinica, boolean edicion) {
        validarClinica(clinica);
        UUID pacienteId = datos.getIdPersonaRol() == null ? null : datos.getIdPersonaRol().getIdPersonaRol();
        PersonaRol paciente = pacienteId == null ? null : personaRolDAO.buscarPorId(pacienteId);
        if (!personaEnClinica(paciente, clinica) || paciente.getIdRol().getNombre() == null
                || !"paciente".equalsIgnoreCase(paciente.getIdRol().getNombre().trim())) {
            throw new FlujoConsultaException("consultaFlujo.pacienteInvalido");
        }
        validarFechas(datos.getFechaInicio(), datos.getFechaFin());
        datos.setReferenciaExterna(datos.getReferenciaExterna() == null ? null : datos.getReferenciaExterna().trim());
        datos.setObservaciones(datos.getObservaciones() == null ? null : datos.getObservaciones().trim());
        if (datos.getReferenciaExterna() != null && datos.getReferenciaExterna().length() > 255) {
            throw new FlujoConsultaException("consultaFlujo.referenciaExtensa");
        }
        if (!edicion) {
            datos.setIdPersonaRol(paciente);
            return consultaDAO.guardar(datos);
        }
        Consulta persistida = consultaEnClinica(datos.getIdConsulta(), clinica);
        exigirRelacion(persistida.getIdPersonaRol().getIdPersonaRol(), pacienteId);
        persistida.setFechaInicio(datos.getFechaInicio());
        persistida.setFechaFin(datos.getFechaFin());
        persistida.setReferenciaExterna(datos.getReferenciaExterna());
        persistida.setObservaciones(datos.getObservaciones());
        return consultaDAO.actualizar(persistida);
    }

    public ConsultaProcedimiento crearProcedimiento(ConsultaProcedimiento datos, UUID clinica) {
        validarClinica(clinica);
        Consulta consulta = consultaEnClinica(idConsulta(datos), clinica);
        UUID id = datos.getIdProcedimiento();
        Procedimiento procedimiento = id == null ? null : procedimientoDAO.buscarPorId(id);
        if (procedimiento == null || !Boolean.TRUE.equals(procedimiento.getActivo())) {
            throw new FlujoConsultaException("consultaFlujo.procedimientoInactivo");
        }
        validarFechas(datos.getFechaInicio(), datos.getFechaFin());
        ProcedimientoPaso inicial = procedimientoPasoDAO.obtenerPasoInicial(id);
        if (inicial == null || inicial.getIdRol() == null
                || inicial.getIdProcedimiento() == null
                || !id.equals(inicial.getIdProcedimiento().getIdProcedimiento())
                || inicial.getIdRol().getIdRol() == null || !Boolean.TRUE.equals(inicial.getIdRol().getActivo())) {
            throw new FlujoConsultaException("consultaFlujo.rolInactivo");
        }
        var responsables = personaRolDAO.buscarResponsablesPorClinicaYRol(clinica, inicial.getIdRol().getIdRol());
        PersonaRol responsable = responsables.stream()
                .filter(r -> personaEnClinica(r, clinica))
                .filter(r -> inicial.getIdRol().getIdRol().equals(r.getIdRol().getIdRol()))
                .findFirst().orElseThrow(() -> new FlujoConsultaException("consultaFlujo.sinResponsable"));
        datos.setIdConsulta(consulta);
        ConsultaProcedimiento asociacion = consultaProcedimientoDAO.guardar(datos);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        paso.setIdConsultaProcedimiento(asociacion);
        paso.setIdPersonaRol(responsable);
        paso.setFechaInicio(datos.getFechaInicio());
        paso.setEstado("PENDIENTE");
        pasoDAO.guardar(paso);
        return asociacion;
    }

    public ConsultaProcedimiento actualizarProcedimiento(ConsultaProcedimiento datos, UUID clinica) {
        validarClinica(clinica);
        ConsultaProcedimiento persistido = datos.getIdConsultaProcedimiento() == null ? null
                : consultaProcedimientoDAO.buscarPorId(datos.getIdConsultaProcedimiento());
        if (persistido == null) { throw new FlujoConsultaException("consultaFlujo.contextoInvalido"); }
        consultaEnClinica(idConsulta(persistido), clinica);
        exigirRelacion(idConsulta(persistido), idConsulta(datos));
        exigirRelacion(persistido.getIdProcedimiento(), datos.getIdProcedimiento());
        validarFechas(datos.getFechaInicio(), datos.getFechaFin());
        persistido.setFechaInicio(datos.getFechaInicio());
        persistido.setFechaFin(datos.getFechaFin());
        persistido.setObservaciones(datos.getObservaciones());
        return consultaProcedimientoDAO.actualizar(persistido);
    }

    public ConsultaProcedimientoPaso actualizarPaso(ConsultaProcedimientoPaso datos, UUID clinica) {
        validarClinica(clinica);
        ConsultaProcedimientoPaso persistido = datos.getIdConsultaProcedimientoPaso() == null ? null
                : pasoDAO.buscarPorId(datos.getIdConsultaProcedimientoPaso());
        if (persistido == null || persistido.getIdConsultaProcedimiento() == null || datos.getIdConsultaProcedimiento() == null
                || persistido.getIdPersonaRol() == null || datos.getIdPersonaRol() == null) {
            throw new FlujoConsultaException("consultaFlujo.contextoInvalido");
        }
        consultaEnClinica(idConsulta(persistido.getIdConsultaProcedimiento()), clinica);
        exigirRelacion(persistido.getIdConsultaProcedimiento().getIdConsultaProcedimiento(),
                datos.getIdConsultaProcedimiento().getIdConsultaProcedimiento());
        exigirRelacion(persistido.getIdPersonaRol().getIdPersonaRol(), datos.getIdPersonaRol().getIdPersonaRol());
        PersonaRol responsable = personaRolDAO.buscarPorId(datos.getIdPersonaRol().getIdPersonaRol());
        if (!personaEnClinica(responsable, clinica)) {
            throw new FlujoConsultaException("consultaFlujo.sinResponsable");
        }
        validarFechas(datos.getFechaInicio(), datos.getFechaFin());
        if (!java.util.Set.of("PENDIENTE", "EN_CURSO", "COMPLETADO").contains(datos.getEstado() == null ? "" : datos.getEstado())) {
            throw new FlujoConsultaException("consultaFlujo.estadoInvalido");
        }
        persistido.setFechaInicio(datos.getFechaInicio());
        persistido.setFechaFin(datos.getFechaFin());
        persistido.setEstado(datos.getEstado());
        return pasoDAO.actualizar(persistido);
    }


    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Clinica> clinicasDisponibles(java.util.Set<UUID> ids, int primero, int tamano) {
        return clinicaDAO.buscarDisponibles(ids, primero, tamano);
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarClinicasDisponibles(java.util.Set<UUID> ids) { return clinicaDAO.contarDisponibles(ids); }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<PersonaRol> buscarPacientes(UUID clinica, String criterio) {
        validarClinica(clinica);
        return personaRolDAO.buscarPacientesPorClinica(clinica, criterio, 15);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Procedimiento> procedimientosDisponibles(int primero, int tamano) {
        return procedimientoDAO.obtenerPagina(primero, tamano,
                List.of(new FiltroDAO("activo", OperadorFiltro.IGUAL, true)),
                List.of(new OrdenDAO("idProcedimiento", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarProcedimientosDisponibles() {
        return procedimientoDAO.contar(List.of(new FiltroDAO("activo", OperadorFiltro.IGUAL, true)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<ConsultaProcedimiento> procedimientosConsulta(UUID clinica, UUID consulta, int primero, int tamano) {
        consultaEnClinica(consulta, clinica);
        return consultaProcedimientoDAO.obtenerPagina(primero, tamano,
                List.of(new FiltroDAO("idConsulta.idConsulta", OperadorFiltro.IGUAL, consulta)),
                List.of(new OrdenDAO("idConsultaProcedimiento", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarProcedimientosConsulta(UUID clinica, UUID consulta) {
        consultaEnClinica(consulta, clinica);
        return consultaProcedimientoDAO.contar(List.of(new FiltroDAO("idConsulta.idConsulta", OperadorFiltro.IGUAL, consulta)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public ConsultaProcedimiento buscarProcedimientoConsulta(UUID clinica, UUID id) {
        var value = consultaProcedimientoDAO.buscarPorId(id);
        if (value != null) consultaEnClinica(idConsulta(value), clinica);
        return value;
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<ConsultaProcedimientoPaso> pasosConsulta(UUID clinica, UUID consulta, int primero, int tamano) {
        consultaEnClinica(consulta, clinica);
        return pasoDAO.obtenerPagina(primero, tamano,
                List.of(new FiltroDAO("idConsultaProcedimiento.idConsulta.idConsulta", OperadorFiltro.IGUAL, consulta)),
                List.of(new OrdenDAO("idConsultaProcedimientoPaso", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarPasosConsulta(UUID clinica, UUID consulta) {
        consultaEnClinica(consulta, clinica);
        return pasoDAO.contar(List.of(new FiltroDAO("idConsultaProcedimiento.idConsulta.idConsulta", OperadorFiltro.IGUAL, consulta)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public ConsultaProcedimientoPaso buscarPasoConsulta(UUID clinica, UUID id) {
        var value = pasoDAO.buscarPorId(id);
        if (value != null) consultaEnClinica(idConsulta(value.getIdConsultaProcedimiento()), clinica);
        return value;
    }

    private void validarClinica(UUID id) {
        Clinica clinica = id == null ? null : clinicaDAO.buscarPorId(id);
        if (clinica == null || !Boolean.TRUE.equals(clinica.getActivo())) {
            throw new FlujoConsultaException("clinicaActual.noDisponible");
        }
    }

    private Consulta consultaEnClinica(UUID id, UUID clinica) {
        Consulta consulta = id == null ? null : consultaDAO.buscarPorId(id);
        if (consulta == null || !personaEnClinica(consulta.getIdPersonaRol(), clinica)
                || !"paciente".equalsIgnoreCase(consulta.getIdPersonaRol().getIdRol().getNombre())) {
            throw new FlujoConsultaException("consultaFlujo.contextoInvalido");
        }
        return consulta;
    }

    public static boolean personaEnClinica(PersonaRol persona, UUID clinica) {
        return clinica != null && persona != null && persona.getIdPersonaRol() != null
                && persona.getIdPersona() != null && persona.getIdPersona().getIdPersona() != null
                && persona.getIdClinica() != null && clinica.equals(persona.getIdClinica().getIdClinica())
                && Boolean.TRUE.equals(persona.getIdClinica().getActivo())
                && persona.getIdRol() != null && Boolean.TRUE.equals(persona.getIdRol().getActivo());
    }

    private static UUID idConsulta(ConsultaProcedimiento p) {
        return p.getIdConsulta() == null ? null : p.getIdConsulta().getIdConsulta();
    }

    private static void exigirRelacion(UUID original, UUID actual) {
        if (original == null || !Objects.equals(original, actual)) {
            throw new FlujoConsultaException("consultaFlujo.relacionBloqueada");
        }
    }

    private static void validarFechas(Date inicio, Date fin) {
        if (inicio == null || (fin != null && fin.before(inicio))) {
            throw new FlujoConsultaException("consultaFlujo.fechasInvalidas");
        }
    }
}
