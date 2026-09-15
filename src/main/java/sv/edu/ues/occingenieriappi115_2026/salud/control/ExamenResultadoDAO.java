package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenResultado;

@ApplicationScoped
public class ExamenResultadoDAO extends DefaultDAO<ExamenResultado> {

    public ExamenResultadoDAO() {
        super(ExamenResultado.class);
    }
}
