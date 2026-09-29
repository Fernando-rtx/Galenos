package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import sv.edu.ues.occingenieriappi115_2026.salud.model.ClinicaModel;
import sv.edu.ues.occingenieriappi115_2026.salud.model.ProcedimientoModel;
import sv.edu.ues.occingenieriappi115_2026.salud.model.ProcedimientoPasoExamenModel;
import sv.edu.ues.occingenieriappi115_2026.salud.model.ProcedimientoPasoModel;
import sv.edu.ues.occingenieriappi115_2026.salud.model.ProcedimientoPasoSecuenciaModel;
import sv.edu.ues.occingenieriappi115_2026.salud.model.RolModel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginasXhtmlTest {

    private static final Map<String, Class<?>> BEANS = Map.of(
            "procedimientoModel", ProcedimientoModel.class,
            "clinicaModel", ClinicaModel.class,
            "rolModel", RolModel.class,
            "procedimientoPasoModel", ProcedimientoPasoModel.class,
            "procedimientoPasoSecuenciaModel", ProcedimientoPasoSecuenciaModel.class,
            "procedimientoPasoExamenModel", ProcedimientoPasoExamenModel.class);

    private static final List<String> PAGINAS = List.of(
            "Procedimiento.xhtml", "Clinica.xhtml", "Rol.xhtml",
            "ProcedimientoPaso.xhtml", "ProcedimientoPasoSecuencia.xhtml",
            "ProcedimientoPasoExamen.xhtml");

    private static final Pattern EXPRESION = Pattern.compile("#\\{([^}]+)\\}");

    private int expresionesValidadas;

    @Test
    void lasVistasReferencianSoloPropiedadesYMetodosQueExisten() throws IOException {
        for (String pagina : PAGINAS) {
            String texto = Files.readString(Path.of("src/main/webapp/paginas", pagina));
            Matcher matcher = EXPRESION.matcher(texto);
            while (matcher.find()) {
                validar(pagina, matcher.group(1));
            }
        }
        assertTrue(expresionesValidadas > 50,
                () -> "solo se validaron " + expresionesValidadas + " expresiones");
    }

    private void validar(String pagina, String expresion) {
        for (String parte : expresion.split("\\s+|\\?|:")) {
            String segmento = parte.trim();
            if (segmento.isEmpty()) {
                continue;
            }
            String raiz = raiz(segmento);
            if (!raiz.endsWith("Model")) {
                continue;
            }
            Class<?> tipo = BEANS.get(raiz);
            assertNotNull(tipo, () -> pagina + ": bean inexistente " + raiz
                    + " en #{" + expresion.trim() + "}");
            expresionesValidadas++;
            recorrer(pagina, segmento.substring(raiz.length()), tipo, expresion);
        }
    }

    private void recorrer(String pagina, String cadena, Class<?> tipo, String expresion) {
        while (!cadena.isEmpty()) {
            if (cadena.startsWith(".")) {
                cadena = cadena.substring(1);
            } else if (cadena.startsWith("[")) {
                return;
            }
            int fin = 0;
            while (fin < cadena.length()
                    && (Character.isJavaIdentifierPart(cadena.charAt(fin)))) {
                fin++;
            }
            String token = cadena.substring(0, fin);
            if (token.isEmpty()) {
                return;
            }
            if (fin < cadena.length() && cadena.charAt(fin) == '(') {
                int cierre = fin;
                int anidadas = 0;
                while (cierre < cadena.length()) {
                    if (cadena.charAt(cierre) == '(') {
                        anidadas++;
                    } else if (cadena.charAt(cierre) == ')') {
                        anidadas--;
                        if (anidadas == 0) {
                            break;
                        }
                    }
                    cierre++;
                }
                for (String argumento : cadena.substring(fin + 1, cierre).split(",")) {
                    validar(pagina, argumento);
                }
                Method metodo = metodo(tipo, token);
                assertNotNull(metodo, pagina + ": no existe " + tipo.getSimpleName()
                        + "." + token + "(...) en #{" + expresion.trim() + "}");
                tipo = metodo.getReturnType();
                cadena = cadena.substring(cierre + 1);
            } else {
                Method getter = getter(tipo, token);
                assertNotNull(getter, pagina + ": no existe "
                        + tipo.getSimpleName() + "." + token + " en #{"
                        + expresion.trim() + "}");
                tipo = getter.getReturnType();
                cadena = cadena.substring(fin);
            }
        }
    }

    private String raiz(String segmento) {
        if (segmento.isEmpty() || !Character.isJavaIdentifierStart(segmento.charAt(0))) {
            return "";
        }
        int fin = 0;
        while (fin < segmento.length()
                && Character.isJavaIdentifierPart(segmento.charAt(fin))) {
            fin++;
        }
        return segmento.substring(0, fin);
    }

    private Method metodo(Class<?> tipo, String nombre) {
        return Arrays.stream(tipo.getMethods())
                .filter(m -> m.getName().equals(nombre)
                        && m.getDeclaringClass() != Object.class)
                .findFirst()
                .orElse(null);
    }

    private Method getter(Class<?> tipo, String propiedad) {
        String inicial = Character.toUpperCase(propiedad.charAt(0)) + propiedad.substring(1);
        return Arrays.stream(tipo.getMethods())
                .filter(m -> m.getParameterCount() == 0
                        && m.getDeclaringClass() != Object.class
                        && (m.getName().equals("get" + inicial)
                                || m.getName().equals("is" + inicial)))
                .findFirst()
                .orElse(null);
    }
}
