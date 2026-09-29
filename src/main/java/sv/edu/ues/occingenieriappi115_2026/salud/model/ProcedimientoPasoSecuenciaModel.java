package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;

/**
 * Backing bean JSF del catálogo de secuencias de pasos de procedimiento.
 *
 * <p>Antes de persistir valida las reglas de negocio del grafo de pasos:
 * enlaces duplicados, orden repetido para el mismo paso origen, destino
 * existente, pasos del mismo procedimiento y ausencia de ciclos.</p>
 */
@Named
@ViewScoped
public class ProcedimientoPasoSecuenciaModel extends AbstractModel<ProcedimientoPasoSecuencia> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ProcedimientoPasoSecuenciaDAO secuenciaDAO;
    @EJB
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    @Inject
    transient FacesContext facesContext;
    private ProcedimientoPasoSecuencia seleccionado;

    private List<ProcedimientoPaso> procedimientosPaso;

    public ProcedimientoPasoSecuenciaModel() {
    }

    public ProcedimientoPasoSecuenciaModel(ProcedimientoPasoSecuenciaDAO secuenciaDAO,
            ProcedimientoPasoDAO procedimientoPasoDAO) {
        this.secuenciaDAO = secuenciaDAO;
        this.procedimientoPasoDAO = procedimientoPasoDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.procedimientosPaso = procedimientoPasoDAO.obtenerTodos();
    }

    @Override
    protected ProcedimientoPasoSecuenciaDAO getDao() {
        return secuenciaDAO;
    }

    public ProcedimientoPasoSecuencia getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ProcedimientoPasoSecuencia seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<ProcedimientoPaso> getProcedimientosPaso() {
        return procedimientosPaso;
    }

    public void nuevo() {
        seleccionado = new ProcedimientoPasoSecuencia();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ProcedimientoPasoSecuencia seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        if (!validarAntesDeGuardar()) {
            return;
        }
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION, LISTADO -> seleccionado = getDao().actualizar(seleccionado);
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Aplica las reglas de negocio de la secuencia antes de persistir.
     *
     * @return {@code true} si la secuencia es válida; si no, agrega el error
     * faces y devuelve {@code false}
     */
    private boolean validarAntesDeGuardar() {
        ProcedimientoPaso origen = seleccionado.getIdProcedimientoPaso();
        if (origen == null || origen.getIdProcedimientoPaso() == null) {
            agregarError("secuencia.pasoOrigenRequerido");
            return false;
        }
        seleccionado.setTipoSecuencia(normalizar(seleccionado.getTipoSecuencia()));

        UUID destino = seleccionado.getIdProcedimientoPasoReferencia();
        if (destino != null) {
            ProcedimientoPaso pasoDestino = procedimientoPasoDAO.buscarPorId(destino);
            if (pasoDestino == null) {
                agregarError("secuencia.pasoDestinoNoExiste");
                return false;
            }
            if (!mismoProcedimiento(origen, pasoDestino)) {
                agregarError("secuencia.pasosDeProcedimientosDistintos");
                return false;
            }
        }

        UUID idOrigen = origen.getIdProcedimientoPaso();
        for (ProcedimientoPasoSecuencia existente : obtenerSecuencias()) {
            if (mismoRegistro(existente) || !esMismoOrigen(existente, idOrigen)) {
                continue;
            }
            if (destino != null
                    && destino.equals(existente.getIdProcedimientoPasoReferencia())) {
                agregarError("secuencia.enlaceDuplicado");
                return false;
            }
            String tipoExistente = normalizar(existente.getTipoSecuencia());
            if (seleccionado.getTipoSecuencia() != null
                    && seleccionado.getTipoSecuencia().equalsIgnoreCase(tipoExistente)) {
                agregarError("secuencia.mismoOrden");
                return false;
            }
        }

        if (destino != null && formaCiclo(idOrigen, destino)) {
            agregarError("secuencia.cicloDetectado");
            return false;
        }
        return true;
    }

    private List<ProcedimientoPasoSecuencia> obtenerSecuencias() {
        List<ProcedimientoPasoSecuencia> secuencias = getDao().obtenerTodos();
        return secuencias == null ? List.of() : secuencias;
    }

    private boolean mismoRegistro(ProcedimientoPasoSecuencia existente) {
        UUID idEditando = seleccionado.getIdProcedimientoPasoSecuencia();
        return idEditando != null
                && idEditando.equals(existente.getIdProcedimientoPasoSecuencia());
    }

    private boolean esMismoOrigen(ProcedimientoPasoSecuencia existente, UUID idOrigen) {
        return existente.getIdProcedimientoPaso() != null
                && idOrigen.equals(existente.getIdProcedimientoPaso().getIdProcedimientoPaso());
    }

    private boolean mismoProcedimiento(ProcedimientoPaso origen, ProcedimientoPaso destino) {
        UUID procedimientoOrigen = origen.getIdProcedimiento() == null
                ? null : origen.getIdProcedimiento().getIdProcedimiento();
        UUID procedimientoDestino = destino.getIdProcedimiento() == null
                ? null : destino.getIdProcedimiento().getIdProcedimiento();
        return Objects.equals(procedimientoOrigen, procedimientoDestino);
    }

    /**
     * Determina si el nuevo enlace origen→destino cerraría un ciclo en el
     * grafo de secuencias existentes.
     *
     * @param origen UUID del paso origen del enlace nuevo
     * @param destino UUID del paso destino del enlace nuevo
     * @return {@code true} si destino vuelve a alcanzar origen
     */
    private boolean formaCiclo(UUID origen, UUID destino) {
        if (origen.equals(destino)) {
            return true;
        }
        Map<UUID, List<UUID>> aristas = new HashMap<>();
        for (ProcedimientoPasoSecuencia existente : obtenerSecuencias()) {
            if (mismoRegistro(existente)
                    || existente.getIdProcedimientoPaso() == null
                    || existente.getIdProcedimientoPaso().getIdProcedimientoPaso() == null
                    || existente.getIdProcedimientoPasoReferencia() == null) {
                continue;
            }
            aristas.computeIfAbsent(
                    existente.getIdProcedimientoPaso().getIdProcedimientoPaso(),
                    clave -> new ArrayList<>())
                    .add(existente.getIdProcedimientoPasoReferencia());
        }
        Deque<UUID> pendientes = new ArrayDeque<>();
        Set<UUID> visitados = new HashSet<>();
        pendientes.add(destino);
        visitados.add(destino);
        while (!pendientes.isEmpty()) {
            UUID actual = pendientes.poll();
            if (origen.equals(actual)) {
                return true;
            }
            for (UUID siguiente : aristas.getOrDefault(actual, List.of())) {
                if (visitados.add(siguiente)) {
                    pendientes.add(siguiente);
                }
            }
        }
        return false;
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private void agregarError(String clave) {
        agregarMensaje(clave, FacesMessage.SEVERITY_ERROR);
        if (facesContext != null) {
            facesContext.validationFailed();
        }
    }

    private void agregarMensaje(String clave, FacesMessage.Severity severidad) {
        FacesContext contexto = facesContext;
        if (contexto == null) {
            return;
        }
        String mensaje = contexto.getApplication()
                .getResourceBundle(contexto, "msg")
                .getString(clave);
        contexto.addMessage(null, new FacesMessage(severidad, mensaje, null));
    }
}
