package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.MatchMode;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DireccionOrden;
import sv.edu.ues.occingenieriappi115_2026.salud.control.FiltroDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OperadorFiltro;
import sv.edu.ues.occingenieriappi115_2026.salud.control.OrdenDAO;

/**
 * Adaptador genérico entre tablas lazy de PrimeFaces y la capa DAO.
 *
 * <p>
 * {@link LazyDataModel} evita cargar una tabla completa de PostgreSQL:
 * PrimeFaces solicita el conteo y únicamente la página visible. Este modelo
 * convierte {@link FilterMeta} y {@link SortMeta} en {@link FiltroDAO} y
 * {@link OrdenDAO}; el DAO concreto delega en {@code DefaultDAO}, que crea la
 * consulta Criteria ejecutada por JPA.</p>
 *
 * <p>
 * Recorrido: PrimeFaces → AbstractModel → DAO concreto → DefaultDAO → JPA →
 * PostgreSQL. También traduce entre el UUID de la entidad y el {@code rowKey}
 * textual requerido para selección de filas.</p>
 *
 * @param <T> entidad mostrada por la tabla
 */
public abstract class AbstractModel<T> extends LazyDataModel<T> {

    /**
     * Contrato de persistencia inyectado por el modelo concreto.
     */
    private final DAOInterface<T> dao;
    /**
     * Estado que decide si la vista lista, crea o edita.
     */
    private ESTADO_CRUD estado = ESTADO_CRUD.LISTADO;

    protected AbstractModel(DAOInterface<T> dao) {
        this.dao = Objects.requireNonNull(dao, "El DAO es requerido");
    }

    protected DAOInterface<T> getDao() {
        return dao;
    }

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = Objects.requireNonNull(estado, "El estado es requerido");
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        // PrimeFaces usa este total para calcular cuántas páginas mostrar.
        return limitarConteo(dao.contar(convertirFiltros(filterBy)));
    }

    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy,
            Map<String, FilterMeta> filterBy) {
        // Solo se solicita al DAO el segmento visible; filtros y ordenamientos
        // se conservan para que el conteo y la consulta sean coherentes.
        List<FiltroDAO> filtros = convertirFiltros(filterBy);
        setRowCount(limitarConteo(dao.contar(filtros)));
        return dao.obtenerPagina(first, pageSize, filtros, convertirOrdenamientos(sortBy));
    }

    @Override
    public String getRowKey(T objeto) {
        // El UUID estable permite a PrimeFaces reconocer una fila entre AJAX.
        UUID id = dao.obtenerId(objeto);
        return id == null ? null : id.toString();
    }

    @Override
    public T getRowData(String rowKey) {
        // La selección recibida desde el navegador se resuelve otra vez por ID.
        if (rowKey == null || rowKey.isBlank()) {
            return null;
        }
        try {
            return dao.buscarPorId(UUID.fromString(rowKey));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private List<FiltroDAO> convertirFiltros(Map<String, FilterMeta> filterBy) {
        // Se desacopla la API de PrimeFaces de la capa JPA mediante records.
        List<FiltroDAO> resultado = new ArrayList<>();
        if (filterBy == null) {
            return resultado;
        }
        filterBy.values().stream()
                .filter(FilterMeta::isActive)
                .forEach(meta -> {
                    String campo = meta.getField();
                    if (campo != null && !campo.isBlank() && meta.getFilterValue() != null) {
                        resultado.add(new FiltroDAO(campo, convertirOperador(meta.getMatchMode()),
                                meta.getFilterValue()));
                    }
                });
        return resultado;
    }

    private List<OrdenDAO> convertirOrdenamientos(Map<String, SortMeta> sortBy) {
        // La prioridad natural de SortMeta preserva el orden múltiple de columnas.
        List<SortMeta> activos = sortBy == null ? List.of() : sortBy.values().stream()
                .filter(SortMeta::isActive)
                .sorted(Comparator.naturalOrder())
                .toList();
        return activos.stream()
                .filter(meta -> meta.getField() != null && !meta.getField().isBlank())
                .map(meta -> new OrdenDAO(meta.getField(), meta.getOrder() == SortOrder.DESCENDING
                ? DireccionOrden.DESCENDENTE : DireccionOrden.ASCENDENTE))
                .toList();
    }

    private OperadorFiltro convertirOperador(MatchMode modo) {
        if (modo == null) {
            return OperadorFiltro.IGUAL;
        }
        return switch (modo) {
            case CONTAINS ->
                OperadorFiltro.CONTIENE;
            case STARTS_WITH ->
                OperadorFiltro.INICIA_CON;
            case ENDS_WITH ->
                OperadorFiltro.TERMINA_CON;
            case NOT_CONTAINS, NOT_STARTS_WITH, NOT_ENDS_WITH, NOT_EXACT, NOT_EQUALS ->
                OperadorFiltro.DISTINTO;
            case LESS_THAN ->
                OperadorFiltro.MENOR_QUE;
            case LESS_THAN_EQUALS ->
                OperadorFiltro.MENOR_O_IGUAL;
            case GREATER_THAN ->
                OperadorFiltro.MAYOR_QUE;
            case GREATER_THAN_EQUALS ->
                OperadorFiltro.MAYOR_O_IGUAL;
            default ->
                OperadorFiltro.IGUAL;
        };
    }

    private int limitarConteo(long conteo) {
        return conteo > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) conteo;
    }

    /**
     * Lanza una validación con el mensaje i18n indicado. Centraliza el acceso al
     * bundle {@code msg} para que los modelos concretos solo definan la regla.
     *
     * @param contexto contexto Faces activo
     * @param clave clave del bundle de mensajes
     * @throws ValidatorException siempre, con el mensaje resuelto
     */
    protected void lanzarValidacion(FacesContext contexto, String clave) {
        String mensaje = contexto
                .getApplication()
                .getResourceBundle(contexto, "msg")
                .getString(clave);
        throw new ValidatorException(
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        mensaje,
                        mensaje
                )
        );
    }
}
