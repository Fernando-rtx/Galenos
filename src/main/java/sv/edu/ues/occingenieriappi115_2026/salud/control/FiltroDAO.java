package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.Objects;

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
