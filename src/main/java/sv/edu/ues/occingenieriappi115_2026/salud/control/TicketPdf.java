package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * PDF real generado con JDK: páginas rasterizadas con Java2D para preservar acentos. Sin librerías
 * externas, scripts, HTML ni recursos de red.
 */
public final class TicketPdf {
  private TicketPdf() {}

  private static byte[] ascii(String text) {
    return text.getBytes(StandardCharsets.ISO_8859_1);
  }

  public static byte[] generar(List<String> lineas) {
    try {
      Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 23);
      var measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
      measure.setFont(font);
      List<String> wrapped = new ArrayList<>();
      for (String linea : lineas) {
        StringBuilder part = new StringBuilder();
        for (int cp : linea.codePoints().toArray()) {
          String next = new String(Character.toChars(cp));
          if (measure.getFontMetrics().stringWidth(part + next) > 1020) {
            wrapped.add(part.toString());
            part.setLength(0);
          }
          part.appendCodePoint(cp);
        }
        wrapped.add(part.toString());
      }
      measure.dispose();
      List<byte[]> objects = new ArrayList<>();
      int pages = Math.max(1, (wrapped.size() + 39) / 40);
      objects.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
      StringBuilder kids = new StringBuilder();
      for (int i = 0; i < pages; i++) kids.append(3 + i * 3).append(" 0 R ");
      objects.add(ascii("<< /Type /Pages /Count " + pages + " /Kids [" + kids + "] >>"));
      for (int page = 0; page < pages; page++) {
        var image = new BufferedImage(1190, 1684, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1190, 1684);
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(25, 78, 115));
        g.fillRect(60, 45, 1070, 12);
        g.setFont(font);
        for (int i = page * 40; i < Math.min(wrapped.size(), (page + 1) * 40); i++)
          g.drawString(wrapped.get(i), 80, 105 + (i % 40) * 36);
        g.drawString("GalenosSV · " + (page + 1) + "/" + pages, 80, 1610);
        g.dispose();
        var jpg = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "jpeg", jpg)) throw new IOException("JPEG no disponible");
        int number = 3 + page * 3;
        objects.add(
            ascii(
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /XObject << /Im"
                    + " "
                    + (number + 2)
                    + " 0 R >> >> /Contents "
                    + (number + 1)
                    + " 0 R >>"));
        byte[] content = ascii("q 595 0 0 842 0 0 cm /Im Do Q");
        objects.add(stream("", content));
        objects.add(
            stream(
                "/Type /XObject /Subtype /Image /Width 1190 /Height 1684 /ColorSpace /DeviceRGB"
                    + " /BitsPerComponent 8 /Filter /DCTDecode",
                jpg.toByteArray()));
      }
      var pdf = new ByteArrayOutputStream();
      pdf.write(ascii("%PDF-1.4\n%âãÏÓ\n"));
      List<Integer> offsets = new ArrayList<>();
      for (int i = 0; i < objects.size(); i++) {
        offsets.add(pdf.size());
        pdf.write(ascii((i + 1) + " 0 obj\n"));
        pdf.write(objects.get(i));
        pdf.write(ascii("\nendobj\n"));
      }
      int xref = pdf.size();
      pdf.write(ascii("xref\n0 " + (objects.size() + 1) + "\n0000000000 65535 f \n"));
      for (int offset : offsets)
        pdf.write(ascii(String.format(Locale.ROOT, "%010d 00000 n \n", offset)));
      pdf.write(
          ascii(
              "trailer\n<< /Size "
                  + (objects.size() + 1)
                  + " /Root 1 0 R >>\nstartxref\n"
                  + xref
                  + "\n%%EOF\n"));
      return pdf.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo generar el ticket", e);
    }
  }

  private static byte[] stream(String dict, byte[] data) throws IOException {
    var bytes = new ByteArrayOutputStream();
    bytes.write(ascii("<< " + dict + " /Length " + data.length + " >>\nstream\n"));
    bytes.write(data);
    bytes.write(ascii("\nendstream"));
    return bytes.toByteArray();
  }
}
