package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.PersonaRolDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

@Named
@ViewScoped
public class ConsultaModel extends AbstractModel<Consulta> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ConsultaDAO consultaDAO;
    @EJB
    private PersonaRolDAO personaRolDAO;
    private Consulta seleccionado;
    private List<PersonaRol> personasRoles;

    public ConsultaModel() {
    }

    public ConsultaModel(ConsultaDAO consultaDAO, PersonaRolDAO personaRolDAO) {
        this.consultaDAO = consultaDAO;
        this.personaRolDAO = personaRolDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.personasRoles = personaRolDAO.obtenerTodos();
    }

    @Override
    protected ConsultaDAO getDao() {
        return consultaDAO;
    }

    public Consulta getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Consulta seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<PersonaRol> getPersonasRoles() {
        return personasRoles;
    }

    public void nuevo() {
        seleccionado = new Consulta();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(Consulta seleccionado) {
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
