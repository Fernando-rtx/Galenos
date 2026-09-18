package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.List;
import java.util.UUID;

/**
 * Contrato genérico de persistencia consumido por la capa de modelos.
 *
 * <p>Desacopla {@code AbstractModel} de los DAO concretos: la vista necesita
 * estas operaciones, pero no conoce cómo JPA las implementa. Las interfaces
 * especializadas extienden este contrato cuando una relación necesita
 * consultas adicionales.</p>
 *
 * @param <T> entidad administrada
 */
public interface DAOInterface<T> {

    /** @param entidad entidad nueva @return entidad persistida */
    T guardar(T entidad);

    /** @param id UUID primario @return entidad encontrada o {@code null} */
    T buscarPorId(UUID id);

    /** @return todos los registros de la entidad */
    List<T> obtenerTodos();

    /**
     * @param primero desplazamiento base cero
     * @param tamano máximo de registros
     * @param filtros condiciones dinámicas
     * @param ordenamientos orden solicitado
     * @return página de entidades
     */
    List<T> obtenerPagina(int primero, int tamano, List<FiltroDAO> filtros,List<OrdenDAO> ordenamientos);

    /** @param entidad entidad con UUID @return instancia administrada resultante */
    T actualizar(T entidad);

    /** @param id UUID primario @return {@code true} si se eliminó */
    boolean eliminar(UUID id);

    /** @return total de registros */
    long contar();

    /** @param filtros condiciones aplicadas @return total filtrado */
    long contar(List<FiltroDAO> filtros);

    /** @param entidad entidad cuyo identificador se solicita @return UUID primario */
    UUID obtenerId(T entidad);
}
