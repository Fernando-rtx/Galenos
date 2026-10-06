package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

/** Identidad de trabajo elegida en la pantalla Cambiar de rol. */
@Named
@SessionScoped
public class RolActualBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID idPersonaRol;
    private String nombrePersona;
    private String nombreRol;
    private String nombreClinica;

    public void seleccionar(PersonaRol asignacion) {
        idPersonaRol = asignacion.getIdPersonaRol();
        nombrePersona = (asignacion.getIdPersona().getNombres() + " "
                + asignacion.getIdPersona().getApellidos()).trim();
        nombreRol = asignacion.getIdRol().getNombre();
        nombreClinica = asignacion.getIdClinica().getNombre();
    }

    public UUID getIdPersonaRol() { return idPersonaRol; }
    public String getNombrePersona() { return nombrePersona; }
    public String getNombreRol() { return nombreRol; }
    public String getNombreClinica() { return nombreClinica; }
    public boolean isSeleccionado() { return idPersonaRol != null; }
}
