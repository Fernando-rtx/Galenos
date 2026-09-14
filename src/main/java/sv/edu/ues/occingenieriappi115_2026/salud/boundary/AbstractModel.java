package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occingenieriappi115_2026.salud.control.DAOInterface;

/**
 * Modelo base para pantallas CRUD que trabajan con una entidad JPA.
 *
 * <p>La clase es generica para concentrar la logica que no depende de una
 * entidad concreta. Asi, un modelo de TipoExamen o TipoMedioContacto puede
 * reutilizar el mismo flujo de inicializacion, listado y operaciones basicas
 * sin repetir codigo.</p>
 *
 * @param <T> tipo de entidad que administra el modelo concreto.
 */
public abstract class AbstractModel<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Registro que representa la entidad actualmente mostrada o editada por la
     * vista.
     */
    protected T registro;

    /**
     * Modelo lazy que alimenta las tablas PrimeFaces sin cargar todos los datos.
     */
    protected LazyDataModel<T> modelo;

    /**
     * Cantidad de filas solicitadas por pagina.
     */
    private int cantidadRegistros = 50;

    /**
     * Estado CRUD actual de la pantalla: sin operacion, creando o modificando.
     */
    protected ESTADO_CRUD estado = ESTADO_CRUD.NINGUNO;

    /**
     * Inicializa el modelo cuando el contenedor crea el bean.
     *
     * <p>El flujo queda en la clase base porque es comun a las pantallas CRUD:
     * dejar el modelo sin operacion activa, cargar listas auxiliares y preparar
     * el listado principal.</p>
     */
    @PostConstruct
    public void inicializar() {
        estado = ESTADO_CRUD.NINGUNO;
        inicializarListas();
        inicializarRegistros();
    }

    /**
     * Prepara el modelo lazy usando las firmas reales de DAOInterface.
     */
    public void inicializarRegistros() {
        modelo = new LazyDataModel<>() {

            private static final long serialVersionUID = 1L;

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return AbstractModel.this.contar();
            }

            @Override
            public List<T> load(
                    int first,
                    int pageSize,
                    Map<String, SortMeta> sortBy,
                    Map<String, FilterMeta> filterBy) {

                int total = count(filterBy);
                setRowCount(total);

                if (total <= 0) {
                    return List.of();
                }

                return getDAO().findRange(first, pageSize);
            }

            @Override
            public String getRowKey(T object) {
                return AbstractModel.this.getRowKey(object);
            }

            @Override
            public T getRowData(String rowKey) {
                return AbstractModel.this.getRowData(rowKey);
            }
        };

        modelo.setPageSize(cantidadRegistros);
        modelo.setRowCount(contar());
    }

    /**
     * Punto de extension para que una clase hija cargue listas propias.
     *
     * <p>La implementacion base no hace nada porque una entidad generica T no
     * permite saber que catalogos o relaciones necesita cada pantalla.</p>
     */
    public void inicializarListas() {
    }

    /**
     * Prepara el modelo para capturar un registro nuevo.
     */
    public void nuevo() {
        registro = instanciaRegistro();
        estado = ESTADO_CRUD.CREAR;
    }

    /**
     * Prepara el modelo para modificar un registro existente.
     *
     * @param registro entidad seleccionada desde la vista.
     */
    public void seleccionar(T registro) {
        if (registro == null) {
            return;
        }

        this.registro = registro;
        estado = ESTADO_CRUD.MODIFICAR;
    }

    /**
     * Selecciona un registro desde el evento rowSelect de PrimeFaces.
     *
     * @param event evento de seleccion de fila.
     */
    public void seleccionarRegistro(SelectEvent<T> event) {
        if (event == null) {
            return;
        }

        seleccionar(event.getObject());
    }

    /**
     * Guarda la operacion CRUD activa usando el DAO del modelo concreto.
     */
    public void guardar() {
        if (registro == null
                || estado == null
                || estado == ESTADO_CRUD.NINGUNO) {
            return;
        }

        switch (estado) {
            case CREAR:
                getDAO().crear(registro);
                break;
            case MODIFICAR:
                registro = getDAO().modificar(registro);
                break;
            default:
                return;
        }

        estado = ESTADO_CRUD.NINGUNO;
        inicializarRegistros();
    }

    /**
     * Cancela la operacion activa y deja un registro limpio para la vista.
     */
    public void cancelar() {
        estado = ESTADO_CRUD.NINGUNO;
    }

    /**
     * Devuelve la cantidad de registros usando el DAO del modelo concreto.
     *
     * @return total de registros convertido a int para conservar la firma
     * observada en clase.
     */
    public int contar() {
        return (int) getDAO().contar();
    }

    /**
     * Devuelve la llave de fila que PrimeFaces usara para identificar registros.
     *
     * @param object registro de la tabla.
     * @return llave como texto o null si no hay registro/PK.
     */
    public String getRowKey(T object) {
        Object id = getIdByRegistro(object);

        if (id == null) {
            return null;
        }

        return id.toString();
    }

    /**
     * Recupera un registro directamente desde la PK representada por rowKey.
     *
     * @param rowKey llave textual enviada por PrimeFaces.
     * @return registro encontrado o null si rowKey esta vacio.
     */
    public T getRowData(String rowKey) {
        if (rowKey == null || rowKey.isBlank()) {
            return null;
        }

        return getRegistroById(getIdByRowKey(rowKey));
    }

    /**
     * Cada modelo hijo debe proporcionar el DAO concreto mediante el contrato
     * generico DAOInterface, sin acoplar esta clase a implementaciones
     * especificas.
     *
     * @return DAO que administra la entidad T.
     */
    protected abstract DAOInterface<T> getDAO();

    /**
     * Obtiene la llave primaria real de un registro.
     *
     * @param registro entidad administrada.
     * @return identificador real del registro.
     */
    protected abstract Object getIdByRegistro(T registro);

    /**
     * Convierte el rowKey textual al tipo real de la llave primaria.
     *
     * @param rowKey llave textual generada por PrimeFaces.
     * @return identificador con el tipo usado por JPA.
     */
    protected abstract Object getIdByRowKey(String rowKey);

    /**
     * Busca un registro por su llave primaria real.
     *
     * @param id identificador convertido desde rowKey.
     * @return entidad encontrada.
     */
    protected T getRegistroById(Object id) {
        return getDAO().findById(id);
    }

    /**
     * Nombre legible del modelo para vistas y componentes genericos.
     *
     * @return nombre visible de la entidad administrada.
     */
    public abstract String getNombreModelo();

    /**
     * Cada modelo hijo debe crear una instancia vacia de su entidad concreta.
     *
     * <p>AbstractModel no puede construir T directamente porque Java no conserva
     * el tipo generico como una clase instanciable.</p>
     *
     * @return nueva instancia de la entidad administrada.
     */
    public abstract T instanciaRegistro();

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public LazyDataModel<T> getModelo() {
        return modelo;
    }

    public void setModelo(LazyDataModel<T> modelo) {
        this.modelo = modelo;
    }

    public int getCantidadRegistros() {
        return cantidadRegistros;
    }

    public void setCantidadRegistros(int cantidadRegistros) {
        this.cantidadRegistros = cantidadRegistros;
    }

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }
}
