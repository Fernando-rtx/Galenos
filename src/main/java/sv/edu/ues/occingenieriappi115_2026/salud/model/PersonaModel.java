package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;

@Named
@ViewScoped
public class PersonaModel extends AbstractModel<Persona> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[\\p{L}][\\p{L}\\p{M}' -]*$");
    private Persona seleccionado;

    @Inject
    public PersonaModel(PersonaDAO dao) {
        super(dao);
    }

    public Persona getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Persona seleccionado) {
        this.seleccionado = seleccionado;
    }

    public String getIdPersonaContexto() {
        return seleccionado == null || seleccionado.getIdPersona() == null
                ? null : seleccionado.getIdPersona().toString();
    }

    public void setIdPersonaContexto(String id) {
        if (id == null || id.isBlank()) {
            return;
        }

        try {
            Persona persona = getDao().buscarPorId(UUID.fromString(id));
            if (persona != null) {
                seleccionar(persona);
            }
        } catch (IllegalArgumentException ex) {
            seleccionado = null;
            setEstado(ESTADO_CRUD.LISTADO);
        }
    }

    public void nuevo() {
        seleccionado = new Persona();
        seleccionado.setFechaCreacion(new Date());
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Persona seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public Date getFechaMinimaNacimiento() {
        Calendar calendario = Calendar.getInstance();
        calendario.clear();
        calendario.set(1900, Calendar.JANUARY, 1);
        return calendario.getTime();
    }

    public Date getFechaMaximaNacimiento() {
        return new Date();
    }

    public void validarNombre(FacesContext contexto, UIComponent componente, Object valor) {
        String texto = valor == null ? "" : valor.toString().trim();
        String prefijo = "apellidos".equals(componente.getId()) ? "persona.apellidos" : "persona.nombres";

        if (texto.isEmpty()) {
            lanzarValidacion(contexto, prefijo + "Requeridos");
        }
        if (texto.length() < 2) {
            lanzarValidacion(contexto, prefijo + "Minimo");
        }
        if (texto.length() > 255) {
            lanzarValidacion(contexto, prefijo + "Maximo");
        }
        if (!PATRON_NOMBRE.matcher(texto).matches()) {
            lanzarValidacion(contexto, prefijo + "Formato");
        }
    }

    public void validarFechaNacimiento(FacesContext contexto, UIComponent componente, Object valor) {
        if (!(valor instanceof Date fecha)) {
            lanzarValidacion(contexto, "persona.fechaNacimientoRequerida");
            return;
        }
        if (fecha.before(getFechaMinimaNacimiento())) {
            lanzarValidacion(contexto, "persona.fechaNacimientoMinima");
        }
        if (fecha.after(getFechaMaximaNacimiento())) {
            lanzarValidacion(contexto, "persona.fechaNacimientoFutura");
        }
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        seleccionado.setNombres(normalizar(seleccionado.getNombres()));
        seleccionado.setApellidos(normalizar(seleccionado.getApellidos()));
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> { }
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private void lanzarValidacion(FacesContext contexto, String clave) {
        String mensaje = contexto.getApplication().getResourceBundle(contexto, "msg").getString(clave);
        throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, mensaje));
    }
}
