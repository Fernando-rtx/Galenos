package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TipoDocumentoDAOTest {

    @Test
    void consultaNombreIgnorandoEspaciosExterioresYMayusculas() {
        UUID id = UUID.randomUUID();
        var fixture = new Fixture(List.of(new TipoDocumento(id)));

        assertTrue(fixture.dao.existeNombreNormalizado("  DUI  ", null));

        verify(fixture.builder).trim(fixture.nombre);
        verify(fixture.builder).lower(fixture.trimmed);
        verify(fixture.builder).equal(fixture.lowered, "dui");
    }

    @Test
    void consultaExcluyeElUuidActualAlEditar() {
        UUID idActual = UUID.randomUUID();
        var fixture = new Fixture(List.of());

        assertFalse(fixture.dao.existeNombreNormalizado("DUI", idActual));

        verify(fixture.builder).notEqual(fixture.idTipo, idActual);
    }

    private static class TestTipoDocumentoDAO extends TipoDocumentoDAO {
        private final EntityManager entityManager;

        TestTipoDocumentoDAO(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        @Override
        protected EntityManager getEntityManager() {
            return entityManager;
        }
    }

    private static class Fixture {
        private final TestTipoDocumentoDAO dao;
        private final CriteriaBuilder builder = mock(CriteriaBuilder.class);
        private final Path<String> nombre = mock(Path.class);
        private final Expression<String> trimmed = mock(Expression.class);
        private final Expression<String> lowered = mock(Expression.class);
        private final Path<UUID> idTipo = mock(Path.class);

        @SuppressWarnings({"unchecked", "rawtypes"})
        Fixture(List<TipoDocumento> encontrados) {
            EntityManager entityManager = mock(EntityManager.class);
            CriteriaQuery<TipoDocumento> criteria = mock(CriteriaQuery.class);
            Root<TipoDocumento> root = mock(Root.class);
            TypedQuery<TipoDocumento> query = mock(TypedQuery.class);
            Predicate porNombre = mock(Predicate.class);
            Predicate excluirActual = mock(Predicate.class);

            when(entityManager.getCriteriaBuilder()).thenReturn(builder);
            when(builder.createQuery(TipoDocumento.class)).thenReturn(criteria);
            when(criteria.from(TipoDocumento.class)).thenReturn(root);
            when(root.<String>get("nombre")).thenReturn(nombre);
            when(builder.trim(nombre)).thenReturn(trimmed);
            when(builder.lower(trimmed)).thenReturn(lowered);
            when(builder.equal(lowered, "dui")).thenReturn(porNombre);
            when(root.<UUID>get("idTipoDocumento")).thenReturn(idTipo);
            when(builder.notEqual(eq(idTipo), any(UUID.class))).thenReturn(excluirActual);
            when(criteria.select(root)).thenReturn(criteria);
            when(criteria.where(any(Predicate[].class))).thenReturn(criteria);
            when(entityManager.createQuery(criteria)).thenReturn(query);
            when(query.setMaxResults(anyInt())).thenReturn(query);
            when(query.getResultList()).thenReturn(encontrados);
            dao = new TestTipoDocumentoDAO(entityManager);
        }
    }
}
