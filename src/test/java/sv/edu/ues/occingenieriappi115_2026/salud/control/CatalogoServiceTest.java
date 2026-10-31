package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.PersistenceException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogoServiceTest {
    @Test void nombreInvalidoNoConsultaNiEscribe() {
        var dao = mock(TipoDocumentoDAO.class);
        var service = new CatalogoService(dao);
        for (String nombre : new String[]{null,"","  ","x".repeat(156)}) {
            var datos = new TipoDocumento();datos.setNombre(nombre);
            var ex = assertThrows(ReglaNegocioException.class, () -> service.guardarTipoDocumento(datos,false));
            assertEquals(ReglaNegocioException.Motivo.VALIDACION,ex.getMotivo());
            assertEquals("nombre",ex.getCampo());
        }
        verifyNoInteractions(dao);
    }
    @Test void duplicadoExcluyeElRegistroActualYNoPersiste() {
        var dao = mock(TipoDocumentoDAO.class);var service = new CatalogoService(dao);
        var datos = new TipoDocumento(UUID.randomUUID());datos.setNombre(" Documento ");
        when(dao.existeNombreNormalizado(" Documento ",datos.getIdTipoDocumento())).thenReturn(true);
        var ex = assertThrows(ReglaNegocioException.class, () -> service.guardarTipoDocumento(datos,true));
        assertEquals(ReglaNegocioException.Motivo.DUPLICADO,ex.getMotivo());
        verify(dao,never()).actualizar(any());
        assertEquals(" Documento ",datos.getNombre());
    }
    @Test void regexInvalidaPreservaLaCausaYNoEscribe() {
        var dao = mock(TipoDocumentoDAO.class);var service = new CatalogoService(dao);
        var datos = new TipoDocumento();datos.setNombre("Documento");datos.setExpresionRegular("[");
        var ex = assertThrows(ReglaNegocioException.class, () -> service.guardarTipoDocumento(datos,false));
        assertInstanceOf(java.util.regex.PatternSyntaxException.class,ex.getCause());
        verify(dao,never()).guardar(any());
    }
    @Test void normalizaYPersisteSinInventarEstadoNiEliminarRelaciones() {
        var dao = mock(TipoDocumentoDAO.class);var service = new CatalogoService(dao);
        var datos = new TipoDocumento();datos.setNombre(" Documento ");datos.setActivo(false);
        when(dao.guardar(datos)).thenReturn(datos);
        assertSame(datos,service.guardarTipoDocumento(datos,false));
        assertEquals("Documento",datos.getNombre());assertFalse(datos.getActivo());
        verify(dao).guardar(datos);
    }
    @Test void falloDeJpaNoSeTransformaEnValidacion() {
        var dao = mock(TipoDocumentoDAO.class);var service = new CatalogoService(dao);
        var datos = new TipoDocumento();datos.setNombre("Documento");
        var causa = new PersistenceException("Fallo sintetico de conexion");
        when(dao.guardar(datos)).thenThrow(causa);
        assertSame(causa,assertThrows(PersistenceException.class, () -> service.guardarTipoDocumento(datos,false)));
    }
}
