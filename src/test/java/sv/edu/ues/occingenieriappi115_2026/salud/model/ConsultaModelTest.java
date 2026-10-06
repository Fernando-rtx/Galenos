package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import java.util.Date;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import org.primefaces.event.SelectEvent;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.*;
import static sv.edu.ues.occingenieriappi115_2026.salud.model.ContextoConsultaFixture.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConsultaModelTest {

    @Test
    void nuevoCreaConsultaYCambiaEstadoACreacion() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        configurar(model);

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertNotNull(model.getSeleccionado().getFechaInicio());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        configurar(model);
        Consulta consulta = consulta();

        model.seleccionar(consulta);

        assertSame(consulta, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(
                dao, mock(PersonaRolDAO.class));
        configurar(model);
        model.nuevo();
        Consulta seleccionado = model.getSeleccionado();

        model.guardar();

        verify(servicio(model)).guardarConsulta(seleccionado, CLINICA, false);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(
                dao, mock(PersonaRolDAO.class));
        configurar(model);
        Consulta consulta = consulta();
        Consulta actualizada = consulta();
        model.seleccionar(consulta);
        when(servicio(model).guardarConsulta(consulta, CLINICA, true)).thenReturn(actualizada);

        model.guardar();

        verify(servicio(model)).guardarConsulta(consulta, CLINICA, true);
        assertSame(actualizada, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoNoDelegaYPermaneceEnListado() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(
                dao, mock(PersonaRolDAO.class));
        configurar(model);
        model.nuevo();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        configurar(model);
        model.seleccionar(consulta());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(
                dao, mock(PersonaRolDAO.class));
        configurar(model);

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaPersonasRoles() {
        PersonaRolDAO personaRolDAO = mock(PersonaRolDAO.class);
        when(personaRolDAO.obtenerTodos()).thenReturn(List.of(new PersonaRol()));

        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class), personaRolDAO);
        configurar(model);

        assertEquals(0, model.getPersonasRoles().size());
        verify(personaRolDAO, org.mockito.Mockito.never()).obtenerTodos();
    }

    @Test
    void dobleClicSeleccionaConsultaDelEventoYCambiaAEdicion() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        configurar(model);
        Consulta consulta = consulta();
        SelectEvent<Consulta> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(consulta);

        model.seleccionarFila(evento);

        assertSame(consulta, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void dobleClicSinFilaNoCambiaLaSeleccionNiElEstado() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        configurar(model);
        SelectEvent<Consulta> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(null);

        assertDoesNotThrow(() -> model.seleccionarFila(evento));

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void fechaFinAnteriorAInicioEsRechazada() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        configurar(model);
        Consulta consulta = consulta();
        consulta.setFechaInicio(new Date(2_000L));
        model.seleccionar(consulta);

        assertThrows(ValidatorException.class, () -> model.validarFechaFin(
                contexto(), componente("consultaFechaFin"), new Date(1_000L)));
    }

    @Test
    void fechaFinIgualAInicioEsAceptada() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        configurar(model);
        Consulta consulta = consulta();
        consulta.setFechaInicio(new Date(1_000L));
        model.seleccionar(consulta);

        assertDoesNotThrow(() -> model.validarFechaFin(
                contexto(), componente("consultaFechaFin"), new Date(1_000L)));
    }

    @Test
    void fechaFinNulaEsAceptada() {
        ConsultaModel model = new ConsultaModel(
                mock(ConsultaDAO.class),
                mock(PersonaRolDAO.class));
        configurar(model);
        Consulta consulta = consulta();
        consulta.setFechaInicio(new Date(1_000L));
        model.seleccionar(consulta);

        assertDoesNotThrow(() -> model.validarFechaFin(
                contexto(), componente("consultaFechaFin"), null));
    }

    @Test
    void guardarRecortaReferenciaYObservaciones() {
        ConsultaDAO dao = mock(ConsultaDAO.class);
        ConsultaModel model = new ConsultaModel(dao, mock(PersonaRolDAO.class));
        configurar(model);
        model.nuevo();
        Consulta consulta = model.getSeleccionado();
        consulta.setReferenciaExterna("  REF-1  ");
        consulta.setObservaciones("  nota  ");

        model.guardar();

        assertEquals("REF-1", consulta.getReferenciaExterna());
        assertEquals("nota", consulta.getObservaciones());
        verify(servicio(model)).guardarConsulta(consulta, CLINICA, false);
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
                    {"consulta.fechaFinAnterior", "Fecha fin anterior"}
                };
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(bundle);
        return contexto;
    }
}
