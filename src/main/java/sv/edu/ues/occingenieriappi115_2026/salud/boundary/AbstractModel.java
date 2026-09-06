package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
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
     * Registros que alimentan la pagina o tabla de listado.
     */
    protected List<T> registros;

    /**
     * Estado CRUD actual de la pantalla: sin operacion, creando o modificando.
     */
    protected ESTADO_CRUD estado = ESTADO_CRUD.NINGUNO;

    /**
     * Inicializa el modelo cuando el contenedor crea el bean.
     *
     * <p>El flujo queda en la clase base porque es comun a las pantallas CRUD:
     * dejar el modelo sin operacion activa, cargar listas auxiliares y cargar
     * el listado principal.</p>
     */
    @PostConstruct
    public void inicializar() {
        estado = ESTADO_CRUD.NINGUNO;
        inicializarListas();
        inicializarRegistros();
    }

    /**
     * Carga el listado principal usando las firmas reales de DAOInterface.
     *
     * <p>DAOInterface no expone un metodo findAll. Por eso se consulta primero
     * el total y luego se solicita ese rango completo. No se fija un tamano de
     * pagina porque ese valor no esta confirmado todavia en el patron de
     * clase.</p>
     */
    public void inicializarRegistros() {
        long total = getDAO().contar();

        if (total <= 0) {
            registros = List.of();
            return;
        }

        registros = getDAO().findRange(0, (int) total);
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
     * Cada modelo hijo debe proporcionar el DAO concreto mediante el contrato
     * generico DAOInterface, sin acoplar esta clase a implementaciones
     * especificas.
     *
     * @return DAO que administra la entidad T.
     */
    protected abstract DAOInterface<T> getDAO();

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

    public List<T> getRegistros() {
        return registros;
    }

    public void setRegistros(List<T> registros) {
        this.registros = registros;
    }

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }
}
