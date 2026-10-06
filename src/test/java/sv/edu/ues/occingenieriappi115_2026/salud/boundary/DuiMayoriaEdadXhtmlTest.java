package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Element;
import static org.junit.jupiter.api.Assertions.*;

class DuiMayoriaEdadXhtmlTest {
    @ParameterizedTest
    @ValueSource(strings = {"Persona", "Documento"})
    void comboValidaEdadPorAjaxSinProcesarOBorrarLosOtrosCampos(String pagina) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        var doc = factory.newDocumentBuilder().parse(Path.of("src/main/webapp/paginas", pagina + ".xhtml").toFile());
        var xpath = XPathFactory.newInstance().newXPath();
        Element combo = (Element) xpath.evaluate("//*[local-name()='selectOneMenu' and @id='tipo']", doc, XPathConstants.NODE);
        assertNotNull(combo);
        assertTrue(combo.getAttribute("disabled").contains("EDICION"));
        Element ajax = (Element) xpath.evaluate("*[local-name()='ajax']", combo, XPathConstants.NODE);
        assertNotNull(ajax);
        assertEquals("#{documentoModel.validarEdadDuiSeleccionado}", ajax.getAttribute("listener"));
        assertEquals("@this", ajax.getAttribute("process"));
        assertEquals("@this tipoMessage", ajax.getAttribute("update"));
        assertFalse(ajax.hasAttribute("resetValues"));
        Element mensaje = (Element) xpath.evaluate("//*[local-name()='message' and @id='tipoMessage']", doc, XPathConstants.NODE);
        assertNotNull(mensaje);
        assertEquals("tipo", mensaje.getAttribute("for"));
        assertEquals("true", mensaje.getAttribute("showSummary"));
        assertEquals("false", mensaje.getAttribute("showDetail"));
        if (pagina.equals("Persona")) {
            Element fecha = (Element) xpath.evaluate("//*[@id='fechaNacimiento']", doc, XPathConstants.NODE);
            assertEquals("America/El_Salvador", fecha.getAttribute("timeZone"));
            Element conversion = (Element) xpath.evaluate("//*[local-name()='convertDateTime' and @pattern='dd/MM/yyyy']",
                    doc, XPathConstants.NODE);
            assertEquals("America/El_Salvador", conversion.getAttribute("timeZone"));
        } else {
            Element persona = (Element) xpath.evaluate("//*[@id='persona']/*[local-name()='ajax']", doc, XPathConstants.NODE);
            assertNotNull(persona);
            assertEquals("#{documentoModel.validarEdadDuiSeleccionado}", persona.getAttribute("listener"));
            assertEquals("@this", persona.getAttribute("process"));
            assertEquals("tipo tipoMessage", persona.getAttribute("update"));
        }
    }
}
