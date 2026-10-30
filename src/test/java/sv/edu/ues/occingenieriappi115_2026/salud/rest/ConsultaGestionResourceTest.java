package sv.edu.ues.occingenieriappi115_2026.salud.rest;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaGestionResourceTest {
 @Test void otraClinicaNoConsultaPersistencia() {
  var resource=new ConsultaGestionResource();resource.service=mock(ConsultaFlujoService.class);
  resource.settings=new ApiSettings(Map.of("GALENOS_CRUD_DEMO","true","GALENOS_DEMO_MODE","true","POSTGRESQL_DB_NAME","galenos_demo_crud",
    "GALENOS_CRUD_ACTOR_CLINICS","editor=00000000-0000-4000-8000-000000000011"));
  resource.request=mock(HttpServletRequest.class);when(resource.request.getUserPrincipal()).thenReturn(()->"editor");
  assertEquals(403,assertThrows(ApiException.class,()->resource.listar("00000000-0000-4000-8000-000000000012",null,null,null,null,null)).getStatus());
  verifyNoInteractions(resource.service);
 }
 @Test void modoHabitualNoPublicaDatosDeGestion() {
  var resource=new ConsultaGestionResource();resource.service=mock(ConsultaFlujoService.class);resource.settings=new ApiSettings(Map.of());
  assertEquals(404,assertThrows(ApiException.class,()->resource.listar("00000000-0000-4000-8000-000000000011",null,null,null,null,null)).getStatus());
  verifyNoInteractions(resource.service);
 }
}
