package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Arrays;
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
    private TipoMedioContactoDAO tipoMedioContactoDAO;

    /** Entidad actualmente enlazada al formulario del diálogo. */
    private TipoMedioContacto seleccionado;

    @Inject
    transient FacesContext facesContext;

    public enum FormatoContacto {
        TELEFONO("^[267][0-9]{7}$"),
        RESIDENCIAL("^2[0-9]{7}$"),
        CORREO("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"),
        ACTUAL(null);

        private final String expresion;

        FormatoContacto(String expresion) {
            this.expresion = expresion;
        }

        public String getClave() {
            return "tipoMedioContacto.formato." + name();
        }
    }

    public FormatoContacto getFormatoContacto() {
        String expresion = seleccionado == null ? null : seleccionado.getExpresionRegular();
        if (expresion == null || expresion.isBlank()) {
            return null;
        }
        for (FormatoContacto formato : FormatoContacto.values()) {
            if (expresion.equals(formato.expresion)) {
                return formato;
            }
        }
        return FormatoContacto.ACTUAL;
    }

    public void setFormatoContacto(FormatoContacto formato) {
        // Preserve custom/legacy rules unless the user explicitly chooses another format.
        if (seleccionado != null && formato != FormatoContacto.ACTUAL
                && formato != getFormatoContacto()) {
            seleccionado.setExpresionRegular(formato == null ? null : formato.expresion);
        }
    }

    public List<FormatoContacto> getFormatosContacto() {
        return Arrays.stream(FormatoContacto.values())
                .filter(formato -> formato != FormatoContacto.ACTUAL
                        || getFormatoContacto() == FormatoContacto.ACTUAL)
                .toList();
    }

    public TipoMedioContactoModel() {
    }

    public TipoMedioContactoModel(TipoMedioContactoDAO tipoMedioContactoDAO) {
        this.tipoMedioContactoDAO = tipoMedioContactoDAO;
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
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoMedioContacto seleccionado) {
        // Cambia a edición con la fila entregada por PrimeFaces.
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        // Delega en el DAO común según CREACION o EDICION.
        if (seleccionado == null) {
            return;
        }
        if (seleccionado.getNombre() == null || seleccionado.getNombre().isBlank()) {
            errorCampo("nombre", "tipoMedioContacto.nombreRequerido");
            return;
        }
        if (seleccionado.getIndicaciones() == null || seleccionado.getIndicaciones().isBlank()) {
            errorCampo("indicaciones", "tipoMedioContacto.indicacionesRequeridas");
            return;
        }
        if (getFormatoContacto() == null) {
            errorCampo("formatoContacto", "tipoMedioContacto.formatoRequerido");
            return;
        }
        seleccionado.setNombre(seleccionado.getNombre().strip());
        seleccionado.setIndicaciones(seleccionado.getIndicaciones().strip());
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
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
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
