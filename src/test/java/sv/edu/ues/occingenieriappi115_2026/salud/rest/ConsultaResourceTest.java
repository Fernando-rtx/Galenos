package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaResourceTest {
    private ConsultaResource resource;
    private ConsultaDAO dao;
    private ClinicaDAO clinics;
    private UUID clinicId;

    @BeforeEach
    void setup() {
        resource = new ConsultaResource();
        dao = mock(ConsultaDAO.class);
        clinics = mock(ClinicaDAO.class);
        resource.dao = dao;
        resource.clinicaDao = clinics;
        resource.response = mock(HttpServletResponse.class);
        resource.settings = new ApiSettings(Map.of("GALENOS_DEMO_MODE", "true",
                "POSTGRESQL_DB_NAME", "galenos_demo_unit"));
        clinicId = UUID.randomUUID();
        var clinic = new Clinica(clinicId, "Clínica sintética");
        clinic.setActivo(true);
        when(clinics.buscarPorId(clinicId)).thenReturn(clinic);
    }

    @Test
    void modoNormalNoConsultaPersistencia() {
        resource.settings = new ApiSettings(Map.of());
        assertEquals(404, assertThrows(ApiException.class,
                () -> resource.findRange(null, null, null, null, null, null)).getStatus());
        assertEquals(404, assertThrows(ApiException.class,
                () -> resource.findById(UUID.randomUUID().toString(), clinicId.toString())).getStatus());
        verifyNoInteractions(dao, clinics, resource.response);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void paginaYCuentaConLosMismosFiltrosDeClinicaFechasYPersona() {
        UUID assignment = UUID.randomUUID();
        when(dao.contar(anyList())).thenReturn(3L);
        assertTrue(resource.findRange("2", "7", clinicId.toString(), "2026-10-22", "2026-10-22",
                assignment.toString()).isEmpty());
        ArgumentCaptor<List<FiltroDAO>> filters = ArgumentCaptor.forClass((Class) List.class);
        verify(dao).obtenerPagina(eq(2), eq(7), filters.capture(),
                eq(List.of(new OrdenDAO("idConsulta", DireccionOrden.ASCENDENTE))));
        assertEquals(List.of(
                new FiltroDAO("idPersonaRol.idClinica.idClinica", OperadorFiltro.IGUAL, clinicId),
                new FiltroDAO("fechaInicio", OperadorFiltro.MAYOR_O_IGUAL, Date.from(Instant.parse("2026-10-22T06:00:00Z"))),
                new FiltroDAO("fechaInicio", OperadorFiltro.MENOR_QUE, Date.from(Instant.parse("2026-10-23T06:00:00Z"))),
                new FiltroDAO("idPersonaRol.idPersonaRol", OperadorFiltro.IGUAL, assignment)), filters.getValue());
        verify(dao).contar(filters.getValue());
        verify(resource.response).setHeader("Total-registros", "3");
        verify(dao, never()).contar();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void detalleFiltraPorUuidYClinicaEnLaMismaConsulta() {
        UUID id = UUID.randomUUID();
        when(dao.obtenerPagina(eq(0), eq(1), anyList(), eq(List.of())))
                .thenReturn(List.of(new Consulta(id)));
        assertEquals(id.toString(), resource.findById(id.toString(), clinicId.toString()).getIdConsulta());
        ArgumentCaptor<List<FiltroDAO>> filters = ArgumentCaptor.forClass((Class) List.class);
        verify(dao).obtenerPagina(eq(0), eq(1), filters.capture(), eq(List.of()));
        assertEquals(List.of(
                new FiltroDAO("idPersonaRol.idClinica.idClinica", OperadorFiltro.IGUAL, clinicId),
                new FiltroDAO("idConsulta", OperadorFiltro.IGUAL, id)), filters.getValue());
        verify(dao, never()).buscarPorId(any());
    }

    @Test
    void detalleFueraDelConjuntoEs404() {
        assertEquals(404, assertThrows(ApiException.class,
                () -> resource.findById(UUID.randomUUID().toString(), clinicId.toString())).getStatus());
    }

    @ParameterizedTest
    @CsvSource({"2026-02-30,2026-03-01", "2026-10-23,2026-10-22", "2026-1-1,2026-10-22",
            "0000-01-01,2026-10-22", "2026-10-22T00:00:00Z,2026-10-23"})
    void fechasInvalidasSon422(String from, String until) {
        assertEquals(422, assertThrows(ApiException.class,
                () -> resource.findRange(null, null, clinicId.toString(), from, until, null)).getStatus());
        verifyNoInteractions(dao);
    }

    @Test
    void clinicaObligatoriaYPersonaDebeSerUuid() {
        assertEquals(422, assertThrows(ApiException.class,
                () -> resource.findRange(null, null, null, null, null, null)).getStatus());
        assertEquals(422, assertThrows(ApiException.class,
                () -> resource.findRange(null, null, clinicId.toString(), null, null, "abc")).getStatus());
        verifyNoInteractions(dao);
    }

    @Test
    void clinicaAusenteOInactivaNoExponeConsultas() {
        UUID missing = UUID.randomUUID();
        assertEquals(404, assertThrows(ApiException.class,
                () -> resource.findRange(null, null, missing.toString(), null, null, null)).getStatus());
        var inactive = new Clinica(clinicId, "Clínica sintética inactiva");
        inactive.setActivo(false);
        when(clinics.buscarPorId(clinicId)).thenReturn(inactive);
        assertEquals(404, assertThrows(ApiException.class,
                () -> resource.findById(UUID.randomUUID().toString(), clinicId.toString())).getStatus());
        verifyNoInteractions(dao);
    }

    @Test
    void dtoProyectaDatosSinLeerContenidoClinicoNiColecciones() {
        var source = mock(Consulta.class);
        var assignment = new PersonaRol(UUID.randomUUID());
        var person = new Persona(UUID.randomUUID());
        person.setNombres("  Persona ficticia  ");
        person.setApellidos("  Uno ");
        assignment.setIdPersona(person);
        assignment.setIdClinica(new Clinica(clinicId, "Clínica sintética"));
        when(source.getIdConsulta()).thenReturn(UUID.randomUUID());
        when(source.getIdPersonaRol()).thenReturn(assignment);
        when(source.getFechaInicio()).thenReturn(Date.from(Instant.parse("2026-10-22T06:00:00Z")));
        var dto = new ConsultaDto(source);
        assertEquals("Persona ficticia Uno", dto.getPersonaNombre());
        assertEquals("2026-10-22T06:00:00Z", dto.getFechaInicio());
        assertNull(dto.getFechaFin());
        assertNull(dto.getRolNombre());
        assertEquals(clinicId.toString(), dto.getIdClinica());
        verify(source, never()).getObservaciones();
        verify(source, never()).getReferenciaExterna();
        verify(source, never()).getConsultaProcedimientoCollection();
    }

    @Test
    void dtoToleraRelacionesYFechasAusentesSinInventarNombres() {
        var dto = new ConsultaDto(new Consulta(UUID.randomUUID()));
        assertNull(dto.getFechaInicio());
        assertNull(dto.getFechaFin());
        assertNull(dto.getPersonaNombre());
        assertNull(dto.getIdPersonaRol());
        assertNull(dto.getIdClinica());
        assertNull(dto.getClinicaNombre());
    }
}
