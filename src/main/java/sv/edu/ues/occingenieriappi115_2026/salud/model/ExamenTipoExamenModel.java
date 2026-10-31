package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import sv.edu.ues.occingenieriappi115_2026.salud.control.CatalogoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException;
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
    @EJB private CatalogoService catalogoService;

    private ExamenTipoExamen seleccionado;
    private ExamenTipoExamen filaSeleccionada;
    private List<Examen> examenes;
    private List<TipoExamen> tipoExamenes;
    private Examen examenPadre;
    private String idTipoExamenSeleccionado;
    private UUID tipoExamenOriginalId;
    private UUID examenOriginalId;
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
        this.catalogoService = new CatalogoService(examenTipoExamenDAO,examenDAO,tipoExamenDAO);
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
        if(seleccionado==null||seleccionado.getIdExamen()==null||seleccionado.getIdExamen().getIdExamen()==null){agregarError("examen.seleccioneExamen");return;}
        boolean edicion=getEstado()==ESTADO_CRUD.EDICION;
        if(!edicion&&getEstado()!=ESTADO_CRUD.CREACION)return;
        var old=edicion&&seleccionado.getIdExamenTipoExamen()!=null?getDao().buscarPorId(seleccionado.getIdExamenTipoExamen()):null;
        if(edicion&&(old==null||old.getIdExamen()==null||examenPadre==null||!Objects.equals(examenPadre.getIdExamen(),old.getIdExamen().getIdExamen())||!Objects.equals(examenPadre.getIdExamen(),seleccionado.getIdExamen().getIdExamen()))){agregarError("examen.seleccioneExamen");return;}
        UUID tipo=obtenerIdTipoElegido();UUID actual=seleccionado.getIdTipoExamen()==null?null:seleccionado.getIdTipoExamen().getIdTipoExamen();
        if(edicion&&(!Objects.equals(tipo,tipoExamenOriginalId)||!Objects.equals(tipo,actual))){seleccionado.setIdTipoExamen(old.getIdTipoExamen());idTipoExamenSeleccionado=tipoExamenOriginalId==null?null:tipoExamenOriginalId.toString();agregarError("tipoExamen","examen.tipoNoEditable");return;}
        seleccionado.setIdTipoExamen(tipo==null?null:new TipoExamen(tipo));
        try {catalogoService.guardarRelacionExamen(seleccionado,edicion,examenPadre==null?null:examenPadre.getIdExamen(),tipoExamenOriginalId);}
        catch(ReglaNegocioException ex){agregarError("tipoExamen",ex.getMessage());return;}
        examenPadre=seleccionado.getIdExamen();seleccionado=null;filaSeleccionada=null;idTipoExamenSeleccionado=null;tipoExamenOriginalId=null;setEstado(ESTADO_CRUD.LISTADO);recargarAsociaciones();agregarMensaje("examen.asociacionGuardada",FacesMessage.SEVERITY_INFO);
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
        idTipoExamenSeleccionado = null;
        examenOriginalId = null;
        tipoExamenOriginalId = null;
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ExamenTipoExamen seleccionado) {
        this.seleccionado = seleccionado;
        examenOriginalId = seleccionado == null || seleccionado.getIdExamen() == null
                ? null : seleccionado.getIdExamen().getIdExamen();
        this.tipoExamenOriginalId = seleccionado == null || seleccionado.getIdTipoExamen() == null
                ? null : seleccionado.getIdTipoExamen().getIdTipoExamen();
        this.idTipoExamenSeleccionado = tipoExamenOriginalId == null
                ? null : tipoExamenOriginalId.toString();
        recargarTiposExamen();
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if(seleccionado==null||getEstado()==ESTADO_CRUD.LISTADO)return;
        try {var saved=catalogoService.guardarRelacionExamen(seleccionado,getEstado()==ESTADO_CRUD.EDICION,examenOriginalId,tipoExamenOriginalId);if(getEstado()==ESTADO_CRUD.EDICION)seleccionado=saved;}
        catch(ReglaNegocioException ex){agregarError(ex.getCampo(),ex.getMessage());return;}
        setEstado(ESTADO_CRUD.LISTADO);agregarMensaje("examen.asociacionGuardada",FacesMessage.SEVERITY_INFO);
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
