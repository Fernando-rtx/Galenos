package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;

/** Small self-contained PDF ticket generator; it adds no runtime dependency. */
final class TicketPdf {
    private static final Charset PDF_TEXT = Charset.forName("windows-1252");
    private TicketPdf() {}

    static byte[] generate(List<String> lines) {
        StringBuilder stream = new StringBuilder("BT\n/F1 12 Tf\n50 790 Td\n16 TL\n");
        for (String line : lines) stream.append('(').append(escape(line)).append(") Tj\nT*\n");
        stream.append("ET\n");
        byte[] content = stream.toString().getBytes(PDF_TEXT);
        List<byte[]> objects = List.of(
                bytes("<< /Type /Catalog /Pages 2 0 R >>"),
                bytes("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"),
                bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>"),
                bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"),
                concat(bytes("<< /Length " + content.length + " >>\nstream\n"), content, bytes("endstream")));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, bytes("%PDF-1.4\n%âãÏÓ\n"));
        int[] offsets = new int[objects.size() + 1];
        for (int i = 0; i < objects.size(); i++) {
            offsets[i + 1] = out.size(); write(out, bytes((i + 1) + " 0 obj\n"));
            write(out, objects.get(i)); write(out, bytes("\nendobj\n"));
        }
        int xref = out.size(); write(out, bytes("xref\n0 " + offsets.length + "\n0000000000 65535 f \n"));
        for (int i = 1; i < offsets.length; i++) write(out, bytes(String.format("%010d 00000 n \n", offsets[i])));
        write(out, bytes("trailer\n<< /Size " + offsets.length + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF"));
        return out.toByteArray();
    }
    private static String escape(String value) {
        String safe = value == null ? "" : value.replaceAll("[^\\x20-\\x7e\\u00a0-\\u00ff]", "?");
        return safe.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
    private static byte[] bytes(String value) { return value.getBytes(PDF_TEXT); }
    private static byte[] concat(byte[]... chunks) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); for (byte[] chunk : chunks) write(out, chunk); return out.toByteArray();
    }
    private static void write(ByteArrayOutputStream out, byte[] value) { out.write(value, 0, value.length); }
}
