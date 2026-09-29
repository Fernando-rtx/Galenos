package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Verifica que el mapeo JPA de las entidades del dominio coincida con el
 * esquema real documentado en {@code clinica_ppi115_2026_08_20.sql}.
 *
 * <p>Detecta el tipo de discrepancia que rompió Órdenes de examen: una columna
 * mapeada que no existe en la base. El SQL versionado es la única fuente de
 * verdad; si una entidad mapea algo que el esquema no tiene, la prueba falla.</p>
 */
class SchemaMappingTest {

    private static final Path SQL = Path.of("clinica_ppi115_2026_08_20.sql");
    private static final Path ENTIDADES = Path.of(
            "src/main/java/sv/edu/ues/occingenieriappi115_2026/salud/entity");
    private static final Pattern CREATE_TABLE = Pattern.compile(
            "CREATE TABLE public\\.(\\w+) \\((.*?)\\)\\s*;", Pattern.DOTALL);

    @Test
    void cadaTablaMapeadaExisteEnElEsquema() throws Exception {
        Map<String, Set<String>> esquema = leerEsquema();
        for (Class<?> entidad : entidades()) {
            String tabla = nombreTabla(entidad);
            assertTrue(esquema.containsKey(tabla),
                    entidad.getSimpleName() + " mapea la tabla inexistente " + tabla);
        }
    }

    @Test
    void cadaColumnaMapeadaExisteEnSuTabla() throws Exception {
        Map<String, Set<String>> esquema = leerEsquema();
        for (Class<?> entidad : entidades()) {
            Set<String> columnasReales = esquema.get(nombreTabla(entidad));
            for (String columna : columnasMapeadas(entidad)) {
                assertTrue(columnasReales.contains(columna),
                        entidad.getSimpleName() + " mapea la columna inexistente "
                                + nombreTabla(entidad) + "." + columna);
            }
        }
    }

    @Test
    void consultaProcedimientoPasoNoMapeaIdProcedimientoPaso() throws Exception {
        Set<String> columnas = leerEsquema().get("consulta_procedimiento_paso");
        assertFalse(columnas.contains("id_procedimiento_paso"),
                "El esquema real no tiene id_procedimiento_paso en consulta_procedimiento_paso");

        Class<?> entidad = Class.forName(
                "sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso");
        assertFalse(columnasMapeadas(entidad).contains("id_procedimiento_paso"),
                "ConsultaProcedimientoPaso no debe mapear id_procedimiento_paso");
    }

    private Map<String, Set<String>> leerEsquema() throws IOException {
        String sql = Files.readString(SQL, StandardCharsets.UTF_8);
        Map<String, Set<String>> tablas = new LinkedHashMap<>();
        Matcher matcher = CREATE_TABLE.matcher(sql);
        while (matcher.find()) {
            tablas.put(matcher.group(1), columnasDe(matcher.group(2)));
        }
        assertFalse(tablas.isEmpty(), "No se pudo parsear el esquema " + SQL);
        return tablas;
    }

    private Set<String> columnasDe(String cuerpo) {
        Set<String> columnas = new LinkedHashSet<>();
        for (String linea : cuerpo.split("\\R")) {
            String limpia = linea.trim();
            if (limpia.isEmpty()) {
                continue;
            }
            String token = limpia.split("\\s+")[0].replace(",", "");
            String clave = token.toUpperCase();
            if (clave.equals("CONSTRAINT") || clave.equals("PRIMARY")
                    || clave.equals("UNIQUE") || clave.equals("FOREIGN")
                    || clave.equals("CHECK")) {
                continue;
            }
            columnas.add(token);
        }
        return columnas;
    }

    private java.util.List<Class<?>> entidades() throws Exception {
        try (Stream<Path> archivos = Files.list(ENTIDADES)) {
            java.util.List<Class<?>> entidades = new java.util.ArrayList<>();
            for (Path archivo : archivos.toList()) {
                String nombre = archivo.getFileName().toString();
                if (!nombre.endsWith(".java")) {
                    continue;
                }
                String simple = nombre.substring(0, nombre.length() - ".java".length());
                Class<?> tipo = Class.forName(
                        "sv.edu.ues.occingenieriappi115_2026.salud.entity." + simple);
                if (tipo.isAnnotationPresent(Entity.class)) {
                    entidades.add(tipo);
                }
            }
            return entidades;
        }
    }

    private String nombreTabla(Class<?> entidad) {
        Table tabla = entidad.getAnnotation(Table.class);
        return tabla == null ? entidad.getSimpleName().toLowerCase() : tabla.name();
    }

    private Set<String> columnasMapeadas(Class<?> entidad) {
        Set<String> columnas = new LinkedHashSet<>();
        for (Field campo : entidad.getDeclaredFields()) {
            Column columna = campo.getAnnotation(Column.class);
            if (columna != null && !columna.name().isBlank()) {
                columnas.add(columna.name());
            }
            JoinColumn join = campo.getAnnotation(JoinColumn.class);
            if (join != null && !join.name().isBlank()) {
                columnas.add(join.name());
            }
        }
        return columnas;
    }
}
