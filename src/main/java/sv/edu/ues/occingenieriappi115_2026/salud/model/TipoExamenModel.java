package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
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
        seleccionado.setActivo(Boolean.TRUE);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoExamen seleccionado) {
        // La tabla entrega la fila seleccionada y el diálogo entra en edición.
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        // CREACION delega en persist; EDICION delega en merge. LISTADO no escribe.
        if (seleccionado == null) {
            return;
        }
        seleccionado.setNombre(normalizar(seleccionado.getNombre()));
        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        // Descarta la selección local; no ejecuta ninguna operación de base.
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void validarNombre(FacesContext contexto, UIComponent componente, Object valor) {
        String nombre = normalizar(valor == null ? null : valor.toString());
        if (nombre == null || nombre.isEmpty()) {
            String mensaje = contexto.getApplication()
                    .getResourceBundle(contexto, "msg")
                    .getString("tipoExamen.nombreRequerido");
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
        }
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
