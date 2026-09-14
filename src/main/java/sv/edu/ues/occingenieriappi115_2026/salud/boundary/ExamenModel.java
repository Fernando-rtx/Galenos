package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenTipoExamenDAOInterface;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

@Named
@ViewScoped
public class ExamenModel extends AbstractModel<Examen>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ExamenDAO examenDAO;

    @Inject
    private ExamenTipoExamenDAOInterface examenTipoExamenDAO;

    @Inject
    private TipoExamenDAO tipoExamenDAO;

    private Examen examenSeleccionado;

    private TipoExamen tipoExamenSeleccionado;

    private LazyDataModel<ExamenTipoExamen> modeloDetalle;

    private LazyDataModel<TipoExamen> modeloTiposExamen;

    private int cantidadRegistrosDetalle = 10;

    private int cantidadRegistrosTiposExamen = 10;

    @Override
    protected DAOInterface<Examen> getDAO() {
        return examenDAO;
    }

    @Override
    public void inicializarListas() {
        inicializarTiposExamen();
    }

    @Override
    public void inicializarRegistros() {
        super.inicializarRegistros();
        inicializarDetalle();
    }

    public void inicializarDetalle() {
        modeloDetalle = new LazyDataModel<>() {

            private static final long serialVersionUID = 1L;

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return ExamenModel.this.contarDetalle();
            }

            @Override
            public List<ExamenTipoExamen> load(
                    int first,
                    int pageSize,
                    Map<String, SortMeta> sortBy,
                    Map<String, FilterMeta> filterBy) {

                UUID idExamen = getIdExamenSeleccionado();

                if (idExamen == null || examenTipoExamenDAO == null) {
                    setRowCount(0);
                    return List.of();
                }

                int total = count(filterBy);
                setRowCount(total);

                if (total <= 0) {
                    return List.of();
                }

                return examenTipoExamenDAO.findByIdExamen(
                        idExamen,
                        first,
                        pageSize
                );
            }

            @Override
            public String getRowKey(ExamenTipoExamen object) {
                return ExamenModel.this.getRowKeyDetalle(object);
            }

            @Override
            public ExamenTipoExamen getRowData(String rowKey) {
                return ExamenModel.this.getRowDataDetalle(rowKey);
            }
        };

        modeloDetalle.setPageSize(cantidadRegistrosDetalle);
        modeloDetalle.setRowCount(contarDetalle());
    }

    public void inicializarTiposExamen() {
        modeloTiposExamen = new LazyDataModel<>() {

            private static final long serialVersionUID = 1L;

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return ExamenModel.this.contarTiposExamen();
            }

            @Override
            public List<TipoExamen> load(
                    int first,
                    int pageSize,
                    Map<String, SortMeta> sortBy,
                    Map<String, FilterMeta> filterBy) {

                if (tipoExamenDAO == null) {
                    setRowCount(0);
                    return List.of();
                }

                int total = count(filterBy);
                setRowCount(total);

                if (total <= 0) {
                    return List.of();
                }

                return tipoExamenDAO.findRange(first, pageSize);
            }

            @Override
            public String getRowKey(TipoExamen object) {
                return ExamenModel.this.getRowKeyTipoExamen(object);
            }

            @Override
            public TipoExamen getRowData(String rowKey) {
                return ExamenModel.this.getRowDataTipoExamen(rowKey);
            }
        };

        modeloTiposExamen.setPageSize(cantidadRegistrosTiposExamen);
        modeloTiposExamen.setRowCount(contarTiposExamen());
    }

    public int contarDetalle() {
        UUID idExamen = getIdExamenSeleccionado();

        if (idExamen == null || examenTipoExamenDAO == null) {
            return 0;
        }

        return (int) examenTipoExamenDAO.countByIdExamen(idExamen);
    }

    public int contarTiposExamen() {
        if (tipoExamenDAO == null) {
            return 0;
        }

        return (int) tipoExamenDAO.contar();
    }

    public void prepararAsociacion() {
        tipoExamenSeleccionado = null;
        inicializarTiposExamen();
    }

    public void asociarTipoExamen() {
        UUID idExamen = getIdExamenSeleccionado();
        UUID idTipoExamen = getIdTipoExamenSeleccionado();

        if (idExamen == null) {
            agregarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "examen.seleccioneExamen"
            );
            return;
        }

        if (idTipoExamen == null) {
            agregarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "examen.seleccioneTipo"
            );
            return;
        }

        long existentes = examenTipoExamenDAO
                .countByIdExamenAndIdTipoExamen(idExamen, idTipoExamen);

        if (existentes > 0) {
            agregarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "examen.tipoDuplicado"
            );
            return;
        }

        ExamenTipoExamen asociacion = new ExamenTipoExamen();
        asociacion.setIdExamenTipoExamen(UUID.randomUUID());
        asociacion.setFechaCreacion(new Date());
        asociacion.setIdExamen(examenSeleccionado);
        asociacion.setIdTipoExamen(tipoExamenSeleccionado);

        examenTipoExamenDAO.crear(asociacion);
        tipoExamenSeleccionado = null;
        inicializarDetalle();
        agregarMensaje(
                FacesMessage.SEVERITY_INFO,
                "examen.asociacionCreada"
        );
    }

    public void quitarAsociacion(ExamenTipoExamen asociacion) {
        if (asociacion == null
                || asociacion.getIdExamenTipoExamen() == null
                || examenTipoExamenDAO == null) {
            return;
        }

        examenTipoExamenDAO.eliminar(asociacion.getIdExamenTipoExamen());
        inicializarDetalle();
        agregarMensaje(
                FacesMessage.SEVERITY_INFO,
                "examen.asociacionQuitada"
        );
    }

    private void agregarMensaje(
            FacesMessage.Severity severidad,
            String claveMensaje) {

        FacesContext context = getFacesContext();

        if (context == null) {
            return;
        }

        String texto = obtenerMensaje(context, claveMensaje);
        context.addMessage(null, new FacesMessage(severidad, texto, texto));
    }

    private String obtenerMensaje(FacesContext context, String claveMensaje) {
        try {
            ResourceBundle bundle =
                    context.getApplication().getResourceBundle(context, "msg");
            return bundle.getString(claveMensaje);

        } catch (MissingResourceException | NullPointerException ex) {
            return claveMensaje;
        }
    }

    private FacesContext getFacesContext() {
        try {
            return FacesContext.getCurrentInstance();

        } catch (LinkageError ex) {
            return null;
        }
    }

    public void seleccionarExamen(SelectEvent<Examen> event) {
        if (event == null) {
            return;
        }

        setExamenSeleccionado(event.getObject());
    }

    @Override
    public void seleccionar(Examen registro) {
        if (registro == null) {
            return;
        }

        super.seleccionar(registro);
        setExamenSeleccionado(registro);
    }

    @Override
    public void nuevo() {
        super.nuevo();
        setExamenSeleccionado(null);
    }

    @Override
    public void guardar() {
        boolean operacionActiva = registro != null
                && estado != null
                && estado != ESTADO_CRUD.NINGUNO;

        super.guardar();

        if (operacionActiva) {
            setExamenSeleccionado(registro);
        }
    }

    public void eliminar(Examen examen) {
        if (examen == null || examen.getIdExamen() == null) {
            return;
        }

        getDAO().eliminar(examen.getIdExamen());

        if (esMismoExamen(examen, examenSeleccionado)) {
            examenSeleccionado = null;
        }

        if (esMismoExamen(examen, registro)) {
            registro = null;
            estado = ESTADO_CRUD.NINGUNO;
        }

        inicializarRegistros();
    }

    private boolean esMismoExamen(Examen primero, Examen segundo) {
        return primero != null
                && segundo != null
                && primero.getIdExamen() != null
                && primero.getIdExamen().equals(segundo.getIdExamen());
    }

    @Override
    public Examen instanciaRegistro() {
        Examen nuevo = new Examen();
        nuevo.setIdExamen(UUID.randomUUID());
        nuevo.setActivo(Boolean.TRUE);
        return nuevo;
    }

    @Override
    protected Object getIdByRegistro(Examen registro) {
        if (registro == null) {
            return null;
        }

        return registro.getIdExamen();
    }

    @Override
    protected Object getIdByRowKey(String rowKey) {
        return UUID.fromString(rowKey);
    }

    public String getRowKeyDetalle(ExamenTipoExamen detalle) {
        if (detalle == null || detalle.getIdExamenTipoExamen() == null) {
            return null;
        }

        return detalle.getIdExamenTipoExamen().toString();
    }

    public ExamenTipoExamen getRowDataDetalle(String rowKey) {
        if (rowKey == null
                || rowKey.isBlank()
                || examenTipoExamenDAO == null) {
            return null;
        }

        return examenTipoExamenDAO.findById(UUID.fromString(rowKey));
    }

    public String getRowKeyTipoExamen(TipoExamen tipoExamen) {
        if (tipoExamen == null || tipoExamen.getIdTipoExamen() == null) {
            return null;
        }

        return tipoExamen.getIdTipoExamen().toString();
    }

    public TipoExamen getRowDataTipoExamen(String rowKey) {
        if (rowKey == null
                || rowKey.isBlank()
                || tipoExamenDAO == null) {
            return null;
        }

        return tipoExamenDAO.findById(UUID.fromString(rowKey));
    }

    public UUID getIdExamenSeleccionado() {
        if (examenSeleccionado == null) {
            return null;
        }

        return examenSeleccionado.getIdExamen();
    }

    public UUID getIdTipoExamenSeleccionado() {
        if (tipoExamenSeleccionado == null) {
            return null;
        }

        return tipoExamenSeleccionado.getIdTipoExamen();
    }

    @Override
    public String getNombreModelo() {
        return "Examen";
    }

    public Examen getExamenSeleccionado() {
        return examenSeleccionado;
    }

    public void setExamenSeleccionado(Examen examenSeleccionado) {
        this.examenSeleccionado = examenSeleccionado;
        inicializarDetalle();
    }

    public TipoExamen getTipoExamenSeleccionado() {
        return tipoExamenSeleccionado;
    }

    public void setTipoExamenSeleccionado(
            TipoExamen tipoExamenSeleccionado) {
        this.tipoExamenSeleccionado = tipoExamenSeleccionado;
    }

    public LazyDataModel<ExamenTipoExamen> getModeloDetalle() {
        return modeloDetalle;
    }

    public void setModeloDetalle(
            LazyDataModel<ExamenTipoExamen> modeloDetalle) {
        this.modeloDetalle = modeloDetalle;
    }

    public int getCantidadRegistrosDetalle() {
        return cantidadRegistrosDetalle;
    }

    public void setCantidadRegistrosDetalle(int cantidadRegistrosDetalle) {
        this.cantidadRegistrosDetalle = cantidadRegistrosDetalle;
    }

    public LazyDataModel<TipoExamen> getModeloTiposExamen() {
        return modeloTiposExamen;
    }

    public void setModeloTiposExamen(
            LazyDataModel<TipoExamen> modeloTiposExamen) {
        this.modeloTiposExamen = modeloTiposExamen;
    }

    public int getCantidadRegistrosTiposExamen() {
        return cantidadRegistrosTiposExamen;
    }

    public void setCantidadRegistrosTiposExamen(
            int cantidadRegistrosTiposExamen) {
        this.cantidadRegistrosTiposExamen = cantidadRegistrosTiposExamen;
    }
}
