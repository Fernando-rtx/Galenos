package sv.edu.ues.occingenieriappi115_2026.salud.model;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ClinicaModelTest {

    @Test
    void nuevoCreaClinicaYCambiaEstadoACreacion() {
        ClinicaModel model = new ClinicaModel(mock(ClinicaDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ClinicaModel model = new ClinicaModel(mock(ClinicaDAO.class));
        Clinica clinica = new Clinica();

        model.seleccionar(clinica);

        assertSame(clinica, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao);
        model.nuevo();
        Clinica seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao);
        Clinica clinica = new Clinica();
        Clinica actualizado = new Clinica();
        model.seleccionar(clinica);
        when(dao.actualizar(clinica)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(clinica);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ClinicaModel model = new ClinicaModel(mock(ClinicaDAO.class));
        model.seleccionar(new Clinica());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ClinicaDAO dao = mock(ClinicaDAO.class);
        ClinicaModel model = new ClinicaModel(dao);

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }
}
