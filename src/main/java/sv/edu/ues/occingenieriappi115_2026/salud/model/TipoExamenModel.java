package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import sv.edu.ues.occingenieriappi115_2026.salud.control.CatalogoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

/**
 * Backing bean JSF del catálogo de tipos de examen.
 *
 * <p>{@code @Named} lo expone como {@code tipoExamenModel}; {@code @ViewScoped}
 * conserva selección y estado durante las peticiones AJAX de la misma vista.
 * CDI inyecta el EJB {@link TipoExamenDAO}. La superclase atiende paginación,
 * filtros y orden, mientras este bean coordina creación, edición y cancelación.</p>
 */
@Named
@ViewScoped
public class TipoExamenModel extends AbstractModel<TipoExamen> implements Serializable {

    /** Versión necesaria para serializar el bean de ámbito de vista. */
    private static final long serialVersionUID = 1L;
    @EJB
    private CatalogoService catalogoService;

    @EJB
    private TipoExamenDAO tipoExamenDAO;

    /** Entidad nueva o fila seleccionada que se enlaza con el diálogo XHTML. */
    private TipoExamen seleccionado;

    public TipoExamenModel() {
    }

    public TipoExamenModel(TipoExamenDAO tipoExamenDAO) {
        this.tipoExamenDAO = tipoExamenDAO;
        this.catalogoService = new CatalogoService(tipoExamenDAO);
    }

    @Override
    protected TipoExamenDAO getDao() {
        return tipoExamenDAO;
    }

    public TipoExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(TipoExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        // La entidad permanece transitoria hasta que el usuario pulsa Guardar.
        seleccionado = new TipoExamen();
        seleccionado.setActivo(Boolean.FALSE);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoExamen seleccionado) {
        // La tabla entrega la fila seleccionada y el diálogo entra en edición.
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void seleccionar(SelectEvent<TipoExamen> evento) {
        if (evento != null) {
            seleccionar(evento.getObject());
        }
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) return;
        boolean edicion = getEstado() == ESTADO_CRUD.EDICION;
        try {
            var guardado = catalogoService.guardarTipoExamen(seleccionado, edicion);
            if (edicion) seleccionado = guardado;
        } catch (ReglaNegocioException ex) {
            marcarNombreError(ex.getMessage());
            agregarCallbackGuardado(false);
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
        agregarMensaje(FacesMessage.SEVERITY_INFO, "mensajes.guardado"); agregarCallbackGuardado(true);
    }

    public void cancelar() {
        // Descarta la selección local; no ejecuta ninguna operación de base.
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Valida nombre obligatorio, longitud y duplicados. Se invoca desde el
     * campo del XHTML antes de llegar a {@link #guardar()}.
     *
     * @param contexto contexto Faces activo
     * @param componente componente que dispara la validación
     * @param valor nombre ingresado
     */
    public void validarNombre(FacesContext contexto, UIComponent componente, Object valor) {
        try {
            catalogoService.validarNombreTipoExamen(valor == null ? null : valor.toString(),
                    getEstado() == ESTADO_CRUD.EDICION && seleccionado != null ? seleccionado.getIdTipoExamen() : null,
                    getEstado() == ESTADO_CRUD.EDICION && seleccionado != null ? seleccionado.getNombre() : null);
        } catch (ReglaNegocioException ex) {
            lanzarValidacion(contexto, ex.getMessage());
        }
    }


    private void marcarNombreError(String clave) {
        FacesContext contexto = contextoActual();
        if (contexto == null) {
            return;
        }
        contexto.validationFailed();
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg")
                .getString(clave);
        UIComponent componente = buscarComponente(contexto.getViewRoot(), "nombre");
        if (componente instanceof UIInput entrada) {
            entrada.setValid(false);
        }
        String clientId = componente == null ? null : componente.getClientId(contexto);
        contexto.addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }

    private UIComponent buscarComponente(UIComponent componente, String id) {
        if (componente == null) {
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

    private void agregarCallbackGuardado(boolean guardado) {
        FacesContext contexto = contextoActual();
        if (contexto != null && contexto.getPartialViewContext().isAjaxRequest()) {
            PrimeFaces.current().ajax().addCallbackParam("guardado", guardado);
        }
    }

    private FacesContext contextoActual() {
        try {
            return FacesContext.getCurrentInstance();
        } catch (LinkageError ex) {
            // En pruebas unitarias no existe una implementación JSF instalada.
            return null;
        }
    }
}
