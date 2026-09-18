package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonaRolModelTest {
    private PersonaRolModel model(PersonaRolDAO d,PersonaDAO p,RolDAO r,ClinicaDAO c){return new PersonaRolModel(d,p,r,c);}
    @Test void nuevoInicializaFecha(){PersonaRolModel m=model(mock(PersonaRolDAO.class),mock(PersonaDAO.class),mock(RolDAO.class),mock(ClinicaDAO.class));m.nuevo();assertNotNull(m.getSeleccionado().getFechaCreacion());assertEquals(ESTADO_CRUD.CREACION,m.getEstado());}
    @Test void seleccionarYCancelar(){PersonaRolModel m=model(mock(PersonaRolDAO.class),mock(PersonaDAO.class),mock(RolDAO.class),mock(ClinicaDAO.class));PersonaRol pr=new PersonaRol();m.seleccionar(pr);assertSame(pr,m.getSeleccionado());m.cancelar();assertNull(m.getSeleccionado());}
    @Test void guardarCreacionYEdicion(){PersonaRolDAO d=mock(PersonaRolDAO.class);PersonaRolModel m=model(d,mock(PersonaDAO.class),mock(RolDAO.class),mock(ClinicaDAO.class));m.nuevo();PersonaRol n=m.getSeleccionado();n.setIdPersona(new Persona());n.setIdRol(new Rol());n.setIdClinica(new Clinica());m.guardar();verify(d).guardar(n);PersonaRol a=new PersonaRol();when(d.actualizar(n)).thenReturn(a);m.seleccionar(n);m.guardar();assertSame(a,m.getSeleccionado());}
    @Test void asignaTresRelaciones(){PersonaDAO p=mock(PersonaDAO.class);RolDAO r=mock(RolDAO.class);ClinicaDAO c=mock(ClinicaDAO.class);Persona persona=new Persona(UUID.randomUUID());Rol rol=new Rol(UUID.randomUUID());Clinica clinica=new Clinica(UUID.randomUUID());when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);when(r.buscarPorId(rol.getIdRol())).thenReturn(rol);when(c.buscarPorId(clinica.getIdClinica())).thenReturn(clinica);PersonaRolModel m=model(mock(PersonaRolDAO.class),p,r,c);m.nuevo();m.setPersonaSeleccionadaId(persona.getIdPersona().toString());m.setRolSeleccionadoId(rol.getIdRol().toString());m.setClinicaSeleccionadaId(clinica.getIdClinica().toString());assertSame(persona,m.getSeleccionado().getIdPersona());assertSame(rol,m.getSeleccionado().getIdRol());assertSame(clinica,m.getSeleccionado().getIdClinica());}
    @Test void contextoPreselecciona(){PersonaDAO p=mock(PersonaDAO.class);Persona persona=new Persona(UUID.randomUUID());when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);PersonaRolModel m=model(mock(PersonaRolDAO.class),p,mock(RolDAO.class),mock(ClinicaDAO.class));m.setIdPersonaContexto(persona.getIdPersona().toString());m.nuevo();assertSame(persona,m.getSeleccionado().getIdPersona());}
    @Test void personaNullImpideGuardar(){PersonaRolDAO d=mock(PersonaRolDAO.class);PersonaRolModel m=model(d,mock(PersonaDAO.class),mock(RolDAO.class),mock(ClinicaDAO.class));m.nuevo();m.getSeleccionado().setIdRol(new Rol());m.getSeleccionado().setIdClinica(new Clinica());m.guardar();verifyNoInteractions(d);}
    @Test void rolNullImpideGuardar(){PersonaRolDAO d=mock(PersonaRolDAO.class);PersonaRolModel m=model(d,mock(PersonaDAO.class),mock(RolDAO.class),mock(ClinicaDAO.class));m.nuevo();m.getSeleccionado().setIdPersona(new Persona());m.getSeleccionado().setIdClinica(new Clinica());m.guardar();verifyNoInteractions(d);}
    @Test void clinicaNullImpideGuardar(){PersonaRolDAO d=mock(PersonaRolDAO.class);PersonaRolModel m=model(d,mock(PersonaDAO.class),mock(RolDAO.class),mock(ClinicaDAO.class));m.nuevo();m.getSeleccionado().setIdPersona(new Persona());m.getSeleccionado().setIdRol(new Rol());m.guardar();verifyNoInteractions(d);}
}
