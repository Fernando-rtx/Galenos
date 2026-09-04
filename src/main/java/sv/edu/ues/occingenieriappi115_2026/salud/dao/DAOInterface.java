package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import java.util.List;

/**
 * Contrato generico para las operaciones basicas de persistencia.
 *
 * <p>Esta interfaz representa el punto comun que comparten todos los DAO del
 * proyecto. La implementacion concreta queda en {@link DefaultDAO}, para evitar
 * repetir la misma logica de JPA en cada entidad.</p>
 *
 * @param <T> tipo de entidad JPA que administra el DAO.
 */
public interface DAOInterface<T> {

    /**
     * Guarda una entidad nueva en el contexto de persistencia.
     *
     * @param registro entidad que se desea crear.
     * @throws IllegalArgumentException si el registro es null.
     * @throws IllegalStateException si JPA reporta un error al persistir.
     */
    public void crear(T registro)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Sincroniza los cambios de una entidad existente.
     *
     * @param registro entidad con los datos actualizados.
     * @return instancia administrada devuelta por EntityManager.merge.
     * @throws IllegalArgumentException si el registro es null.
     * @throws IllegalStateException si JPA reporta un error al modificar.
     */
    public T modificar(T registro)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Elimina una entidad usando su llave primaria.
     *
     * @param id identificador unico del registro.
     * @throws IllegalArgumentException si el identificador es null.
     * @throws IllegalStateException si JPA reporta un error al eliminar.
     */
    public void eliminar(Object id)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Busca una entidad por su llave primaria.
     *
     * @param id identificador unico del registro.
     * @return entidad encontrada o null si no existe.
     * @throws IllegalArgumentException si el identificador es null.
     * @throws IllegalStateException si JPA reporta un error al buscar.
     */
    public T findById(Object id)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Consulta un segmento paginado de registros.
     *
     * @param first posicion inicial base cero.
     * @param max cantidad maxima de registros a recuperar.
     * @return lista con los registros encontrados.
     * @throws IllegalArgumentException si first o max estan fuera de rango.
     * @throws IllegalStateException si JPA reporta un error al consultar.
     */
    public List<T> findRange(int first, int max)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Cuenta cuantos registros existen para la entidad administrada.
     *
     * @return total de registros como long, que es el tipo natural de COUNT en
     * JPA.
     * @throws IllegalStateException si JPA reporta un error al contar.
     */
    public long contar()
            throws IllegalStateException;
}
