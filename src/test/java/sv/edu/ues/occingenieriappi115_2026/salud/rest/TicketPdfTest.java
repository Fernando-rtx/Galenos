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
}
