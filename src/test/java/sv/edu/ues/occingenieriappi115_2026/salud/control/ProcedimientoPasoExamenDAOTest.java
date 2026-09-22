package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcedimientoPasoExamenDAOTest {

    private static class TestableDAO extends ProcedimientoPasoExamenDAO {
        private final EntityManager em;
        TestableDAO(EntityManager em) { this.em = em; }
        @Override public EntityManager getEntityManager() { return em; }
    }

    @Test
    void findByIdProcedimientoPasoUsaNamedQuery() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<ProcedimientoPasoExamen> query = Mockito.mock(TypedQuery.class);
        List<ProcedimientoPasoExamen> esperado = List.of(new ProcedimientoPasoExamen());
        TestableDAO dao = new TestableDAO(em);

        Mockito.when(em.createNamedQuery("ProcedimientoPasoExamen.findByIdProcedimientoPaso", ProcedimientoPasoExamen.class)).thenReturn(query);
        Mockito.when(query.setParameter("idProcedimientoPaso", id)).thenReturn(query);
        Mockito.when(query.setFirstResult(0)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(esperado);

        List<ProcedimientoPasoExamen> resultado = dao.findByIdProcedimientoPaso(id, 0, 10);

        assertSame(esperado, resultado);
        Mockito.verify(query).setParameter("idProcedimientoPaso", id);
    }

    @Test
    void countByIdProcedimientoPasoUsaNamedQuery() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        TestableDAO dao = new TestableDAO(em);

        Mockito.when(em.createNamedQuery("ProcedimientoPasoExamen.countByIdProcedimientoPaso", Long.class)).thenReturn(query);
        Mockito.when(query.setParameter("idProcedimientoPaso", id)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(3L);

        long resultado = dao.countByIdProcedimientoPaso(id);

        assertEquals(3L, resultado);
    }

    @Test
    void findByIdExamenUsaNamedQuery() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<ProcedimientoPasoExamen> query = Mockito.mock(TypedQuery.class);
        List<ProcedimientoPasoExamen> esperado = List.of(new ProcedimientoPasoExamen());
        TestableDAO dao = new TestableDAO(em);

        Mockito.when(em.createNamedQuery("ProcedimientoPasoExamen.findByIdExamen", ProcedimientoPasoExamen.class)).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", id)).thenReturn(query);
        Mockito.when(query.setFirstResult(0)).thenReturn(query);
        Mockito.when(query.setMaxResults(5)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(esperado);

        List<ProcedimientoPasoExamen> resultado = dao.findByIdExamen(id, 0, 5);

        assertSame(esperado, resultado);
        Mockito.verify(query).setParameter("idExamen", id);
    }

    @Test
    void countByIdExamenUsaNamedQuery() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
        TestableDAO dao = new TestableDAO(em);

        Mockito.when(em.createNamedQuery("ProcedimientoPasoExamen.countByIdExamen", Long.class)).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", id)).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(2L);

        long resultado = dao.countByIdExamen(id);

        assertEquals(2L, resultado);
    }

    @Test
    void validaArgumentosObligatorios() {
        TestableDAO dao = new TestableDAO(Mockito.mock(EntityManager.class));
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> dao.findByIdProcedimientoPaso(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdProcedimientoPaso(id, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdProcedimientoPaso(id, 0, 0));

        assertThrows(IllegalArgumentException.class, () -> dao.countByIdProcedimientoPaso(null));

        assertThrows(IllegalArgumentException.class, () -> dao.findByIdExamen(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdExamen(id, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByIdExamen(id, 0, 0));

        assertThrows(IllegalArgumentException.class, () -> dao.countByIdExamen(null));
    }

    @Test
    void findByIdProcedimientoPasoEnvuelveErroresDePersistencia() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TestableDAO dao = new TestableDAO(em);
        Mockito.when(em.createNamedQuery("ProcedimientoPasoExamen.findByIdProcedimientoPaso", ProcedimientoPasoExamen.class))
                .thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dao.findByIdProcedimientoPaso(id, 0, 10));

        assertEquals("Error al consultar los examenes por paso de procedimiento", ex.getMessage());
    }

    @Test
    void findByIdExamenEnvuelveErroresDePersistencia() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TestableDAO dao = new TestableDAO(em);
        Mockito.when(em.createNamedQuery("ProcedimientoPasoExamen.findByIdExamen", ProcedimientoPasoExamen.class))
                .thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dao.findByIdExamen(id, 0, 10));

        assertEquals("Error al consultar los pasos de procedimiento por examen", ex.getMessage());
    }

    @Test
    void countByIdProcedimientoPasoEnvuelveErroresDePersistencia() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TestableDAO dao = new TestableDAO(em);
        Mockito.when(em.createNamedQuery("ProcedimientoPasoExamen.countByIdProcedimientoPaso", Long.class))
                .thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dao.countByIdProcedimientoPaso(id));

        assertEquals("Error al contar los examenes por paso de procedimiento", ex.getMessage());
    }

    @Test
    void countByIdExamenEnvuelveErroresDePersistencia() {
        UUID id = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);
        TestableDAO dao = new TestableDAO(em);
        Mockito.when(em.createNamedQuery("ProcedimientoPasoExamen.countByIdExamen", Long.class))
                .thenThrow(new RuntimeException("fallo JPA"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dao.countByIdExamen(id));

        assertEquals("Error al contar los pasos de procedimiento por examen", ex.getMessage());
    }
}
