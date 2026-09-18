package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.Objects;

/**
 * Condición inmutable de filtrado independiente de PrimeFaces y de JPA.
 *
 * <p>{@code AbstractModel} transforma cada {@code FilterMeta} de la tabla en
 * este record; {@link DefaultDAO} lo convierte después en un Predicate de
 * Criteria. El constructor compacto evita campos vacíos y permite valores
 * nulos únicamente para los operadores que consultan nulidad.</p>
 *
 * @param campo atributo JPA, incluso una ruta como {@code relacion.nombre}
 * @param operador comparación que debe aplicarse
 * @param valor valor recibido desde la interfaz
 */
public record FiltroDAO(String campo, OperadorFiltro operador, Object valor) {

    public FiltroDAO {
        if (campo == null || campo.isBlank()) {
            throw new IllegalArgumentException("El campo del filtro es requerido");
        }
        Objects.requireNonNull(operador, "El operador del filtro es requerido");
        if (valor == null && operador != OperadorFiltro.ES_NULO
                && operador != OperadorFiltro.NO_ES_NULO) {
            throw new IllegalArgumentException("El valor del filtro es requerido");
        }
    }
}
