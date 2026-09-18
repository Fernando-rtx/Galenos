package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedioContactoModelTest {
    private MedioContactoModel model(MedioContactoDAO d,PersonaDAO p,TipoMedioContactoDAO t){return new MedioContactoModel(d,p,t);}
    @Test void nuevoInicializaFecha(){MedioContactoModel m=model(mock(MedioContactoDAO.class),mock(PersonaDAO.class),mock(TipoMedioContactoDAO.class));m.nuevo();assertNotNull(m.getSeleccionado().getFechaCreacion());assertEquals(ESTADO_CRUD.CREACION,m.getEstado());}
    @Test void seleccionarYCancelar(){MedioContactoModel m=model(mock(MedioContactoDAO.class),mock(PersonaDAO.class),mock(TipoMedioContactoDAO.class));MedioContacto c=new MedioContacto();m.seleccionar(c);assertSame(c,m.getSeleccionado());m.cancelar();assertNull(m.getSeleccionado());}
    @Test void guardarCreacionYEdicion(){MedioContactoDAO d=mock(MedioContactoDAO.class);MedioContactoModel m=model(d,mock(PersonaDAO.class),mock(TipoMedioContactoDAO.class));m.nuevo();MedioContacto n=m.getSeleccionado();n.setIdPersona(new Persona());n.setIdTipoMedioContacto(new TipoMedioContacto());n.setValor("contacto");m.guardar();verify(d).guardar(n);MedioContacto a=new MedioContacto();when(d.actualizar(n)).thenReturn(a);m.seleccionar(n);m.guardar();assertSame(a,m.getSeleccionado());}
    @Test void asignaRelaciones(){PersonaDAO p=mock(PersonaDAO.class);TipoMedioContactoDAO t=mock(TipoMedioContactoDAO.class);Persona persona=new Persona(UUID.randomUUID());TipoMedioContacto tipo=new TipoMedioContacto(UUID.randomUUID());when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);when(t.buscarPorId(tipo.getIdTipoMedioContacto())).thenReturn(tipo);MedioContactoModel m=model(mock(MedioContactoDAO.class),p,t);m.nuevo();m.setPersonaSeleccionadaId(persona.getIdPersona().toString());m.setTipoMedioSeleccionadoId(tipo.getIdTipoMedioContacto().toString());assertSame(persona,m.getSeleccionado().getIdPersona());assertSame(tipo,m.getSeleccionado().getIdTipoMedioContacto());}
    @Test void contextoPreselecciona(){PersonaDAO p=mock(PersonaDAO.class);Persona persona=new Persona(UUID.randomUUID());when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);MedioContactoModel m=model(mock(MedioContactoDAO.class),p,mock(TipoMedioContactoDAO.class));m.setIdPersonaContexto(persona.getIdPersona().toString());m.nuevo();assertSame(persona,m.getSeleccionado().getIdPersona());}
    @Test void relacionesObligatoriasImpidenGuardar(){MedioContactoDAO d=mock(MedioContactoDAO.class);MedioContactoModel m=model(d,mock(PersonaDAO.class),mock(TipoMedioContactoDAO.class));m.nuevo();m.getSeleccionado().setValor("contacto");m.guardar();verifyNoInteractions(d);}
    @Test void valorVacioImpideGuardar(){MedioContactoDAO d=mock(MedioContactoDAO.class);MedioContactoModel m=model(d,mock(PersonaDAO.class),mock(TipoMedioContactoDAO.class));m.nuevo();m.getSeleccionado().setIdPersona(new Persona());m.getSeleccionado().setIdTipoMedioContacto(new TipoMedioContacto());m.getSeleccionado().setValor("   ");m.guardar();verifyNoInteractions(d);}
    @Test void expresionRegularDelTipoRechazaValorInvalido(){MedioContactoDAO d=mock(MedioContactoDAO.class);MedioContactoModel m=model(d,mock(PersonaDAO.class),mock(TipoMedioContactoDAO.class));TipoMedioContacto tipo=new TipoMedioContacto();tipo.setExpresionRegular("[0-9]{8}");m.nuevo();m.getSeleccionado().setIdPersona(new Persona());m.getSeleccionado().setIdTipoMedioContacto(tipo);m.getSeleccionado().setValor("correo@example.com");m.guardar();verifyNoInteractions(d);}
}
