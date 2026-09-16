package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExamenTipoExamenDAOTest {

    @Test
    public void findByIdExamenUsaNamedQueryParametrosYPaginacion() {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<ExamenTipoExamen> query = Mockito.mock(TypedQuery.class);
        List<ExamenTipoExamen> esperado = List.of(new ExamenTipoExamen(UUID.randomUUID()));
        ExamenTipoExamenDAO cut = new ExamenTipoExamenDAO();
        cut.em = em;

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.findByIdExamen",
                ExamenTipoExamen.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", idExamen)).thenReturn(query);
        Mockito.when(query.setFirstResult(5)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(esperado);

        List<ExamenTipoExamen> resultado = cut.findByIdExamen(idExamen, 5, 10);

        assertSame(esperado, resultado);
        Mockito.verify(em).createNamedQuery(
                "ExamenTipoExamen.findByIdExamen",
                ExamenTipoExamen.class
        );
        Mockito.verify(query).setParameter("idExamen", idExamen);
        Mockito.verify(query).setFirstResult(5);
        Mockito.verify(query).setMaxResults(10);
        Mockito.verify(query).getResultList();
    }

    @Test
    public void countByIdExamenUsaNamedQueryParametroYDevuelveConteo() {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        ExamenTipoExamenDAO cut = new ExamenTipoExamenDAO();
        cut.em = em;

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.countByIdExamen",
                Long.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", idExamen)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(7L);

        long resultado = cut.countByIdExamen(idExamen);

        assertEquals(7L, resultado);
        Mockito.verify(em).createNamedQuery(
                "ExamenTipoExamen.countByIdExamen",
                Long.class
        );
        Mockito.verify(query).setParameter("idExamen", idExamen);
        Mockito.verify(query).getSingleResult();
    }

    @Test
    public void countByIdExamenAndIdTipoExamenUsaNamedQueryParametrosYDevuelveConteo() {
        UUID idExamen = UUID.randomUUID();
        UUID idTipoExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        ExamenTipoExamenDAO cut = new ExamenTipoExamenDAO();
        cut.em = em;

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.countByIdExamenAndIdTipoExamen",
                Long.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", idExamen)).thenReturn(query);
        Mockito.when(query.setParameter("idTipoExamen", idTipoExamen)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(1L);

        long resultado = cut.countByIdExamenAndIdTipoExamen(idExamen, idTipoExamen);

        assertEquals(1L, resultado);
        Mockito.verify(em).createNamedQuery(
                "ExamenTipoExamen.countByIdExamenAndIdTipoExamen",
                Long.class
        );
        Mockito.verify(query).setParameter("idExamen", idExamen);
        Mockito.verify(query).setParameter("idTipoExamen", idTipoExamen);
        Mockito.verify(query).getSingleResult();
    }

    @Test
    public void validaArgumentosObligatoriosYPaginacion() {
        ExamenTipoExamenDAO cut = new ExamenTipoExamenDAO();
        UUID idExamen = UUID.randomUUID();
        UUID idTipoExamen = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> cut.findByIdExamen(null, 0, 10));
        assertThrows(IllegalArgumentException.class,
                () -> cut.findByIdExamen(idExamen, -1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> cut.findByIdExamen(idExamen, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> cut.findByIdExamen(idExamen, 0, -1));

        assertThrows(IllegalArgumentException.class,
                () -> cut.countByIdExamen(null));

        assertThrows(IllegalArgumentException.class,
                () -> cut.countByIdExamenAndIdTipoExamen(null, idTipoExamen));
        assertThrows(IllegalArgumentException.class,
                () -> cut.countByIdExamenAndIdTipoExamen(idExamen, null));
    }

    @Test
    public void findByIdExamenEnvuelveErroresDePersistencia() {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        ExamenTipoExamenDAO cut = new ExamenTipoExamenDAO();
        cut.em = em;

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.findByIdExamen",
                ExamenTipoExamen.class
        )).thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> cut.findByIdExamen(idExamen, 0, 10));

        assertEquals("Error al consultar los tipos de examen por examen", ex.getMessage());
        assertEquals("fallo JPA", ex.getCause().getMessage());
    }

    @Test
    public void countByIdExamenEnvuelveErroresDePersistencia() {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        ExamenTipoExamenDAO cut = new ExamenTipoExamenDAO();
        cut.em = em;

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.countByIdExamen",
                Long.class
        )).thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> cut.countByIdExamen(idExamen));

        assertEquals("Error al contar los tipos de examen por examen", ex.getMessage());
        assertEquals("fallo JPA", ex.getCause().getMessage());
    }

    @Test
    public void countByIdExamenAndIdTipoExamenEnvuelveErroresDePersistencia() {
        UUID idExamen = UUID.randomUUID();
        UUID idTipoExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        ExamenTipoExamenDAO cut = new ExamenTipoExamenDAO();
        cut.em = em;

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.countByIdExamenAndIdTipoExamen",
                Long.class
        )).thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> cut.countByIdExamenAndIdTipoExamen(idExamen, idTipoExamen));

        assertEquals("Error al contar la asociacion examen tipo de examen", ex.getMessage());
        assertEquals("fallo JPA", ex.getCause().getMessage());
    }
}
