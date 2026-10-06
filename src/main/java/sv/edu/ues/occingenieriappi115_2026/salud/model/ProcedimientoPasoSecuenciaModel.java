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
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ProcedimientoPasoSecuenciaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
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
    private Procedimiento procedimientoSeleccionado;
    private ProcedimientoPaso pasoSiguienteSeleccionado;
    private List<String> tiposSecuencia;
    private List<ProcedimientoPasoSecuencia> secuenciasCache;

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

    public List<Procedimiento> getProcedimientos() {
        if (procedimientosPaso == null) {
            return List.of();
        }
        return procedimientosPaso.stream()
                .filter(paso -> paso != null && paso.getIdProcedimiento() != null
                && paso.getIdProcedimiento().getIdProcedimiento() != null)
                .map(ProcedimientoPaso::getIdProcedimiento)
                .filter(procedimiento -> !Boolean.FALSE.equals(procedimiento.getActivo()))
                .collect(java.util.stream.Collectors.toMap(
                        Procedimiento::getIdProcedimiento,
                        procedimiento -> procedimiento,
                        (primero, ignorado) -> primero,
                        java.util.LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(Procedimiento::getNombre,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public Procedimiento getProcedimientoSeleccionado() {
        return procedimientoSeleccionado;
    }

    public void setProcedimientoSeleccionado(Procedimiento procedimientoSeleccionado) {
        this.procedimientoSeleccionado = procedimientoSeleccionado;
    }

    public List<ProcedimientoPaso> getPasosDelProcedimiento() {
        if (procedimientoSeleccionado == null
                || procedimientoSeleccionado.getIdProcedimiento() == null
                || procedimientosPaso == null) {
            return List.of();
        }
        UUID idProcedimiento = procedimientoSeleccionado.getIdProcedimiento();
        return procedimientosPaso.stream()
                .filter(paso -> paso != null && paso.getIdProcedimiento() != null
                && idProcedimiento.equals(paso.getIdProcedimiento().getIdProcedimiento()))
                .toList();
    }

    public List<ProcedimientoPaso> getPasosSiguientesDisponibles() {
        ProcedimientoPaso origen = seleccionado == null
                ? null : seleccionado.getIdProcedimientoPaso();
        UUID idOrigen = origen == null ? null : origen.getIdProcedimientoPaso();
        return getPasosDelProcedimiento().stream()
                .filter(paso -> idOrigen == null || !idOrigen.equals(paso.getIdProcedimientoPaso()))
                .toList();
    }

    public ProcedimientoPaso getPasoSiguienteSeleccionado() {
        return pasoSiguienteSeleccionado;
    }

    public void setPasoSiguienteSeleccionado(ProcedimientoPaso pasoSiguienteSeleccionado) {
        this.pasoSiguienteSeleccionado = pasoSiguienteSeleccionado;
    }

    public List<String> getTiposSecuencia() {
        if (tiposSecuencia == null) {
            cargarTiposSecuencia();
        }
        return tiposSecuencia;
    }

    public boolean isEstructuraEditable() {
        return getEstado() != ESTADO_CRUD.EDICION;
    }

    public boolean isPasoSiguienteEditable() {
        return isEstructuraEditable() || seleccionado == null
                || seleccionado.getIdProcedimientoPasoReferencia() == null;
    }

    public String getNombrePasoSiguiente(ProcedimientoPasoSecuencia secuencia) {
        if (secuencia == null || secuencia.getIdProcedimientoPasoReferencia() == null
                || procedimientosPaso == null) {
            return null;
        }
        UUID id = secuencia.getIdProcedimientoPasoReferencia();
        return procedimientosPaso.stream()
                .filter(paso -> paso != null && id.equals(paso.getIdProcedimientoPaso()))
                .map(ProcedimientoPaso::getNombre)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public String getTipoSecuenciaLabel(String tipo) {
        String normalizado = normalizar(tipo);
        if (normalizado == null || normalizado.isEmpty()) {
            return normalizado;
        }
        String clave = switch (normalizado.toUpperCase(Locale.ROOT)) {
            case "SIGUIENTE" -> "secuencia.tipoSiguiente";
            case "ALTERNATIVA" -> "secuencia.tipoAlternativa";
            case "DESPUES" -> "secuencia.tipoDespues";
            case "INICIAL" -> "secuencia.tipoInicial";
            case "OPCIONAL" -> "secuencia.tipoOpcional";
            case "BLOQUEANTE" -> "secuencia.tipoBloqueante";
            default -> null;
        };
        FacesContext contexto = FacesContext.getCurrentInstance();
        return clave == null || contexto == null
                ? normalizado
                : contexto.getApplication().getResourceBundle(contexto, "msg").getString(clave);
    }

    public void procedimientoCambio() {
        if (getEstado() == ESTADO_CRUD.CREACION) {
            seleccionado.setIdProcedimientoPaso(null);
            seleccionado.setIdProcedimientoPasoReferencia(null);
            pasoSiguienteSeleccionado = null;
        }
    }

    public void pasoOrigenCambio() {
        if (getEstado() == ESTADO_CRUD.CREACION) {
            seleccionado.setIdProcedimientoPasoReferencia(null);
            pasoSiguienteSeleccionado = null;
        }
    }

    public List<ProcedimientoPasoSecuencia> getSecuenciasPorProcedimiento(
            sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento procedimiento) {
        if (procedimiento == null || procedimiento.getIdProcedimiento() == null) {
            return List.of();
        }
        UUID idProcedimiento = procedimiento.getIdProcedimiento();
        return obtenerSecuencias().stream()
                .filter(secuencia -> secuencia != null
                && secuencia.getIdProcedimientoPaso() != null
                && secuencia.getIdProcedimientoPaso().getIdProcedimiento() != null)
                .filter(secuencia -> idProcedimiento.equals(secuencia
                .getIdProcedimientoPaso()
                .getIdProcedimiento()
                .getIdProcedimiento()))
                .toList();
    }

    public List<ProcedimientoPasoSecuencia> getSecuenciasPorPaso(ProcedimientoPaso paso) {
        if (paso == null || paso.getIdProcedimientoPaso() == null) {
            return List.of();
        }
        UUID idPaso = paso.getIdProcedimientoPaso();
        return obtenerSecuencias().stream()
                .filter(secuencia -> secuencia != null
                && secuencia.getIdProcedimientoPaso() != null
                && idPaso.equals(secuencia.getIdProcedimientoPaso().getIdProcedimientoPaso()))
                .sorted(Comparator.comparing(ProcedimientoPasoSecuencia::getTipoSecuencia,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public void invalidarCache() {
        secuenciasCache = null;
    }

    public boolean isPasoInicial(ProcedimientoPaso paso) {
        if (paso == null || paso.getIdProcedimientoPaso() == null) {
            return false;
        }
        UUID idPaso = paso.getIdProcedimientoPaso();
        return obtenerSecuencias().stream()
                .noneMatch(secuencia -> secuencia != null
                && idPaso.equals(secuencia.getIdProcedimientoPasoReferencia()));
    }

    public void nuevo() {
        seleccionado = new ProcedimientoPasoSecuencia();
        procedimientoSeleccionado = null;
        pasoSiguienteSeleccionado = null;
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ProcedimientoPasoSecuencia seleccionado) {
        this.seleccionado = seleccionado;
        prepararEdicion();
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void seleccionarFila() {
        if (seleccionado != null) {
            prepararEdicion();
            setEstado(ESTADO_CRUD.EDICION);
        }
    }

    private void prepararEdicion() {
        ProcedimientoPaso origen = seleccionado == null ? null : seleccionado.getIdProcedimientoPaso();
        procedimientoSeleccionado = origen == null ? null : origen.getIdProcedimiento();
        UUID idSiguiente = seleccionado == null
                ? null : seleccionado.getIdProcedimientoPasoReferencia();
        pasoSiguienteSeleccionado = idSiguiente == null || procedimientosPaso == null
                ? null
                : procedimientosPaso.stream()
                        .filter(paso -> paso != null && idSiguiente.equals(paso.getIdProcedimientoPaso()))
                        .findFirst().orElse(null);
    }

    private void cargarTiposSecuencia() {
        Set<String> tipos = new LinkedHashSet<>(List.of("SIGUIENTE", "ALTERNATIVA"));
        for (ProcedimientoPasoSecuencia secuencia : obtenerSecuencias()) {
            String tipo = secuencia == null ? null : normalizar(secuencia.getTipoSecuencia());
            if (tipo != null && !tipo.isEmpty()) {
                tipos.add(tipo);
            }
        }
        tiposSecuencia = List.copyOf(tipos);
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) {
            return;
        }
        if (!validarAntesDeGuardar()) {
            return;
        }
        try {
            switch (getEstado()) {
                case CREACION -> {
                    getDao().guardar(seleccionado);
                    agregarMensaje("secuencia.creadaCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case EDICION -> {
                    seleccionado = getDao().actualizar(seleccionado);
                    agregarMensaje("secuencia.actualizadaCorrectamente", FacesMessage.SEVERITY_INFO);
                }
                case LISTADO -> { }
            }
        } catch (RuntimeException ex) {
            if (facesContext == null) {
                throw ex;
            }
            agregarMensaje("secuencia.errorGuardar", FacesMessage.SEVERITY_ERROR);
            facesContext.validationFailed();
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
        secuenciasCache = null;
    }

    public void cancelar() {
        volverAlListado();
        agregarMensaje("secuencia.cancelado", FacesMessage.SEVERITY_INFO);
    }

    private void volverAlListado() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Retira el vínculo de secuencia indicado sin tocar los pasos que
     * enlaza: solo elimina la fila de la tabla de unión.
     *
     * @param fila secuencia seleccionada en la tabla
     */
    public void quitar(ProcedimientoPasoSecuencia fila) {
        if (fila == null || fila.getIdProcedimientoPasoSecuencia() == null) {
            agregarError("secuencia.errorQuitar");
            return;
        }
        try {
            ProcedimientoPasoSecuencia persistida
                    = getDao().buscarPorId(fila.getIdProcedimientoPasoSecuencia());
            if (persistida == null
                    || !getDao().eliminar(persistida.getIdProcedimientoPasoSecuencia())) {
                agregarError("secuencia.errorQuitar");
                return;
            }
        } catch (RuntimeException ex) {
            agregarError("secuencia.errorQuitar");
            return;
        }
        if (seleccionado != null && fila.getIdProcedimientoPasoSecuencia().equals(
                seleccionado.getIdProcedimientoPasoSecuencia())) {
            volverAlListado();
        }
        agregarMensaje("secuencia.quitada", FacesMessage.SEVERITY_INFO);
        secuenciasCache = null;
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
        ProcedimientoPaso origenPersistido = procedimientoPasoDAO.buscarPorId(origen.getIdProcedimientoPaso());
        if (origenPersistido == null) {
            agregarError("secuencia.pasoOrigenNoExiste");
            return false;
        }
        if (procedimientoSeleccionado != null
                && (procedimientoSeleccionado.getIdProcedimiento() == null
                || origenPersistido.getIdProcedimiento() == null
                || !procedimientoSeleccionado.getIdProcedimiento().equals(
                        origenPersistido.getIdProcedimiento().getIdProcedimiento()))) {
            agregarError("secuencia.pasoFueraProcedimiento");
            return false;
        }
        seleccionado.setIdProcedimientoPaso(origenPersistido);
        seleccionado.setTipoSecuencia(normalizar(seleccionado.getTipoSecuencia()));
        if (seleccionado.getTipoSecuencia() == null
                || seleccionado.getTipoSecuencia().isEmpty()) {
            agregarError("secuencia.tipoRequerido");
            return false;
        }
        if (seleccionado.getTipoSecuencia().length() > 20) {
            agregarError("secuencia.tipoMaximo");
            return false;
        }

        UUID destino = pasoSiguienteSeleccionado == null
                ? seleccionado.getIdProcedimientoPasoReferencia()
                : pasoSiguienteSeleccionado.getIdProcedimientoPaso();
        if (destino == null) {
            agregarError("secuencia.pasoDestinoRequerido");
            return false;
        }
        ProcedimientoPaso pasoDestino = procedimientoPasoDAO.buscarPorId(destino);
        if (pasoDestino == null) {
            agregarError("secuencia.pasoDestinoNoExiste");
            return false;
        }
        if (!mismoProcedimiento(origenPersistido, pasoDestino)) {
            agregarError("secuencia.pasosDeProcedimientosDistintos");
            return false;
        }
        seleccionado.setIdProcedimientoPasoReferencia(destino);

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

        if (formaCiclo(idOrigen, destino)) {
            agregarError("secuencia.cicloDetectado");
            return false;
        }
        return true;
    }

    private List<ProcedimientoPasoSecuencia> obtenerSecuencias() {
        if (secuenciasCache == null) {
            List<ProcedimientoPasoSecuencia> secuencias = getDao().obtenerTodos();
            secuenciasCache = secuencias == null ? List.of() : secuencias;
        }
        return secuenciasCache;
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
        return procedimientoOrigen != null && procedimientoDestino != null
                && Objects.equals(procedimientoOrigen, procedimientoDestino);
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
