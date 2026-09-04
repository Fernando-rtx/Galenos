package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenResultado;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ExamenTipoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba estructural de los DAO concretos.
 *
 * <p>No duplica pruebas CRUD porque esa logica se cubre en DefaultDAOTest. Aqui
 * se verifica que cada DAO concreto declare su entidad, use GalenoPU y devuelva
 * el EntityManager inyectado.</p>
 */
public class ConcreteDAOTest {

    /**
     * Caso de prueba que relaciona una clase DAO con su entidad esperada.
     *
     * @param daoClass clase del DAO concreto.
     * @param entityClass entidad que debe pasar a DefaultDAO.
     */
    private record DaoCase(
            Class<? extends DefaultDAO<?>> daoClass,
            Class<?> entityClass) {
    }

    @Test
    public void testDaoConcretosDeclaranEntidadYEntityManager()
            throws Exception {
        // Escenario: todos los DAO concretos siguen el mismo patron.
        // Esperado: @Stateless, @LocalBean, GalenoPU y entityClass correcto.
        List<DaoCase> casos = List.of(
                new DaoCase(ClinicaDAO.class, Clinica.class),
                new DaoCase(ConsultaDAO.class, Consulta.class),
                new DaoCase(
                        ConsultaProcedimientoDAO.class,
                        ConsultaProcedimiento.class
                ),
                new DaoCase(
                        ConsultaProcedimientoPasoDAO.class,
                        ConsultaProcedimientoPaso.class
                ),
                new DaoCase(DocumentoDAO.class, Documento.class),
                new DaoCase(ExamenDAO.class, Examen.class),
                new DaoCase(ExamenResultadoDAO.class, ExamenResultado.class),
                new DaoCase(ExamenTipoExamenDAO.class, ExamenTipoExamen.class),
                new DaoCase(MedioContactoDAO.class, MedioContacto.class),
                new DaoCase(OrdenExamenDAO.class, OrdenExamen.class),
                new DaoCase(PersonaDAO.class, Persona.class),
                new DaoCase(PersonaRolDAO.class, PersonaRol.class),
                new DaoCase(ProcedimientoDAO.class, Procedimiento.class),
                new DaoCase(ProcedimientoPasoDAO.class, ProcedimientoPaso.class),
                new DaoCase(
                        ProcedimientoPasoExamenDAO.class,
                        ProcedimientoPasoExamen.class
                ),
                new DaoCase(
                        ProcedimientoPasoSecuenciaDAO.class,
                        ProcedimientoPasoSecuencia.class
                ),
                new DaoCase(RolDAO.class, Rol.class),
                new DaoCase(TipoDocumentoDAO.class, TipoDocumento.class),
                new DaoCase(TipoExamenDAO.class, TipoExamen.class),
                new DaoCase(
                        TipoMedioContactoDAO.class,
                        TipoMedioContacto.class
                )
        );

        Field entityClassField =
                DefaultDAO.class.getDeclaredField("entityClass");
        entityClassField.setAccessible(true);

        for (DaoCase caso : casos) {
            DefaultDAO<?> dao =
                    caso.daoClass().getDeclaredConstructor().newInstance();

            assertTrue(caso.daoClass().isAnnotationPresent(Stateless.class));
            assertTrue(caso.daoClass().isAnnotationPresent(LocalBean.class));
            assertSame(caso.entityClass(), entityClassField.get(dao));

            // Simula la inyeccion de EntityManager que haria el contenedor
            // Jakarta EE al ejecutar la aplicacion real.
            EntityManager mockEM = Mockito.mock(EntityManager.class);
            Field emField = caso.daoClass().getDeclaredField("em");
            emField.setAccessible(true);
            emField.set(dao, mockEM);

            PersistenceContext persistenceContext =
                    emField.getAnnotation(PersistenceContext.class);

            assertNotNull(persistenceContext);
            assertEquals("GalenoPU", persistenceContext.unitName());
            assertSame(mockEM, dao.getEntityManager());
        }
    }
}
