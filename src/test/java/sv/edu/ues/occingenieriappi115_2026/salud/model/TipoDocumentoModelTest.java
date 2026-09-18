package sv.edu.ues.occingenieriappi115_2026.salud.model;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoDocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        TipoDocumento tipoDocumento = new TipoDocumento();
        TipoDocumento actualizado = new TipoDocumento();
        model.seleccionar(tipoDocumento);
        when(dao.actualizar(tipoDocumento)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(tipoDocumento);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
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
}
