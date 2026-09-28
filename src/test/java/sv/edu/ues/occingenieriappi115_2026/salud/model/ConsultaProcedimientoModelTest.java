package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import java.util.Date;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConsultaProcedimientoModelTest {

    @Test
    void nuevoCreaConsultaProcedimientoYCambiaEstadoACreacion() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class),
                mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class),
                mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        ConsultaProcedimiento entidad = new ConsultaProcedimiento();

        model.seleccionar(entidad);

        assertSame(entidad, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        model.nuevo();
        ConsultaProcedimiento seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        ConsultaProcedimiento entidad = new ConsultaProcedimiento();
        ConsultaProcedimiento actualizada = new ConsultaProcedimiento();
        model.seleccionar(entidad);
        when(dao.actualizar(entidad)).thenReturn(actualizada);

        model.guardar();

        verify(dao).actualizar(entidad);
        assertSame(actualizada, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class),
                mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        model.seleccionar(new ConsultaProcedimiento());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaConsultas() {
        ConsultaDAO consultaDAO = mock(ConsultaDAO.class);
        when(consultaDAO.obtenerTodos()).thenReturn(List.of(new Consulta()));

        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), consultaDAO,
                mock(ProcedimientoDAO.class));

        assertEquals(1, model.getConsultas().size());
    }

    @Test
    void constructorCargaProcedimientosDelCatalogo() {
        ProcedimientoDAO procedimientoDAO = mock(ProcedimientoDAO.class);
        when(procedimientoDAO.obtenerTodos()).thenReturn(List.of(new Procedimiento()));

        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), procedimientoDAO);

        assertEquals(1, model.getProcedimientos().size());
    }

    @Test
    void getProcedimientosPorConsultaSinIdDevuelveListaVaciaYNoConsultaDao() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));

        List<ConsultaProcedimiento> resultado = model.getProcedimientosPorConsulta(new Consulta());

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(dao);
    }

    @Test
    void getProcedimientosPorConsultaFiltraPorIdDeConsulta() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        UUID idConsulta = UUID.randomUUID();
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(idConsulta);
        ConsultaProcedimiento procedimiento = new ConsultaProcedimiento();
        when(dao.obtenerPagina(
                0,
                Integer.MAX_VALUE,
                List.of(new FiltroDAO("idConsulta.idConsulta", OperadorFiltro.IGUAL, idConsulta)),
                List.of())).thenReturn(List.of(procedimiento));

        List<ConsultaProcedimiento> resultado = model.getProcedimientosPorConsulta(consulta);

        assertEquals(List.of(procedimiento), resultado);
    }

    @Test
    void nuevoParaConsultaAsignaConsultaYCambiaEstadoACreacion() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());

        model.nuevoParaConsulta(consulta);

        assertNotNull(model.getSeleccionado());
        assertSame(consulta, model.getSeleccionado().getIdConsulta());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void nuevoParaConsultaSinIdLimpiaSeleccionado() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        model.seleccionar(new ConsultaProcedimiento());

        model.nuevoParaConsulta(new Consulta());

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void isSeleccionadoParaConsultaEsFalsoCuandoProcedimientoEsDeOtraConsulta() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        Consulta otraConsulta = new Consulta();
        otraConsulta.setIdConsulta(UUID.randomUUID());
        ConsultaProcedimiento ajeno = new ConsultaProcedimiento();
        ajeno.setIdConsulta(otraConsulta);
        model.seleccionar(ajeno);

        assertFalse(model.isSeleccionadoParaConsulta(consulta));
    }

    @Test
    void isSeleccionadoParaConsultaEsVerdaderoCuandoProcedimientoPertenece() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        ConsultaProcedimiento propio = new ConsultaProcedimiento();
        propio.setIdConsulta(consulta);
        model.seleccionar(propio);

        assertTrue(model.isSeleccionadoParaConsulta(consulta));
    }

    @Test
    void seleccionarParaConsultaIgnoraProcedimientoDeOtraConsulta() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());

        model.seleccionarParaConsulta(new ConsultaProcedimiento(), consulta);

        assertNull(model.getSeleccionado());
    }

    @Test
    void seleccionarParaConsultaSeleccionaProcedimientoPropio() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        ConsultaProcedimiento propio = new ConsultaProcedimiento();
        propio.setIdConsulta(consulta);

        model.seleccionarParaConsulta(propio, consulta);

        assertSame(propio, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void getNombreProcedimientoDevuelveElNombreDelCatalogo() {
        ProcedimientoDAO procedimientoDAO = mock(ProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), procedimientoDAO);
        UUID idProcedimiento = UUID.randomUUID();
        ConsultaProcedimiento consultaProcedimiento = new ConsultaProcedimiento();
        consultaProcedimiento.setIdProcedimiento(idProcedimiento);
        Procedimiento procedimiento = new Procedimiento(idProcedimiento);
        procedimiento.setNombre("Endoscopia");
        when(procedimientoDAO.buscarPorId(idProcedimiento)).thenReturn(procedimiento);

        assertEquals("Endoscopia", model.getNombreProcedimiento(consultaProcedimiento));
    }

    @Test
    void getNombreProcedimientoDevuelveVacioSinProcedimientoOSinId() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));

        assertEquals("", model.getNombreProcedimiento(null));
        assertEquals("", model.getNombreProcedimiento(new ConsultaProcedimiento()));
    }

    @Test
    void getNombreProcedimientoUsaCacheYNoRepiteLaConsultaAlDao() {
        ProcedimientoDAO procedimientoDAO = mock(ProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), procedimientoDAO);
        UUID idProcedimiento = UUID.randomUUID();
        ConsultaProcedimiento consultaProcedimiento = new ConsultaProcedimiento();
        consultaProcedimiento.setIdProcedimiento(idProcedimiento);
        when(procedimientoDAO.buscarPorId(idProcedimiento)).thenReturn(new Procedimiento(idProcedimiento));

        model.getNombreProcedimiento(consultaProcedimiento);
        model.getNombreProcedimiento(consultaProcedimiento);

        verify(procedimientoDAO, times(1)).buscarPorId(idProcedimiento);
    }

    @Test
    void getNombreProcedimientoCaeAlIdCuandoNoExisteEnElCatalogo() {
        ProcedimientoDAO procedimientoDAO = mock(ProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), procedimientoDAO);
        UUID idProcedimiento = UUID.randomUUID();
        ConsultaProcedimiento consultaProcedimiento = new ConsultaProcedimiento();
        consultaProcedimiento.setIdProcedimiento(idProcedimiento);

        assertEquals(idProcedimiento.toString(), model.getNombreProcedimiento(consultaProcedimiento));
    }

    @Test
    void fechaFinAnteriorAInicioEsRechazada() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        ConsultaProcedimiento seleccionado = new ConsultaProcedimiento();
        seleccionado.setFechaInicio(new Date(2_000L));
        model.seleccionar(seleccionado);

        assertThrows(ValidatorException.class, () -> model.validarFechaFin(
                contexto(), componente("fechaFin"), new Date(1_000L)));
    }

    @Test
    void fechaFinNulaEsAceptada() {
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                mock(ConsultaProcedimientoDAO.class), mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        ConsultaProcedimiento seleccionado = new ConsultaProcedimiento();
        seleccionado.setFechaInicio(new Date(1_000L));
        model.seleccionar(seleccionado);

        assertDoesNotThrow(() -> model.validarFechaFin(
                contexto(), componente("fechaFin"), null));
    }

    @Test
    void guardarRecortaObservaciones() {
        ConsultaProcedimientoDAO dao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoModel model = new ConsultaProcedimientoModel(
                dao, mock(ConsultaDAO.class), mock(ProcedimientoDAO.class));
        model.nuevo();
        ConsultaProcedimiento seleccionado = model.getSeleccionado();
        seleccionado.setObservaciones("  revision  ");

        model.guardar();

        assertEquals("revision", seleccionado.getObservaciones());
        verify(dao).guardar(seleccionado);
    }

    private UIComponent componente(String id) {
        UIComponent componente = mock(UIComponent.class);
        when(componente.getId()).thenReturn(id);
        return componente;
    }

    private FacesContext contexto() {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                    {"consultaProcedimiento.fechaFinAnterior", "Fecha fin anterior"}
                };
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(bundle);
        return contexto;
    }
}
