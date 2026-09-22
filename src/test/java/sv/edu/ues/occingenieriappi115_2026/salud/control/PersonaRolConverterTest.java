package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersonaRolConverterTest {

    private final PersonaRolDAO dao = mock(PersonaRolDAO.class);
    private final PersonaRolConverter converter = new PersonaRolConverter(dao);
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
        PersonaRol esperado = new PersonaRol();
        when(dao.buscarPorId(id)).thenReturn(esperado);

        PersonaRol resultado = converter.getAsObject(context, component, id.toString());

        assertEquals(esperado, resultado);
        verify(dao).buscarPorId(id);
    }

    @Test
    void getAsStringConNullDevuelveVacio() {
        assertEquals("", converter.getAsString(context, component, null));
    }

    @Test
    void getAsStringConEntidadDevuelveUuid() {
        PersonaRol entidad = new PersonaRol();
        UUID id = UUID.randomUUID();
        entidad.setIdPersonaRol(id);

        String resultado = converter.getAsString(context, component, entidad);

        assertEquals(id.toString(), resultado);
    }
}
