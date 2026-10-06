package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.boundary.ClinicaActualBean;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static sv.edu.ues.occingenieriappi115_2026.salud.model.ContextoConsultaFixture.*;

class ConsultaContextoTest {
    @Test void sinContextoNoListaNiGuardaNiResuelveFila() {
        var dao = mock(ConsultaDAO.class);
        var model = new ConsultaModel(dao, mock(PersonaRolDAO.class));
        model.nuevo(); model.guardar();
        assertEquals(0, model.count(Map.of()));
        assertTrue(model.load(0,10,Map.of(),Map.of()).isEmpty());
        assertNull(model.getRowData(UUID.randomUUID().toString()));
        verifyNoInteractions(dao);
    }
    @Test void cambioDeClinicaInvalidaFormularioInclusoSiSeRegresaALaOriginal() {
        var contexto = mock(ClinicaActualBean.class);
        when(contexto.getIdClinicaActual()).thenReturn(CLINICA); when(contexto.isSeleccionada()).thenReturn(true);
        var service = mock(ConsultaFlujoService.class);
        var model = new ConsultaModel(mock(ConsultaDAO.class),mock(PersonaRolDAO.class),null,contexto,service);
        model.nuevo();
        when(contexto.getRevision()).thenReturn(2L);
        model.guardar();
        verifyNoInteractions(service);
        assertEquals(ESTADO_CRUD.CREACION,model.getEstado());
    }
    @Test void conteoYPaginaCompartenFiltroDeClinica() {
        var dao = mock(ConsultaDAO.class); var model = new ConsultaModel(dao,mock(PersonaRolDAO.class));
        configurar(model);
        var filtros = List.of(new FiltroDAO("idPersonaRol.idClinica.idClinica",OperadorFiltro.IGUAL,CLINICA));
        when(dao.contar(filtros)).thenReturn(12L);
        assertEquals(12,model.count(Map.of()));
        model.load(10,10,Map.of(),Map.of());
        verify(dao).obtenerPagina(10,10,filtros,List.of());
        assertEquals(12,model.getRowCount());
    }
    @Test void errorDeNegocioConservaFormularioParaCorregir() {
        var model = new ConsultaModel(mock(ConsultaDAO.class),mock(PersonaRolDAO.class)); configurar(model);
        model.nuevo();
        when(servicio(model).guardarConsulta(any(),any(),anyBoolean()))
                .thenThrow(new FlujoConsultaException("consultaFlujo.pacienteInvalido"));
        model.guardar();
        assertEquals(ESTADO_CRUD.CREACION,model.getEstado()); assertNotNull(model.getSeleccionado());
    }
}
