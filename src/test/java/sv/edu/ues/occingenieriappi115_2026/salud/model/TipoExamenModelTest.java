package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas del flujo de estado de {@link TipoExamenModel}.
 *
 * <p>Mockito aísla el DAO para comprobar que creación llama {@code guardar},
 * edición llama {@code actualizar} y cancelación no persiste cambios.</p>
 */
class TipoExamenModelTest {

    @Test
    void nuevoCreaTipoExamenYCambiaEstadoACreacion() {
        TipoExamenModel model = new TipoExamenModel(mock(TipoExamenDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertTrue(model.getSeleccionado().getActivo());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void nombreSoloConEspaciosEsRechazado() {
        TipoExamenModel model = new TipoExamenModel(mock(TipoExamenDAO.class));

        assertThrows(ValidatorException.class,
                () -> model.validarNombre(contexto(), null, "   "));
    }

    @Test
    void nombreValidoEsAceptado() {
        TipoExamenModel model = new TipoExamenModel(mock(TipoExamenDAO.class));

        assertDoesNotThrow(() -> model.validarNombre(null, null, "Laboratorio"));
    }

    @Test
    void guardarRecortaNombreYObservaciones() {
        TipoExamenDAO dao = mock(TipoExamenDAO.class);
        TipoExamenModel model = new TipoExamenModel(dao);
        model.nuevo();
        TipoExamen seleccionado = model.getSeleccionado();
        seleccionado.setNombre("  Laboratorio  ");
        seleccionado.setObservaciones("  Rutina  ");

        model.guardar();

        assertEquals("Laboratorio", seleccionado.getNombre());
        assertEquals("Rutina", seleccionado.getObservaciones());
        verify(dao).guardar(seleccionado);
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        TipoExamenModel model = new TipoExamenModel(mock(TipoExamenDAO.class));
        TipoExamen tipoExamen = new TipoExamen();

        model.seleccionar(tipoExamen);

        assertSame(tipoExamen, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        TipoExamenDAO dao = mock(TipoExamenDAO.class);
        TipoExamenModel model = new TipoExamenModel(dao);
        model.nuevo();
        TipoExamen seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        TipoExamenDAO dao = mock(TipoExamenDAO.class);
        TipoExamenModel model = new TipoExamenModel(dao);
        TipoExamen tipoExamen = new TipoExamen();
        TipoExamen actualizado = new TipoExamen();
        model.seleccionar(tipoExamen);
        when(dao.actualizar(tipoExamen)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(tipoExamen);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        TipoExamenModel model = new TipoExamenModel(mock(TipoExamenDAO.class));
        model.seleccionar(new TipoExamen());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        TipoExamenDAO dao = mock(TipoExamenDAO.class);
        TipoExamenModel model = new TipoExamenModel(dao);

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void nuevoInicializaActivo() {
        TipoExamenModel model = new TipoExamenModel(mock(TipoExamenDAO.class));

        model.nuevo();

        assertTrue(model.getSeleccionado().getActivo());
    }

    @Test
    void validarNombreValidoNoLanza() {
        TipoExamenModel model = new TipoExamenModel(mock(TipoExamenDAO.class));

        assertDoesNotThrow(() -> model.validarNombre(null, null, "Hemograma"));
    }

    @Test
    void validarNombreDuplicadoLanza() {
        TipoExamenDAO dao = mock(TipoExamenDAO.class);
        TipoExamen existente = new TipoExamen(UUID.randomUUID());
        existente.setNombre("Hemograma");
        when(dao.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
                .thenReturn(List.of(existente));
        TipoExamenModel model = new TipoExamenModel(dao);

        assertThrows(ValidatorException.class,
                () -> model.validarNombre(contexto(), null, "Hemograma"));
    }
    private FacesContext contexto() {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        ResourceBundle mensajes = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"tipoExamen.nombreRequerido", "Nombre requerido"},
                    {"tipoExamen.nombreMinimo", "Nombre demasiado corto"},
                    {"tipoExamen.nombreMaximo", "Nombre demasiado largo"},
                    {"tipoExamen.nombreDuplicado", "Nombre duplicado"}
                };
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(mensajes);
        return contexto;
    }
}
