package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TicketPdfTest {
    @Test void generaUnPdfConTicketSintetico() {
        byte[] pdf = TicketPdf.generate(List.of("GalenosSV", "Cita sintética"));
        String header = new String(pdf, 0, 8, StandardCharsets.ISO_8859_1);
        String contents = new String(pdf, StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-1.4", header);
        assertTrue(contents.contains("/Type /Catalog"));
        assertTrue(contents.contains("%%EOF"));
    }

    @Test void textoEscapadoYTablaDeReferenciasApuntanAObjetosReales() {
        byte[] pdf = TicketPdf.generate(List.of("Clínica (norte) \\ sintética"));
        String contents = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(contents.contains("(Clínica \\(norte\\) \\\\ sintética) Tj"));
        int marker = contents.lastIndexOf("startxref\n") + "startxref\n".length();
        int xref = Integer.parseInt(contents.substring(marker, contents.indexOf('\n', marker)));
        assertEquals("xref", contents.substring(xref, xref + 4));
        String[] table = contents.substring(xref).split("\n");
        int count = Integer.parseInt(table[1].split(" ")[1]);
        for (int object = 1; object < count; object++) {
            int offset = Integer.parseInt(table[object + 2].substring(0, 10));
            assertTrue(contents.substring(offset).startsWith(object + " 0 obj\n"));
        }
        int stream = contents.indexOf("stream\n") + "stream\n".length();
        var length = java.util.regex.Pattern.compile("/Length (\\d+)").matcher(contents);
        assertTrue(length.find());
        assertEquals(contents.indexOf("endstream", stream) - stream, Integer.parseInt(length.group(1)));
    }
}
