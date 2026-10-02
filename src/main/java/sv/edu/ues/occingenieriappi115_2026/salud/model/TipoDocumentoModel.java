package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoDocumentoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

/**
 * Backing bean JSF del catálogo {@link TipoDocumento}.
 *
 * <p>CDI lo publica con {@code @Named}, conserva su estado con
 * {@code @ViewScoped} e inyecta {@link TipoDocumentoDAO}. Hereda de
 * {@link AbstractModel} el soporte de tabla lazy y coordina el diálogo CRUD.</p>
 */
@Named
@ViewScoped
public class TipoDocumentoModel extends AbstractModel<TipoDocumento> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private TipoDocumentoDAO tipoDocumentoDAO;

    /** Registro enlazado al formulario; es nuevo o proviene de la tabla. */
    private TipoDocumento seleccionado;

    public TipoDocumentoModel() {
    }

    public TipoDocumentoModel(TipoDocumentoDAO tipoDocumentoDAO) {
        this.tipoDocumentoDAO = tipoDocumentoDAO;
    }

    @Override
    protected TipoDocumentoDAO getDao() {
        return tipoDocumentoDAO;
    }

    public TipoDocumento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(TipoDocumento seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        // Prepara una entidad transitoria y cambia el formulario a creación.
        seleccionado = new TipoDocumento();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoDocumento seleccionado) {
        // Conserva la fila elegida para editarla durante las peticiones AJAX.
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        // El estado decide entre guardar con persist o actualizar con merge.
        if (seleccionado == null) {
            return;
        }
        if (seleccionado.getNombre() == null || seleccionado.getNombre().isBlank()) {
            marcarError("nombre", "tipoDocumento.nombreRequerido");
            return;
        }
        String expresion = seleccionado.getExpresionRegular();
        if (expresion != null && !expresion.isBlank()) {
            try {
                Pattern.compile(expresion);
            } catch (PatternSyntaxException ex) {
                marcarError("expresionRegular", "tipoDocumento.expresionRegularInvalida");
                return;
            }
        }
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
        agregarMensaje(FacesMessage.SEVERITY_INFO, "mensajes.guardado");
    }

    public void cancelar() {
        // Restablece el listado sin escribir cambios en PostgreSQL.
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    private void marcarError(String idComponente, String clave) {
        FacesContext contexto;
        try {
            contexto = FacesContext.getCurrentInstance();
        } catch (LinkageError ex) {
            return;
        }
        if (contexto == null) {
            return;
        }
        contexto.validationFailed();
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg").getString(clave);
        UIComponent componente = buscarComponente(contexto.getViewRoot(), idComponente);
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
}
