package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de las consultas propias de ProcedimientoPasoExamenDAO.
 */
public class ProcedimientoPasoExamenDAOTest {

    private ProcedimientoPasoExamenDAO crearDAO(EntityManager em)
            throws Exception {
        ProcedimientoPasoExamenDAO dao = new ProcedimientoPasoExamenDAO();
        var field = ProcedimientoPasoExamenDAO.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(dao, em);
        return dao;
    }

    @Test
    public void testFindByIdProcedimientoPasoIdNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdProcedimientoPaso(null, 0, 10)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdProcedimientoPasoFirstNegativo()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdProcedimientoPaso(
                        UUID.randomUUID(),
                        -1,
                        10
                )
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdProcedimientoPasoMaxCero()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdProcedimientoPaso(
                        UUID.randomUUID(),
                        0,
                        0
                )
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdProcedimientoPasoMaxNegativo()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdProcedimientoPaso(
                        UUID.randomUUID(),
                        0,
                        -1
                )
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdProcedimientoPasoConsultaCorrecta()
            throws Exception {
        UUID idProcedimientoPaso = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        @SuppressWarnings("unchecked")
        TypedQuery<ProcedimientoPasoExamen> query =
                Mockito.mock(TypedQuery.class);

        List<ProcedimientoPasoExamen> esperado = List.of(
                new ProcedimientoPasoExamen(UUID.randomUUID())
        );

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPasoExamen.findByIdProcedimientoPaso",
                ProcedimientoPasoExamen.class
        )).thenReturn(query);
        Mockito.when(query.setParameter(
                "idProcedimientoPaso",
                idProcedimientoPaso
        )).thenReturn(query);
        Mockito.when(query.setFirstResult(5)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(esperado);

        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        List<ProcedimientoPasoExamen> resultado =
                cut.findByIdProcedimientoPaso(idProcedimientoPaso, 5, 10);

        assertSame(esperado, resultado);
        Mockito.verify(em).createNamedQuery(
                "ProcedimientoPasoExamen.findByIdProcedimientoPaso",
                ProcedimientoPasoExamen.class
        );
        Mockito.verify(query).setParameter(
                "idProcedimientoPaso",
                idProcedimientoPaso
        );
        Mockito.verify(query).setFirstResult(5);
        Mockito.verify(query).setMaxResults(10);
        Mockito.verify(query).getResultList();
    }

    @Test
    public void testFindByIdProcedimientoPasoManejoExcepcion()
            throws Exception {
        UUID idProcedimientoPaso = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPasoExamen.findByIdProcedimientoPaso",
                ProcedimientoPasoExamen.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findByIdProcedimientoPaso(
                        idProcedimientoPaso,
                        0,
                        10
                )
        );

        assertEquals(
                "Error al consultar los examenes por paso de procedimiento",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }

    @Test
    public void testCountByIdProcedimientoPasoIdNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.countByIdProcedimientoPaso(null)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testCountByIdProcedimientoPasoConsultaCorrecta()
            throws Exception {
        UUID idProcedimientoPaso = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        @SuppressWarnings("unchecked")
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPasoExamen.countByIdProcedimientoPaso",
                Long.class
        )).thenReturn(query);
        Mockito.when(query.setParameter(
                "idProcedimientoPaso",
                idProcedimientoPaso
        )).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(8L);

        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        long resultado =
                cut.countByIdProcedimientoPaso(idProcedimientoPaso);

        assertEquals(8L, resultado);
        Mockito.verify(em).createNamedQuery(
                "ProcedimientoPasoExamen.countByIdProcedimientoPaso",
                Long.class
        );
        Mockito.verify(query).setParameter(
                "idProcedimientoPaso",
                idProcedimientoPaso
        );
        Mockito.verify(query).getSingleResult();
    }

    @Test
    public void testCountByIdProcedimientoPasoManejoExcepcion()
            throws Exception {
        UUID idProcedimientoPaso = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPasoExamen.countByIdProcedimientoPaso",
                Long.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.countByIdProcedimientoPaso(idProcedimientoPaso)
        );

        assertEquals(
                "Error al contar los examenes por paso de procedimiento",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }

    @Test
    public void testFindByIdExamenIdNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdExamen(null, 0, 10)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdExamenFirstNegativo()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdExamen(UUID.randomUUID(), -1, 10)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdExamenMaxCero()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdExamen(UUID.randomUUID(), 0, 0)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdExamenMaxNegativo()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.findByIdExamen(UUID.randomUUID(), 0, -1)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testFindByIdExamenConsultaCorrecta()
            throws Exception {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        @SuppressWarnings("unchecked")
        TypedQuery<ProcedimientoPasoExamen> query =
                Mockito.mock(TypedQuery.class);

        List<ProcedimientoPasoExamen> esperado = List.of(
                new ProcedimientoPasoExamen(UUID.randomUUID())
        );

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPasoExamen.findByIdExamen",
                ProcedimientoPasoExamen.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", idExamen))
                .thenReturn(query);
        Mockito.when(query.setFirstResult(3)).thenReturn(query);
        Mockito.when(query.setMaxResults(7)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(esperado);

        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        List<ProcedimientoPasoExamen> resultado =
                cut.findByIdExamen(idExamen, 3, 7);

        assertSame(esperado, resultado);
        Mockito.verify(em).createNamedQuery(
                "ProcedimientoPasoExamen.findByIdExamen",
                ProcedimientoPasoExamen.class
        );
        Mockito.verify(query).setParameter("idExamen", idExamen);
        Mockito.verify(query).setFirstResult(3);
        Mockito.verify(query).setMaxResults(7);
        Mockito.verify(query).getResultList();
    }

    @Test
    public void testFindByIdExamenManejoExcepcion()
            throws Exception {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPasoExamen.findByIdExamen",
                ProcedimientoPasoExamen.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findByIdExamen(idExamen, 0, 10)
        );

        assertEquals(
                "Error al consultar los pasos de procedimiento por examen",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }

    @Test
    public void testCountByIdExamenIdNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.countByIdExamen(null)
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testCountByIdExamenConsultaCorrecta()
            throws Exception {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        @SuppressWarnings("unchecked")
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPasoExamen.countByIdExamen",
                Long.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", idExamen))
                .thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(11L);

        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        long resultado = cut.countByIdExamen(idExamen);

        assertEquals(11L, resultado);
        Mockito.verify(em).createNamedQuery(
                "ProcedimientoPasoExamen.countByIdExamen",
                Long.class
        );
        Mockito.verify(query).setParameter("idExamen", idExamen);
        Mockito.verify(query).getSingleResult();
    }

    @Test
    public void testCountByIdExamenManejoExcepcion()
            throws Exception {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        Mockito.when(em.createNamedQuery(
                "ProcedimientoPasoExamen.countByIdExamen",
                Long.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ProcedimientoPasoExamenDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.countByIdExamen(idExamen)
        );

        assertEquals(
                "Error al contar los pasos de procedimiento por examen",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }
}
