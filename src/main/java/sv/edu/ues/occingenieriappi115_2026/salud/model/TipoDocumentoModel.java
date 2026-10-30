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
import java.util.Arrays;
import java.util.List;
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
    private CatalogoService catalogoService;

    @EJB
    private TipoDocumentoDAO tipoDocumentoDAO;

    /** Registro enlazado al formulario; es nuevo o proviene de la tabla. */
    private TipoDocumento seleccionado;
    private FormatoRegexSugerido formatoSugerido = FormatoRegexSugerido.PERSONALIZADO;

    public TipoDocumentoModel() {
    }

    public TipoDocumentoModel(TipoDocumentoDAO tipoDocumentoDAO) {
        this.tipoDocumentoDAO = tipoDocumentoDAO;
        this.catalogoService = new CatalogoService(tipoDocumentoDAO);
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

    public FormatoRegexSugerido getFormatoSugerido() {
        return formatoSugerido;
    }

    public void setFormatoSugerido(FormatoRegexSugerido formato) {
        FormatoRegexSugerido nuevoFormato = formato == null
                ? FormatoRegexSugerido.PERSONALIZADO : formato;
        if (seleccionado != null && nuevoFormato != formatoSugerido) {
            if (nuevoFormato.getExpresionRegular() != null) {
                seleccionado.setExpresionRegular(nuevoFormato.getExpresionRegular());
            }
            formatoSugerido = nuevoFormato;
        }
    }

    public List<FormatoRegexSugerido> getFormatosSugeridos() {
        return Arrays.asList(FormatoRegexSugerido.values());
    }

    public void nuevo() {
        // Prepara una entidad transitoria y cambia el formulario a creación.
        seleccionado = new TipoDocumento();
        formatoSugerido = FormatoRegexSugerido.PERSONALIZADO;
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoDocumento seleccionado) {
        // Conserva la fila elegida para editarla durante las peticiones AJAX.
        this.seleccionado = seleccionado;
        formatoSugerido = seleccionado == null ? FormatoRegexSugerido.PERSONALIZADO
                : FormatoRegexSugerido.desdeExpresion(seleccionado.getExpresionRegular());
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) return;
        boolean edicion = getEstado() == ESTADO_CRUD.EDICION;
        try {
            var guardado = catalogoService.guardarTipoDocumento(seleccionado, edicion);
            if (edicion) seleccionado = guardado;
        } catch (ReglaNegocioException ex) {
            marcarError(ex.getCampo(), ex.getMessage());
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
        agregarMensaje(FacesMessage.SEVERITY_INFO, "mensajes.guardado");
    }

    public void cancelar() {
        // Restablece el listado sin escribir cambios en PostgreSQL.
        seleccionado = null;
        formatoSugerido = FormatoRegexSugerido.PERSONALIZADO;
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
