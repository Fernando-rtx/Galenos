package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoFlujoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.RolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/** Maneja la creación guiada del paso inicial y los pasos enlazados. */
@Named
@ViewScoped
public class ProcedimientoFlujoModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProcedimientoFlujoService flujoService;
    @EJB
    private ProcedimientoPasoDAO pasoDAO;
    @EJB
    private RolDAO rolDAO;
    @EJB
    private ExamenDAO examenDAO;
    @Inject
    private ProcedimientoPasoSecuenciaModel secuenciaModel;
    @Inject
    private ProcedimientoPasoExamenModel procedimientoPasoExamenModel;
    @Inject
    private transient FacesContext facesContext;

    private Procedimiento procedimiento;
    private ProcedimientoPaso pasoOrigen;
    private String nombre;
    private Rol rol;
    private boolean indicaFin;
    private String tipoSecuencia = "SIGUIENTE";
    private List<Examen> examenesSeleccionados = List.of();
    private UUID pasosCacheProcedimiento;
    private List<ProcedimientoPaso> pasosCache;

    public void nuevoInicial(Procedimiento procedimiento) {
        preparar(procedimiento, null);
    }

    public void nuevoSiguiente(Procedimiento procedimiento, ProcedimientoPaso origen) {
        preparar(procedimiento, origen);
    }

    private void preparar(Procedimiento procedimiento, ProcedimientoPaso origen) {
        this.procedimiento = procedimiento;
        this.pasoOrigen = origen;
        this.nombre = "";
        this.rol = null;
        this.indicaFin = false;
        this.tipoSecuencia = "SIGUIENTE";
        this.examenesSeleccionados = List.of();
    }

    public Procedimiento getProcedimiento() {
        return procedimiento;
    }

    public ProcedimientoPaso getPasoOrigen() {
        return pasoOrigen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isIndicaFin() {
        return indicaFin;
    }

    public void setIndicaFin(boolean indicaFin) {
        this.indicaFin = indicaFin;
    }

    public String getTipoSecuencia() {
        return tipoSecuencia;
    }

    public void setTipoSecuencia(String tipoSecuencia) {
        this.tipoSecuencia = tipoSecuencia;
    }

    public List<Examen> getExamenesSeleccionados() {
        return examenesSeleccionados;
    }

    public void setExamenesSeleccionados(List<Examen> examenesSeleccionados) {
        this.examenesSeleccionados = examenesSeleccionados == null
                ? List.of() : examenesSeleccionados;
    }

    public List<Rol> getRolesDisponibles() {
        UUID idProcedimiento = procedimiento == null ? null : procedimiento.getIdProcedimiento();
        if (idProcedimiento == null) {
            return List.of();
        }
        List<UUID> rolesUsados = pasosDelProcedimiento(idProcedimiento).stream()
                .filter(paso -> paso.getIdRol() != null)
                .map(paso -> paso.getIdRol().getIdRol())
                .toList();
        return rolDAO.obtenerTodos().stream()
                .filter(opcion -> opcion != null && !Boolean.FALSE.equals(opcion.getActivo()))
                .filter(opcion -> !rolesUsados.contains(opcion.getIdRol()))
                .sorted(java.util.Comparator.comparing(Rol::getNombre,
                        java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public List<Examen> getExamenesDisponibles() {
        return examenDAO.obtenerTodos().stream()
                .filter(examen -> examen != null && !Boolean.FALSE.equals(examen.getActivo()))
                .sorted(java.util.Comparator.comparing(Examen::getNombre,
                        java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public List<ProcedimientoPaso> getPasos(Procedimiento procedimiento) {
        if (procedimiento == null || procedimiento.getIdProcedimiento() == null) {
            return List.of();
        }
        return pasosDelProcedimiento(procedimiento.getIdProcedimiento());
    }

    public boolean isInicialDisponible(Procedimiento procedimiento) {
        return getPasos(procedimiento).isEmpty();
    }

    public boolean puedeCrearInicial(Procedimiento procedimiento) {
        return procedimiento != null && !Boolean.FALSE.equals(procedimiento.getActivo())
                && isInicialDisponible(procedimiento);
    }

    public boolean puedeAgregarSiguiente(ProcedimientoPaso paso, Procedimiento procedimiento) {
        return procedimiento != null && !Boolean.FALSE.equals(procedimiento.getActivo())
                && paso != null && !Boolean.TRUE.equals(paso.getIndicaFin());
    }

    public boolean isInicial() {
        return pasoOrigen == null;
    }

    public void guardar() {
        if (procedimiento == null || procedimiento.getIdProcedimiento() == null) {
            agregarError("procedimientoPaso.procedimientoRequerido");
            return;
        }
        if (rol == null || rol.getIdRol() == null) {
            agregarError("procedimientoPaso.rolRequerido");
            return;
        }
        try {
            List<UUID> idsExamenes = examenesSeleccionados.stream()
                    .filter(examen -> examen != null && examen.getIdExamen() != null)
                    .map(Examen::getIdExamen)
                    .toList();
            flujoService.crearPaso(procedimiento.getIdProcedimiento(),
                    pasoOrigen == null ? null : pasoOrigen.getIdProcedimientoPaso(),
                    nombre, rol.getIdRol(), indicaFin, tipoSecuencia, idsExamenes);
            pasosCacheProcedimiento = null;
            pasosCache = null;
            secuenciaModel.invalidarCache();
            procedimientoPasoExamenModel.invalidarCache();
            agregarMensaje("flujo.pasoCreado", FacesMessage.SEVERITY_INFO);
        } catch (IllegalArgumentException ex) {
            agregarError(ex.getMessage());
        } catch (RuntimeException ex) {
            agregarError("flujo.errorCrearPaso");
        }
    }

    private List<ProcedimientoPaso> pasosDelProcedimiento(UUID idProcedimiento) {
        if (idProcedimiento.equals(pasosCacheProcedimiento) && pasosCache != null) {
            return pasosCache;
        }
        pasosCacheProcedimiento = idProcedimiento;
        pasosCache = pasoDAO.obtenerTodos().stream()
                .filter(paso -> paso != null && paso.getIdProcedimiento() != null
                && idProcedimiento.equals(paso.getIdProcedimiento().getIdProcedimiento()))
                .sorted(java.util.Comparator.comparing(ProcedimientoPaso::getNombre,
                        java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        return pasosCache;
    }

    private void agregarError(String clave) {
        agregarMensaje(clave, FacesMessage.SEVERITY_ERROR);
        if (facesContext != null) {
            facesContext.validationFailed();
        }
    }

    private void agregarMensaje(String clave, FacesMessage.Severity severidad) {
        if (facesContext == null) {
            return;
        }
        String texto = facesContext.getApplication()
                .getResourceBundle(facesContext, "msg").getString(clave);
        facesContext.addMessage(null, new FacesMessage(severidad, texto, null));
    }
}
