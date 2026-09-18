package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentoModelTest {
    private DocumentoModel model(DocumentoDAO d, PersonaDAO p, TipoDocumentoDAO t) { return new DocumentoModel(d,p,t); }
    @Test void nuevoCreaDocumento() { DocumentoModel m=model(mock(DocumentoDAO.class),mock(PersonaDAO.class),mock(TipoDocumentoDAO.class));m.nuevo();assertNotNull(m.getSeleccionado());assertEquals(ESTADO_CRUD.CREACION,m.getEstado()); }
    @Test void seleccionarYCancelar() { DocumentoModel m=model(mock(DocumentoDAO.class),mock(PersonaDAO.class),mock(TipoDocumentoDAO.class));Documento d=new Documento();m.seleccionar(d);assertSame(d,m.getSeleccionado());m.cancelar();assertNull(m.getSeleccionado()); }
    @Test void guardarCreacionYEdicion() { DocumentoDAO d=mock(DocumentoDAO.class);DocumentoModel m=model(d,mock(PersonaDAO.class),mock(TipoDocumentoDAO.class));m.nuevo();Documento nuevo=m.getSeleccionado();nuevo.setIdPersona(new Persona());nuevo.setIdTipoDocumento(new TipoDocumento());nuevo.setValor("ABC");m.guardar();verify(d).guardar(nuevo);Documento actualizado=new Documento();when(d.actualizar(nuevo)).thenReturn(actualizado);m.seleccionar(nuevo);m.guardar();assertSame(actualizado,m.getSeleccionado()); }
    @Test void asignaRelacionesPorUuid() { PersonaDAO p=mock(PersonaDAO.class);TipoDocumentoDAO t=mock(TipoDocumentoDAO.class);Persona persona=new Persona(UUID.randomUUID());TipoDocumento tipo=new TipoDocumento(UUID.randomUUID());when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);when(t.buscarPorId(tipo.getIdTipoDocumento())).thenReturn(tipo);DocumentoModel m=model(mock(DocumentoDAO.class),p,t);m.nuevo();m.setPersonaSeleccionadaId(persona.getIdPersona().toString());m.setTipoDocumentoSeleccionadoId(tipo.getIdTipoDocumento().toString());assertSame(persona,m.getSeleccionado().getIdPersona());assertSame(tipo,m.getSeleccionado().getIdTipoDocumento()); }
    @Test void contextoPreseleccionaPersona() { PersonaDAO p=mock(PersonaDAO.class);Persona persona=new Persona(UUID.randomUUID());when(p.buscarPorId(persona.getIdPersona())).thenReturn(persona);DocumentoModel m=model(mock(DocumentoDAO.class),p,mock(TipoDocumentoDAO.class));m.setIdPersonaContexto(persona.getIdPersona().toString());m.nuevo();assertSame(persona,m.getSeleccionado().getIdPersona());assertTrue(m.isConPersonaContexto()); }
    @Test void relacionesObligatoriasImpidenGuardar() { DocumentoDAO d=mock(DocumentoDAO.class);DocumentoModel m=model(d,mock(PersonaDAO.class),mock(TipoDocumentoDAO.class));m.nuevo();m.getSeleccionado().setValor("ABC");m.guardar();verifyNoInteractions(d); }
    @Test void valorVacioImpideGuardar() { DocumentoDAO d=mock(DocumentoDAO.class);DocumentoModel m=model(d,mock(PersonaDAO.class),mock(TipoDocumentoDAO.class));m.nuevo();m.getSeleccionado().setIdPersona(new Persona());m.getSeleccionado().setIdTipoDocumento(new TipoDocumento());m.getSeleccionado().setValor("   ");m.guardar();verifyNoInteractions(d); }
    @Test void expresionRegularDelTipoRechazaValorInvalido() { DocumentoDAO d=mock(DocumentoDAO.class);DocumentoModel m=model(d,mock(PersonaDAO.class),mock(TipoDocumentoDAO.class));TipoDocumento tipo=new TipoDocumento();tipo.setExpresionRegular("[0-9]{4}");m.nuevo();m.getSeleccionado().setIdPersona(new Persona());m.getSeleccionado().setIdTipoDocumento(tipo);m.getSeleccionado().setValor("ABC");m.guardar();verifyNoInteractions(d); }
}
