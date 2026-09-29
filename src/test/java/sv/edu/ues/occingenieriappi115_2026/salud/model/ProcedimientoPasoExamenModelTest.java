package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProcedimientoPasoExamenModelTest {

    @Test
    void nuevoCreaPPEYCambiaEstadoACreacion() {
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                mock(ProcedimientoPasoExamenDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ExamenDAO.class));

        model.nuevo();

        assertNotNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado().getFechaCreacion());
        assertEquals(true, model.getSeleccionado().getActivo());
    }

    @Test
    void seleccionarAsignaEntidadYCambiaEstadoAEdicion() {
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                mock(ProcedimientoPasoExamenDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ExamenDAO.class));
        ProcedimientoPasoExamen ppe = new ProcedimientoPasoExamen();

        model.seleccionar(ppe);

        assertSame(ppe, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
    }

    @Test
    void guardarEnCreacionDelegaAGuardarYVuelveAListado() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenDao = mock(ExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, ppDao, examenDao);
        model.nuevo();
        UUID idPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(idPaso);
        Examen examen = new Examen();
        examen.setIdExamen(idExamen);
        model.getSeleccionado().setIdProcedimientoPaso(paso);
        model.getSeleccionado().setIdExamen(examen);
        when(ppDao.buscarPorId(idPaso)).thenReturn(paso);
        when(examenDao.buscarPorId(idExamen)).thenReturn(examen);
        ProcedimientoPasoExamen seleccionado = model.getSeleccionado();

        model.guardar();

        verify(dao).guardar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnEdicionDelegaAActualizarYVuelveAListado() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenDao = mock(ExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, ppDao, examenDao);
        UUID idPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(idPaso);
        Examen examen = new Examen();
        examen.setIdExamen(idExamen);
        ProcedimientoPasoExamen ppe = new ProcedimientoPasoExamen();
        ppe.setIdProcedimientoPaso(paso);
        ppe.setIdExamen(examen);
        ProcedimientoPasoExamen actualizado = new ProcedimientoPasoExamen();
        model.seleccionar(ppe);
        when(ppDao.buscarPorId(idPaso)).thenReturn(paso);
        when(examenDao.buscarPorId(idExamen)).thenReturn(examen);
        when(dao.actualizar(ppe)).thenReturn(actualizado);

        model.guardar();

        verify(dao).actualizar(ppe);
        assertSame(actualizado, model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void cancelarLimpiaSeleccionadoYVuelveAListado() {
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                mock(ProcedimientoPasoExamenDAO.class),
                mock(ProcedimientoPasoDAO.class),
                mock(ExamenDAO.class));
        model.seleccionar(new ProcedimientoPasoExamen());

        model.cancelar();

        assertNull(model.getSeleccionado());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConSeleccionadoNullNoRompeNiDelega() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, mock(ProcedimientoPasoDAO.class), mock(ExamenDAO.class));

        assertDoesNotThrow(model::guardar);

        verifyNoInteractions(dao);
    }

    @Test
    void constructorCargaExamenesYProcedimientosPaso() {
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenDao = mock(ExamenDAO.class);
        when(ppDao.obtenerTodos()).thenReturn(List.of(new ProcedimientoPaso()));
        when(examenDao.obtenerTodos()).thenReturn(List.of(new Examen()));

        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                mock(ProcedimientoPasoExamenDAO.class), ppDao, examenDao);

        assertEquals(1, model.getProcedimientosPaso().size());
        assertEquals(1, model.getExamenes().size());
    }

    @Test
    void guardarSinPasoNoDelegaNiCambiaEstado() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, mock(ProcedimientoPasoDAO.class), mock(ExamenDAO.class));
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
    }

    @Test
    void guardarSinExamenNoDelega() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, mock(ProcedimientoPasoDAO.class), mock(ExamenDAO.class));
        model.nuevo();
        model.getSeleccionado().setIdProcedimientoPaso(
                new ProcedimientoPaso(UUID.randomUUID()));

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarConPasoInexistenteNoDelega() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, ppDao, mock(ExamenDAO.class));
        model.nuevo();
        UUID idPaso = UUID.randomUUID();
        Examen examen = new Examen();
        examen.setIdExamen(UUID.randomUUID());
        model.getSeleccionado().setIdProcedimientoPaso(new ProcedimientoPaso(idPaso));
        model.getSeleccionado().setIdExamen(examen);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void guardarConExamenInexistenteNoDelega() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenDao = mock(ExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, ppDao, examenDao);
        model.nuevo();
        UUID idPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(idPaso);
        Examen examen = new Examen();
        examen.setIdExamen(idExamen);
        model.getSeleccionado().setIdProcedimientoPaso(paso);
        model.getSeleccionado().setIdExamen(examen);
        when(ppDao.buscarPorId(idPaso)).thenReturn(paso);

        model.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void rechazaAsociacionDuplicada() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenDao = mock(ExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, ppDao, examenDao);
        model.nuevo();
        UUID idPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(idPaso);
        Examen examen = new Examen();
        examen.setIdExamen(idExamen);
        model.getSeleccionado().setIdProcedimientoPaso(paso);
        model.getSeleccionado().setIdExamen(examen);
        when(ppDao.buscarPorId(idPaso)).thenReturn(paso);
        when(examenDao.buscarPorId(idExamen)).thenReturn(examen);
        ProcedimientoPasoExamen existente = new ProcedimientoPasoExamen();
        existente.setIdProcedimientoPaso(paso);
        existente.setIdExamen(examen);
        when(dao.obtenerTodos()).thenReturn(List.of(existente));

        model.guardar();

        verify(dao, never()).guardar(any());
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
    }

    @Test
    void permiteEditarElRegistroPropioConLaMismaAsociacion() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenDao = mock(ExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, ppDao, examenDao);
        UUID idPropio = UUID.randomUUID();
        UUID idPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(idPaso);
        Examen examen = new Examen();
        examen.setIdExamen(idExamen);
        ProcedimientoPasoExamen ppe = new ProcedimientoPasoExamen();
        ppe.setIdProcedimientoPasoExamen(idPropio);
        ppe.setIdProcedimientoPaso(paso);
        ppe.setIdExamen(examen);
        ProcedimientoPasoExamen existente = new ProcedimientoPasoExamen();
        existente.setIdProcedimientoPasoExamen(idPropio);
        existente.setIdProcedimientoPaso(paso);
        existente.setIdExamen(examen);
        when(ppDao.buscarPorId(idPaso)).thenReturn(paso);
        when(examenDao.buscarPorId(idExamen)).thenReturn(examen);
        when(dao.obtenerTodos()).thenReturn(List.of(existente));
        model.seleccionar(ppe);
        when(dao.actualizar(ppe)).thenReturn(ppe);

        model.guardar();

        verify(dao).actualizar(ppe);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarEnListadoActualizaLaRelacion() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoDAO ppDao = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenDao = mock(ExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, ppDao, examenDao);
        model.nuevo();
        UUID idPaso = UUID.randomUUID();
        UUID idExamen = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(idPaso);
        Examen examen = new Examen();
        examen.setIdExamen(idExamen);
        model.getSeleccionado().setIdProcedimientoPaso(paso);
        model.getSeleccionado().setIdExamen(examen);
        when(ppDao.buscarPorId(idPaso)).thenReturn(paso);
        when(examenDao.buscarPorId(idExamen)).thenReturn(examen);
        ProcedimientoPasoExamen seleccionado = model.getSeleccionado();
        model.setEstado(ESTADO_CRUD.LISTADO);

        model.guardar();

        verify(dao).actualizar(seleccionado);
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
    }

    @Test
    void guardarConContextoPublicaErrorDePaso() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(
                dao, mock(ProcedimientoPasoDAO.class), mock(ExamenDAO.class));
        FacesContext contexto = contexto("ppe.pasoRequerido");
        model.facesContext = contexto;
        model.nuevo();

        model.guardar();

        verifyNoInteractions(dao);
        verificarError(contexto, "ppe.pasoRequerido");
    }

    private FacesContext contexto(String... claves) {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                Object[][] contenidos = new Object[claves.length][2];
                for (int i = 0; i < claves.length; i++) {
                    contenidos[i] = new Object[]{claves[i], "Mensaje " + claves[i]};
                }
                return contenidos;
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(bundle);
        return contexto;
    }

    private void verificarError(FacesContext contexto, String clave) {
        verify(contexto).validationFailed();
        verify(contexto).addMessage(isNull(), argThat(mensaje ->
                FacesMessage.SEVERITY_ERROR.equals(mensaje.getSeverity())
                && ("Mensaje " + clave).equals(mensaje.getSummary())));
    }
}
