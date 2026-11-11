package sv.edu.ues.occingenieriappi115_2026.salud.rest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaGestionResourceTest {
 @Test void identificadorClinicaInvalidoNoConsultaPersistencia() {
  var resource=new ConsultaGestionResource();resource.service=mock(ConsultaFlujoService.class);
  resource.settings=new ApiSettings(Map.of("GALENOS_API_MODE","academic","POSTGRESQL_DB_NAME","clinica"));
  assertEquals(422,assertThrows(ApiException.class,()->resource.listar("inválido",null,null,null,null,null)).getStatus());
  verifyNoInteractions(resource.service);
 }
 @Test void modoHabitualNoPublicaDatosDeGestion() {
  var resource=new ConsultaGestionResource();resource.service=mock(ConsultaFlujoService.class);resource.settings=new ApiSettings(Map.of());
  assertEquals(404,assertThrows(ApiException.class,()->resource.listar("00000000-0000-4000-8000-000000000011",null,null,null,null,null)).getStatus());
  verifyNoInteractions(resource.service);
 }
}
