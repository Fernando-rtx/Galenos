package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Unitaria: ejecuta DAO y Criteria de producción con dependencias JPA sintéticas. */
class MedioContactoDAOTest {
    private final EntityManager em = mock(EntityManager.class);
    private final CriteriaBuilder cb = mock(CriteriaBuilder.class);
    private final CriteriaQuery<MedioContacto> criteria = mock(CriteriaQuery.class);
    private final Root<MedioContacto> root = mock(Root.class);
    private final TypedQuery<MedioContacto> query = mock(TypedQuery.class);
    private final UUID persona = UUID.fromString("00000000-0000-4000-8000-000000000041");
    private final UUID tipo = UUID.fromString("00000000-0000-4000-8000-000000000051");
    private final MedioContactoDAO dao = new MedioContactoDAO() {
        @Override protected EntityManager getEntityManager() { return em; }
    };

    @BeforeEach
    void preparar() {
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(MedioContacto.class)).thenReturn(criteria);
        when(criteria.from(MedioContacto.class)).thenReturn(root);
        when(em.createQuery(criteria)).thenReturn(query);
        when(query.setFirstResult(0)).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        doReturn(MedioContacto.class).when(root).getJavaType();
        var metamodel = mock(Metamodel.class);
        when(em.getMetamodel()).thenReturn(metamodel);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Path campo(Path parent, Class owner, String name, Class type) {
        var metamodel = em.getMetamodel();
        ManagedType managed = metamodel.managedType(owner);
        if (managed == null) {
            managed = mock(ManagedType.class);
            doReturn(managed).when(metamodel).managedType(owner);
        }
        var attribute = mock(SingularAttribute.class);
        when(managed.getAttribute(name)).thenReturn(attribute);
        Path path = mock(Path.class);
        doReturn(path).when(parent).get(name);
        doReturn(type).when(path).getJavaType();
        return path;
    }

    private void filtros() {
        var p = campo(root, MedioContacto.class, "idPersona", Persona.class);
        var idPersona = campo(p, Persona.class, "idPersona", UUID.class);
        var t = campo(root, MedioContacto.class, "idTipoMedioContacto", TipoMedioContacto.class);
        var idTipo = campo(t, TipoMedioContacto.class, "idTipoMedioContacto", UUID.class);
        var valor = campo(root, MedioContacto.class, "valor", String.class);
        var predPersona = mock(Predicate.class);
        when(cb.equal(idPersona, persona)).thenReturn(predPersona);
        var predTipo = mock(Predicate.class);
        when(cb.equal(idTipo, tipo)).thenReturn(predTipo);
        var predValor = mock(Predicate.class);
        when(cb.equal(valor, "contacto sintetico")).thenReturn(predValor);
    }

    @Test void duplicadoUsaPersonaTipoYValorExactosConLimiteEnJpa() {
        filtros();
        when(query.getResultList()).thenReturn(List.of(new MedioContacto(UUID.randomUUID())));
        assertTrue(dao.existeContacto(persona, tipo, "contacto sintetico", null));
        verify(cb).equal(root.get("idPersona").get("idPersona"), persona);
        verify(cb).equal(root.get("idTipoMedioContacto").get("idTipoMedioContacto"), tipo);
        verify(cb).equal(root.get("valor"), "contacto sintetico");
        verify(criteria).where(any(Predicate[].class));
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(1);
    }

    @Test void consultaValidaSinCoincidenciasEsFalse() {
        filtros();
        when(query.getResultList()).thenReturn(List.of());
        assertFalse(dao.existeContacto(persona, tipo, "contacto sintetico", null));
        verify(cb, never()).notEqual(any(), any());
    }

    @Test void edicionExcluyeSoloElUuidActualEnLaConsulta() {
        filtros();
        UUID actual = UUID.randomUUID();
        var id = campo(root, MedioContacto.class, "idMedioContacto", UUID.class);
        var predExcluir = mock(Predicate.class);
        when(cb.notEqual(id, actual)).thenReturn(predExcluir);
        when(query.getResultList()).thenReturn(List.of());
        assertFalse(dao.existeContacto(persona, tipo, "contacto sintetico", actual));
        verify(cb).notEqual(id, actual);
        var predicates = org.mockito.ArgumentCaptor.forClass(Predicate[].class);
        verify(criteria).where(predicates.capture());
        assertEquals(4, predicates.getValue().length);
    }

    @Test void obligatoriosInvalidosNoEjecutanJpa() {
        clearInvocations(em);
        assertThrows(IllegalArgumentException.class, () -> dao.existeContacto(null, tipo, "x", null));
        assertThrows(IllegalArgumentException.class, () -> dao.existeContacto(persona, null, "x", null));
        for (String valor : new String[]{null, "", " \t\n"}) {
            assertThrows(IllegalArgumentException.class, () -> dao.existeContacto(persona, tipo, valor, null));
        }
        verifyNoInteractions(em);
    }

    @Test void falloDeCriteriaNoSeConvierteEnFalse() {
        var causa = new IllegalStateException("Criteria sintetico no disponible");
        when(em.getCriteriaBuilder()).thenThrow(causa);
        assertSame(causa, assertThrows(IllegalStateException.class,
                () -> dao.existeContacto(persona, tipo, "x", null)));
    }

    @Test void falloDePersistenciaConservaInstanciaYCausa() {
        filtros();
        var causa = new java.sql.SQLException("Conexion sintetica interrumpida");
        var fallo = new PersistenceException("Fallo sintetico", causa);
        when(query.getResultList()).thenThrow(fallo);
        var recibido = assertThrows(PersistenceException.class,
                () -> dao.existeContacto(persona, tipo, "contacto sintetico", null));
        assertSame(fallo, recibido);
        assertSame(causa, recibido.getCause());
    }

    @Test void falloAlCrearConsultaNoSeConfundeConInexistencia() {
        filtros();
        var fallo = new PersistenceException("EntityManager sintetico no disponible");
        when(em.createQuery(criteria)).thenThrow(fallo);
        assertSame(fallo, assertThrows(PersistenceException.class,
                () -> dao.existeContacto(persona, tipo, "contacto sintetico", null)));
    }

    @Test void listaEspecializadaConservaLaPersonaYPropagaFallos() {
        var lista = List.of(new MedioContacto(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(MedioContacto.class))).thenReturn(query);
        when(query.setParameter("persona", persona)).thenReturn(query);
        when(query.getResultList()).thenReturn(lista);
        assertSame(lista, dao.buscarPorPersona(persona));
        verify(query).setParameter("persona", persona);
        var fallo = new PersistenceException("Fallo sintetico");
        when(query.getResultList()).thenThrow(fallo);
        assertSame(fallo, assertThrows(PersistenceException.class, () -> dao.buscarPorPersona(persona)));
        clearInvocations(em);
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorPersona(null));
        verifyNoInteractions(em);
    }
}
