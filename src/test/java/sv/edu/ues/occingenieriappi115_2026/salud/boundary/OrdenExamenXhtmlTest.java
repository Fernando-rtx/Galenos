package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.junit.jupiter.api.Assertions.*;

class OrdenExamenXhtmlTest {
    private Document pagina() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(Path.of("src/main/webapp/paginas/OrdenExamen.xhtml").toFile());
    }

    private Element buscar(Document pagina, String expresion) throws Exception {
        return (Element) XPathFactory.newInstance().newXPath().evaluate(expresion, pagina, XPathConstants.NODE);
    }

    @Test
    void seleccionarPasoEsObligatorioYErrorApareceJuntoAlSelector() throws Exception {
        Document pagina = pagina();
        Element selector = buscar(pagina, "//*[@id='consultaProcedimientoPaso']");
        assertEquals("true", selector.getAttribute("required"));
        assertEquals("#{msg['ordenExamen.pasoRequerido']}", selector.getAttribute("requiredMessage"));
        Element mensaje = buscar(pagina, "//*[@id='consultaProcedimientoPaso']/following-sibling::*[local-name()='message']");
        assertNotNull(mensaje);
        assertEquals("consultaProcedimientoPaso", mensaje.getAttribute("for"));
        assertEquals("true", mensaje.getAttribute("showSummary"));
        assertEquals("false", mensaje.getAttribute("showDetail"));
        Element fecha = buscar(pagina, "//*[@id='fechaCreacion']");
        assertEquals("#{msg['ordenExamen.fechaRequerida']}", fecha.getAttribute("requiredMessage"));
        assertEquals("#{msg['ordenExamen.fechaInvalida']}", fecha.getAttribute("converterMessage"));
        assertNotNull(buscar(pagina, "//*[local-name()='message' and @for='fechaCreacion']"));
        assertFalse(buscar(pagina, "//*[@id='indicaciones']").hasAttribute("required"));
    }

    @Test
    void errorNoCierraElDialogoNiSeRepiteEnElListado() throws Exception {
        Document pagina = pagina();
        Element botones = buscar(pagina, "//*[local-name()='botones-bottom']");
        assertEquals("@this :layoutForm:edicionContenido", botones.getAttribute("procesar"));
        assertEquals(":layoutForm:messages :layoutForm:ordenExamenTable :layoutForm:edicionContenido",
                botones.getAttribute("actualizar"));
        assertEquals("if (!args.validationFailed) PF('ordenExamenDialog').hide()",
                botones.getAttribute("oncompleteGuardar"));
        assertFalse(botones.hasAttribute("resetValues"));
        Element mensajes = buscar(pagina, "//*[@id='messages']/*[local-name()='messages']");
        assertEquals("true", mensajes.getAttribute("globalOnly"));
        assertEquals("false", mensajes.getAttribute("redisplay"));
        assertEquals("#{ordenExamenModel.estado eq 'LISTADO'}", mensajes.getAttribute("rendered"));
        Element seleccion = buscar(pagina, "//*[local-name()='ajax' and @event='rowDblselect']");
        assertEquals(":layoutForm:messages :layoutForm:edicionDialog", seleccion.getAttribute("update"));
        assertEquals("true", seleccion.getAttribute("resetValues"));
    }
}
