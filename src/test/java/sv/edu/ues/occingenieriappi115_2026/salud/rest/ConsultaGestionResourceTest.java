package sv.edu.ues.occingenieriappi115_2026.salud.rest;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import jakarta.servlet.http.HttpServletResponse;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaFlujoService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultaGestionResourceTest {
 @Test void identificadorClinicaInvalidoNoConsultaPersistencia() {
  var resource=new ConsultaGestionResource();resource.service=mock(ConsultaFlujoService.class);
  resource.settings=new ApiSettings(Map.of("GALENOS_API_MODE","academic","POSTGRESQL_DB_NAME","clinica"));
  assertEquals(422,assertThrows(ApiException.class,()->resource.listar("inválido",null,null,null,null,null,null)).getStatus());
  verifyNoInteractions(resource.service);
 }
 @Test void modoHabitualNoPublicaDatosDeGestion() {
  var resource=new ConsultaGestionResource();resource.service=mock(ConsultaFlujoService.class);resource.settings=new ApiSettings(Map.of());
  assertEquals(404,assertThrows(ApiException.class,()->resource.listar("00000000-0000-4000-8000-000000000011",null,null,null,null,null,null)).getStatus());
  verifyNoInteractions(resource.service);
 }
 @Test void filtraFinalizadasConElMismoCriterioEnListadoYConteo() {
  var resource=new ConsultaGestionResource();resource.service=mock(ConsultaFlujoService.class);
  resource.settings=new ApiSettings(Map.of("GALENOS_API_MODE","academic","POSTGRESQL_DB_NAME","clinica"));
  resource.response=mock(HttpServletResponse.class);
  var clinic=UUID.fromString("00000000-0000-4000-8000-000000000011");
  var day=LocalDate.of(2026,11,17);var zone=resource.settings.getClinicZone();
  var row=new Consulta(UUID.fromString("00000000-0000-4000-8000-000000000012"));
  when(resource.service.buscarConsultas(clinic,day,day,zone,null,true,2,1)).thenReturn(List.of(row));
  when(resource.service.contarConsultas(clinic,day,day,zone,null,true)).thenReturn(3L);
  var result=resource.listar(clinic.toString(),"2","1",day.toString(),day.toString(),null,"FINALIZADAS");
  assertEquals(row.getIdConsulta().toString(),result.getFirst().idConsulta());
  verify(resource.response).setHeader("Total-registros","3");
  verify(resource.service).buscarConsultas(clinic,day,day,zone,null,true,2,1);
 }
 @Test void estadoDesconocidoNoConsultaPersistencia() {
  var resource=new ConsultaGestionResource();resource.service=mock(ConsultaFlujoService.class);
  resource.settings=new ApiSettings(Map.of("GALENOS_API_MODE","academic","POSTGRESQL_DB_NAME","clinica"));
  assertEquals(422,assertThrows(ApiException.class,()->resource.listar("00000000-0000-4000-8000-000000000011",null,null,null,null,null,"COMPLETADO")).getStatus());
  verifyNoInteractions(resource.service);
 }
}
