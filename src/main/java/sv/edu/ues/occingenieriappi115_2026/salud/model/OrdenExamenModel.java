package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ConsultaProcedimientoPasoDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;

@Named
@ViewScoped
public class OrdenExamenModel extends AbstractModel<OrdenExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private OrdenExamenDAO ordenExamenDAO;
    @EJB
    private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;
    private OrdenExamen seleccionado;
    private List<ConsultaProcedimientoPaso> consultaProcedimientoPasos;
    private String idPasoContexto;
    private ConsultaProcedimientoPaso pasoContexto;

    public OrdenExamenModel() {
    }

    public OrdenExamenModel(OrdenExamenDAO ordenExamenDAO,
            ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO) {
        this.ordenExamenDAO = ordenExamenDAO;
        this.consultaProcedimientoPasoDAO = consultaProcedimientoPasoDAO;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        this.consultaProcedimientoPasos = consultaProcedimientoPasoDAO.obtenerTodos();
    }

    @Override
    protected OrdenExamenDAO getDao() {
        return ordenExamenDAO;
    }

    public String getIdPasoContexto() {
        return idPasoContexto;
    }

    public void setIdPasoContexto(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        this.idPasoContexto = id;
        try {
            ConsultaProcedimientoPaso paso =
                    consultaProcedimientoPasoDAO.buscarPorId(UUID.fromString(id));
            pasoContexto = paso;
            consultaProcedimientoPasos = paso == null ? List.of() : List.of(paso);
        } catch (IllegalArgumentException ex) {
            // id inválido: se conserva la lista completa cargada en el constructor
        }
    }

    public OrdenExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(OrdenExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<ConsultaProcedimientoPaso> getConsultaProcedimientoPasos() {
        return consultaProcedimientoPasos;
    }

    public void nuevo() {
        seleccionado = new OrdenExamen();
        if (pasoContexto != null && pasoContexto.getIdConsultaProcedimientoPaso() != null) {
            seleccionado.setIdConsultaProcedimientoPaso(pasoContexto);
        }
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(OrdenExamen seleccionado) {
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
