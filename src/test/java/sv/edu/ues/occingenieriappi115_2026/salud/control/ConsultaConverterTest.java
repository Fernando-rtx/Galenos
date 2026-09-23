package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultaConverterTest {

    private final ConsultaDAO consultaDAO = mock(ConsultaDAO.class);
    private final ConsultaConverter converter = new ConsultaConverter(consultaDAO);
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
        Consulta esperado = new Consulta();
        when(consultaDAO.buscarPorId(id)).thenReturn(esperado);

        Consulta resultado = converter.getAsObject(context, component, id.toString());

        assertEquals(esperado, resultado);
        verify(consultaDAO).buscarPorId(id);
    }

    @Test
    void getAsStringConNullDevuelveVacio() {
        assertEquals("", converter.getAsString(context, component, null));
    }

    @Test
    void getAsStringConEntidadDevuelveUuid() {
        Consulta consulta = new Consulta();
        UUID id = UUID.randomUUID();
        consulta.setIdConsulta(id);

        String resultado = converter.getAsString(context, component, consulta);

        assertEquals(id.toString(), resultado);
    }
}
