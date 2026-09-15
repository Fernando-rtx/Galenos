package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.List;
import java.util.UUID;

public interface DAOInterface<T> {

    T guardar(T entidad);

    T buscarPorId(UUID id);

    List<T> obtenerTodos();

    List<T> obtenerPagina(int primero, int tamano, List<FiltroDAO> filtros,
            List<OrdenDAO> ordenamientos);

    T actualizar(T entidad);

    boolean eliminar(UUID id);

    long contar();

    long contar(List<FiltroDAO> filtros);

    UUID obtenerId(T entidad);
}
