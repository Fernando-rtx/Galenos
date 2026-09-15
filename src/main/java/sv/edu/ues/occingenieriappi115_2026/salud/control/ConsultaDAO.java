package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;

@ApplicationScoped
public class ConsultaDAO extends DefaultDAO<Consulta> {

    public ConsultaDAO() {
        super(Consulta.class);
    }
}
