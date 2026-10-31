package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import sv.edu.ues.occingenieriappi115_2026.salud.control.CatalogoService;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;

@Named
@ViewScoped
public class ExamenModel extends AbstractModel<Examen> implements Serializable {
    private static final long serialVersionUID = 1L;
    @EJB private CatalogoService catalogoService;
    @EJB
    private ExamenDAO examenDAO;
    private Examen seleccionado;

    public ExamenModel() {
    }

    public ExamenModel(ExamenDAO examenDAO) {
        this.examenDAO = examenDAO;
        this.catalogoService = new CatalogoService(examenDAO);
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
        try { catalogoService.validarNombreExamen(valor == null ? null : valor.toString(), seleccionado == null ? null : seleccionado.getIdExamen()); }
        catch (ReglaNegocioException ex) { lanzarValidacion(contexto, ex.getMessage()); }
    }



    public void guardar() {
        if (seleccionado == null || getEstado() == ESTADO_CRUD.LISTADO) return;
        try {
            boolean edicion = getEstado() == ESTADO_CRUD.EDICION;
            var guardado = catalogoService.guardarExamen(seleccionado, edicion);
            if (edicion) seleccionado = guardado;
        } catch (ReglaNegocioException ex) { registrarError(ex.getMessage());return; }
        setEstado(ESTADO_CRUD.LISTADO);agregarMensaje(FacesMessage.SEVERITY_INFO, "examen.guardado");
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
