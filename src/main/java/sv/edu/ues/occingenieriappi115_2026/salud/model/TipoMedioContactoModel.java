package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import sv.edu.ues.occingenieriappi115_2026.salud.control.CatalogoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoMedioContactoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;

/**
 * Backing bean JSF para administrar {@link TipoMedioContacto}.
 *
 * <p>Es serializable porque vive en {@code @ViewScoped}; {@code @Named} permite
 * usarlo desde EL y CDI inyecta {@link TipoMedioContactoDAO}. La carga lazy se
 * hereda de {@link AbstractModel} y este bean controla la selección y estados.</p>
 */
@Named
@ViewScoped
public class TipoMedioContactoModel extends AbstractModel<TipoMedioContacto> implements Serializable {

    private static final long serialVersionUID = 1L;
    @EJB
    private CatalogoService catalogoService;

    @EJB
    private TipoMedioContactoDAO tipoMedioContactoDAO;

    /** Entidad actualmente enlazada al formulario del diálogo. */
    private TipoMedioContacto seleccionado;

    @Inject
    transient FacesContext facesContext;

    private FormatoRegexSugerido formatoSugerido = FormatoRegexSugerido.PERSONALIZADO;

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
        return List.of(FormatoRegexSugerido.values());
    }

    private FormatoRegexSugerido reconocerFormatoActual() {
        String expresion = seleccionado == null ? null : seleccionado.getExpresionRegular();
        return FormatoRegexSugerido.desdeExpresion(expresion);
    }

    public TipoMedioContactoModel() {
    }

    public TipoMedioContactoModel(TipoMedioContactoDAO tipoMedioContactoDAO) {
        this.tipoMedioContactoDAO = tipoMedioContactoDAO;
        this.catalogoService = new CatalogoService(tipoMedioContactoDAO);
    }

    @Override
    protected TipoMedioContactoDAO getDao() {
        return tipoMedioContactoDAO;
    }

    public TipoMedioContacto getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(TipoMedioContacto seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        // Inicia una creación sin persistir todavía.
        seleccionado = new TipoMedioContacto();
        formatoSugerido = FormatoRegexSugerido.PERSONALIZADO;
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoMedioContacto seleccionado) {
        // Cambia a edición con la fila entregada por PrimeFaces.
        this.seleccionado = seleccionado;
        formatoSugerido = reconocerFormatoActual();
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) return;
        boolean edicion = getEstado() == ESTADO_CRUD.EDICION;
        try {
            var guardado = catalogoService.guardarTipoMedioContacto(seleccionado, edicion);
            if (edicion) seleccionado = guardado;
        } catch (ReglaNegocioException ex) {
            errorCampo(ex.getCampo(), ex.getMessage());
            return;
        }
        setEstado(ESTADO_CRUD.LISTADO);
        agregarMensaje(FacesMessage.SEVERITY_INFO, "mensajes.guardado");
    }

    private void errorCampo(String id, String clave) {
        FacesContext contexto = facesContext == null ? FacesContext.getCurrentInstance() : facesContext;
        if (contexto == null) {
            throw new IllegalStateException("FacesContext es requerido para validar el formulario");
        }
        UIComponent componente = buscarComponente(contexto.getViewRoot(), id);
        String clientId = componente == null ? id : componente.getClientId(contexto);
        if (componente instanceof UIInput input) {
            input.setValid(false);
        }
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg").getString(clave);
        contexto.addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, mensaje));
        contexto.validationFailed();
    }

    private UIComponent buscarComponente(UIComponent componente, String id) {
        if (componente == null || id.equals(componente.getId())) {
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

    public void cancelar() {
        // Limpia el formulario y regresa a LISTADO sin tocar la base.
        seleccionado = null;
        formatoSugerido = FormatoRegexSugerido.PERSONALIZADO;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
