package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenTipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

@Named
@ViewScoped
public class ExamenTipoExamenModel extends AbstractModel<ExamenTipoExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ExamenTipoExamen seleccionado;
    private ExamenTipoExamen filaSeleccionada;
    private List<Examen> examenes;
    private List<TipoExamen> tipoExamenes;
    private Examen examenPadre;
    private String idTipoExamenSeleccionado;
    private UUID tipoExamenOriginalId;
    private List<ExamenTipoExamen> asociaciones = List.of();
    @EJB
    private ExamenTipoExamenDAO examenTipoExamenDAO;
    @EJB
    private ExamenDAO examenDAO;
    @EJB
    private TipoExamenDAO tipoExamenDAO;

    @Inject
    transient FacesContext facesContext;

    public ExamenTipoExamenModel() {
    }

    public ExamenTipoExamenModel(ExamenTipoExamenDAO examenTipoExamenDAO,
            ExamenDAO examenDAO, TipoExamenDAO tipoExamenDAO) {
        this.examenTipoExamenDAO = examenTipoExamenDAO;
        this.examenDAO = examenDAO;
        this.tipoExamenDAO = tipoExamenDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.examenes = examenDAO.obtenerTodos();
        recargarTiposExamen();
    }

    @Override
    protected ExamenTipoExamenDAO getDao() {
        return examenTipoExamenDAO;
    }

    public ExamenTipoExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ExamenTipoExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public ExamenTipoExamen getFilaSeleccionada() {
        return filaSeleccionada;
    }

    public void setFilaSeleccionada(ExamenTipoExamen filaSeleccionada) {
        this.filaSeleccionada = filaSeleccionada;
    }

    public List<Examen> getExamenes() {
        return examenes;
    }

    public List<TipoExamen> getTipoExamenes() {
        if (tipoExamenes == null) {
            return List.of();
        }
        List<TipoExamen> disponibles = new ArrayList<>(tipoExamenes.stream()
                .filter(tipo -> Boolean.TRUE.equals(tipo.getActivo()))
                .toList());
        if (getEstado() == ESTADO_CRUD.EDICION && seleccionado != null
                && seleccionado.getIdTipoExamen() != null
                && seleccionado.getIdTipoExamen().getIdTipoExamen() != null
                && disponibles.stream().noneMatch(tipo -> tipo.getIdTipoExamen()
                        .equals(seleccionado.getIdTipoExamen().getIdTipoExamen()))) {
            // Mantiene visible la relación histórica en un selector deshabilitado.
            disponibles.add(seleccionado.getIdTipoExamen());
        }
        return disponibles;
    }

    public List<ExamenTipoExamen> getAsociaciones() {
        return asociaciones;
    }

    public String getIdTipoExamenSeleccionado() {
        return idTipoExamenSeleccionado;
    }

    public void setIdTipoExamenSeleccionado(String idTipoExamenSeleccionado) {
        if (getEstado() == ESTADO_CRUD.EDICION
                && !Objects.equals(tipoExamenOriginalId, uuidOrNull(idTipoExamenSeleccionado))) {
            return;
        }
        this.idTipoExamenSeleccionado = idTipoExamenSeleccionado;
    }

    public void cargarPorExamen(Examen examen) {
        examenPadre = examen;
        filaSeleccionada = null;
        seleccionado = null;
        idTipoExamenSeleccionado = null;
        tipoExamenOriginalId = null;
        setEstado(ESTADO_CRUD.LISTADO);
        recargarAsociaciones();
    }

    public void nuevaAsociacion(Examen examen) {
        recargarTiposExamen();
        examenPadre = examen;
        filaSeleccionada = null;
        seleccionado = new ExamenTipoExamen();
        seleccionado.setIdExamen(examen);
        seleccionado.setFechaCreacion(new Date());
        idTipoExamenSeleccionado = null;
        tipoExamenOriginalId = null;
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionarAsociacion(SelectEvent<ExamenTipoExamen> evento) {
        ExamenTipoExamen fila = evento == null ? null : evento.getObject();
        if (fila == null || fila.getIdExamenTipoExamen() == null
                || examenPadre == null || examenPadre.getIdExamen() == null
                || fila.getIdExamen() == null
                || !examenPadre.getIdExamen().equals(fila.getIdExamen().getIdExamen())) {
            agregarError("examen.seleccioneExamen");
            return;
        }
        // La copia permite cancelar sin modificar los datos de la tabla.
        seleccionado = new ExamenTipoExamen(fila.getIdExamenTipoExamen());
        seleccionado.setIdExamen(fila.getIdExamen());
        seleccionado.setIdTipoExamen(fila.getIdTipoExamen());
        seleccionado.setFechaCreacion(fila.getFechaCreacion());
        seleccionado.setObservaciones(fila.getObservaciones());
        filaSeleccionada = fila;
        idTipoExamenSeleccionado = fila.getIdTipoExamen() == null ? null
                : fila.getIdTipoExamen().getIdTipoExamen().toString();
        tipoExamenOriginalId = fila.getIdTipoExamen() == null
                ? null : fila.getIdTipoExamen().getIdTipoExamen();
        recargarTiposExamen();
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardarAsociacion() {
        if (seleccionado == null
                || seleccionado.getIdExamen() == null
                || seleccionado.getIdExamen().getIdExamen() == null) {
            agregarError("examen.seleccioneExamen");
            return;
        }

        ExamenTipoExamen persistida = null;
        if (getEstado() == ESTADO_CRUD.EDICION) {
            persistida = seleccionado.getIdExamenTipoExamen() == null ? null
                    : getDao().buscarPorId(seleccionado.getIdExamenTipoExamen());
            if (persistida == null || persistida.getIdExamen() == null
                    || examenPadre == null || examenPadre.getIdExamen() == null
                    || !examenPadre.getIdExamen().equals(persistida.getIdExamen().getIdExamen())
                    || !examenPadre.getIdExamen().equals(seleccionado.getIdExamen().getIdExamen())) {
                agregarError("examen.seleccioneExamen");
                return;
            }
        } else if (getEstado() != ESTADO_CRUD.CREACION) {
            return;
        }

        UUID idTipoElegido = obtenerIdTipoElegido();
        if (getEstado() == ESTADO_CRUD.EDICION) {
            UUID idPersistido = persistida.getIdTipoExamen() == null
                    ? null : persistida.getIdTipoExamen().getIdTipoExamen();
            UUID idEnSeleccionado = seleccionado.getIdTipoExamen() == null
                    ? null : seleccionado.getIdTipoExamen().getIdTipoExamen();
            if (!Objects.equals(idPersistido, idTipoElegido)
                    || !Objects.equals(idPersistido, idEnSeleccionado)) {
                seleccionado.setIdTipoExamen(persistida.getIdTipoExamen());
                idTipoExamenSeleccionado = idPersistido == null ? null : idPersistido.toString();
                agregarError("tipoExamen", "examen.tipoNoEditable");
                return;
            }
        }
        if (idTipoElegido == null) {
            agregarError("tipoExamen", "examen.seleccioneTipo");
            return;
        }

        TipoExamen tipoPersistido = tipoExamenDAO.buscarPorId(idTipoElegido);
        if (tipoPersistido == null) {
            agregarError("tipoExamen", "examen.seleccioneTipo");
            return;
        }
        if (getEstado() == ESTADO_CRUD.CREACION && !Boolean.TRUE.equals(tipoPersistido.getActivo())) {
            agregarError("tipoExamen", "examen.tipoInactivo");
            return;
        }
        seleccionado.setIdTipoExamen(tipoPersistido);

        long coincidenciasPropias = 0;
        if (getEstado() == ESTADO_CRUD.EDICION) {
            if (persistida.getIdTipoExamen() != null
                    && seleccionado.getIdTipoExamen().getIdTipoExamen()
                            .equals(persistida.getIdTipoExamen().getIdTipoExamen())) {
                coincidenciasPropias = 1;
            }
        }
        long existentes = examenTipoExamenDAO.countByIdExamenAndIdTipoExamen(
                seleccionado.getIdExamen().getIdExamen(),
                seleccionado.getIdTipoExamen().getIdTipoExamen());
        if (existentes > coincidenciasPropias) {
            agregarError("tipoExamen", "examen.tipoDuplicado");
            return;
        }

        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
        if (getEstado() == ESTADO_CRUD.EDICION) {
            getDao().actualizar(seleccionado);
        } else {
            getDao().guardar(seleccionado);
        }
        examenPadre = seleccionado.getIdExamen();
        seleccionado = null;
        filaSeleccionada = null;
        idTipoExamenSeleccionado = null;
        tipoExamenOriginalId = null;
        setEstado(ESTADO_CRUD.LISTADO);
        recargarAsociaciones();
        agregarMensaje("examen.asociacionGuardada", FacesMessage.SEVERITY_INFO);
    }

    public void cancelarAsociacion() {
        seleccionado = null;
        filaSeleccionada = null;
        idTipoExamenSeleccionado = null;
        tipoExamenOriginalId = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void quitarAsociacion(ExamenTipoExamen asociacion, Examen examen) {
        if (asociacion == null || asociacion.getIdExamenTipoExamen() == null
                || examen == null || examen.getIdExamen() == null
                || examenPadre == null
                || !examen.getIdExamen().equals(examenPadre.getIdExamen())) {
            agregarError("examen.errorQuitarAsociacion");
            return;
        }
        try {
            ExamenTipoExamen persistida = getDao().buscarPorId(asociacion.getIdExamenTipoExamen());
            if (persistida == null || persistida.getIdExamen() == null
                    || !examen.getIdExamen().equals(persistida.getIdExamen().getIdExamen())) {
                agregarError("examen.errorQuitarAsociacion");
                return;
            }
            if (!getDao().eliminar(persistida.getIdExamenTipoExamen())) {
                agregarError("examen.errorQuitarAsociacion");
                return;
            }
        } catch (RuntimeException ex) {
            agregarError("examen.errorQuitarAsociacion");
            return;
        }
        if (seleccionado != null && asociacion.getIdExamenTipoExamen()
                .equals(seleccionado.getIdExamenTipoExamen())) {
            cancelarAsociacion();
        }
        recargarAsociaciones();
        agregarMensaje("examen.asociacionQuitada", FacesMessage.SEVERITY_INFO);
    }

    public void nuevo() {
        recargarTiposExamen();
        seleccionado = new ExamenTipoExamen();
        tipoExamenOriginalId = null;
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ExamenTipoExamen seleccionado) {
        this.seleccionado = seleccionado;
        this.tipoExamenOriginalId = seleccionado == null || seleccionado.getIdTipoExamen() == null
                ? null : seleccionado.getIdTipoExamen().getIdTipoExamen();
        this.idTipoExamenSeleccionado = tipoExamenOriginalId == null
                ? null : tipoExamenOriginalId.toString();
        recargarTiposExamen();
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        if (!resolverTipoParaCrudGenerico()) {
            return;
        }
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
        agregarMensaje("examen.asociacionGuardada", FacesMessage.SEVERITY_INFO);
    }

    public void cancelar() {
        seleccionado = null;
        tipoExamenOriginalId = null;
        idTipoExamenSeleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    private void recargarAsociaciones() {
        if (examenPadre == null || examenPadre.getIdExamen() == null) {
            asociaciones = List.of();
            return;
        }
        List<ExamenTipoExamen> encontradas = examenTipoExamenDAO.findByIdExamen(
                examenPadre.getIdExamen(), 0, Integer.MAX_VALUE);
        asociaciones = encontradas == null ? List.of() : encontradas;
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private UUID obtenerIdTipoElegido() {
        if (idTipoExamenSeleccionado != null && !idTipoExamenSeleccionado.isBlank()) {
            return uuidOrNull(idTipoExamenSeleccionado);
        }
        return seleccionado == null || seleccionado.getIdTipoExamen() == null
                ? null : seleccionado.getIdTipoExamen().getIdTipoExamen();
    }

    private UUID uuidOrNull(String id) {
        try {
            return id == null || id.isBlank() ? null : UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean resolverTipoParaCrudGenerico() {
        UUID idTipoElegido = seleccionado.getIdTipoExamen() == null
                ? null : seleccionado.getIdTipoExamen().getIdTipoExamen();
        if (idTipoElegido == null
                && (getEstado() == ESTADO_CRUD.CREACION || tipoExamenOriginalId == null)) {
            return true;
        }
        ExamenTipoExamen persistida = null;
        if (getEstado() == ESTADO_CRUD.EDICION) {
            persistida = seleccionado.getIdExamenTipoExamen() == null
                    ? null : getDao().buscarPorId(seleccionado.getIdExamenTipoExamen());
            UUID idOriginal = persistida == null || persistida.getIdTipoExamen() == null
                    ? null : persistida.getIdTipoExamen().getIdTipoExamen();
            if (persistida == null || !Objects.equals(idOriginal, idTipoElegido)) {
                if (persistida != null) {
                    seleccionado.setIdTipoExamen(persistida.getIdTipoExamen());
                }
                agregarError("idTipoExamen", "examen.tipoNoEditable");
                return false;
            }
        } else if (getEstado() != ESTADO_CRUD.CREACION) {
            return false;
        }
        if (idTipoElegido == null) {
            agregarError("examen.seleccioneTipo");
            return false;
        }
        TipoExamen tipoPersistido = tipoExamenDAO.buscarPorId(idTipoElegido);
        if (tipoPersistido == null) {
            agregarError("idTipoExamen", "examen.seleccioneTipo");
            return false;
        }
        if (getEstado() == ESTADO_CRUD.CREACION && !Boolean.TRUE.equals(tipoPersistido.getActivo())) {
            agregarError("idTipoExamen", "examen.tipoInactivo");
            return false;
        }
        seleccionado.setIdTipoExamen(tipoPersistido);
        return true;
    }

    private void recargarTiposExamen() {
        tipoExamenes = tipoExamenDAO == null ? List.of() : tipoExamenDAO.obtenerTodos();
    }

    private void agregarError(String clave) {
        agregarError(null, clave);
    }

    private void agregarError(String idComponente, String clave) {
        if (facesContext != null) {
            String mensaje = facesContext.getApplication()
                    .getResourceBundle(facesContext, "msg")
                    .getString(clave);
            UIComponent componente = buscarComponente(facesContext.getViewRoot(), idComponente);
            String clientId = componente == null ? null : componente.getClientId(facesContext);
            if (componente instanceof UIInput entrada) {
                entrada.setValid(false);
            }
            facesContext.addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
            facesContext.validationFailed();
        }
    }

    private UIComponent buscarComponente(UIComponent componente, String id) {
        if (componente == null || id == null) {
            return null;
        }
        if (id.equals(componente.getId())) {
            return componente;
        }
        var hijos = componente.getFacetsAndChildren();
        while (hijos.hasNext()) {
            UIComponent encontrado = buscarComponente(hijos.next(), id);
            if (encontrado != null) {
                return encontrado;
            }
        }
        return null;
    }

    private void agregarMensaje(String clave, FacesMessage.Severity severidad) {
        if (facesContext == null) {
            return;
        }
        String mensaje = facesContext.getApplication()
                .getResourceBundle(facesContext, "msg")
                .getString(clave);
        facesContext.addMessage(null,
                new FacesMessage(severidad, mensaje, null));
    }
}
