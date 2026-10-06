package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.junit.jupiter.api.Assertions.*;

/** Contratos de las vistas; la interacción real queda para el ensayo en Safari. */
class AuditoriaVistasTest {
    private Element elemento(String pagina, String tipo, String id) throws Exception {
        var factory = DocumentBuilderFactory.newInstance(); factory.setNamespaceAware(true);
        var doc = factory.newDocumentBuilder().parse(Path.of("src/main/webapp/paginas", pagina + ".xhtml").toFile());
        var xpath = XPathFactory.newInstance().newXPath();
        return (Element) xpath.evaluate("//*[local-name()='" + tipo + "' and @id='" + id + "']", doc, XPathConstants.NODE);
    }

    @Test
    void relacionesDePersonaYExamenSeBloqueanEnEdicion() throws Exception {
        for (String pagina : List.of("Documento", "MedioContacto", "PersonaRol")) {
            Element selector = elemento(pagina, "selectOneMenu", "persona");
            assertNotNull(selector);
            assertTrue(selector.getAttribute("disabled").contains("EDICION"), pagina);
            assertTrue(selector.getAttribute("disabled").contains("conPersonaContexto"), pagina);
        }
        assertTrue(elemento("ExamenTipoExamen", "selectOneMenu", "idExamen").getAttribute("disabled").contains("EDICION"));
        assertTrue(elemento("Procedimiento", "selectOneMenu", "pasoRol").getAttribute("disabled").contains("editando"));
        assertTrue(elemento("Procedimiento", "selectCheckboxMenu", "pasoExamenes").getAttribute("disabled").contains("editando"));
    }

    @Test
    void camposNormalesYEstadoPermanecenEditables() throws Exception {
        assertFalse(elemento("Clinica", "inputText", "tipo").hasAttribute("disabled"));
        assertFalse(elemento("Procedimiento", "inputText", "pasoNombre").hasAttribute("disabled"));
        assertFalse(elemento("Procedimiento", "toggleSwitch", "pasoFinal").hasAttribute("disabled"));
        for (String pagina : List.of("Clinica", "Rol", "TipoDocumento", "TipoMedioContacto", "TipoExamen")) {
            assertFalse(elemento(pagina, "toggleSwitch", "activo").hasAttribute("disabled"), pagina);
        }
        assertFalse(elemento("Documento", "inputText", "valor").hasAttribute("disabled"));
        assertFalse(elemento("MedioContacto", "inputText", "valor").hasAttribute("disabled"));
    }

    @Test
    void mensajesDeListaTienenContenedorEstableYNoRepitenMensajesDeCampo() throws Exception {
        for (String pagina : List.of("Clinica", "Rol", "TipoMedioContacto", "TipoDocumento", "TipoExamen", "Documento",
                "MedioContacto", "PersonaRol", "ExamenTipoExamen", "Procedimiento", "ProcedimientoPaso",
                "ProcedimientoPasoSecuencia", "ProcedimientoPasoExamen", "Persona", "Examen", "Consulta", "ExamenResultado", "OrdenExamen")) {
            assertNotNull(elemento(pagina, "outputPanel", "messages"), pagina);
            Element mensajes = elemento(pagina, "messages", "listaMessages");
            assertEquals("true", mensajes.getAttribute("globalOnly"), pagina);
            assertEquals("false", mensajes.getAttribute("redisplay"), pagina);
            assertTrue(mensajes.getAttribute("rendered").contains("LISTADO"), pagina);
            Element dialogo = elemento(pagina, "messages", pagina.equals("Consulta") ? "consultaMessages" : "dialogMessages");
            assertNotNull(dialogo, pagina);
            assertEquals("true", dialogo.getAttribute("globalOnly"), pagina);
            assertEquals("false", dialogo.getAttribute("redisplay"), pagina);
        }
        assertNotNull(elemento("Procedimiento", "messages", "pasoMessages"));
    }

    @Test
    void mensajesDeCampoMuestranUnaExplicacionEnTodasLasVistas() throws Exception {
        int revisados = 0;
        try (var archivos = Files.walk(Path.of("src/main/webapp"))) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".xhtml")).toList()) {
                if (!Files.readString(archivo).contains("<p:message ")) {
                    continue;
                }
                var factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                var doc = factory.newDocumentBuilder().parse(archivo.toFile());
                NodeList mensajes = (NodeList) XPathFactory.newInstance().newXPath().evaluate(
                        "//*[local-name()='message' and namespace-uri()='primefaces']",
                        doc, XPathConstants.NODESET);
                for (int i = 0; i < mensajes.getLength(); i++) {
                    Element mensaje = (Element) mensajes.item(i);
                    String campo = archivo + " / " + mensaje.getAttribute("for");
                    // p:message oculta el resumen por defecto: desactivar también el detalle deja solo el icono.
                    assertEquals("true", mensaje.getAttribute("showSummary"), campo);
                    assertEquals("false", mensaje.getAttribute("showDetail"), campo);
                    revisados++;
                }
            }
        }
        assertTrue(revisados > 0, "La revisión debe encontrar mensajes de campo");
    }

    @Test
    void guardarProcesaFormularioYNoRestableceValoresSiFalla() throws Exception {
        for (String pagina : List.of("Clinica", "Rol", "TipoMedioContacto", "TipoDocumento", "TipoExamen", "Documento",
                "MedioContacto", "PersonaRol", "ExamenTipoExamen", "ProcedimientoPaso", "ProcedimientoPasoSecuencia", "ProcedimientoPasoExamen", "ExamenResultado", "OrdenExamen")) {
            var factory = DocumentBuilderFactory.newInstance(); factory.setNamespaceAware(true);
            var doc = factory.newDocumentBuilder().parse(Path.of("src/main/webapp/paginas", pagina + ".xhtml").toFile());
            NodeList botones = (NodeList) XPathFactory.newInstance().newXPath().evaluate(
                    "//*[local-name()='botones-bottom']", doc, XPathConstants.NODESET);
            assertEquals(1, botones.getLength(), pagina);
            Element boton = (Element) botones.item(0);
            assertFalse(boton.getAttribute("procesar").isBlank(), pagina);
            assertFalse(boton.hasAttribute("resetValues"), pagina);
            String callback = boton.getAttribute("oncompleteGuardar");
            assertTrue(callback.contains("validationFailed") || callback.contains("guardado === true"), pagina);
            assertTrue(boton.getAttribute("actualizar").contains("Table"), pagina);
            assertFalse(boton.getAttribute("actualizar").contains("edicionDialog"), pagina);
            assertTrue(boton.getAttribute("actualizar").contains("edicionContenido"), pagina);
        }
        var factory = DocumentBuilderFactory.newInstance(); factory.setNamespaceAware(true);
        var doc = factory.newDocumentBuilder().parse(Path.of("src/main/webapp/paginas/Procedimiento.xhtml").toFile());
        Element cierre = (Element) XPathFactory.newInstance().newXPath().evaluate(
                "//*[local-name()='dialog' and @id='pasoDialog']/*[local-name()='ajax' and @event='close']",
                doc, XPathConstants.NODE);
        assertNotNull(cierre);
        // El cierre posterior al éxito no debe borrar el mensaje visible en el diálogo padre.
        assertEquals(":layoutForm:pasoContenido", cierre.getAttribute("update"));
    }
}
