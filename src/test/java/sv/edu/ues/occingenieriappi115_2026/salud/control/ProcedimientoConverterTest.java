package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcedimientoConverterTest {

    @Test
    void getAsObjectNuloOEnBlancoDevuelveNull() {
        ProcedimientoConverter converter = new ProcedimientoConverter(mock(ProcedimientoDAO.class));

        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), null));
        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), "  "));
    }

    @Test
    void getAsObjectBuscaPorUuid() {
        ProcedimientoDAO dao = mock(ProcedimientoDAO.class);
        UUID id = UUID.randomUUID();
        Procedimiento procedimiento = new Procedimiento(id);
        when(dao.buscarPorId(id)).thenReturn(procedimiento);
        ProcedimientoConverter converter = new ProcedimientoConverter(dao);

        assertEquals(procedimiento, converter.getAsObject(
                mock(FacesContext.class), mock(UIComponent.class), id.toString()));
    }

    @Test
    void getAsStringNuloDevuelveVacioYObtieneId() {
        ProcedimientoConverter converter = new ProcedimientoConverter(mock(ProcedimientoDAO.class));
        Procedimiento procedimiento = new Procedimiento(UUID.randomUUID());

        assertEquals("", converter.getAsString(mock(FacesContext.class), mock(UIComponent.class), null));
        assertEquals(procedimiento.getIdProcedimiento().toString(), converter.getAsString(
                mock(FacesContext.class), mock(UIComponent.class), procedimiento));
    }
}
