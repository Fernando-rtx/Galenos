package sv.edu.ues.occingenieriappi115_2026.salud.control;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class SesionAcademicaServiceTest {
  @Test
  void tokenNoEsUUIDYExpira() {
    var instant = Instant.parse("2026-11-11T00:00:00Z");
    class Reloj extends Clock {
      Instant now = instant;

      public ZoneId getZone() {
        return ZoneOffset.UTC;
      }

      public Clock withZone(ZoneId z) {
        return this;
      }

      public Instant instant() {
        return now;
      }
    }
    var reloj = new Reloj();
    var service = new SesionAcademicaService(reloj);
    var a = service.crear(UUID.randomUUID(), "PACIENTE");
    assertEquals(43, a.token().length());
    assertNull(service.buscar("Bearer " + UUID.randomUUID()));
    assertNotNull(service.buscar("Bearer " + a.token()));
    reloj.now = instant.plusSeconds(1800);
    assertNull(service.buscar("Bearer " + a.token()));
  }
}
