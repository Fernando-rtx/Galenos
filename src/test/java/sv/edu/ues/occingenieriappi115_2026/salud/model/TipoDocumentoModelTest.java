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
import static org.mockito.Mockito.never;
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

        verify(dao).existeNombreNormalizado("DUI", null);
        verify(dao, never()).guardar(model.getSeleccionado());
        verify(dao, never()).actualizar(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void elegirPresetDuiRellenaExpresionDelCatalogo() {
        TipoDocumentoModel model = new TipoDocumentoModel(mock(TipoDocumentoDAO.class));
        model.nuevo();

        model.setFormatoSugerido(FormatoRegexSugerido.DUI_SV);

        assertEquals("^[0-9]{8}-[0-9]$", model.getSeleccionado().getExpresionRegular());
    }

    @Test
    void personalizarConservaLaExpresionManual() {
        TipoDocumentoModel model = new TipoDocumentoModel(mock(TipoDocumentoDAO.class));
        model.nuevo();
        model.setFormatoSugerido(FormatoRegexSugerido.DUI_SV);
        model.setFormatoSugerido(FormatoRegexSugerido.PERSONALIZADO);
        model.getSeleccionado().setExpresionRegular("^[A-Z]{2}[0-9]{7}$");

        assertEquals("^[A-Z]{2}[0-9]{7}$", model.getSeleccionado().getExpresionRegular());
        assertEquals(FormatoRegexSugerido.PERSONALIZADO, model.getFormatoSugerido());
    }

    @Test
    void editarReconocePresetExactoSinModificarLaExpresionPersistida() {
        TipoDocumentoModel model = new TipoDocumentoModel(mock(TipoDocumentoDAO.class));
        TipoDocumento tipo = new TipoDocumento();
        tipo.setExpresionRegular("^[0-9]{8}-[0-9]$");

        model.seleccionar(tipo);

        assertEquals(FormatoRegexSugerido.DUI_SV, model.getFormatoSugerido());
        assertEquals("^[0-9]{8}-[0-9]$", tipo.getExpresionRegular());
    }

    @Test
    void editarExpresionDesconocidaLaMuestraComoPersonalizadaSinAlterarla() {
        TipoDocumentoModel model = new TipoDocumentoModel(mock(TipoDocumentoDAO.class));
        TipoDocumento tipo = new TipoDocumento();
        tipo.setExpresionRegular("^[A-Z]{2}[0-9]{7}$");

        model.seleccionar(tipo);

        assertEquals(FormatoRegexSugerido.PERSONALIZADO, model.getFormatoSugerido());
        assertEquals("^[A-Z]{2}[0-9]{7}$", tipo.getExpresionRegular());
    }

    @Test
    void nombreDuiDuplicadoSeRechazaAlCrear() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        when(dao.existeNombreNormalizado("DUI", null)).thenReturn(true);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.nuevo();
        model.getSeleccionado().setNombre("DUI");

        model.guardar();

        verify(dao).existeNombreNormalizado("DUI", null);
        verify(dao, never()).guardar(model.getSeleccionado());
        assertEquals("DUI", model.getSeleccionado().getNombre());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void nombreDuiEnMinusculasSeRechazaAlCrear() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        when(dao.existeNombreNormalizado("dui", null)).thenReturn(true);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.nuevo();
        model.getSeleccionado().setNombre("dui");

        model.guardar();

        verify(dao).existeNombreNormalizado("dui", null);
        verify(dao, never()).guardar(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void nombreConEspaciosExterioresSeRechazaAlCrear() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        when(dao.existeNombreNormalizado(" DUI ", null)).thenReturn(true);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.nuevo();
        model.getSeleccionado().setNombre(" DUI ");

        model.guardar();

        verify(dao).existeNombreNormalizado(" DUI ", null);
        verify(dao, never()).guardar(model.getSeleccionado());
        assertEquals(" DUI ", model.getSeleccionado().getNombre());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void duiExistenteInactivoTambienImpideCrearOtro() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        when(dao.existeNombreNormalizado("DUI", null)).thenReturn(true);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.nuevo();
        model.getSeleccionado().setNombre("DUI");
        model.getSeleccionado().setActivo(true);

        model.guardar();

        verify(dao).existeNombreNormalizado("DUI", null);
        verify(dao, never()).guardar(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void editarConservandoNombreExcluyeElUuidActualYPermiteGuardar() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumento tipo = new TipoDocumento(java.util.UUID.randomUUID());
        tipo.setNombre("DUI");
        when(dao.existeNombreNormalizado("DUI", tipo.getIdTipoDocumento())).thenReturn(false);
        when(dao.actualizar(tipo)).thenReturn(tipo);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.seleccionar(tipo);

        model.guardar();

        verify(dao).existeNombreNormalizado("DUI", tipo.getIdTipoDocumento());
        verify(dao).actualizar(tipo);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void editarOtroTipoHaciaDuiExistenteSeRechazaYExcluyeSuPropioUuid() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumento tipo = new TipoDocumento(java.util.UUID.randomUUID());
        tipo.setNombre("Pasaporte");
        when(dao.existeNombreNormalizado("DUI", tipo.getIdTipoDocumento())).thenReturn(true);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.seleccionar(tipo);
        tipo.setNombre("DUI");

        model.guardar();

        verify(dao).existeNombreNormalizado("DUI", tipo.getIdTipoDocumento());
        verify(dao, never()).actualizar(tipo);
        assertEquals("DUI", model.getSeleccionado().getNombre());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void nombreDiferenteSePermiteYSeGuardaSinEspaciosExteriores() {
        TipoDocumentoDAO dao = mock(TipoDocumentoDAO.class);
        TipoDocumentoModel model = new TipoDocumentoModel(dao);
        model.nuevo();
        TipoDocumento seleccionado = model.getSeleccionado();
        seleccionado.setNombre("  Pasaporte  ");

        model.guardar();

        verify(dao).existeNombreNormalizado("  Pasaporte  ", null);
        verify(dao).guardar(seleccionado);
        assertEquals("Pasaporte", seleccionado.getNombre());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }
}
