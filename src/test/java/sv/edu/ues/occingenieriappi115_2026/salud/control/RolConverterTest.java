package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RolConverterTest {

    @Test
    void getAsObjectNuloOEnBlancoDevuelveNull() {
        RolConverter converter = new RolConverter(mock(RolDAO.class));

        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), null));
        assertNull(converter.getAsObject(mock(FacesContext.class), mock(UIComponent.class), "  "));
    }

    @Test
    void getAsObjectBuscaPorUuid() {
        RolDAO dao = mock(RolDAO.class);
        UUID id = UUID.randomUUID();
        Rol rol = new Rol(id);
        when(dao.buscarPorId(id)).thenReturn(rol);
        RolConverter converter = new RolConverter(dao);

        assertEquals(rol, converter.getAsObject(
                mock(FacesContext.class), mock(UIComponent.class), id.toString()));
    }

    @Test
    void getAsStringNuloDevuelveVacioYObtieneId() {
        RolConverter converter = new RolConverter(mock(RolDAO.class));
        Rol rol = new Rol(UUID.randomUUID());

        assertEquals("", converter.getAsString(mock(FacesContext.class), mock(UIComponent.class), null));
        assertEquals(rol.getIdRol().toString(), converter.getAsString(
                mock(FacesContext.class), mock(UIComponent.class), rol));
    }
}
