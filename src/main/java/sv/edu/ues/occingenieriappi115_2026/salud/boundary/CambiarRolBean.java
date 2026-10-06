package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/** Controla la selección de clínica, rol y persona para iniciar el trabajo. */
@Named
@ViewScoped
public class CambiarRolBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB private ClinicaDAO clinicaDAO;
    @EJB private PersonaRolDAO personaRolDAO;
    @Inject private ClinicaActualBean clinicaActualBean;
    @Inject private RolActualBean rolActualBean;

    private String clinicaSeleccionada;
    private String rolSeleccionado;
    private String personaRolSeleccionada;

    @PostConstruct
    public void inicializar() {
        UUID actual = clinicaActualBean.getIdClinicaActual();
        if (actual != null && getClinicas().stream()
                .anyMatch(clinica -> actual.equals(clinica.getIdClinica()))) {
            clinicaSeleccionada = actual.toString();
        }
    }

    public List<Clinica> getClinicas() {
        return clinicaActualBean.getClinicas();
    }

    public List<Rol> getRolesDisponibles() {
        UUID idClinica = uuid(clinicaSeleccionada);
        if (idClinica == null) return List.of();
        Map<UUID, Rol> roles = new LinkedHashMap<>();
        for (PersonaRol asignacion : personaRolDAO.buscarPorClinica(idClinica)) {
            Rol rol = asignacion == null ? null : asignacion.getIdRol();
            if (rol != null && Boolean.TRUE.equals(rol.getActivo()) && !esPaciente(rol)) {
                roles.putIfAbsent(rol.getIdRol(), rol);
            }
        }
        return roles.values().stream()
                .sorted(Comparator.comparing(Rol::getNombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public List<PersonaRol> getPersonasDisponibles() {
        UUID idClinica = uuid(clinicaSeleccionada);
        UUID idRol = uuid(rolSeleccionado);
        if (idClinica == null || idRol == null) return List.of();
        return personaRolDAO.buscarPorRolYClinica(idRol, idClinica).stream()
                .sorted(Comparator.comparing((PersonaRol pr) -> pr.getIdPersona().getApellidos(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(pr -> pr.getIdPersona().getNombres(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public String getNombrePersonaRol(PersonaRol asignacion) {
        if (asignacion == null || asignacion.getIdPersona() == null) return "";
        return (asignacion.getIdPersona().getNombres() + " "
                + asignacion.getIdPersona().getApellidos()).trim();
    }

    public void cambiarClinica() {
        rolSeleccionado = null;
        personaRolSeleccionada = null;
    }

    public void cambiarRol() {
        personaRolSeleccionada = null;
    }

    public String aplicar() {
        UUID idClinica = uuid(clinicaSeleccionada);
        UUID idRol = uuid(rolSeleccionado);
        UUID idPersonaRol = uuid(personaRolSeleccionada);
        Clinica clinica = idClinica == null ? null : clinicaDAO.buscarPorId(idClinica);
        PersonaRol asignacion = getPersonasDisponibles().stream()
                .filter(pr -> pr != null && idPersonaRol != null
                        && idPersonaRol.equals(pr.getIdPersonaRol()))
                .findFirst().orElse(null);
        if (clinica == null || !Boolean.TRUE.equals(clinica.getActivo())
                || idRol == null || asignacion == null
                || asignacion.getIdRol() == null
                || !idRol.equals(asignacion.getIdRol().getIdRol())
                || esPaciente(asignacion.getIdRol())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            texto("cambiarRol.seleccionInvalida"), null));
            FacesContext.getCurrentInstance().validationFailed();
            return null;
        }
        clinicaActualBean.seleccionarClinica(idClinica);
        rolActualBean.seleccionar(asignacion);
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, texto("cambiarRol.aplicado"), null));
        return "/paginas/Consulta?faces-redirect=true";
    }

    private String texto(String clave) {
        return FacesContext.getCurrentInstance().getApplication()
                .getResourceBundle(FacesContext.getCurrentInstance(), "msg").getString(clave);
    }

    private UUID uuid(String value) {
        try { return value == null || value.isBlank() ? null : UUID.fromString(value); }
        catch (IllegalArgumentException ex) { return null; }
    }

    private boolean esPaciente(Rol rol) {
        return rol != null && rol.getNombre() != null
                && "paciente".equalsIgnoreCase(rol.getNombre().trim());
    }

    public String getClinicaSeleccionada() { return clinicaSeleccionada; }
    public void setClinicaSeleccionada(String value) { clinicaSeleccionada = value; }
    public String getRolSeleccionado() { return rolSeleccionado; }
    public void setRolSeleccionado(String value) { rolSeleccionado = value; }
    public String getPersonaRolSeleccionada() { return personaRolSeleccionada; }
    public void setPersonaRolSeleccionada(String value) { personaRolSeleccionada = value; }
    public boolean isPuedeAplicar() {
        return clinicaSeleccionada != null && rolSeleccionado != null
                && personaRolSeleccionada != null;
    }
}
