package sv.edu.ues.occingenieriappi115_2026.salud.model;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoDocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas del backing bean {@link TipoDocumentoModel} con un DAO simulado.
 * Verifican transiciones LISTADO/CREACION/EDICION y que guardar delegue en la
 * operación correcta, siguiendo el patrón Arrange/Act/Assert.
 */
class TipoDocumentoModelTest {

    @Test
    void nuevoCreaTipoDocumentoYCambiaEstadoACreacion() {
        TipoDocumentoModel model = new TipoDocumentoModel(mock(TipoDocumentoDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        TipoDocumentoModel model = new TipoDocumentoModel(mock(TipoDocumentoDAO.class));
        TipoDocumento tipoDocumento = new TipoDocumento();

        model.seleccionar(tipoDocumento);

        assertSame(tipoDocumento, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.nuevo();
        TipoDocumento seleccionado = model.getSeleccionado();
        seleccionado.setNombre("DUI");
        seleccionado.setActivo(true);
        seleccionado.setExpresionRegular("^[0-9]{8}-[0-9]$");

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setNombre("Pasaporte");
        tipoDocumento.setActivo(false);
        tipoDocumento.setExpresionRegular("^[A-Z0-9]{6,12}$");
        TipoDocumento actualizado = tipoDocumento;
        model.seleccionar(tipoDocumento);
        when(dao.actualizar(tipoDocumento)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(tipoDocumento);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
        assertEquals("Pasaporte", model.getSeleccionado().getNombre());
        assertFalse(model.getSeleccionado().getActivo());
        assertEquals("^[A-Z0-9]{6,12}$", model.getSeleccionado().getExpresionRegular());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        TipoDocumentoModel model = new TipoDocumentoModel(mock(TipoDocumentoDAO.class));
        model.seleccionar(new TipoDocumento());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void tipoInactivoConRegexConfiguradaSeConservaAlGuardar() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.nuevo();
        TipoDocumento seleccionado = model.getSeleccionado();
        seleccionado.setNombre("Pasaporte");
        seleccionado.setActivo(false);
        seleccionado.setExpresionRegular("^[A-Z]{2}[0-9]{6}$");

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertFalse(seleccionado.getActivo());
        assertEquals("^[A-Z]{2}[0-9]{6}$", seleccionado.getExpresionRegular());
    }

    @Test
    void expresionRegularInvalidaImpidePersistirTipoDocumento() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.nuevo();
        model.getSeleccionado().setNombre("DUI");
        model.getSeleccionado().setExpresionRegular("[0-9");

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }
}
