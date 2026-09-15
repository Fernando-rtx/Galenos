package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.enterprise.context.ApplicationScoped;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;

@ApplicationScoped
public class ClinicaDAO extends DefaultDAO<Clinica> {

    public ClinicaDAO() {
        super(Clinica.class);
    }
}
