package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Regresiones del parcial: los setters y la mutación directa no sustituyen FKs. */
class AuditoriaRelacionesModelTest {
    private FacesContext contexto() {
        FacesContext context = mock(FacesContext.class);
        Application app = mock(Application.class);
        when(context.getApplication()).thenReturn(app);
        when(app.getResourceBundle(context, "msg"))
                .thenReturn(ResourceBundle.getBundle("i18n.messages", Locale.forLanguageTag("es")));
        return context;
    }

    private void inyectar(Object objeto, String nombre, Object valor) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    private void errorUnico(FacesContext context) {
        verify(context, times(1)).validationFailed();
        verify(context, times(1)).addMessage(any(), argThat(m ->
                FacesMessage.SEVERITY_ERROR.equals(m.getSeverity()) && !m.getSummary().isBlank()));
    }

    @Test
    void documentoConservaPersonaYBorradorAnteCambioDirecto() throws Exception {
        DocumentoDAO dao = mock(DocumentoDAO.class);
        DocumentoModel model = new DocumentoModel(dao, mock(PersonaDAO.class), mock(TipoDocumentoDAO.class));
        Documento datos = new Documento(UUID.randomUUID());
        Persona original = new Persona(UUID.randomUUID());
        datos.setIdPersona(original);
        datos.setValor("Borrador");
        model.seleccionar(datos);
        FacesContext context = contexto();
        inyectar(model, "facesContext", context);
        model.setPersonaSeleccionadaId(UUID.randomUUID().toString());
        assertSame(original, datos.getIdPersona());
        datos.setIdPersona(new Persona(UUID.randomUUID()));
        model.guardar();
        assertSame(original, datos.getIdPersona());
        assertEquals("Borrador", datos.getValor());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
        verify(dao, never()).actualizar(any());
        errorUnico(context);
    }

    @Test
    void documentoPermiteSeleccionarPersonaAlCrear() {
        PersonaDAO personas = mock(PersonaDAO.class);
        Persona persona = new Persona(UUID.randomUUID());
        when(personas.buscarPorId(persona.getIdPersona())).thenReturn(persona);
        DocumentoModel model = new DocumentoModel(mock(DocumentoDAO.class), personas, mock(TipoDocumentoDAO.class));
        model.nuevo();
        model.setPersonaSeleccionadaId(persona.getIdPersona().toString());
        assertSame(persona, model.getSeleccionado().getIdPersona());
    }

    @Test
    void contactoNoPermiteCambiarPersonaPorSetterNiMutacion() throws Exception {
        MedioContactoDAO dao = mock(MedioContactoDAO.class);
        MedioContactoModel model = new MedioContactoModel(dao, mock(PersonaDAO.class), mock(TipoMedioContactoDAO.class));
        MedioContacto datos = new MedioContacto(UUID.randomUUID());
        Persona original = new Persona(UUID.randomUUID());
        datos.setIdPersona(original);
        datos.setValor("Borrador");
        model.seleccionar(datos);
        FacesContext context = contexto();
        inyectar(model, "facesContext", context);
        model.setPersonaSeleccionadaId(UUID.randomUUID().toString());
        assertSame(original, datos.getIdPersona());
        datos.setIdPersona(new Persona(UUID.randomUUID()));
        model.guardar();
        assertSame(original, datos.getIdPersona());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
        verify(dao, never()).actualizar(any());
        errorUnico(context);
    }

    @Test
    void asignacionRolNoPermiteCambiarPersona() throws Exception {
        PersonaRolDAO dao = mock(PersonaRolDAO.class);
        PersonaRolModel model = new PersonaRolModel(dao, mock(PersonaDAO.class), mock(RolDAO.class), mock(ClinicaDAO.class));
        PersonaRol datos = new PersonaRol(UUID.randomUUID());
        Persona original = new Persona(UUID.randomUUID());
        datos.setIdPersona(original);
        model.seleccionar(datos);
        FacesContext context = contexto();
        inyectar(model, "facesContext", context);
        model.setPersonaSeleccionadaId(UUID.randomUUID().toString());
        assertSame(original, datos.getIdPersona());
        datos.setIdPersona(new Persona(UUID.randomUUID()));
        model.guardar();
        assertSame(original, datos.getIdPersona());
        verify(dao, never()).actualizar(any());
        errorUnico(context);
    }

    @Test
    void examenTipoExamenRechazaCambioDelExamenEnCrudIndependiente() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(dao, mock(ExamenDAO.class), mock(TipoExamenDAO.class));
        ExamenTipoExamen datos = new ExamenTipoExamen(UUID.randomUUID());
        datos.setIdExamen(new Examen(UUID.randomUUID()));
        datos.setIdTipoExamen(new TipoExamen(UUID.randomUUID()));
        when(dao.buscarPorId(datos.getIdExamenTipoExamen())).thenReturn(datos);
        model.seleccionar(datos);
        model.facesContext = contexto();
        datos.setIdExamen(new Examen(UUID.randomUUID()));
        model.guardar();
        verify(dao, never()).actualizar(any());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
        errorUnico(model.facesContext);
    }

    @Test
    void tipoDeExamenNoCambiaAunqueDaoDevuelvaElMismoObjetoMutado() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        TipoExamenDAO tipos = mock(TipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(dao, mock(ExamenDAO.class), tipos);
        ExamenTipoExamen datos = new ExamenTipoExamen(UUID.randomUUID());
        datos.setIdExamen(new Examen(UUID.randomUUID()));
        datos.setIdTipoExamen(new TipoExamen(UUID.randomUUID()));
        when(dao.buscarPorId(datos.getIdExamenTipoExamen())).thenReturn(datos);
        model.seleccionar(datos);
        model.facesContext = contexto();
        datos.setIdTipoExamen(new TipoExamen(UUID.randomUUID()));
        model.guardar();
        verify(dao, never()).actualizar(any());
        verify(tipos, never()).buscarPorId(any());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
        errorUnico(model.facesContext);
    }

    @Test
    void asociacionVaciaNoSeGuardaNiAutorizaCerrar() {
        ExamenTipoExamenDAO dao = mock(ExamenTipoExamenDAO.class);
        ExamenTipoExamenModel model = new ExamenTipoExamenModel(dao, mock(ExamenDAO.class), mock(TipoExamenDAO.class));
        model.nuevo();
        model.facesContext = contexto();
        model.guardar();
        assertEquals(ESTADO_CRUD.CREACION, model.getEstado());
        assertNotNull(model.getSeleccionado());
        verify(dao, never()).guardar(any());
        errorUnico(model.facesContext);
    }

    @Test
    void secuenciaNoSustituyeOrigenPorMutacionDirecta() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(dao, mock(ProcedimientoPasoDAO.class));
        ProcedimientoPasoSecuencia datos = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        datos.setIdProcedimientoPaso(new ProcedimientoPaso(UUID.randomUUID()));
        datos.setIdProcedimientoPasoReferencia(UUID.randomUUID());
        model.seleccionar(datos);
        model.facesContext = contexto();
        datos.setIdProcedimientoPaso(new ProcedimientoPaso(UUID.randomUUID()));
        model.guardar();
        verify(dao, never()).actualizar(any());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
        errorUnico(model.facesContext);
    }

    @Test
    void secuenciaNoSustituyeDestinoPorSelectorNiUuid() {
        ProcedimientoPasoSecuenciaDAO dao = mock(ProcedimientoPasoSecuenciaDAO.class);
        ProcedimientoPasoSecuenciaModel model = new ProcedimientoPasoSecuenciaModel(dao, mock(ProcedimientoPasoDAO.class));
        ProcedimientoPasoSecuencia datos = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        datos.setIdProcedimientoPaso(new ProcedimientoPaso(UUID.randomUUID()));
        datos.setIdProcedimientoPasoReferencia(UUID.randomUUID());
        model.seleccionar(datos);
        model.facesContext = contexto();
        model.setPasoSiguienteSeleccionado(new ProcedimientoPaso(UUID.randomUUID()));
        model.guardar();
        verify(dao, never()).actualizar(any());
        errorUnico(model.facesContext);
        model.setPasoSiguienteSeleccionado(null);
        datos.setIdProcedimientoPasoReferencia(UUID.randomUUID());
        model.facesContext = contexto();
        model.guardar();
        verify(dao, never()).actualizar(any());
        errorUnico(model.facesContext);
        assertFalse(model.isPasoSiguienteEditable());
        model.nuevo();
        assertTrue(model.isPasoSiguienteEditable());
    }

    @Test
    void examenPorPasoNoSustituyeExamenGuardado() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(dao, mock(ProcedimientoPasoDAO.class), mock(ExamenDAO.class));
        ProcedimientoPasoExamen datos = new ProcedimientoPasoExamen(UUID.randomUUID());
        datos.setIdProcedimientoPaso(new ProcedimientoPaso(UUID.randomUUID()));
        datos.setIdExamen(new Examen(UUID.randomUUID()));
        model.seleccionar(datos);
        model.facesContext = contexto();
        datos.setIdExamen(new Examen(UUID.randomUUID()));
        model.guardar();
        verify(dao, never()).actualizar(any());
        assertEquals(ESTADO_CRUD.EDICION, model.getEstado());
        errorUnico(model.facesContext);
    }

    @Test
    void examenPorPasoInactivoPermiteEditarObservacionesYEstado() {
        ProcedimientoPasoExamenDAO dao = mock(ProcedimientoPasoExamenDAO.class);
        ProcedimientoPasoDAO pasos = mock(ProcedimientoPasoDAO.class);
        ExamenDAO examenes = mock(ExamenDAO.class);
        ProcedimientoPasoExamenModel model = new ProcedimientoPasoExamenModel(dao, pasos, examenes);
        ProcedimientoPasoExamen datos = new ProcedimientoPasoExamen(UUID.randomUUID());
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
        Examen examen = new Examen(UUID.randomUUID());
        examen.setActivo(false);
        datos.setIdProcedimientoPaso(paso);
        datos.setIdExamen(examen);
        when(pasos.buscarPorId(paso.getIdProcedimientoPaso())).thenReturn(paso);
        when(examenes.buscarPorId(examen.getIdExamen())).thenReturn(examen);
        when(dao.actualizar(datos)).thenReturn(datos);
        model.seleccionar(datos);
        assertEquals(List.of(examen), model.getExamenes());
        datos.setObservaciones(" Cambio ");
        datos.setActivo(false);
        model.facesContext = contexto();
        model.guardar();
        verify(dao).actualizar(datos);
        assertEquals("Cambio", datos.getObservaciones());
        assertFalse(datos.getActivo());
        assertEquals(ESTADO_CRUD.LISTADO, model.getEstado());
        verify(model.facesContext, never()).validationFailed();
        verify(model.facesContext, times(1)).addMessage(isNull(), argThat(m -> FacesMessage.SEVERITY_INFO.equals(m.getSeverity())));
    }

    @Test
    void opcionesDePasosAlCrearSoloIncluyenRolesYProcedimientosActivos() {
        ProcedimientoDAO procedimientos = mock(ProcedimientoDAO.class);
        RolDAO roles = mock(RolDAO.class);
        Procedimiento activo = new Procedimiento(UUID.randomUUID()); activo.setActivo(true);
        Procedimiento inactivo = new Procedimiento(UUID.randomUUID()); inactivo.setActivo(false);
        Rol activoRol = new Rol(UUID.randomUUID()); activoRol.setActivo(true);
        Rol inactivoRol = new Rol(UUID.randomUUID()); inactivoRol.setActivo(false);
        when(procedimientos.obtenerTodos()).thenReturn(List.of(activo, inactivo));
        when(roles.obtenerTodos()).thenReturn(List.of(activoRol, inactivoRol));
        ProcedimientoPasoModel model = new ProcedimientoPasoModel(mock(ProcedimientoPasoDAO.class), procedimientos, roles);
        ProcedimientoPaso historico = new ProcedimientoPaso(UUID.randomUUID());
        historico.setIdProcedimiento(inactivo); historico.setIdRol(inactivoRol);
        model.seleccionar(historico);
        assertTrue(model.getRoles().contains(inactivoRol));
        model.nuevo();
        assertEquals(List.of(activo), model.getProcedimientos());
        assertEquals(List.of(activoRol), model.getRoles());
        assertTrue(model.isEstructuraEditable());
    }
}
