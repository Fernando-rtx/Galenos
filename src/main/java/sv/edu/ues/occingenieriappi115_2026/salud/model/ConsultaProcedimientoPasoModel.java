package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

@Named
@ViewScoped
public class ConsultaProcedimientoPasoModel extends AbstractModel<ConsultaProcedimientoPaso> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ConsultaProcedimientoPaso seleccionado;
    private List<ConsultaProcedimiento> consultaProcedimientos;
    private List<PersonaRol> personasRoles;

    @Inject
    public ConsultaProcedimientoPasoModel(
            ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO,
            ConsultaProcedimientoDAO consultaProcedimientoDAO,
            PersonaRolDAO personaRolDAO) {
        super(consultaProcedimientoPasoDAO);
        this.consultaProcedimientos = consultaProcedimientoDAO.obtenerTodos();
        this.personasRoles = personaRolDAO.obtenerTodos();
    }

    public ConsultaProcedimientoPaso getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ConsultaProcedimientoPaso seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<ConsultaProcedimiento> getConsultaProcedimientos() {
        return consultaProcedimientos;
    }

    public List<PersonaRol> getPersonasRoles() {
        return personasRoles;
    }

    public void nuevo() {
        seleccionado = new ConsultaProcedimientoPaso();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ConsultaProcedimientoPaso seleccionado) {
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
