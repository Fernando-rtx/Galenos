package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrdenExamenConverterTest {

    private final OrdenExamenDAO dao = mock(OrdenExamenDAO.class);
    private final OrdenExamenConverter converter = new OrdenExamenConverter(dao);
    private final FacesContext context = mock(FacesContext.class);
    private final UIComponent component = mock(UIComponent.class);

    @Test
    void getAsObjectConNullDevuelveNull() {
        assertNull(converter.getAsObject(context, component, null));
    }

    @Test
    void getAsObjectConBlankDevuelveNull() {
        assertNull(converter.getAsObject(context, component, "  "));
    }

    @Test
    void getAsObjectConUuidValidoBuscaEnDao() {
        UUID id = UUID.randomUUID();
        OrdenExamen esperado = new OrdenExamen();
        when(dao.buscarPorId(id)).thenReturn(esperado);

        OrdenExamen resultado = converter.getAsObject(context, component, id.toString());

        assertEquals(esperado, resultado);
        verify(dao).buscarPorId(id);
    }

    @Test
    void getAsStringConNullDevuelveVacio() {
        assertEquals("", converter.getAsString(context, component, null));
    }

    @Test
    void getAsStringConEntidadDevuelveUuid() {
        OrdenExamen entidad = new OrdenExamen();
        UUID id = UUID.randomUUID();
        entidad.setIdOrdenExamen(id);

        String resultado = converter.getAsString(context, component, entidad);

        assertEquals(id.toString(), resultado);
    }
}
