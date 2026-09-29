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
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConsultaProcedimientoPasoModelTest {

    @Test
    void nuevoCreaPasoYCambiaEstadoACreacion() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertNotNull(model.getSeleccionado().getFechaInicio());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();

        model.seleccionar(paso);

        assertSame(paso, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));
        model.nuevo();
        ConsultaProcedimientoPaso seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        ConsultaProcedimientoPaso actualizado = new ConsultaProcedimientoPaso();
        model.seleccionar(paso);
        when(dao.actualizar(paso)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(paso);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        model.seleccionar(new ConsultaProcedimientoPaso());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaPersonasRoles() {
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        when(personaRolDao.obtenerTodos()).thenReturn(List.of(new PersonaRol()));

        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
                personaRolDao);

        assertEquals(1, model.getPersonasRoles().size());
    }

    @Test
    void getPasosPorConsultaSinIdDevuelveListaVaciaYNoConsultaDao() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));

        List<ConsultaProcedimientoPaso> resultado = model.getPasosPorConsulta(new Consulta());

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(dao);
    }

    @Test
    void getPasosPorConsultaFiltraPorRutaDeConsulta() {
        ConsultaProcedimientoPasoDAO dao = mock(ConsultaProcedimientoPasoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                dao, mock(ConsultaProcedimientoDAO.class), mock(PersonaRolDAO.class));
        UUID idConsulta = UUID.randomUUID();
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(idConsulta);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        when(dao.obtenerPagina(
                0,
                Integer.MAX_VALUE,
                List.of(new FiltroDAO("idConsultaProcedimiento.idConsulta.idConsulta",
                        OperadorFiltro.IGUAL, idConsulta)),
                List.of())).thenReturn(List.of(paso));

        List<ConsultaProcedimientoPaso> resultado = model.getPasosPorConsulta(consulta);

        assertEquals(List.of(paso), resultado);
    }

    @Test
    void getConsultaProcedimientosPorConsultaSinIdDevuelveListaVaciaYNoConsultaDao() {
        ConsultaProcedimientoDAO procDao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class), procDao, mock(PersonaRolDAO.class));

        List<ConsultaProcedimiento> resultado = model.getConsultaProcedimientosPorConsulta(new Consulta());

        assertTrue(resultado.isEmpty());
        verify(procDao, never()).obtenerPagina(anyInt(), anyInt(), anyList(), anyList());
    }

    @Test
    void getConsultaProcedimientosPorConsultaFiltraPorIdDeConsulta() {
        ConsultaProcedimientoDAO procDao = mock(ConsultaProcedimientoDAO.class);
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class), procDao, mock(PersonaRolDAO.class));
        UUID idConsulta = UUID.randomUUID();
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(idConsulta);
        ConsultaProcedimiento procedimiento = new ConsultaProcedimiento();
        when(procDao.obtenerPagina(
                0,
                Integer.MAX_VALUE,
                List.of(new FiltroDAO("idConsulta.idConsulta", OperadorFiltro.IGUAL, idConsulta)),
                List.of())).thenReturn(List.of(procedimiento));

        List<ConsultaProcedimiento> resultado = model.getConsultaProcedimientosPorConsulta(consulta);

        assertEquals(List.of(procedimiento), resultado);
    }

    @Test
    void isSeleccionadoParaConsultaEsFalsoCuandoPasoEsDeOtraConsulta() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        Consulta otraConsulta = new Consulta();
        otraConsulta.setIdConsulta(UUID.randomUUID());
        ConsultaProcedimiento procedimientoAjeno = new ConsultaProcedimiento();
        procedimientoAjeno.setIdConsulta(otraConsulta);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        paso.setIdConsultaProcedimiento(procedimientoAjeno);
        model.seleccionar(paso);

        assertFalse(model.isSeleccionadoParaConsulta(consulta));
    }

    @Test
    void isSeleccionadoParaConsultaEsVerdaderoCuandoPasoPertenece() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        ConsultaProcedimiento procedimiento = new ConsultaProcedimiento();
        procedimiento.setIdConsulta(consulta);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        paso.setIdConsultaProcedimiento(procedimiento);
        model.seleccionar(paso);

        assertTrue(model.isSeleccionadoParaConsulta(consulta));
    }

    @Test
    void seleccionarParaConsultaIgnoraPasoDeOtraConsulta() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());

        model.seleccionarParaConsulta(new ConsultaProcedimientoPaso(), consulta);

        assertNull(model.getSeleccionado());
    }

    @Test
    void isFormularioVisibleParaConsultaEsFalsoSinConsultaActiva() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        model.nuevo();

        assertFalse(model.isFormularioVisibleParaConsulta(null));
        assertFalse(model.isFormularioVisibleParaConsulta(new Consulta()));
    }

    @Test
    void isFormularioVisibleParaConsultaEsVerdaderoEnCreacionSinProcedimiento() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        model.nuevo();

        assertTrue(model.isFormularioVisibleParaConsulta(consulta));
    }

    @Test
    void isFormularioVisibleParaConsultaEsFalsoEnListadoSinSeleccionado() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());

        assertFalse(model.isFormularioVisibleParaConsulta(consulta));
    }

    @Test
    void isFormularioVisibleParaConsultaEsVerdaderoCuandoElPasoPertenece() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        ConsultaProcedimiento procedimiento = new ConsultaProcedimiento();
        procedimiento.setIdConsulta(consulta);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        paso.setIdConsultaProcedimiento(procedimiento);
        model.seleccionar(paso);

        assertTrue(model.isFormularioVisibleParaConsulta(consulta));
    }

    @Test
    void isFormularioVisibleParaConsultaEsFalsoCuandoElPasoEsDeOtraConsulta() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        Consulta consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        Consulta otraConsulta = new Consulta();
        otraConsulta.setIdConsulta(UUID.randomUUID());
        ConsultaProcedimiento procedimientoAjeno = new ConsultaProcedimiento();
        procedimientoAjeno.setIdConsulta(otraConsulta);
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        paso.setIdConsultaProcedimiento(procedimientoAjeno);
        model.seleccionar(paso);

        assertFalse(model.isFormularioVisibleParaConsulta(consulta));
    }

    @Test
    void fechaFinAnteriorAInicioEsRechazada() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        paso.setFechaInicio(new Date(2_000L));
        model.seleccionar(paso);

        assertThrows(ValidatorException.class, () -> model.validarFechaFin(
                contexto(), componente("fechaFin"), new Date(1_000L)));
    }

    @Test
    void fechaFinNulaEsAceptada() {
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class),
                mock(ConsultaProcedimientoDAO.class),
                mock(PersonaRolDAO.class));
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso();
        paso.setFechaInicio(new Date(1_000L));
        model.seleccionar(paso);

        assertDoesNotThrow(() -> model.validarFechaFin(
                contexto(), componente("fechaFin"), null));
    }

    @Test
    void validarResponsableAceptaResponsableDelCatalogo() {
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        PersonaRol responsable = new PersonaRol();
        responsable.setIdPersonaRol(UUID.randomUUID());
        when(personaRolDao.obtenerTodos()).thenReturn(List.of(responsable));
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
                personaRolDao);

        assertDoesNotThrow(() -> model.validarResponsable(
                contexto(), componente("pasoPersonaRol"), responsable));
    }

    @Test
    void validarResponsableRechazaResponsableDesconocido() {
        PersonaRolDAO personaRolDao = mock(PersonaRolDAO.class);
        PersonaRol existente = new PersonaRol();
        existente.setIdPersonaRol(UUID.randomUUID());
        when(personaRolDao.obtenerTodos()).thenReturn(List.of(existente));
        ConsultaProcedimientoPasoModel model = new ConsultaProcedimientoPasoModel(
                mock(ConsultaProcedimientoPasoDAO.class), mock(ConsultaProcedimientoDAO.class),
                personaRolDao);
        PersonaRol desconocido = new PersonaRol();
        desconocido.setIdPersonaRol(UUID.randomUUID());

        assertThrows(ValidatorException.class, () -> model.validarResponsable(
                contexto(), componente("pasoPersonaRol"), desconocido));
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
                    {"consultaProcedimientoPaso.fechaFinAnterior", "Fecha fin anterior"},
                    {"consultaProcedimientoPaso.responsableNoExiste", "Responsable desconocido"}                };
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(bundle);
        return contexto;
    }
}
