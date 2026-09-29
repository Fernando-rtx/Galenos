package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.TipoExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;

/**
 * Backing bean JSF del catálogo de tipos de examen.
 *
 * <p>{@code @Named} lo expone como {@code tipoExamenModel}; {@code @ViewScoped}
 * conserva selección y estado durante las peticiones AJAX de la misma vista.
 * CDI inyecta el EJB {@link TipoExamenDAO}. La superclase atiende paginación,
 * filtros y orden, mientras este bean coordina creación, edición y cancelación.</p>
 */
@Named
@ViewScoped
public class TipoExamenModel extends AbstractModel<TipoExamen> implements Serializable {

    /** Versión necesaria para serializar el bean de ámbito de vista. */
    private static final long serialVersionUID = 1L;

    @EJB
    private TipoExamenDAO tipoExamenDAO;

    /** Entidad nueva o fila seleccionada que se enlaza con el diálogo XHTML. */
    private TipoExamen seleccionado;

    public TipoExamenModel() {
    }

    public TipoExamenModel(TipoExamenDAO tipoExamenDAO) {
        this.tipoExamenDAO = tipoExamenDAO;
    }

    @Override
    protected TipoExamenDAO getDao() {
        return tipoExamenDAO;
    }

    public TipoExamen getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(TipoExamen seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void nuevo() {
        // La entidad permanece transitoria hasta que el usuario pulsa Guardar.
        seleccionado = new TipoExamen();
        seleccionado.setActivo(Boolean.TRUE);
        setEstado(ESTADO_CRUD.CREACION);
    }

    public void seleccionar(TipoExamen seleccionado) {
        // La tabla entrega la fila seleccionada y el diálogo entra en edición.
        this.seleccionado = seleccionado;
        setEstado(ESTADO_CRUD.EDICION);
    }

    public void guardar() {
        // CREACION delega en persist; EDICION delega en merge. LISTADO no escribe.
        if (seleccionado == null) {
            return;
        }
        seleccionado.setNombre(normalizar(seleccionado.getNombre()));
        seleccionado.setObservaciones(normalizar(seleccionado.getObservaciones()));
        switch (getEstado()) {
            case CREACION -> getDao().guardar(seleccionado);
            case EDICION -> seleccionado = getDao().actualizar(seleccionado);
            case LISTADO -> {
            }
        }
        setEstado(ESTADO_CRUD.LISTADO);
    }

    public void cancelar() {
        // Descarta la selección local; no ejecuta ninguna operación de base.
        seleccionado = null;
        setEstado(ESTADO_CRUD.LISTADO);
    }

    /**
     * Valida nombre obligatorio, longitud y duplicados. Se invoca desde el
     * campo del XHTML antes de llegar a {@link #guardar()}.
     *
     * @param contexto contexto Faces activo
     * @param componente componente que dispara la validación
     * @param valor nombre ingresado
     */
    public void validarNombre(FacesContext contexto, UIComponent componente, Object valor) {
        String nombre = normalizar(valor == null ? null : valor.toString());
        if (nombre == null || nombre.isEmpty()) {
            lanzarValidacion(contexto, "tipoExamen.nombreRequerido");
        }
        // Permite editar otros campos de registros antiguos cuyo nombre no
        // cumple las reglas actuales, siempre que el nombre no cambie.
        if (getEstado() == ESTADO_CRUD.EDICION && seleccionado != null
                && nombre.equals(normalizar(seleccionado.getNombre()))) {
            return;
        }
        if (nombre.length() < 2) {
            lanzarValidacion(contexto, "tipoExamen.nombreMinimo");
        }
        if (nombre.length() > 255) {
            lanzarValidacion(contexto, "tipoExamen.nombreMaximo");
        }
        if (nombreDuplicado(nombre)) {
            lanzarValidacion(contexto, "tipoExamen.nombreDuplicado");
        }
    }

    private boolean nombreDuplicado(String nombre) {
        List<TipoExamen> existentes = getDao().obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("nombre", OperadorFiltro.IGUAL, nombre)), List.of());
        if (existentes == null || existentes.isEmpty()) {
            return false;
        }
        UUID idActual = seleccionado == null ? null : seleccionado.getIdTipoExamen();
        return existentes.stream().anyMatch(tipo -> tipo.getIdTipoExamen() == null
                || !tipo.getIdTipoExamen().equals(idActual));
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
