package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;

/** Una clínica de trabajo por sesión; la revisión invalida formularios antiguos. */
@Named
@SessionScoped
public class ClinicaActualBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB
    private ClinicaDAO clinicaDAO;
    @Inject
    private transient FacesContext facesContext;
    private UUID idClinicaActual;
    private String idClinicaSeleccionada;
    private long revision;

    public ClinicaActualBean() { }

    public ClinicaActualBean(ClinicaDAO dao) {
        clinicaDAO = dao;
    }

    public List<Clinica> getClinicas() {
        return clinicaDAO.obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("activo", OperadorFiltro.IGUAL, true)), List.of());
    }

    public UUID getIdClinicaActual() { return idClinicaActual; }
    public long getRevision() { return revision; }
    public boolean isSeleccionada() { return idClinicaActual != null; }
    public String getIdClinicaSeleccionada() { return idClinicaSeleccionada; }
    public void setIdClinicaSeleccionada(String id) { idClinicaSeleccionada = id; }

    public void seleccionarClinica(UUID id) {
        Clinica clinica = id == null ? null : clinicaDAO.buscarPorId(id);
        if (clinica == null || !Boolean.TRUE.equals(clinica.getActivo())) {
            throw new IllegalArgumentException("clinicaActual.noDisponible");
        }
        if (!id.equals(idClinicaActual)) {
            revision++;
        }
        idClinicaActual = id;
        idClinicaSeleccionada = id.toString();
    }

    public void cambiarClinica() {
        try {
            seleccionarClinica(UUID.fromString(idClinicaSeleccionada));
        } catch (IllegalArgumentException | NullPointerException ex) {
            idClinicaSeleccionada = idClinicaActual == null ? null : idClinicaActual.toString();
            if (facesContext != null) {
                String mensaje = facesContext.getApplication().getResourceBundle(facesContext, "msg")
                        .getString("clinicaActual.noDisponible");
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
                facesContext.validationFailed();
            }
        }
    }
}
