package sv.edu.ues.occingenieriappi115_2026.salud.model;

/**
 * Estado de interacción compartido por los backing beans CRUD.
 *
 * <p>{@link #LISTADO} muestra la tabla; {@link #CREACION} mantiene una entidad
 * nueva; {@link #EDICION} conserva la fila seleccionada. Los métodos
 * {@code nuevo}, {@code seleccionar}, {@code guardar} y {@code cancelar}
 * realizan las transiciones.</p>
 */
public enum ESTADO_CRUD {
    /** Vista en modo consulta de registros. */
    LISTADO,
    /** Formulario preparado para persistir una entidad nueva. */
    CREACION,
    /** Formulario preparado para actualizar una entidad existente. */
    EDICION
}
