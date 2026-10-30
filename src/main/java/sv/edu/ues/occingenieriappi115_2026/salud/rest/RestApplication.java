package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

/** API versionada dentro del WAR existente; las vistas JSF conservan sus rutas. */
@ApplicationPath("/resources")
public class RestApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(TipoDocumentoResource.class, ConsultaResource.class, ClinicaResource.class,
                ApiExceptionMapper.class, CorsFilter.class, CatalogoResource.class, DemoAccessFilter.class, ContextoResource.class);
    }
}
