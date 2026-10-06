package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstadoCrudTest {

    @Test
    void estadoCrudContieneLosTresEstadosDefinidos() {
        assertEquals(List.of(ESTADO_CRUD.LISTADO, ESTADO_CRUD.CREACION, ESTADO_CRUD.EDICION),
                List.of(ESTADO_CRUD.values()));
    }
}
