package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import jakarta.ejb.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ConsultaFlujoServiceTest {
    ConsultaFlujoService service;
    ClinicaDAO clinicas;
    PersonaRolDAO personas;
    ConsultaDAO consultas;
    ProcedimientoDAO procedimientos;
    ProcedimientoPasoDAO plantillas;
    ConsultaProcedimientoDAO asociaciones;
    ConsultaProcedimientoPasoDAO pasos;
    Clinica clinica;
    Consulta consulta;
    Procedimiento procedimiento;
    ProcedimientoPaso inicial;
    PersonaRol responsable;

    private void inyectar(String name, Object value) throws Exception {
        var field = ConsultaFlujoService.class.getDeclaredField(name);
        field.setAccessible(true); field.set(service, value);
    }
    private PersonaRol persona(Rol rol, Clinica c) {
        var r = new PersonaRol(UUID.randomUUID()); r.setIdRol(rol); r.setIdClinica(c);
        r.setIdPersona(new Persona(UUID.randomUUID())); return r;
    }
    private Rol rol(String nombre) {
        var r = new Rol(UUID.randomUUID()); r.setNombre(nombre); r.setActivo(true); return r;
    }
    private ConsultaProcedimiento datos() {
        var p = new ConsultaProcedimiento(); p.setIdConsulta(consulta);
        p.setIdProcedimiento(procedimiento.getIdProcedimiento()); p.setFechaInicio(new Date()); return p;
    }
    @BeforeEach void preparar() throws Exception {
        service = new ConsultaFlujoService();
        clinicas = mock(ClinicaDAO.class); personas = mock(PersonaRolDAO.class);
        consultas = mock(ConsultaDAO.class); procedimientos = mock(ProcedimientoDAO.class);
        plantillas = mock(ProcedimientoPasoDAO.class); asociaciones = mock(ConsultaProcedimientoDAO.class);
        pasos = mock(ConsultaProcedimientoPasoDAO.class);
        inyectar("clinicaDAO",clinicas); inyectar("personaRolDAO",personas); inyectar("consultaDAO",consultas);
        inyectar("procedimientoDAO",procedimientos); inyectar("procedimientoPasoDAO",plantillas);
        inyectar("consultaProcedimientoDAO",asociaciones); inyectar("pasoDAO",pasos);
        clinica = new Clinica(UUID.randomUUID()); clinica.setActivo(true);
        when(clinicas.buscarPorId(clinica.getIdClinica())).thenReturn(clinica);
        consulta = new Consulta(UUID.randomUUID()); consulta.setIdPersonaRol(persona(rol("Paciente"),clinica));
        when(consultas.buscarPorId(consulta.getIdConsulta())).thenReturn(consulta);
        procedimiento = new Procedimiento(UUID.randomUUID()); procedimiento.setActivo(true);
        when(procedimientos.buscarPorId(procedimiento.getIdProcedimiento())).thenReturn(procedimiento);
        inicial = new ProcedimientoPaso(UUID.randomUUID()); inicial.setIdProcedimiento(procedimiento);
        inicial.setIdRol(rol("Atención al público"));
        when(plantillas.obtenerPasoInicial(procedimiento.getIdProcedimiento())).thenReturn(inicial);
        responsable = persona(inicial.getIdRol(),clinica);
        when(personas.buscarResponsablesPorClinicaYRol(clinica.getIdClinica(),inicial.getIdRol().getIdRol())).thenReturn(List.of(responsable));
        when(asociaciones.guardar(any())).thenAnswer(i -> i.getArgument(0));
    }
    @Test void creaPrimerPasoConRecepcionMismaClinicaYRolExacto() {
        var datos = datos();
        assertSame(datos, service.crearProcedimiento(datos,clinica.getIdClinica()));
        var captor = ArgumentCaptor.forClass(ConsultaProcedimientoPaso.class);
        var orden = inOrder(asociaciones,pasos);
        orden.verify(asociaciones).guardar(datos); orden.verify(pasos).guardar(captor.capture());
        var paso = captor.getValue();
        assertSame(responsable,paso.getIdPersonaRol()); assertSame(datos,paso.getIdConsultaProcedimiento());
        assertEquals("PENDIENTE",paso.getEstado()); assertNull(paso.getFechaFin());
        assertEquals(datos.getFechaInicio(),paso.getFechaInicio());
    }
    @Test void sinResponsableNoEscribeNingunRegistro() {
        when(personas.buscarResponsablesPorClinicaYRol(any(),any())).thenReturn(List.of());
        assertThrows(FlujoConsultaException.class, () -> service.crearProcedimiento(datos(),clinica.getIdClinica()));
        verifyNoInteractions(asociaciones,pasos);
    }
    @Test void rechazaResponsableAjenoOConOtroRolAunqueDaoLoDevuelva() {
        var ajena = new Clinica(UUID.randomUUID()); ajena.setActivo(true);
        when(personas.buscarResponsablesPorClinicaYRol(any(),any())).thenReturn(List.of(persona(inicial.getIdRol(),ajena),persona(rol("Doctor"),clinica)));
        assertThrows(FlujoConsultaException.class, () -> service.crearProcedimiento(datos(),clinica.getIdClinica()));
        verifyNoInteractions(asociaciones,pasos);
    }
    @Test void procedimientoInactivoONullNoSeAsocia() {
        for (Boolean estado : new Boolean[]{false,null}) {
            procedimiento.setActivo(estado);
            assertThrows(FlujoConsultaException.class, () -> service.crearProcedimiento(datos(),clinica.getIdClinica()));
        }
        verifyNoInteractions(asociaciones,pasos);
    }
    @Test void rolInactivoYPlantillaInvalidaNoEscriben() {
        inicial.getIdRol().setActivo(false);
        assertThrows(FlujoConsultaException.class, () -> service.crearProcedimiento(datos(),clinica.getIdClinica()));
        inicial.getIdRol().setActivo(true);
        when(plantillas.obtenerPasoInicial(any())).thenThrow(new FlujoConsultaException("consultaFlujo.plantillaInvalida"));
        assertThrows(FlujoConsultaException.class, () -> service.crearProcedimiento(datos(),clinica.getIdClinica()));
        verifyNoInteractions(asociaciones,pasos);
    }
    @Test void falloDePersistenciaSePropagaParaRollbackYErrorDeNegocioLoExige() {
        doThrow(new IllegalStateException("fallo BD")).when(pasos).guardar(any());
        assertThrows(IllegalStateException.class, () -> service.crearProcedimiento(datos(),clinica.getIdClinica()));
        assertTrue(FlujoConsultaException.class.getAnnotation(ApplicationException.class).rollback());
    }
    @Test void pacienteInactivoSinRolODeOtraClinicaNoSeGuarda() {
        var p = consulta.getIdPersonaRol(); when(personas.buscarPorId(p.getIdPersonaRol())).thenReturn(p);
        consulta.setFechaInicio(new Date());
        p.getIdRol().setActivo(false);
        assertThrows(FlujoConsultaException.class, () -> service.guardarConsulta(consulta,clinica.getIdClinica(),false));
        p.setIdRol(null);
        assertThrows(FlujoConsultaException.class, () -> service.guardarConsulta(consulta,clinica.getIdClinica(),false));
        p.setIdRol(rol("Paciente")); p.setIdClinica(new Clinica(UUID.randomUUID()));
        assertThrows(FlujoConsultaException.class, () -> service.guardarConsulta(consulta,clinica.getIdClinica(),false));
        verify(consultas,never()).guardar(any());
    }
    @Test void edicionNoPuedeCambiarPacienteNiProcedimiento() {
        var otro = persona(rol("Paciente"),clinica); when(personas.buscarPorId(otro.getIdPersonaRol())).thenReturn(otro);
        var editada = new Consulta(consulta.getIdConsulta()); editada.setIdPersonaRol(otro); editada.setFechaInicio(new Date());
        assertThrows(FlujoConsultaException.class, () -> service.guardarConsulta(editada,clinica.getIdClinica(),true));
        var persistido = datos(); persistido.setIdConsultaProcedimiento(UUID.randomUUID());
        when(asociaciones.buscarPorId(persistido.getIdConsultaProcedimiento())).thenReturn(persistido);
        var modificado = datos(); modificado.setIdConsultaProcedimiento(persistido.getIdConsultaProcedimiento());
        modificado.setIdProcedimiento(UUID.randomUUID());
        assertThrows(FlujoConsultaException.class, () -> service.actualizarProcedimiento(modificado,clinica.getIdClinica()));
        verify(asociaciones,never()).actualizar(any()); verifyNoInteractions(pasos);
    }
    @Test void edicionValidaNoCreaSegundoPaso() {
        var persistido = datos(); persistido.setIdConsultaProcedimiento(UUID.randomUUID());
        when(asociaciones.buscarPorId(persistido.getIdConsultaProcedimiento())).thenReturn(persistido);
        var editado = datos(); editado.setIdConsultaProcedimiento(persistido.getIdConsultaProcedimiento());
        editado.setObservaciones("nota");
        service.actualizarProcedimiento(editado,clinica.getIdClinica());
        verify(asociaciones).actualizar(persistido); verifyNoInteractions(pasos);
        assertEquals("nota",persistido.getObservaciones());
    }
}
