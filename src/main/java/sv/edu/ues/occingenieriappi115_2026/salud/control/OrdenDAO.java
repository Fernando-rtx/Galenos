package sv.edu.ues.occingenieriappi115_2026.salud.control;

import java.util.Objects;

public record OrdenDAO(String campo, DireccionOrden direccion) {

    public OrdenDAO {
        if (campo == null || campo.isBlank()) {
            throw new IllegalArgumentException("El campo del ordenamiento es requerido");
        }
        Objects.requireNonNull(direccion, "La direccion del ordenamiento es requerida");
    }
}
