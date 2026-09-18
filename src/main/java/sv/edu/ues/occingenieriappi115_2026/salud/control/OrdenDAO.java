package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.Objects;

/**
 * Criterio inmutable de ordenamiento enviado desde el modelo al DAO.
 *
 * <p>{@code AbstractModel} adapta {@code SortMeta} de PrimeFaces a este record
 * y {@link DefaultDAO} lo traduce a {@code builder.asc} o {@code builder.desc}.
 * Su constructor compacto garantiza que campo y dirección sean válidos.</p>
 *
 * @param campo atributo JPA por el cual ordenar
 * @param direccion sentido ascendente o descendente
 */
public record OrdenDAO(String campo, DireccionOrden direccion) {

    public OrdenDAO {
        if (campo == null || campo.isBlank()) {
            throw new IllegalArgumentException("El campo del ordenamiento es requerido");
        }
        Objects.requireNonNull(direccion, "La direccion del ordenamiento es requerida");
    }
}
