package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class TraduccionesTest {
    private Properties cargar(String sufijo) throws IOException {
        Properties resultado = new Properties();
        try (var reader = Files.newBufferedReader(
                Path.of("src/main/resources/i18n/messages" + sufijo + ".properties"),
                StandardCharsets.UTF_8)) {
            resultado.load(reader);
        }
        return resultado;
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "en", "de", "zh_CN"})
    void cadaIdiomaTieneTodasLasClavesSinDependerDelEspanol(String idioma) throws IOException {
        var base = cargar("");
        var traducciones = cargar("_" + idioma);
        var faltantes = new TreeSet<>(base.stringPropertyNames());
        faltantes.removeAll(traducciones.stringPropertyNames());
        assertTrue(faltantes.isEmpty(), () -> idioma + ": " + faltantes);
        traducciones.forEach((clave, valor) ->
                assertFalse(valor.toString().isBlank(), () -> idioma + ": " + clave));
    }

    @Test
    void todasLasClavesLiteralesDeLasVistasExisten() throws IOException {
        var base = cargar("");
        var patron = Pattern.compile("msg\\['([^']+)'\\]");
        try (var archivos = Files.walk(Path.of("src/main/webapp"))) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".xhtml")).toList()) {
                var matcher = patron.matcher(Files.readString(archivo));
                while (matcher.find()) {
                    String clave = matcher.group(1);
                    assertTrue(base.containsKey(clave), () -> archivo + ": " + clave);
                }
            }
        }
    }

    @Test
    void plantillaAplicaIdiomaDeSesionYRefrescaPaginaCompleta() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        var doc = factory.newDocumentBuilder().parse(
                Path.of("src/main/webapp/WEB-INF/plantillas/general.xhtml").toFile());
        var views = doc.getElementsByTagNameNS("jakarta.faces.core", "view");
        assertEquals(1, views.getLength());
        assertEquals("#{idiomaBean.locale}", views.item(0).getAttributes().getNamedItem("locale").getNodeValue());
        var ajax = doc.getElementsByTagNameNS("primefaces", "ajax").item(0).getAttributes();
        assertEquals("@this", ajax.getNamedItem("process").getNodeValue());
        assertEquals("@all", ajax.getNamedItem("update").getNodeValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "en", "de", "zh_CN"})
    void idiomaElegidoSeConservaEnSesionSerializable(String idioma) throws Exception {
        var bean = new IdiomaBean();
        bean.setIdioma(idioma);
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(bean);
        }
        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            var recuperado = (IdiomaBean) in.readObject();
            assertEquals(idioma, recuperado.getIdioma());
            assertEquals(bean.getLocale(), recuperado.getLocale());
        }
    }
}
