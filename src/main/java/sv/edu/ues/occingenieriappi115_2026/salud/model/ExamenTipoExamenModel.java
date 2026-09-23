package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenTipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

@Named
@ViewScoped
public class ExamenTipoExamenModel extends AbstractModel<ExamenTipoExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    private ExamenTipoExamen seleccionado;
    private List<Examen> examenes;
    private List<TipoExamen> tipoExamenes;
    private Examen examenPadre;
    private String idTipoExamenSeleccionado;
    private List<ExamenTipoExamen> asociaciones = List.of();
    private final ExamenTipoExamenDAO examenTipoExamenDAO;
    private final TipoExamenDAO tipoExamenDAO;

    @Inject
    public ExamenTipoExamenModel(
            ExamenTipoExamenDAO examenTipoExamenDAO,
            ExamenDAO examenDAO,
            TipoExamenDAO tipoExamenDAO) {
        super(examenTipoExamenDAO);
        this.examenTipoExamenDAO = examenTipoExamenDAO;
        this.tipoExamenDAO = tipoExamenDAO;
        this.examenes = examenDAO.obtenerTodos();
        this.tipoExamenes = tipoExamenDAO.obtenerTodos();
    }

    public ExamenTipoExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(ExamenTipoExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<Examen> getExamenes() {
        return examenes;
    }

    public List<TipoExamen> getTipoExamenes() {
        return tipoExamenes;
    }

    public List<ExamenTipoExamen> getAsociaciones() {
        return asociaciones;
    }

    public String getIdTipoExamenSeleccionado() {
        return idTipoExamenSeleccionado;
    }

    public void setIdTipoExamenSeleccionado(String idTipoExamenSeleccionado) {
        this.idTipoExamenSeleccionado = idTipoExamenSeleccionado;
    }

    public void cargarPorExamen(Examen examen) {
        examenPadre = examen;
        seleccionado = null;
        idTipoExamenSeleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
        recargarAsociaciones();
    }

    public void nuevaAsociacion(Examen examen) {
        examenPadre = examen;
        seleccionado = new ExamenTipoExamen();
        seleccionado.setIdExamen(examen);
        seleccionado.setFechaCreacion(new Date());
        idTipoExamenSeleccionado = null;
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void guardarAsociacion() {
        if (seleccionado != null
                && seleccionado.getIdTipoExamen() == null
                && idTipoExamenSeleccionado != null) {
            seleccionado.setIdTipoExamen(tipoExamenDAO.buscarPorId(
                    UUID.fromString(idTipoExamenSeleccionado)));
        }
        if (seleccionado == null
                || seleccionado.getIdExamen() == null
                || seleccionado.getIdExamen().getIdExamen() == null
                || seleccionado.getIdTipoExamen() == null
                || seleccionado.getIdTipoExamen().getIdTipoExamen() == null) {
            return;
        }

        long existentes = examenTipoExamenDAO.countByIdExamenAndIdTipoExamen(
                seleccionado.getIdExamen().getIdExamen(),
                seleccionado.getIdTipoExamen().getIdTipoExamen());
        if (existentes > 0) {
            return;
        }

        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
        getDao().guardar(seleccionado);
        examenPadre = seleccionado.getIdExamen();
        seleccionado = null;
        idTipoExamenSeleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
        recargarAsociaciones();
    }

    public void cancelarAsociacion() {
        seleccionado = null;
        idTipoExamenSeleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void nuevo() {
        seleccionado = new ExamenTipoExamen();
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(ExamenTipoExamen seleccionado) {
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

    private void recargarAsociaciones() {
        if (examenPadre == null || examenPadre.getIdExamen() == null) {
            asociaciones = List.of();
            return;
        }
        asociaciones = examenTipoExamenDAO.findByIdExamen(
                examenPadre.getIdExamen(), 0, Integer.MAX_VALUE);
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
