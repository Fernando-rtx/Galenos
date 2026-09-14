package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de las consultas propias de ProcedimientoPasoDAO.
 */
public class ProcedimientoPasoDAOTest {

    private ProcedimientoPasoDAO crearDAO(EntityManager em)
            throws Exception {
        ProcedimientoPasoDAO dao = new ProcedimientoPasoDAO();
        var field = ProcedimientoPasoDAO.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(dao, em);
        return dao;
    }

    @Test
    public void testFindByIdProcedimientoIdNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdProcedimiento(null, 0, 10)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdProcedimientoFirstNegativo()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdProcedimiento(UUID.randomUUID(), -1, 10)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdProcedimientoMaxCero()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdProcedimiento(UUID.randomUUID(), 0, 0)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdProcedimientoMaxNegativo()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdProcedimiento(UUID.randomUUID(), 0, -1)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdProcedimientoConsultaCorrecta()
            throws Exception {
        UUID idProcedimiento = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        @SuppressWarnings("unchecked")
        TypedQuery<ProcedimientoPaso> query = Mockito.mock(TypedQuery.class);

        List<ProcedimientoPaso> esperado = List.of(
                new ProcedimientoPaso(UUID.randomUUID())
        );

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPaso.findByIdProcedimiento",
                ProcedimientoPaso.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idProcedimiento", idProcedimiento))
                .thenReturn(query);
        Mockito.when(query.setFirstResult(4)).thenReturn(query);
        Mockito.when(query.setMaxResults(12)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(esperado);

        ProcedimientoPasoDAO cut = crearDAO(em);

        List<ProcedimientoPaso> resultado =
                cut.findByIdProcedimiento(idProcedimiento, 4, 12);

        assertSame(esperado, resultado);
        Mockito.verify(em).createNamedQuery(
                "ProcedimientoPaso.findByIdProcedimiento",
                ProcedimientoPaso.class
        );
        Mockito.verify(query).setParameter(
                "idProcedimiento",
                idProcedimiento
        );
        Mockito.verify(query).setFirstResult(4);
        Mockito.verify(query).setMaxResults(12);
        Mockito.verify(query).getResultList();
    }

    @Test
    public void testFindByIdProcedimientoManejoExcepcion()
            throws Exception {
        UUID idProcedimiento = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPaso.findByIdProcedimiento",
                ProcedimientoPaso.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ProcedimientoPasoDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findByIdProcedimiento(idProcedimiento, 0, 10)
        );

        assertEquals(
                "Error al consultar los pasos por procedimiento",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }

    @Test
    public void testCountByIdProcedimientoIdNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.countByIdProcedimiento(null)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testCountByIdProcedimientoConsultaCorrecta()
            throws Exception {
        UUID idProcedimiento = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        @SuppressWarnings("unchecked")
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPaso.countByIdProcedimiento",
                Long.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idProcedimiento", idProcedimiento))
                .thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(6L);

        ProcedimientoPasoDAO cut = crearDAO(em);

        long resultado = cut.countByIdProcedimiento(idProcedimiento);

        assertEquals(6L, resultado);
        Mockito.verify(em).createNamedQuery(
                "ProcedimientoPaso.countByIdProcedimiento",
                Long.class
        );
        Mockito.verify(query).setParameter(
                "idProcedimiento",
                idProcedimiento
        );
        Mockito.verify(query).getSingleResult();
    }

    @Test
    public void testCountByIdProcedimientoManejoExcepcion()
            throws Exception {
        UUID idProcedimiento = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPaso.countByIdProcedimiento",
                Long.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ProcedimientoPasoDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.countByIdProcedimiento(idProcedimiento)
        );

        assertEquals(
                "Error al contar los pasos por procedimiento",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }
}
