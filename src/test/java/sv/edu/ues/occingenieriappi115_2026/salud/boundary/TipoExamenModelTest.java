package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TipoExamenModelTest {

    @Test
    public void testDeclaraNamedYViewScoped() {
        assertTrue(TipoExamenModel.class.isAnnotationPresent(Named.class));
        assertTrue(TipoExamenModel.class.isAnnotationPresent(ViewScoped.class));
    }

    @Test
    public void testGetDAORetornaDAOInyectado()
            throws Exception {
        TipoExamenModel cut = new TipoExamenModel();
        TipoExamenDAO dao = Mockito.mock(TipoExamenDAO.class);

        Field daoField = TipoExamenModel.class.getDeclaredField("tipoExamenDAO");
        daoField.setAccessible(true);
        daoField.set(cut, dao);

        assertSame(dao, cut.getDAO());
    }

    @Test
    public void testInstanciaRegistroGeneraUUID() {
        TipoExamenModel cut = new TipoExamenModel();

        TipoExamen registro = cut.instanciaRegistro();

        assertNotNull(registro);
        assertNotNull(registro.getIdTipoExamen());
    }

    @Test
    public void testGetIdByRegistroRetornaIdTipoExamen() {
        TipoExamenModel cut = new TipoExamenModel();
        UUID id = UUID.randomUUID();
        TipoExamen registro = new TipoExamen(id);

        assertEquals(id, cut.getIdByRegistro(registro));
    }

    @Test
    public void testGetIdByRowKeyConvierteUUID() {
        TipoExamenModel cut = new TipoExamenModel();
        UUID id = UUID.randomUUID();

        assertEquals(id, cut.getIdByRowKey(id.toString()));
    }

    @Test
    public void testGetIdByRowKeyInvalido() {
        TipoExamenModel cut = new TipoExamenModel();

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.getIdByRowKey("uuid-invalido")
        );
    }

    @Test
    public void testGetRowDataUsaDAOFindById()
            throws Exception {
        TipoExamenModel cut = new TipoExamenModel();
        TipoExamenDAO dao = Mockito.mock(TipoExamenDAO.class);
        UUID id = UUID.randomUUID();
        TipoExamen esperado = new TipoExamen(id);

        Field daoField = TipoExamenModel.class.getDeclaredField("tipoExamenDAO");
        daoField.setAccessible(true);
        daoField.set(cut, dao);

        Mockito.when(dao.findById(id)).thenReturn(esperado);

        assertSame(esperado, cut.getRowData(id.toString()));
        Mockito.verify(dao).findById(id);
    }

    @Test
    public void testNombreModelo() {
        TipoExamenModel cut = new TipoExamenModel();

        assertEquals("Tipo de examen", cut.getNombreModelo());
    }
}
