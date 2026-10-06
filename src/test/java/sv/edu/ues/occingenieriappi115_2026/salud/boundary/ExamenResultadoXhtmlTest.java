package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.junit.jupiter.api.Assertions.*;

class ExamenResultadoXhtmlTest {
    private Document pagina() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(Path.of("src/main/webapp/paginas/ExamenResultado.xhtml").toFile());
    }

    private Element buscar(Document pagina, String xpath) throws Exception {
        return (Element) XPathFactory.newInstance().newXPath().evaluate(xpath, pagina, XPathConstants.NODE);
    }

    @Test
    void erroresSeMuestranJuntoACadaCampoDentroDelFormulario() throws Exception {
        Document pagina = pagina();
        for (String campo : List.of("ordenExamen", "fechaCreacion", "resultado", "interpretacion", "rutaAtestado")) {
            Element mensaje = buscar(pagina, "//*[local-name()='outputPanel' and @id='edicionContenido']"
                    + "//*[local-name()='message' and @for='" + campo + "']");
            assertNotNull(mensaje, campo);
            assertEquals("true", mensaje.getAttribute("showSummary"), campo);
            assertEquals("false", mensaje.getAttribute("showDetail"), campo);
        }
        Element orden = buscar(pagina, "//*[@id='ordenExamen']");
        assertEquals("true", orden.getAttribute("required"));
        assertEquals("#{msg['examenResultado.ordenRequerida']}", orden.getAttribute("requiredMessage"));
        Element fecha = buscar(pagina, "//*[@id='fechaCreacion']");
        assertEquals("true", fecha.getAttribute("required"));
        assertEquals("#{msg['examenResultado.fechaRequerida']}", fecha.getAttribute("requiredMessage"));
        assertEquals("#{msg['examenResultado.fechaInvalida']}", fecha.getAttribute("converterMessage"));
        for (String campo : List.of("resultado", "interpretacion", "rutaAtestado")) {
            // Estos valores son opcionales en el formulario y en la entidad existente.
            assertFalse(buscar(pagina, "//*[@id='" + campo + "']").hasAttribute("required"), campo);
        }
    }

    @Test
    void guardarConErrorConservaDialogoYValoresYActualizaMensajes() throws Exception {
        Document pagina = pagina();
        Element guardar = buscar(pagina, "//*[local-name()='botones-bottom']");
        assertEquals("@this :layoutForm:edicionContenido", guardar.getAttribute("procesar"));
        assertEquals(":layoutForm:messages :layoutForm:examenResultadoTable :layoutForm:edicionContenido",
                guardar.getAttribute("actualizar"));
        assertFalse(guardar.hasAttribute("resetValues"));
        assertEquals("if (!args.validationFailed) PF('examenResultadoDialog').hide()",
                guardar.getAttribute("oncompleteGuardar"));
        Element seleccion = buscar(pagina, "//*[local-name()='ajax' and @event='rowDblselect']");
        assertEquals(":layoutForm:messages :layoutForm:edicionDialog", seleccion.getAttribute("update"));
        assertEquals("true", seleccion.getAttribute("resetValues"));
    }

    @Test
    void listadoNoRepiteErroresDelFormulario() throws Exception {
        Document pagina = pagina();
        Element listado = buscar(pagina, "//*[@id='messages']/*[local-name()='messages']");
        assertNotNull(listado);
        assertEquals("true", listado.getAttribute("globalOnly"));
        assertEquals("false", listado.getAttribute("redisplay"));
        assertEquals("#{examenResultadoModel.estado eq 'LISTADO'}", listado.getAttribute("rendered"));
        Element dialogo = buscar(pagina, "//*[@id='edicionContenido']/*[local-name()='messages']");
        assertNotNull(dialogo);
        assertEquals("true", dialogo.getAttribute("globalOnly"));
        assertEquals("false", dialogo.getAttribute("redisplay"));
    }
}
