package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Sesiones efímeras; sólo se conserva hash del token emitido por el servidor. */
@ApplicationScoped
public class SesionAcademicaService {
  public record Sesion(UUID persona, String tipo, Instant expira) {}

  public record Emision(String token, Instant expira) {}

  private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
  private final SecureRandom random = new SecureRandom();
  private final Clock clock;

  public SesionAcademicaService() {
    this(Clock.systemUTC());
  }

  public SesionAcademicaService(Clock clock) {
    this.clock = clock;
  }

  private static String hash(String token) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public synchronized Emision crear(UUID persona, String tipo) {
    Instant now = clock.instant();
    sesiones.values().removeIf(s -> !s.expira().isAfter(now));
    if (sesiones.size() >= 10000) throw new IllegalStateException("Capacidad de sesiones agotada");
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    Instant expira = now.plusSeconds(1800);
    sesiones.put(hash(token), new Sesion(persona, tipo, expira));
    return new Emision(token, expira);
  }

  public Sesion buscar(String bearer) {
    if (bearer == null || !bearer.matches("Bearer [A-Za-z0-9_-]{43}")) return null;
    String key = hash(bearer.substring(7));
    Sesion value = sesiones.get(key);
    if (value != null && !value.expira().isAfter(clock.instant())) {
      sesiones.remove(key);
      return null;
    }
    return value;
  }

  public void cerrar(String bearer) {
    if (bearer != null && bearer.matches("Bearer [A-Za-z0-9_-]{43}"))
      sesiones.remove(hash(bearer.substring(7)));
  }
}
