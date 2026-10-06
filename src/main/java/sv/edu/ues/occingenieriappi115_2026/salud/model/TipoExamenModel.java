package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
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
    private TipoExamenDAO tipoExamenDAO;

    /** Entidad nueva o fila seleccionada que se enlaza con el diálogo XHTML. */
    private TipoExamen seleccionado;

    public TipoExamenModel() {
    }

    public TipoExamenModel(TipoExamenDAO tipoExamenDAO) {
        this.tipoExamenDAO = tipoExamenDAO;
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
        if (seleccionado == null) {
            return;
        }
        String nombre = normalizar(seleccionado.getNombre());
        if (nombre == null || nombre.isEmpty()) {
            marcarNombreRequerido();
            agregarCallbackGuardado(false);
            return;
        }
        seleccionado.setNombre(nombre);
        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
        agregarMensaje(FacesMessage.SEVERITY_INFO, "mensajes.guardado");
        agregarCallbackGuardado(true);
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
        String nombre = normalizar(valor == null ? null : valor.toString());
        if (nombre == null || nombre.isEmpty()) {
            lanzarValidacion(contexto, "tipoExamen.nombreRequerido");
        }
        // Permite editar otros campos de registros antiguos cuyo nombre no
        // cumple las reglas actuales, siempre que el nombre no cambie.
        if (getEstado() == ESTADO_CRUD.EDICION && seleccionado != null
                && nombre.equals(normalizar(seleccionado.getNombre()))) {
            return;
        }
        if (nombre.length() < 2) {
            lanzarValidacion(contexto, "tipoExamen.nombreMinimo");
        }
        if (nombre.length() > 255) {
            lanzarValidacion(contexto, "tipoExamen.nombreMaximo");
        }
        if (nombreDuplicado(nombre)) {
            lanzarValidacion(contexto, "tipoExamen.nombreDuplicado");
        }
    }

    private boolean nombreDuplicado(String nombre) {
        List<TipoExamen> existentes = getDao().obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("nombre", OperadorFiltro.IGUAL, nombre)), List.of());
        if (existentes == null || existentes.isEmpty()) {
            return false;
        }
        UUID idActual = seleccionado == null ? null : seleccionado.getIdTipoExamen();
        return existentes.stream().anyMatch(tipo -> tipo.getIdTipoExamen() == null
                || !tipo.getIdTipoExamen().equals(idActual));
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private void marcarNombreRequerido() {
        FacesContext contexto = contextoActual();
        if (contexto == null) {
            return;
        }
        contexto.validationFailed();
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg")
                .getString("tipoExamen.nombreRequerido");
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
