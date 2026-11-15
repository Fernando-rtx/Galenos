package sv.edu.ues.occingenieriappi115_2026.salud.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;

/** Reglas HU3/HU4 con JPA simulado: no prueba SQL, bloqueos ni rollback real. */
class AcademicCareServiceTest {
  final EntityManager em = mock(EntityManager.class);
  final AcademicCareService service = new AcademicCareService();
  final UUID personId = id(1), clinicId = id(2), roleId = id(3);
  final PersonaRol assignment = new PersonaRol(id(4));
  final Consulta consultation = new Consulta(id(5));
  final ConsultaProcedimiento procedure = new ConsultaProcedimiento(id(6));
  final ProcedimientoPaso template = new ProcedimientoPaso(id(7));
  final ConsultaProcedimientoPaso step = new ConsultaProcedimientoPaso(id(8));
  TypedQuery<PersonaRol> assignments;
  TypedQuery<ConsultaProcedimientoPaso> history;
  TypedQuery<ProcedimientoPasoSecuencia> outgoing;
  static UUID id(long value) { return new UUID(0, value); }

  // Cada consulta tipada tiene su doble; RETURNS_SELF modela solo la API fluida JPA.
  <T> TypedQuery<T> query(Class<T> type, List<T> rows) {
    @SuppressWarnings("unchecked") TypedQuery<T> query = mock(TypedQuery.class, RETURNS_SELF);
    when(em.createQuery(anyString(), eq(type))).thenReturn(query);
    when(query.getResultList()).thenReturn(rows);
    when(query.getResultStream()).thenAnswer(call -> rows.stream());
    return query;
  }

  @BeforeEach void setup() throws Exception {
    var field = AcademicCareService.class.getDeclaredField("em");
    field.setAccessible(true);
    field.set(service, em);
    var role = new Rol(roleId); role.setNombre("Rol clínico sintético"); role.setActivo(true);
    var clinic = new Clinica(clinicId); clinic.setActivo(true);
    assignment.setIdPersona(new Persona(personId));
    assignment.setIdClinica(clinic); assignment.setIdRol(role);
    consultation.setIdPersonaRol(assignment);
    procedure.setIdConsulta(consultation);
    template.setIdRol(role); template.setIndicaFin(false);
    template.setIdProcedimiento(new Procedimiento(id(70)));
    step.setIdConsultaProcedimiento(procedure); step.setIdPersonaRol(assignment);
    step.setIdProcedimientoPaso(template); step.setEstado("PENDIENTE");
    step.setFechaInicio(Date.from(Instant.parse("2026-01-01T12:00:00Z")));
    assignments = query(PersonaRol.class, List.of(assignment));
    query(ConsultaProcedimiento.class, List.of(procedure));
    history = query(ConsultaProcedimientoPaso.class, List.of(step));
    outgoing = query(ProcedimientoPasoSecuencia.class, List.of());
    when(em.find(Rol.class, roleId)).thenReturn(role);
    when(em.find(Consulta.class, consultation.getIdConsulta(), LockModeType.PESSIMISTIC_WRITE))
        .thenReturn(consultation);
  }

  AcademicCareService.CareResult register(String action, UUID next, List<UUID> exams) {
    return service.register(personId, clinicId, roleId, consultation.getIdConsulta(),
        step.getIdConsultaProcedimientoPaso(), action, " Nota sintética ", next, exams);
  }
  void rejects(String code, Runnable action) {
    assertEquals(code, assertThrows(FlujoConsultaException.class, action::run).getMessage());
    verify(em, never()).persist(any());
    verify(em, never()).merge(any());
  }

  @Test void asignacionAusenteNoLeeNiModificaConsulta() {
    when(assignments.getResultList()).thenReturn(List.of());
    rejects("consultaFlujo.asignacionInvalida", () -> register("GUARDAR_PARCIAL", null, List.of()));
    verify(em, never()).find(eq(Consulta.class), any(), any(LockModeType.class));
  }
  @Test void pacienteNoPuedeRegistrarAtencion() {
    assignment.getIdRol().setNombre("Paciente");
    rejects("consultaFlujo.asignacionInvalida", () -> register("GUARDAR_PARCIAL", null, List.of()));
  }
  @Test void consultaDeOtraClinicaNoSeModifica() {
    var other = new PersonaRol(id(90)); other.setIdClinica(new Clinica(id(91)));
    consultation.setIdPersonaRol(other);
    rejects("consultaFlujo.contextoInvalido", () -> register("GUARDAR_PARCIAL", null, List.of()));
  }
  @Test void pasoDeOtroEmpleadoNoSeModifica() {
    var other = new PersonaRol(id(90)); other.setIdPersona(new Persona(id(91)));
    step.setIdPersonaRol(other);
    rejects("consultaFlujo.asignacionInvalida", () -> register("GUARDAR_PARCIAL", null, List.of()));
  }
  @Test void dosPasosActivosProducenConflicto() {
    var other = new ConsultaProcedimientoPaso(id(90)); other.setEstado("EN_CURSO");
    when(history.getResultList()).thenReturn(List.of(step, other));
    rejects("consultaFlujo.conflictoPaso", () -> register("GUARDAR_PARCIAL", null, List.of()));
  }
  @Test void pasoYaCompletadoRechazaReenvio() {
    step.setEstado("COMPLETADO");
    rejects("consultaFlujo.conflictoPaso", () -> register("GUARDAR_PARCIAL", null, List.of()));
  }
  @ParameterizedTest @ValueSource(strings = {"", "  "})
  void notaVaciaNoSeGuarda(String value) {
    rejects("consultaFlujo.atencionRequerida", () -> service.register(personId, clinicId, roleId,
        consultation.getIdConsulta(), step.getIdConsultaProcedimientoPaso(), "GUARDAR_PARCIAL", value, null, List.of()));
  }
  @Test void parcialConservaPasoYNoCierraProcedimiento() {
    var result = register("GUARDAR_PARCIAL", null, List.of());
    assertEquals("EN_CURSO", result.estado()); assertFalse(result.finalizada());
    assertEquals(step.getIdConsultaProcedimientoPaso().toString(), result.idConsultaProcedimientoPaso());
    assertEquals("Nota sintética", step.getValor()); assertNull(step.getFechaFin());
    assertNull(procedure.getFechaFin()); verify(em).merge(step); verify(em, never()).persist(any());
    verify(assignments).setParameter("person", personId);
    verify(assignments).setParameter("clinic", clinicId);
    verify(assignments).setParameter("role", roleId);
  }
  @Test void finalizarRequierePasoFinal() {
    rejects("consultaFlujo.pasoNoFinal", () -> register("FINALIZAR", null, List.of()));
  }
  @Test void finalizaPasoYProcedimientoConLaMismaFecha() {
    template.setIndicaFin(true);
    var result = register("FINALIZAR", null, List.of());
    assertTrue(result.finalizada()); assertNull(result.idPasoActual());
    assertEquals("COMPLETADO", step.getEstado()); assertNotNull(step.getFechaFin());
    assertEquals(step.getFechaFin(), procedure.getFechaFin());
    verify(em).merge(step); verify(em).merge(procedure); verify(em, never()).persist(any());
  }
  @Test void avanzarCreaPasoPendienteConResponsable() {
    var next = new ProcedimientoPaso(id(20)); next.setIdRol(assignment.getIdRol()); next.setIdProcedimiento(template.getIdProcedimiento());
    var link = new ProcedimientoPasoSecuencia(id(21)); link.setIdProcedimientoPasoReferencia(id(20));
    when(outgoing.getResultList()).thenReturn(List.of(link));
    when(em.find(ProcedimientoPaso.class, id(20))).thenReturn(next);
    var result = register("AVANZAR", id(20), List.of());
    var captured = org.mockito.ArgumentCaptor.forClass(ConsultaProcedimientoPaso.class);
    verify(em).persist(captured.capture());
    var created = captured.getValue();
    assertSame(next, created.getIdProcedimientoPaso()); assertSame(assignment, created.getIdPersonaRol());
    assertSame(procedure, created.getIdConsultaProcedimiento()); assertEquals("PENDIENTE", created.getEstado());
    assertEquals(created.getIdConsultaProcedimientoPaso().toString(), result.idConsultaProcedimientoPaso());
    assertEquals("COMPLETADO", step.getEstado()); assertFalse(result.finalizada());
  }
  @Test void retrocederSinHistoriaNoCreaPaso() {
    rejects("consultaFlujo.noHayPasoAnterior", () -> register("RETROCEDER", null, List.of()));
  }
  @Test void retrocederReabrePlantillaAnteriorSinBorrarHistoria() {
    var previous = new ConsultaProcedimientoPaso(id(20));
    previous.setIdConsultaProcedimiento(procedure); previous.setIdProcedimientoPaso(template);
    previous.setEstado("COMPLETADO"); previous.setFechaFin(step.getFechaInicio());
    when(history.getResultList()).thenReturn(List.of(previous, step));
    var result = register("RETROCEDER", null, List.of());
    var captured = org.mockito.ArgumentCaptor.forClass(ConsultaProcedimientoPaso.class);
    verify(em).persist(captured.capture());
    assertEquals("PENDIENTE", captured.getValue().getEstado());
    assertSame(template, captured.getValue().getIdProcedimientoPaso());
    assertEquals("RETROCEDIDO", step.getEstado()); assertEquals("COMPLETADO", previous.getEstado());
    assertFalse(result.finalizada()); verify(em, never()).remove(any());
  }
  @Test void examenesDuplicadosNoCreanOrdenes() {
    rejects("consultaFlujo.examenDuplicado", () -> register("GUARDAR_PARCIAL", null, List.of(id(30), id(30))));
  }
  @Test void examenNoPermitidoNoCreaOrden() {
    query(ProcedimientoPasoExamen.class, List.of());
    rejects("consultaFlujo.examenNoPermitido", () -> register("GUARDAR_PARCIAL", null, List.of(id(30))));
  }
  @Test void examenPermitidoCreaOrdenVinculadaAlPaso() {
    query(ProcedimientoPasoExamen.class, List.of(new ProcedimientoPasoExamen(id(31))));
    query(OrdenExamen.class, List.of());
    var exam = new Examen(id(30)); when(em.find(Examen.class, id(30))).thenReturn(exam);
    register("GUARDAR_PARCIAL", null, List.of(id(30)));
    var captured = org.mockito.ArgumentCaptor.forClass(OrdenExamen.class);
    verify(em).persist(captured.capture());
    assertSame(exam, captured.getValue().getIdExamen());
    assertSame(step, captured.getValue().getIdConsultaProcedimientoPaso());
  }
  @Test void ordenExistenteNoSeDuplica() {
    query(ProcedimientoPasoExamen.class, List.of(new ProcedimientoPasoExamen(id(31))));
    query(OrdenExamen.class, List.of(new OrdenExamen(id(32))));
    register("GUARDAR_PARCIAL", null, List.of(id(30)));
    verify(em, never()).persist(any()); verify(em).merge(step);
  }

  @Test void busquedaPorDocumentoMantienePersonaRolYClinicaEnJpa() {
    var type = new TipoDocumento(id(90)); type.setActivo(true); type.setExpresionRegular("[A-Z]{3}");
    when(em.find(TipoDocumento.class, id(90))).thenReturn(type);
    query(Persona.class, List.of(new Persona(id(91))));
    var appointments = query(Consulta.class, List.of());
    assertTrue(service.searchEmployeeAppointments(personId, clinicId, roleId,
        null, null, 0, 20, id(90), "ABC").isEmpty());
    verify(appointments).setParameter("patient", id(91));
    verify(appointments).setParameter("person", personId);
    verify(appointments).setParameter("clinic", clinicId);
    verify(appointments).setParameter("role", roleId);
    verify(appointments).setMaxResults(20);
    verify(em).createQuery(contains("pr.idPersona.idPersona=:patient"), eq(Consulta.class));
  }
  @Test void documentoInvalidoNoConsultaPacientes() {
    var type = new TipoDocumento(id(90)); type.setActivo(true); type.setExpresionRegular("[A-Z]{3}");
    when(em.find(TipoDocumento.class, id(90))).thenReturn(type);
    assertEquals(422, assertThrows(IdentificacionException.class, () -> service.searchEmployeeAppointments(
        personId, clinicId, roleId, null, null, 0, 20, id(90), "123")).getStatus());
    verify(em, never()).createQuery(anyString(), eq(Persona.class));
  }
  @Test void siguientePasoDeOtroProcedimientoSeRechaza() {
    var next = new ProcedimientoPaso(id(20)); next.setIdRol(assignment.getIdRol());
    next.setIdProcedimiento(new Procedimiento(id(99)));
    var link = new ProcedimientoPasoSecuencia(id(21)); link.setIdProcedimientoPasoReferencia(id(20));
    when(outgoing.getResultList()).thenReturn(List.of(link));
    when(em.find(ProcedimientoPaso.class, id(20))).thenReturn(next);
    rejects("consultaFlujo.plantillaInvalida", () -> register("AVANZAR", id(20), List.of()));
  }

  @Test void bandejaProyectaProcedimientoActualYJerarquiaSinPasosFuturos() {
    procedure.setIdProcedimiento(id(70));
    template.getIdProcedimiento().setNombre("Procedimiento sintético");
    template.setNombre("Paso actual");
    when(em.find(Procedimiento.class, id(70))).thenReturn(template.getIdProcedimiento());
    var previousTemplate = new ProcedimientoPaso(id(71)); previousTemplate.setNombre("Paso anterior");
    previousTemplate.setIdProcedimiento(template.getIdProcedimiento());
    var previous = new ConsultaProcedimientoPaso(id(72)); previous.setIdProcedimientoPaso(previousTemplate);
    previous.setIdConsultaProcedimiento(procedure); previous.setIdPersonaRol(assignment);
    previous.setEstado("COMPLETADO"); previous.setValor("Registro persistido sintético");
    previous.setFechaFin(Date.from(Instant.parse("2026-01-01T11:00:00Z")));
    when(history.getResultList()).thenReturn(List.of(previous, step));
    var link = new ProcedimientoPasoSecuencia(id(73)); link.setIdProcedimientoPaso(previousTemplate);
    link.setIdProcedimientoPasoReferencia(template.getIdProcedimientoPaso()); link.setTipoSecuencia("CONTINUAR");
    when(outgoing.getResultList()).thenReturn(List.of(link));
    var rowsQuery = query(Consulta.class, List.of(consultation));
    var result = service.employeeAppointments(personId, clinicId, roleId,
        java.time.LocalDate.parse("2026-01-01"), java.time.LocalDate.parse("2026-01-01"), 0, 20).getFirst();
    assertEquals("Procedimiento sintético", result.procedimiento());
    assertEquals("PENDIENTE", result.estadoPasoActual());
    assertEquals(procedure.getIdConsultaProcedimiento().toString(), result.idConsultaProcedimientoActual());
    assertEquals(step.getIdConsultaProcedimientoPaso().toString(), result.idConsultaProcedimientoPasoActual());
    var group = result.procedimientos().getFirst();
    assertEquals(1, group.pasos().size());
    assertEquals("Registro persistido sintético", group.pasos().getFirst().valor());
    assertEquals(previousTemplate.getIdProcedimientoPaso().toString(), group.secuencias().getFirst().idPasoOrigen());
    verify(rowsQuery).setParameter("from", Date.from(Instant.parse("2026-01-01T06:00:00Z")));
    verify(rowsQuery).setParameter("until", Date.from(Instant.parse("2026-01-02T06:00:00Z")));
    verify(em).createQuery(contains("p.idPersonaRol.idClinica.idClinica = :clinic"), eq(Consulta.class));
    verify(em, never()).merge(any()); verify(em, never()).persist(any());
  }

  @Test void consultaDevueltaPorJpaNoExponeHistorialSiElPasoPerteneceAOtraClinica() {
    assignment.setIdClinica(new Clinica(id(99)));
    query(Consulta.class, List.of(consultation));
    var error = assertThrows(FlujoConsultaException.class, () -> service.employeeAppointments(
        personId, clinicId, roleId, null, null, 0, 20));
    assertEquals("consultaFlujo.asignacionInvalida", error.getMessage());
  }

  @Test void mismaPersonaYClinicaConOtroRolEnElPasoNoAutorizaAtencion() {
    var other = new PersonaRol(id(90)); other.setIdPersona(assignment.getIdPersona());
    other.setIdClinica(assignment.getIdClinica()); other.setIdRol(new Rol(id(91)));
    step.setIdPersonaRol(other);
    rejects("consultaFlujo.asignacionInvalida", () -> register("GUARDAR_PARCIAL", null, List.of()));
  }

  @Test void variosProcedimientosNoMezclanElHistorialNiElNombreDelActual() {
    procedure.setIdProcedimiento(id(70)); template.getIdProcedimiento().setNombre("Actual");
    when(em.find(Procedimiento.class, id(70))).thenReturn(template.getIdProcedimiento());
    var other = new ConsultaProcedimiento(id(80)); other.setIdConsulta(consultation); other.setIdProcedimiento(id(81));
    var otherTemplate = new Procedimiento(id(81)); otherTemplate.setNombre("Anterior");
    when(em.find(Procedimiento.class, id(81))).thenReturn(otherTemplate);
    query(ConsultaProcedimiento.class, List.of(other, procedure));
    @SuppressWarnings("unchecked") TypedQuery<ConsultaProcedimientoPaso> empty = mock(TypedQuery.class, RETURNS_SELF);
    when(empty.getResultList()).thenReturn(List.of());
    when(history.setParameter("id", other.getIdConsultaProcedimiento())).thenReturn(empty);
    query(Consulta.class, List.of(consultation));
    var result = service.employeeAppointments(personId, clinicId, roleId, null, null, 0, 20).getFirst();
    assertEquals("Actual", result.procedimiento());
    assertEquals(procedure.getIdConsultaProcedimiento().toString(), result.idConsultaProcedimientoActual());
    assertEquals(2, result.procedimientos().size());
    assertTrue(result.procedimientos().getFirst().pasos().isEmpty());
    assertEquals(other.getIdConsultaProcedimiento().toString(), result.procedimientos().getFirst().idConsultaProcedimiento());
  }
}
