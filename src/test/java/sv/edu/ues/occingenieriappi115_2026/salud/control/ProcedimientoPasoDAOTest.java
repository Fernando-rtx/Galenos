package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcedimientoPasoDAOTest {

    private static class TestableDAO extends ProcedimientoPasoDAO {
        private final EntityManager em;
        TestableDAO(EntityManager em) { this.em = em; }
        @Override public EntityManager getEntityManager() { return em; }
    }

    @Test
    void findByIdProcedimientoUsaNamedQueryParametrosYPaginacion() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<ProcedimientoPaso> query = Mockito.mock(TypedQuery.class);
        List<ProcedimientoPaso> esperado = List.of(new ProcedimientoPaso());
        TestableDAO dao = new TestableDAO(em);

        Mockito.when(em.createNamedQuery("ProcedimientoPaso.findByIdProcedimiento", ProcedimientoPaso.class)).thenReturn(query);
        Mockito.when(query.setParameter("idProcedimiento", id)).thenReturn(query);
        Mockito.when(query.setFirstResult(5)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(esperado);

        List<ProcedimientoPaso> resultado = dao.findByIdProcedimiento(id, 5, 10);

        assertSame(esperado, resultado);
        Mockito.verify(query).setParameter("idProcedimiento", id);
        Mockito.verify(query).setFirstResult(5);
        Mockito.verify(query).setMaxResults(10);
    }

    @Test
    void countByIdProcedimientoUsaNamedQueryYDevuelveConteo() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        TestableDAO dao = new TestableDAO(em);

        Mockito.when(em.createNamedQuery("ProcedimientoPaso.countByIdProcedimiento", Long.class)).thenReturn(query);
        Mockito.when(query.setParameter("idProcedimiento", id)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(7L);

        long resultado = dao.countByIdProcedimiento(id);

        assertEquals(7L, resultado);
        Mockito.verify(query).setParameter("idProcedimiento", id);
    }

    @Test
    void findByIdProcedimientoValidaArgumentos() {
        TestableDAO dao = new TestableDAO(Mockito.mock(EntityManager.class));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdProcedimiento(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdProcedimiento(UUID.randomUUID(), -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdProcedimiento(UUID.randomUUID(), 0, 0));
    }

    @Test
    void countByIdProcedimientoValidaArgumentoNulo() {
        TestableDAO dao = new TestableDAO(Mockito.mock(EntityManager.class));
        assertThrows(IllegalArgumentException.class, () -> dao.countByIdProcedimiento(null));
    }

    @Test
    void findByIdProcedimientoEnvuelveErroresDePersistencia() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TestableDAO dao = new TestableDAO(em);
        Mockito.when(em.createNamedQuery("ProcedimientoPaso.findByIdProcedimiento", ProcedimientoPaso.class))
                .thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dao.findByIdProcedimiento(id, 0, 10));

        assertEquals("Error al consultar los pasos por procedimiento", ex.getMessage());
    }

    @Test
    void countByIdProcedimientoEnvuelveErroresDePersistencia() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TestableDAO dao = new TestableDAO(em);
        Mockito.when(em.createNamedQuery("ProcedimientoPaso.countByIdProcedimiento", Long.class))
                .thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dao.countByIdProcedimiento(id));

        assertEquals("Error al contar los pasos por procedimiento", ex.getMessage());
    }
}
