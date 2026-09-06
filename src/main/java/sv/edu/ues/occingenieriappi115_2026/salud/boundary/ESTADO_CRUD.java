package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

/**
 * Estados basicos que describen que operacion CRUD esta preparando la vista.
 *
 * <p>Se mantiene pequeno porque por ahora solo estan confirmados los estados
 * observados en clase.</p>
 */
public enum ESTADO_CRUD {

    /**
     * No hay una operacion de creacion o modificacion activa.
     */
    NINGUNO,

    /**
     * La vista esta preparando un registro nuevo.
     */
    CREAR,

    /**
     * La vista esta editando un registro existente.
     */
    MODIFICAR
}
