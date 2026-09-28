package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;

@Named
@ViewScoped
public class ExamenModel extends AbstractModel<Examen> implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB
    private ExamenDAO examenDAO;
    private Examen seleccionado;

    public ExamenModel() {
    }

    public ExamenModel(ExamenDAO examenDAO) {
        this.examenDAO = examenDAO;
    }

    @Override
    protected ExamenDAO getDao() {
        return examenDAO;
    }

    public Examen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Examen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new Examen();
        seleccionado.setActivo(Boolean.TRUE);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Examen seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void seleccionarFila(SelectEvent<Examen> evento) {
        seleccionar(evento.getObject());
    }

    public void validarNombre(FacesContext contexto, UIComponent componente, Object valor) {
        String nombre = valor == null ? "" : valor.toString().trim();
        if (nombre.isEmpty()) {
            lanzarValidacion(contexto, "examen.nombreRequerido");
        }
        if (nombre.length() < 2) {
            lanzarValidacion(contexto, "examen.nombreMinimo");
        }
        if (nombre.length() > 255) {
            lanzarValidacion(contexto, "examen.nombreMaximo");
        }
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        seleccionado.setNombre(normalizar(seleccionado.getNombre()));
        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
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
}
