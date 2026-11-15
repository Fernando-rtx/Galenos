package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.json.bind.annotation.JsonbNillable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Academic use cases for the patient and employee portals. */
@Stateless @LocalBean
public class AcademicCareService {
    @PersistenceContext(unitName = "GalenoPU") private EntityManager em;
    private static final ZoneId ZONE = ZoneId.of("America/El_Salvador");

    public record Clinic(String idClinica, String nombre) {}
    public record Role(String idPersonaRol, String idRol, String nombreRol, String idClinica, String nombreClinica) {}
    @JsonbNillable public record Identity(String idPersona, String nombres, String apellidos, List<Clinic> clinicas,
                           List<Role> roles) {}
    @JsonbNillable public record History(String idPaso, String idConsultaProcedimientoPaso, String nombre, String fechaInicio, String fechaFin,
                          String responsable, String valor, String estado, String rol, List<ExamOrder> ordenes) {}
    @JsonbNillable public record ExamResult(String idExamenResultado, String fechaCreacion, String resultado, String interpretacion) {}
    @JsonbNillable public record ExamOrder(String idOrdenExamen, String examen, String fechaCreacion, String indicaciones, List<ExamResult> resultados) {}
    @JsonbNillable public record Appointment(String idConsulta, String fechaInicio, String fechaFin,
                              String paciente, String clinica, String procedimiento,
                              String idPasoActual, String pasoActual, List<History> historial,
                              String idConsultaProcedimientoActual, String idConsultaProcedimientoPasoActual,
                              String estadoPasoActual, List<ProcedureHistory> procedimientos) {}
    @JsonbNillable public record Sequence(String idPasoOrigen, String idPasoDestino, String tipoSecuencia) {}
    @JsonbNillable public record ProcedureHistory(String idConsultaProcedimiento, String idProcedimiento,
                              String nombre, List<History> pasos, List<Sequence> secuencias) {}
    @JsonbNillable public record Exam(String idExamen, String nombre) {}
    @JsonbNillable public record NextStep(String idPaso, String nombre, String tipoSecuencia) {}
    @JsonbNillable public record PreviousStep(String idConsultaProcedimientoPaso, String idPaso, String nombre) {}
    @JsonbNillable public record CareDetail(String idConsulta, String idConsultaProcedimientoPaso, String paciente, String clinica, String rol,
                             String procedimiento, String idPasoActual, String pasoActual,
                             String valorActual, boolean pasoFinal, List<History> historial, List<Exam> examenes,
                             List<NextStep> siguientes, boolean puedeRetroceder,
                             String fechaConsulta, String empleado, String idPersonaRol,
                             String idConsultaProcedimiento, String estadoPasoActual,
                             List<ExamOrder> ordenesActuales, List<PreviousStep> anteriores) {}
    @JsonbNillable public record CareResult(String idConsulta, String accion, String estado, String idConsultaProcedimientoPaso,
                             String idPasoActual, boolean finalizada) {}

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Identity employee(UUID typeId, String number, UUID expectedPerson) {
        Persona person = findPerson(typeId, number);
        if (person == null || expectedPerson == null || !expectedPerson.equals(person.getIdPersona())) return null;
        List<PersonaRol> roles = assignments(person.getIdPersona());
        List<PersonaRol> staff = roles.stream().filter(r -> !patient(r.getIdRol())).toList();
        if (staff.isEmpty()) return null;
        return identity(person, staff);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Identity patient(UUID typeId, String number) {
        Persona person = findPerson(typeId, number);
        if (person == null) return null;
        List<PersonaRol> roles = assignments(person.getIdPersona()).stream()
                .filter(r -> patient(r.getIdRol())).toList();
        return roles.isEmpty() ? null : identity(person, roles);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Appointment> patientAppointments(UUID typeId, String number, UUID clinic,
                                                 LocalDate from, LocalDate until, int first, int max) {
        Persona person = requirePerson(typeId, number);
        return patientAppointments(person.getIdPersona(), clinic, from, until, first, max);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Appointment> patientAppointments(UUID personId, UUID clinic,
                                                 LocalDate from, LocalDate until, int first, int max) {
        Persona person = em.find(Persona.class, personId);
        if (person == null) throw new FlujoConsultaException("identidad.noEncontrada");
        PersonaRol patient = assignments(person.getIdPersona()).stream()
                .filter(r -> patient(r.getIdRol()) && clinic.equals(r.getIdClinica().getIdClinica()))
                .findFirst().orElseThrow(() -> new FlujoConsultaException("consultaFlujo.contextoInvalido"));
        return appointments("c.idPersonaRol.idPersonaRol = :assignment", patient.getIdPersonaRol(),
                clinic, null, from, until, first, max);
    }

    public long patientAppointmentCount(UUID typeId, String number, UUID clinic, LocalDate from, LocalDate until) {
        Persona person = requirePerson(typeId, number);
        return patientAppointmentCount(person.getIdPersona(), clinic, from, until);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long patientAppointmentCount(UUID personId, UUID clinic, LocalDate from, LocalDate until) {
        Persona person = em.find(Persona.class, personId);
        if (person == null) throw new FlujoConsultaException("identidad.noEncontrada");
        PersonaRol patient = assignments(person.getIdPersona()).stream()
                .filter(r -> patient(r.getIdRol()) && clinic.equals(r.getIdClinica().getIdClinica()))
                .findFirst().orElseThrow(() -> new FlujoConsultaException("consultaFlujo.contextoInvalido"));
        return countAppointments("c.idPersonaRol.idPersonaRol = :assignment", patient.getIdPersonaRol(), clinic, null, from, until);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Appointment> employeeAppointments(UUID employee, UUID clinic, UUID role,
                                                 LocalDate from, LocalDate until, int first, int max) {
        requireAssignment(employee, clinic, role);
        return appointments(staffPredicate(),
                employee, clinic, role, from, until, first, max);
    }

    public long employeeAppointmentCount(UUID employee, UUID clinic, UUID role, LocalDate from, LocalDate until) {
        requireAssignment(employee, clinic, role);
        return countAppointments(staffPredicate(),
                employee, clinic, role, from, until);
    }

    /** Busca únicamente dentro de la bandeja autorizada; nunca publica personas globales. */
    public List<Appointment> searchEmployeeAppointments(UUID employee, UUID clinic, UUID role,
            LocalDate from, LocalDate until, int first, int max, UUID type, String number) {
        requireAssignment(employee, clinic, role);
        UUID patient = documentPerson(type, number);
        if (patient == null) return List.of();
        return appointments(staffPredicate() + " AND pr.idPersona.idPersona=:patient",
                employee, clinic, role, from, until, first, max, patient);
    }
    public long searchEmployeeAppointmentCount(UUID employee, UUID clinic, UUID role,
            LocalDate from, LocalDate until, UUID type, String number) {
        requireAssignment(employee, clinic, role);
        UUID patient = documentPerson(type, number);
        if (patient == null) return 0;
        return countAppointments(staffPredicate() + " AND pr.idPersona.idPersona=:patient",
                employee, clinic, role, from, until, patient);
    }
    private static String staffPredicate() {
        return "p.idPersonaRol.idPersona.idPersona = :person AND p.idPersonaRol.idRol.idRol = :role "
                + "AND p.idPersonaRol.idClinica.idClinica = :clinic "
                + "AND p.estado IN ('PENDIENTE','EN_CURSO') AND p.idProcedimientoPaso.idRol.idRol = :role";
    }
    private UUID documentPerson(UUID type, String number) {
        TipoDocumento documentType = em.find(TipoDocumento.class, type);
        if (documentType == null) throw new IdentificacionException(404, "Tipo de documento no encontrado");
        if (!Boolean.TRUE.equals(documentType.getActivo()))
            throw new IdentificacionException(422, "Tipo de documento no disponible");
        if (number == null || number.isBlank()) throw new IdentificacionException(422, "Ingrese el número de documento");
        String regex = documentType.getExpresionRegular();
        if (regex != null && !regex.isBlank() && !java.util.regex.Pattern.matches(regex, number.trim()))
            throw new IdentificacionException(422, "Formato de documento inválido");
        Persona person = findPerson(type, number);
        return person == null ? null : person.getIdPersona();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public CareDetail detail(UUID employee, UUID clinic, UUID role, UUID consultationId) {
        requireAssignment(employee, clinic, role);
        Consulta c = em.createQuery("SELECT c FROM Consulta c JOIN FETCH c.idPersonaRol pr "
                + "JOIN FETCH pr.idPersona JOIN FETCH pr.idClinica WHERE c.idConsulta=:id "
                + "AND pr.idClinica.idClinica=:clinic", Consulta.class)
                .setParameter("id", consultationId).setParameter("clinic", clinic)
                .getResultStream().findFirst().orElseThrow(() -> new FlujoConsultaException("consultaFlujo.contextoInvalido"));
        List<ConsultaProcedimiento> associations = em.createQuery("SELECT cp FROM ConsultaProcedimiento cp "
                + "WHERE cp.idConsulta.idConsulta=:id ORDER BY cp.fechaInicio", ConsultaProcedimiento.class)
                .setParameter("id", consultationId).getResultList();
        List<ConsultaProcedimientoPaso> history = associations.stream().flatMap(cp -> steps(cp).stream())
                .sorted(Comparator.comparing(ConsultaProcedimientoPaso::getFechaInicio,
                        Comparator.nullsFirst(Comparator.naturalOrder()))).toList();
        ConsultaProcedimientoPaso current = active(history);
        if (current == null) {
            throw new FlujoConsultaException("consultaFlujo.pasoSinPlantilla");
        }
        assertResponsible(current, employee, clinic, role);
        Procedimiento procedure = requireTemplate(current);
        assertCanAttend(current, employee, clinic, role);
        ProcedimientoPaso template = current.getIdProcedimientoPaso();
        List<ProcedimientoPasoExamen> links = em.createQuery("SELECT l FROM ProcedimientoPasoExamen l "
                + "JOIN FETCH l.idExamen e WHERE l.idProcedimientoPaso.idProcedimientoPaso=:step AND l.activo=TRUE AND e.activo=TRUE",
                ProcedimientoPasoExamen.class).setParameter("step", template.getIdProcedimientoPaso()).getResultList();
        List<NextStep> next = em.createQuery("SELECT s FROM ProcedimientoPasoSecuencia s WHERE "
                + "s.idProcedimientoPaso.idProcedimientoPaso=:step ORDER BY s.idProcedimientoPasoSecuencia", ProcedimientoPasoSecuencia.class)
                .setParameter("step", template.getIdProcedimientoPaso()).getResultList().stream()
                .map(s -> {
                    ProcedimientoPaso target = em.find(ProcedimientoPaso.class, s.getIdProcedimientoPasoReferencia());
                    return !validTarget(target, procedure.getIdProcedimiento()) ? null
                            : new NextStep(target.getIdProcedimientoPaso().toString(), target.getNombre(), s.getTipoSecuencia());
                }).filter(Objects::nonNull).toList();
        List<History> previous = history.stream().filter(s -> "COMPLETADO".equals(s.getEstado())
                || "RETROCEDIDO".equals(s.getEstado())).map(AcademicCareService::history).toList();
        List<PreviousStep> backward = previousSteps(current, history).stream().map(s -> new PreviousStep(
                s.getIdConsultaProcedimientoPaso().toString(), templateId(s), s.getIdProcedimientoPaso().getNombre())).toList();
        return new CareDetail(consultationId.toString(), current.getIdConsultaProcedimientoPaso().toString(), personName(c.getIdPersonaRol().getIdPersona()),
                c.getIdPersonaRol().getIdClinica().getNombre(), template.getIdRol().getNombre(),
                procedure.getNombre(), template.getIdProcedimientoPaso().toString(),
                template.getNombre(), current.getValor(), Boolean.TRUE.equals(template.getIndicaFin()), previous,
                links.stream().map(l -> new Exam(l.getIdExamen().getIdExamen().toString(), l.getIdExamen().getNombre())).toList(),
                next, !backward.isEmpty(), instant(c.getFechaInicio()), personName(current.getIdPersonaRol().getIdPersona()),
                current.getIdPersonaRol().getIdPersonaRol().toString(), current.getIdConsultaProcedimiento().getIdConsultaProcedimiento().toString(),
                current.getEstado(), history(current).ordenes(), backward);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CareResult register(UUID employee, UUID clinic, UUID role, UUID consultationId, UUID stepId,
                        String action, String value, UUID nextStep, List<UUID> examIds) {
        return register(employee, clinic, role, consultationId, stepId, action, value, nextStep, examIds, null);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CareResult register(UUID employee, UUID clinic, UUID role, UUID consultationId, UUID stepId,
                        String action, String value, UUID nextStep, List<UUID> examIds, UUID previousExecution) {
        requireAssignment(employee, clinic, role);
        Consulta consultation = em.find(Consulta.class, consultationId, LockModeType.PESSIMISTIC_WRITE);
        if (consultation == null || consultation.getIdPersonaRol() == null
                || consultation.getIdPersonaRol().getIdClinica() == null
                || !clinic.equals(consultation.getIdPersonaRol().getIdClinica().getIdClinica())) {
            throw new FlujoConsultaException("consultaFlujo.contextoInvalido");
        }
        List<ConsultaProcedimiento> associations = em.createQuery("SELECT cp FROM ConsultaProcedimiento cp "
                + "WHERE cp.idConsulta.idConsulta=:id", ConsultaProcedimiento.class).setParameter("id", consultationId).getResultList();
        List<ConsultaProcedimientoPaso> history = associations.stream().flatMap(cp -> steps(cp).stream()).toList();
        ConsultaProcedimientoPaso current = active(history);
        if (current == null || !current.getIdConsultaProcedimientoPaso().equals(stepId))
            throw new FlujoConsultaException("consultaFlujo.conflictoPaso");
        assertResponsible(current, employee, clinic, role);
        requireTemplate(current);
        assertCanAttend(current, employee, clinic, role);
        if (value == null || value.isBlank() || value.length() > 10000)
            throw new FlujoConsultaException("consultaFlujo.atencionRequerida");
        if (action == null || !Set.of("GUARDAR_PARCIAL", "AVANZAR", "RETROCEDER", "FINALIZAR").contains(action))
            throw new FlujoConsultaException("consultaFlujo.accionInvalida");
        // Resolver y validar toda la operación antes de modificar entidades administradas.
        ConsultaProcedimientoPaso previous = null;
        PersonaRol replayAssignee = null;
        if ("RETROCEDER".equals(action)) {
            List<ConsultaProcedimientoPaso> candidates = previousSteps(current, history);
            if (candidates.isEmpty()) throw new FlujoConsultaException("consultaFlujo.noHayPasoAnterior");
            if (previousExecution != null) candidates = candidates.stream()
                    .filter(s -> previousExecution.equals(s.getIdConsultaProcedimientoPaso())).toList();
            if (candidates.size() != 1) throw new FlujoConsultaException("consultaFlujo.seleccionePasoAnterior");
            previous = candidates.getFirst();
            replayAssignee = previous.getIdPersonaRol();
            if (!usableAssignee(replayAssignee, clinic, previous.getIdProcedimientoPaso().getIdRol().getIdRol()))
                throw new FlujoConsultaException("consultaFlujo.sinResponsable");
        }
        ProcedimientoPaso template = current.getIdProcedimientoPaso();
        if (template == null) throw new FlujoConsultaException("consultaFlujo.pasoSinPlantilla");
        List<ProcedimientoPasoSecuencia> outgoing = !"AVANZAR".equals(action) ? List.of() : em.createQuery("SELECT s FROM ProcedimientoPasoSecuencia s "
                + "WHERE s.idProcedimientoPaso.idProcedimientoPaso=:step", ProcedimientoPasoSecuencia.class)
                .setParameter("step", template.getIdProcedimientoPaso()).getResultList();
        ProcedimientoPaso target = null;
        PersonaRol assignee = null;
        if ("AVANZAR".equals(action)) {
            if (outgoing.isEmpty()) {
                if (nextStep != null) throw new FlujoConsultaException("consultaFlujo.seleccioneSiguientePaso");
                if (!Boolean.TRUE.equals(template.getIndicaFin())) throw new FlujoConsultaException("consultaFlujo.sinTransicion");
                action = "FINALIZAR";
            } else {
                List<ProcedimientoPasoSecuencia> candidates = outgoing.stream()
                        .filter(s -> nextStep == null || nextStep.equals(s.getIdProcedimientoPasoReferencia())).toList();
                if (candidates.size() != 1) throw new FlujoConsultaException("consultaFlujo.seleccioneSiguientePaso");
                target = em.find(ProcedimientoPaso.class, candidates.getFirst().getIdProcedimientoPasoReferencia());
                if (!validTarget(target, template.getIdProcedimiento().getIdProcedimiento()))
                    throw new FlujoConsultaException("consultaFlujo.plantillaInvalida");
                UUID targetRole = target.getIdRol().getIdRol();
                assignee = em.createQuery("SELECT pr FROM PersonaRol pr JOIN FETCH pr.idPersona "
                        + "JOIN FETCH pr.idRol JOIN FETCH pr.idClinica WHERE pr.idClinica.idClinica=:clinic "
                        + "AND pr.idRol.idRol=:role AND pr.idRol.activo=TRUE AND pr.idClinica.activo=TRUE ORDER BY pr.idPersonaRol", PersonaRol.class)
                        .setParameter("clinic", clinic).setParameter("role", targetRole).setMaxResults(1)
                        .getResultStream().findFirst().orElseThrow(() -> new FlujoConsultaException("consultaFlujo.sinResponsable"));
                if (!usableAssignee(assignee, clinic, targetRole)) throw new FlujoConsultaException("consultaFlujo.sinResponsable");
            }
        } else if ("FINALIZAR".equals(action) && !Boolean.TRUE.equals(template.getIndicaFin())) {
            throw new FlujoConsultaException("consultaFlujo.pasoNoFinal");
        }
        List<OrdenExamen> orders = prepareExams(current, examIds == null ? List.of() : examIds);
        Date now = new Date();
        current.setValor(value.strip());
        orders.forEach(em::persist);
        if ("GUARDAR_PARCIAL".equals(action)) {
            current.setEstado("EN_CURSO"); em.merge(current);
            return new CareResult(consultationId.toString(), action, current.getEstado(),
                    current.getIdConsultaProcedimientoPaso().toString(), templateId(current), false);
        }
        current.setEstado(previous == null ? "COMPLETADO" : "RETROCEDIDO");
        current.setFechaFin(now); em.merge(current);
        if (target != null || previous != null) {
            ConsultaProcedimientoPaso next = new ConsultaProcedimientoPaso(UUID.randomUUID());
            next.setIdConsultaProcedimiento(current.getIdConsultaProcedimiento());
            next.setIdProcedimientoPaso(previous == null ? target : previous.getIdProcedimientoPaso());
            next.setIdPersonaRol(previous == null ? assignee : replayAssignee);
            next.setFechaInicio(now); next.setEstado("PENDIENTE"); em.persist(next);
            return new CareResult(consultationId.toString(), action, next.getEstado(),
                    next.getIdConsultaProcedimientoPaso().toString(), templateId(next), false);
        } else {
            current.getIdConsultaProcedimiento().setFechaFin(now);
            em.merge(current.getIdConsultaProcedimiento());
            return new CareResult(consultationId.toString(), action, current.getEstado(), null, null, true);
        }
    }

    private List<OrdenExamen> prepareExams(ConsultaProcedimientoPaso step, List<UUID> ids) {
        Set<UUID> unique = new LinkedHashSet<>(ids);
        if (unique.contains(null) || unique.size() != ids.size()) throw new FlujoConsultaException("consultaFlujo.examenDuplicado");
        List<OrdenExamen> orders = new ArrayList<>();
        for (UUID id : unique) {
            boolean allowed = !em.createQuery("SELECT l FROM ProcedimientoPasoExamen l WHERE "
                    + "l.idProcedimientoPaso.idProcedimientoPaso=:step AND l.idExamen.idExamen=:exam "
                    + "AND l.activo=TRUE AND l.idExamen.activo=TRUE", ProcedimientoPasoExamen.class).setParameter("step", step.getIdProcedimientoPaso().getIdProcedimientoPaso())
                    .setParameter("exam", id).setMaxResults(1).getResultList().isEmpty();
            if (!allowed) throw new FlujoConsultaException("consultaFlujo.examenNoPermitido");
            boolean exists = !em.createQuery("SELECT o FROM OrdenExamen o WHERE o.idConsultaProcedimientoPaso.idConsultaProcedimientoPaso=:step "
                    + "AND o.idExamen.idExamen=:exam", OrdenExamen.class).setParameter("step", step.getIdConsultaProcedimientoPaso())
                    .setParameter("exam", id).setMaxResults(1).getResultList().isEmpty();
            if (!exists) {
                Examen exam = em.find(Examen.class, id);
                if (exam == null || !Boolean.TRUE.equals(exam.getActivo()))
                    throw new FlujoConsultaException("consultaFlujo.examenNoPermitido");
                OrdenExamen order = new OrdenExamen(UUID.randomUUID());
                order.setIdConsultaProcedimientoPaso(step); order.setIdExamen(exam);
                order.setFechaCreacion(new Date()); orders.add(order);
            }
        }
        return orders;
    }

    private boolean validTarget(ProcedimientoPaso target, UUID procedure) {
        return target != null && target.getIdProcedimiento() != null
                && procedure.equals(target.getIdProcedimiento().getIdProcedimiento())
                && target.getIdRol() != null && Boolean.TRUE.equals(target.getIdRol().getActivo()) && !patient(target.getIdRol());
    }

    private static boolean usableAssignee(PersonaRol assignment, UUID clinic, UUID role) {
        return assignment != null && assignment.getIdPersona() != null && assignment.getIdClinica() != null
                && clinic.equals(assignment.getIdClinica().getIdClinica()) && Boolean.TRUE.equals(assignment.getIdClinica().getActivo())
                && assignment.getIdRol() != null && role.equals(assignment.getIdRol().getIdRol())
                && Boolean.TRUE.equals(assignment.getIdRol().getActivo()) && !patient(assignment.getIdRol());
    }

    private Procedimiento requireTemplate(ConsultaProcedimientoPaso step) {
        ProcedimientoPaso template = step.getIdProcedimientoPaso();
        if (template == null) throw new FlujoConsultaException("consultaFlujo.pasoSinPlantilla");
        Procedimiento procedure = template.getIdProcedimiento();
        if (procedure == null || step.getIdConsultaProcedimiento() == null
                || !Objects.equals(procedure.getIdProcedimiento(), step.getIdConsultaProcedimiento().getIdProcedimiento()))
            throw new FlujoConsultaException("consultaFlujo.plantillaInvalida");
        if (step.getIdConsultaProcedimiento().getFechaFin() != null) throw new FlujoConsultaException("consultaFlujo.conflictoPaso");
        if (!Boolean.TRUE.equals(procedure.getActivo())) throw new FlujoConsultaException("consultaFlujo.procedimientoInactivo");
        return procedure;
    }

    private List<ConsultaProcedimientoPaso> previousSteps(ConsultaProcedimientoPaso current, List<ConsultaProcedimientoPaso> history) {
        UUID procedure = current.getIdProcedimientoPaso().getIdProcedimiento().getIdProcedimiento();
        Set<UUID> incoming = new HashSet<>(em.createQuery("SELECT s.idProcedimientoPaso.idProcedimientoPaso "
                + "FROM ProcedimientoPasoSecuencia s WHERE s.idProcedimientoPasoReferencia=:step "
                + "AND s.idProcedimientoPaso.idProcedimiento.idProcedimiento=:procedure", UUID.class)
                .setParameter("step", current.getIdProcedimientoPaso().getIdProcedimientoPaso())
                .setParameter("procedure", procedure).getResultList());
        Map<UUID, ConsultaProcedimientoPaso> previous = new LinkedHashMap<>();
        history.stream().filter(s -> "COMPLETADO".equals(s.getEstado()) && s.getIdProcedimientoPaso() != null
                && current.getIdConsultaProcedimiento().equals(s.getIdConsultaProcedimiento())
                && incoming.contains(s.getIdProcedimientoPaso().getIdProcedimientoPaso())
                && validTarget(s.getIdProcedimientoPaso(), procedure)
                && s.getFechaFin() != null && current.getFechaInicio() != null
                && !s.getFechaFin().after(current.getFechaInicio()))
                .sorted(Comparator.comparing(ConsultaProcedimientoPaso::getFechaFin)).forEach(s -> previous.put(s.getIdProcedimientoPaso().getIdProcedimientoPaso(), s));
        return new ArrayList<>(previous.values());
    }

    private List<Appointment> appointments(String predicate, UUID assignmentOrPerson, UUID clinic, UUID role,
                                          LocalDate from, LocalDate until, int first, int max, UUID... patient) {
        String jpql = "SELECT DISTINCT c FROM Consulta c JOIN FETCH c.idPersonaRol pr JOIN FETCH pr.idPersona person "
                + "JOIN FETCH pr.idClinica clinic LEFT JOIN c.consultaProcedimientoCollection cp "
                + "LEFT JOIN cp.consultaProcedimientoPasoCollection p WHERE pr.idClinica.idClinica=:clinic AND " + predicate
                + (from == null ? "" : " AND c.fechaInicio >= :from")
                + (until == null ? "" : " AND c.fechaInicio < :until") + " "
                + "ORDER BY c.fechaInicio DESC, c.idConsulta";
        Instant start = from == null ? null : from.atStartOfDay(ZONE).toInstant();
        Instant end = until == null ? null : until.plusDays(1).atStartOfDay(ZONE).toInstant();
        var query = em.createQuery(jpql, Consulta.class).setParameter("clinic", clinic)
                ;
        if (patient.length > 0) query.setParameter("patient", patient[0]);
        if (start != null) query.setParameter("from", Date.from(start));
        if (end != null) query.setParameter("until", Date.from(end));
        if (predicate.contains(":assignment")) query.setParameter("assignment", assignmentOrPerson);
        else { query.setParameter("person", assignmentOrPerson); query.setParameter("role", role); }
        List<Consulta> rows = query.setFirstResult(first).setMaxResults(max).getResultList();
        return rows.stream().map(c -> appointment(c,
                predicate.contains(":assignment") ? null : assignmentOrPerson, clinic, role)).toList();
    }

    private long countAppointments(String predicate, UUID assignmentOrPerson, UUID clinic, UUID role,
                                  LocalDate from, LocalDate until, UUID... patient) {
        String jpql = "SELECT COUNT(DISTINCT c.idConsulta) FROM Consulta c JOIN c.idPersonaRol pr "
                + "LEFT JOIN c.consultaProcedimientoCollection cp LEFT JOIN cp.consultaProcedimientoPasoCollection p "
                + "WHERE pr.idClinica.idClinica=:clinic AND " + predicate
                + (from == null ? "" : " AND c.fechaInicio >= :from")
                + (until == null ? "" : " AND c.fechaInicio < :until");
        Instant start = from == null ? null : from.atStartOfDay(ZONE).toInstant();
        Instant end = until == null ? null : until.plusDays(1).atStartOfDay(ZONE).toInstant();
        var query = em.createQuery(jpql, Long.class).setParameter("clinic", clinic)
                ;
        if (patient.length > 0) query.setParameter("patient", patient[0]);
        if (start != null) query.setParameter("from", Date.from(start));
        if (end != null) query.setParameter("until", Date.from(end));
        if (predicate.contains(":assignment")) query.setParameter("assignment", assignmentOrPerson);
        else { query.setParameter("person", assignmentOrPerson); query.setParameter("role", role); }
        return query.getSingleResult();
    }

    private Appointment appointment(Consulta c, UUID employee, UUID clinic, UUID role) {
        List<ConsultaProcedimiento> associations = em.createQuery("SELECT cp FROM ConsultaProcedimiento cp WHERE cp.idConsulta.idConsulta=:id ORDER BY cp.fechaInicio, cp.idConsultaProcedimiento", ConsultaProcedimiento.class)
                .setParameter("id", c.getIdConsulta()).getResultList();
        Map<ConsultaProcedimiento, List<ConsultaProcedimientoPaso>> executions = new LinkedHashMap<>();
        associations.forEach(cp -> executions.put(cp, steps(cp)));
        List<ConsultaProcedimientoPaso> all = executions.values().stream().flatMap(List::stream).toList();
        ConsultaProcedimientoPaso current = employee == null
                ? all.stream().filter(s -> activeState(s.getEstado())).max(Comparator.comparing(
                    ConsultaProcedimientoPaso::getFechaInicio, Comparator.nullsFirst(Comparator.naturalOrder()))).orElse(null)
                : active(all);
        if (employee != null) {
            if (current == null) throw new FlujoConsultaException("consultaFlujo.conflictoPaso");
            assertCanAttend(current, employee, clinic, role);
        }
        List<ProcedureHistory> groups = new ArrayList<>();
        for (var cp : associations) {
            Procedimiento proc = cp.getIdProcedimiento() == null ? null : em.find(Procedimiento.class, cp.getIdProcedimiento());
            List<Sequence> links = em.createQuery("SELECT s FROM ProcedimientoPasoSecuencia s "
                    + "WHERE s.idProcedimientoPaso.idProcedimiento.idProcedimiento=:procedure "
                    + "ORDER BY s.idProcedimientoPasoSecuencia", ProcedimientoPasoSecuencia.class)
                    .setParameter("procedure", cp.getIdProcedimiento()).getResultList().stream()
                    .map(s -> new Sequence(s.getIdProcedimientoPaso().getIdProcedimientoPaso().toString(),
                        s.getIdProcedimientoPasoReferencia() == null ? null : s.getIdProcedimientoPasoReferencia().toString(),
                        s.getTipoSecuencia())).toList();
            var previous = executions.get(cp).stream()
                    .filter(s -> "COMPLETADO".equals(s.getEstado()) || "RETROCEDIDO".equals(s.getEstado()))
                    .map(AcademicCareService::history).toList();
            groups.add(new ProcedureHistory(cp.getIdConsultaProcedimiento().toString(),
                    cp.getIdProcedimiento() == null ? null : cp.getIdProcedimiento().toString(),
                    proc == null ? null : proc.getNombre(), previous, links));
        }
        ConsultaProcedimiento currentProcedure = current == null ? null : current.getIdConsultaProcedimiento();
        String procedure = groups.stream().filter(g -> currentProcedure == null
                || g.idConsultaProcedimiento().equals(currentProcedure.getIdConsultaProcedimiento().toString()))
                .map(g -> Objects.toString(g.nombre(), "Procedimiento sin nombre")).findFirst().orElse("Sin procedimiento");
        List<History> histories = all.stream().map(AcademicCareService::history).toList();
        return new Appointment(c.getIdConsulta().toString(), instant(c.getFechaInicio()), instant(c.getFechaFin()),
                personName(c.getIdPersonaRol().getIdPersona()), c.getIdPersonaRol().getIdClinica().getNombre(), procedure,
                current == null || current.getIdProcedimientoPaso() == null ? null : current.getIdProcedimientoPaso().getIdProcedimientoPaso().toString(),
                current == null || current.getIdProcedimientoPaso() == null ? null : current.getIdProcedimientoPaso().getNombre(), histories,
                currentProcedure == null ? null : currentProcedure.getIdConsultaProcedimiento().toString(),
                current == null ? null : current.getIdConsultaProcedimientoPaso().toString(),
                current == null ? null : current.getEstado(), groups);
    }

    private List<ConsultaProcedimientoPaso> steps(ConsultaProcedimiento cp) {
        return em.createQuery("SELECT p FROM ConsultaProcedimientoPaso p LEFT JOIN FETCH p.idProcedimientoPaso "
                + "LEFT JOIN FETCH p.idPersonaRol pr LEFT JOIN FETCH pr.idPersona WHERE p.idConsultaProcedimiento.idConsultaProcedimiento=:id "
                + "ORDER BY p.fechaInicio, p.idConsultaProcedimientoPaso", ConsultaProcedimientoPaso.class)
                .setParameter("id", cp.getIdConsultaProcedimiento()).getResultList();
    }
    private List<PersonaRol> assignments(UUID person) {
        return em.createQuery("SELECT pr FROM PersonaRol pr JOIN FETCH pr.idPersona JOIN FETCH pr.idRol "
                + "JOIN FETCH pr.idClinica WHERE pr.idPersona.idPersona=:person AND pr.idRol.activo=TRUE "
                + "AND pr.idClinica.activo=TRUE ORDER BY pr.idClinica.nombre,pr.idRol.nombre", PersonaRol.class)
                .setParameter("person", person).getResultList();
    }
    private void requireAssignment(UUID person, UUID clinic, UUID role) {
        boolean found = !em.createQuery("SELECT pr FROM PersonaRol pr WHERE pr.idPersona.idPersona=:person "
                + "AND pr.idClinica.idClinica=:clinic AND pr.idRol.idRol=:role AND pr.idClinica.activo=TRUE AND pr.idRol.activo=TRUE", PersonaRol.class)
                .setParameter("person", person).setParameter("clinic", clinic).setParameter("role", role).setMaxResults(1).getResultList().isEmpty();
        if (!found || patient(em.find(Rol.class, role))) throw new FlujoConsultaException("consultaFlujo.asignacionInvalida");
    }
    private void assertCanAttend(ConsultaProcedimientoPaso step, UUID person, UUID clinic, UUID role) {
        assertResponsible(step, person, clinic, role);
        if (step.getIdProcedimientoPaso() == null || step.getIdProcedimientoPaso().getIdRol() == null
                || !role.equals(step.getIdProcedimientoPaso().getIdRol().getIdRol()))
            throw new FlujoConsultaException("consultaFlujo.asignacionInvalida");
    }
    private void assertResponsible(ConsultaProcedimientoPaso step, UUID person, UUID clinic, UUID role) {
        if (step.getIdPersonaRol() == null || step.getIdPersonaRol().getIdPersona() == null
                || !person.equals(step.getIdPersonaRol().getIdPersona().getIdPersona())
                || !usableAssignee(step.getIdPersonaRol(), clinic, role)
                || !activeState(step.getEstado())) throw new FlujoConsultaException("consultaFlujo.asignacionInvalida");
    }
    private static ConsultaProcedimientoPaso active(List<ConsultaProcedimientoPaso> steps) {
        List<ConsultaProcedimientoPaso> active = steps.stream().filter(s -> activeState(s.getEstado())).toList();
        if (active.size() > 1) throw new FlujoConsultaException("consultaFlujo.conflictoPaso");
        return active.stream().findFirst().orElse(null);
    }
    private static String templateId(ConsultaProcedimientoPaso step) {
        return step.getIdProcedimientoPaso() == null ? null : step.getIdProcedimientoPaso().getIdProcedimientoPaso().toString();
    }
    private static boolean activeState(String state) { return "PENDIENTE".equals(state) || "EN_CURSO".equals(state); }
    private static boolean patient(Rol role) { return role != null && "paciente".equalsIgnoreCase(role.getNombre() == null ? "" : role.getNombre().strip()); }
    private Persona requirePerson(UUID type, String number) {
        Persona p = findPerson(type, number);
        if (p == null) throw new FlujoConsultaException("identidad.noEncontrada");
        return p;
    }
    private Persona findPerson(UUID type, String number) {
        if (type == null || number == null || number.isBlank() || number.length() > 100) return null;
        List<Persona> matches = em.createQuery("SELECT DISTINCT d.idPersona FROM Documento d WHERE "
                + "d.idTipoDocumento.idTipoDocumento=:type AND d.valor=:number", Persona.class)
                .setParameter("type", type).setParameter("number", number.strip()).setMaxResults(2).getResultList();
        return matches.size() == 1 ? matches.getFirst() : null;
    }
    private static Identity identity(Persona p, List<PersonaRol> assignments) {
        Map<UUID, Clinic> clinics = new LinkedHashMap<>();
        List<Role> roles = new ArrayList<>();
        for (PersonaRol pr : assignments) {
            Clinic clinic = new Clinic(pr.getIdClinica().getIdClinica().toString(), pr.getIdClinica().getNombre());
            clinics.putIfAbsent(pr.getIdClinica().getIdClinica(), clinic);
            roles.add(new Role(pr.getIdPersonaRol().toString(), pr.getIdRol().getIdRol().toString(), pr.getIdRol().getNombre(),
                    pr.getIdClinica().getIdClinica().toString(), pr.getIdClinica().getNombre()));
        }
        return new Identity(p.getIdPersona().toString(), p.getNombres(), p.getApellidos(), List.copyOf(clinics.values()), List.copyOf(roles));
    }
    private static History history(ConsultaProcedimientoPaso s) {
        String stepId = s.getIdProcedimientoPaso() == null ? null : s.getIdProcedimientoPaso().getIdProcedimientoPaso().toString();
        String name = s.getIdProcedimientoPaso() == null ? "Paso histórico sin plantilla" : s.getIdProcedimientoPaso().getNombre();
        return new History(stepId, s.getIdConsultaProcedimientoPaso().toString(), name, instant(s.getFechaInicio()), instant(s.getFechaFin()),
                s.getIdPersonaRol() == null || s.getIdPersonaRol().getIdPersona() == null ? null : personName(s.getIdPersonaRol().getIdPersona()), s.getValor(), s.getEstado(),
                s.getIdPersonaRol() == null || s.getIdPersonaRol().getIdRol() == null ? null : s.getIdPersonaRol().getIdRol().getNombre(),
                s.getOrdenExamenCollection() == null ? List.of() : s.getOrdenExamenCollection().stream().map(o ->
                    new ExamOrder(o.getIdOrdenExamen().toString(), o.getIdExamen() == null ? null : o.getIdExamen().getNombre(),
                        instant(o.getFechaCreacion()), o.getIndicaciones(),
                        o.getExamenResultadoCollection() == null ? List.of() : o.getExamenResultadoCollection().stream().map(r ->
                            new ExamResult(r.getIdExamenResultado().toString(), instant(r.getFechaCreacion()), r.getResultado(), r.getInterpretacion())).toList())).toList());
    }
    private static String personName(Persona p) {
        if (p == null) return "Persona";
        return String.join(" ", Arrays.asList(p.getNombres(), p.getApellidos()).stream().filter(v -> v != null && !v.isBlank()).toList());
    }
    private static String instant(Date date) { return date == null ? null : date.toInstant().toString(); }
}
