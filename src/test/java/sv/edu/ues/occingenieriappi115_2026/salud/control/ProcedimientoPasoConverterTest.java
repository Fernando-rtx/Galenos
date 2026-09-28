package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcedimientoPasoConverterTest {

    @Test
    void getAsObjectNuloOEnBlancoDevuelveNull() {
        ProcedimientoPasoConverter converter = new ProcedimientoPasoConverter(mock(ProcedimientoPasoDAO.class));

        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), null));
        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), "  "));
    }

    @Test
    void getAsObjectBuscaPorUuid() {
        ProcedimientoPasoDAO dao = mock(ProcedimientoPasoDAO.class);
        UUID id = UUID.randomUUID();
        ProcedimientoPaso paso = new ProcedimientoPaso(id);
        when(dao.buscarPorId(id)).thenReturn(paso);
        ProcedimientoPasoConverter converter = new ProcedimientoPasoConverter(dao);

        assertEquals(paso, converter.getAsObject(
                mock(FacesContext.class), mock(UIComponent.class), id.toString()));
    }

    @Test
    void getAsStringNuloDevuelveVacioYObtieneId() {
        ProcedimientoPasoConverter converter = new ProcedimientoPasoConverter(mock(ProcedimientoPasoDAO.class));
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());

        assertEquals("", converter.getAsString(mock(FacesContext.class), mock(UIComponent.class), null));
        assertEquals(paso.getIdProcedimientoPaso().toString(), converter.getAsString(
                mock(FacesContext.class), mock(UIComponent.class), paso));
    }
}
