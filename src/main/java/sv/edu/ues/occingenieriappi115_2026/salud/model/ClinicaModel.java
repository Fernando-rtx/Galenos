package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ClinicaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;

/**
 * Backing bean JSF del catálogo de clínicas.
 */
@Named
@ViewScoped
public class ClinicaModel extends AbstractModel<Clinica> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Clinica seleccionado;

    @Inject
    public ClinicaModel(ClinicaDAO clinicaDAO) {
        super(clinicaDAO);
    }

    public Clinica getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Clinica seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        seleccionado = new Clinica();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Clinica seleccionado) {
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        if (seleccionado == null) {
            return;
        }
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }
}
