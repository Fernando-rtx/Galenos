package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de las consultas propias de ExamenTipoExamenDAO.
 */
public class ExamenTipoExamenDAOTest {

    private ExamenTipoExamenDAO crearDAO(EntityManager em)
            throws Exception {
        ExamenTipoExamenDAO dao = new ExamenTipoExamenDAO();
        var field = ExamenTipoExamenDAO.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(dao, em);
        return dao;
    }

    @Test
    public void testFindByIdExamenIdNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ExamenTipoExamenDAO cut = crearDAO(em);

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
        ExamenTipoExamenDAO cut = crearDAO(em);

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
        ExamenTipoExamenDAO cut = crearDAO(em);

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
        ExamenTipoExamenDAO cut = crearDAO(em);

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
        TypedQuery<ExamenTipoExamen> query = Mockito.mock(TypedQuery.class);

        List<ExamenTipoExamen> esperado = List.of(
                new ExamenTipoExamen(UUID.randomUUID())
        );

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.findByIdExamen",
                ExamenTipoExamen.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", idExamen))
                .thenReturn(query);
        Mockito.when(query.setFirstResult(5)).thenReturn(query);
        Mockito.when(query.setMaxResults(10)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(esperado);

        ExamenTipoExamenDAO cut = crearDAO(em);

        List<ExamenTipoExamen> resultado =
                cut.findByIdExamen(idExamen, 5, 10);

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
    public void testFindByIdExamenManejoExcepcion()
            throws Exception {
        UUID idExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.findByIdExamen",
                ExamenTipoExamen.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ExamenTipoExamenDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.findByIdExamen(idExamen, 0, 10)
        );

        assertEquals(
                "Error al consultar los tipos de examen por examen",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }

    @Test
    public void testCountByIdExamenIdNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ExamenTipoExamenDAO cut = crearDAO(em);

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
                "ExamenTipoExamen.countByIdExamen",
                Long.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", idExamen))
                .thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(12L);

        ExamenTipoExamenDAO cut = crearDAO(em);

        long resultado = cut.countByIdExamen(idExamen);

        assertEquals(12L, resultado);
        Mockito.verify(em).createNamedQuery(
                "ExamenTipoExamen.countByIdExamen",
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
                "ExamenTipoExamen.countByIdExamen",
                Long.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ExamenTipoExamenDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.countByIdExamen(idExamen)
        );

        assertEquals(
                "Error al contar los tipos de examen por examen",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }

    @Test
    public void testCountByIdExamenAndIdTipoExamenIdExamenNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ExamenTipoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.countByIdExamenAndIdTipoExamen(
                        null,
                        UUID.randomUUID()
                )
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testCountByIdExamenAndIdTipoExamenIdTipoExamenNull()
            throws Exception {
        EntityManager em = Mockito.mock(EntityManager.class);
        ExamenTipoExamenDAO cut = crearDAO(em);

        assertThrows(
                IllegalArgumentException.class,
                () -> cut.countByIdExamenAndIdTipoExamen(
                        UUID.randomUUID(),
                        null
                )
        );

        Mockito.verifyNoInteractions(em);
    }

    @Test
    public void testCountByIdExamenAndIdTipoExamenConsultaCorrecta()
            throws Exception {
        UUID idExamen = UUID.randomUUID();
        UUID idTipoExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        @SuppressWarnings("unchecked")
        TypedQuery<Long> query = Mockito.mock(TypedQuery.class);

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.countByIdExamenAndIdTipoExamen",
                Long.class
        )).thenReturn(query);
        Mockito.when(query.setParameter("idExamen", idExamen))
                .thenReturn(query);
        Mockito.when(query.setParameter("idTipoExamen", idTipoExamen))
                .thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(1L);

        ExamenTipoExamenDAO cut = crearDAO(em);

        long resultado =
                cut.countByIdExamenAndIdTipoExamen(idExamen, idTipoExamen);

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
    public void testCountByIdExamenAndIdTipoExamenManejoExcepcion()
            throws Exception {
        UUID idExamen = UUID.randomUUID();
        UUID idTipoExamen = UUID.randomUUID();
        EntityManager em = Mockito.mock(EntityManager.class);

        Mockito.when(em.createNamedQuery(
                "ExamenTipoExamen.countByIdExamenAndIdTipoExamen",
                Long.class
        )).thenThrow(new RuntimeException("Error simulado"));

        ExamenTipoExamenDAO cut = crearDAO(em);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cut.countByIdExamenAndIdTipoExamen(
                        idExamen,
                        idTipoExamen
                )
        );

        assertEquals(
                "Error al contar la asociacion examen tipo de examen",
                ex.getMessage()
        );
        assertNotNull(ex.getCause());
    }
}
