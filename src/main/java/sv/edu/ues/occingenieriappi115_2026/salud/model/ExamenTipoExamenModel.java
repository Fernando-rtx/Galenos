package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
        this.tipoExamenes = tipoExamenDAO.obtenerTodos();
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
        return tipoExamenes;
    }

    public List<ExamenTipoExamen> getAsociaciones() {
        return asociaciones;
    }

    public String getIdTipoExamenSeleccionado() {
        return idTipoExamenSeleccionado;
    }

    public void setIdTipoExamenSeleccionado(String idTipoExamenSeleccionado) {
        this.idTipoExamenSeleccionado = idTipoExamenSeleccionado;
    }

    public void cargarPorExamen(Examen examen) {
        examenPadre = examen;
        filaSeleccionada = null;
        seleccionado = null;
        idTipoExamenSeleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
        recargarAsociaciones();
    }

    public void nuevaAsociacion(Examen examen) {
        examenPadre = examen;
        filaSeleccionada = null;
        seleccionado = new ExamenTipoExamen();
        seleccionado.setIdExamen(examen);
        seleccionado.setFechaCreacion(new Date());
        idTipoExamenSeleccionado = null;
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
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardarAsociacion() {
        if (seleccionado != null
                && idTipoExamenSeleccionado != null) {
            seleccionado.setIdTipoExamen(idTipoExamenSeleccionado.isBlank()
                    ? null : buscarTipoExamenSeleccionado());
        }

        if (seleccionado == null
                || seleccionado.getIdExamen() == null
                || seleccionado.getIdExamen().getIdExamen() == null) {
            agregarError("examen.seleccioneExamen");
            return;
        }

        if (seleccionado.getIdTipoExamen() == null
                || seleccionado.getIdTipoExamen().getIdTipoExamen() == null) {
            agregarError("examen.seleccioneTipo");
            return;
        }

        long coincidenciasPropias = 0;
        if (getEstado() == ESTADO_CRUD.EDICION) {
            ExamenTipoExamen persistida = seleccionado.getIdExamenTipoExamen() == null ? null
                    : getDao().buscarPorId(seleccionado.getIdExamenTipoExamen());
            if (persistida == null || persistida.getIdExamen() == null
                    || examenPadre == null || examenPadre.getIdExamen() == null
                    || !examenPadre.getIdExamen().equals(persistida.getIdExamen().getIdExamen())
                    || !examenPadre.getIdExamen().equals(seleccionado.getIdExamen().getIdExamen())) {
                agregarError("examen.seleccioneExamen");
                return;
            }
            if (persistida.getIdTipoExamen() != null
                    && seleccionado.getIdTipoExamen().getIdTipoExamen()
                            .equals(persistida.getIdTipoExamen().getIdTipoExamen())) {
                coincidenciasPropias = 1;
            }
        } else if (getEstado() != ESTADO_CRUD.CREACION) {
            return;
        }
        long existentes = examenTipoExamenDAO.countByIdExamenAndIdTipoExamen(
                seleccionado.getIdExamen().getIdExamen(),
                seleccionado.getIdTipoExamen().getIdTipoExamen());
        if (existentes > coincidenciasPropias) {
            agregarError("examen.tipoDuplicado");
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
        setEstado(ESTADO_CRUD.LISTADO);
        recargarAsociaciones();
    }

    public void cancelarAsociacion() {
        seleccionado = null;
        filaSeleccionada = null;
        idTipoExamenSeleccionado = null;
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
        seleccionado = new ExamenTipoExamen();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ExamenTipoExamen seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    private void recargarAsociaciones() {
        if (examenPadre == null || examenPadre.getIdExamen() == null) {
            asociaciones = List.of();
            return;
        }
        asociaciones = examenTipoExamenDAO.findByIdExamen(
                examenPadre.getIdExamen(), 0, Integer.MAX_VALUE);
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private TipoExamen buscarTipoExamenSeleccionado() {
        try {
            return tipoExamenDAO.buscarPorId(UUID.fromString(idTipoExamenSeleccionado));
        } catch (IllegalArgumentException ex) {
            return null;
        }
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
        String mensaje = facesContext.getApplication()
                .getResourceBundle(facesContext, "msg")
                .getString(clave);
        facesContext.addMessage(null,
                new FacesMessage(severidad, mensaje, null));
    }
}
