package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de herencia de los DAO concretos.
 *
 * <p>
 * Verifica que cada DAO concreto del proyecto herede de {@link DefaultDAO},
 * este anotado como EJB sin estado con vista local sin interfaz y resuelva la
 * clase de entidad que administra mediante el constructor que le pasa a su
 * padre.</p>
 *
 * <p>
 * Es una prueba estructural: valida el diseno fijo que exige la arquitectura
 * del proyecto (DefaultDAO -> DAO concreto) sin necesitar abrir una conexion
 * real a PostgreSQL.</p>
 */
public class ConcreteDAOTest {

    /**
     * Paquete base donde viven todos los DAO del proyecto.
     */
    private static final String PAQUETE_RAFIZ = "sv.edu.ues.occingenieriappi115_2026.salud.control";

    /**
     * DAO concretos que deben existir, cada uno con su entidad asociada.
     */
    private static final List<Class<?>> DAOS_CONCRETOS = List.of(
            PersonaDAO.class,
            DocumentoDAO.class,
            MedioContactoDAO.class,
            TipoMedioContactoDAO.class,
            ConsultaDAO.class,
            ConsultaProcedimientoDAO.class,
            ConsultaProcedimientoPasoDAO.class,
            ProcedimientoDAO.class
    );

    /**
     * Obtiene la clase de entidad declarada en el generico del DAO concreto.
     *
     * <p>
     * DefaultDAO<T> se hereda como DefaultDAO<Entidad>. Al inspeccionar el
     * generico de la superclase se recupera Entidad.class, que es lo que el DAO
     * concreto le paso a {@code super(Entidad.class)}.</p>
     *
     * @param daoClase clase del DAO concreto.
     * @return clase de la entidad administrada o null si no se puede resolver.
     */
    private static Class<?> resolverEntidad(Class<?> daoClase) {
        Type superclase = daoClase.getGenericSuperclass();
        if (superclase instanceof ParameterizedType parametrizado) {
            Type argumento = parametrizado.getActualTypeArguments()[0];
            if (argumento instanceof Class<?> entidad) {
                return entidad;
            }
        }
        return null;
    }

    @Test
    public void testTodosLosDaosExisten() {
        // Escenario: la arquitectura exige 8 DAO para las entidades de Rodrigo.
        // Esperado: los 8 archivos estan en el classpath.
        DAOS_CONCRETOS.forEach(daoClase
                -> assertNotNull(daoClase, "El DAO concreto deberia existir")
        );
    }

    @Test
    public void testHeredanDeDefaultDAO() {
        // Escenario: el DAO generico centraliza la logica de persistencia.
        // Esperado: todo DAO concreto debe extender DefaultDAO para heredar
        // guardar, buscarPorId, obtenerTodos, actualizar, eliminar y contar.
        DAOS_CONCRETOS.forEach(daoClase
                -> assertTrue(
                        DefaultDAO.class.isAssignableFrom(daoClase),
                        daoClase.getSimpleName() + " debe heredar de DefaultDAO"
                )
        );
    }

    @Test
    public void testEstanAnotadosComoEjbStatelessLocalBean() {
        // Escenario: EJB maneja el ciclo de vida y las transacciones del DAO.
        // Esperado: todos estan anotados con @Stateless y @LocalBean.
        DAOS_CONCRETOS.forEach(daoClase -> assertTrue(
                daoClase.isAnnotationPresent(Stateless.class)
                && daoClase.isAnnotationPresent(LocalBean.class),
                daoClase.getSimpleName()
                + " debe tener @Stateless y @LocalBean"
        )
        );
    }

    @Test
    public void testResuelvenLaEntidadCorrecta() {
        // Escenario: cada DAO administra exactamente una entidad del proyecto.
        // Esperado: el generico de la superclase coincide con la entidad que
        // el equipo le asigno al DAO.
        List<Class<?>> entidadesEsperadas = List.of(
                sv.edu.ues.occingenieriappi115_2026.salud.entity.Persona.class,
                sv.edu.ues.occingenieriappi115_2026.salud.entity.Documento.class,
                sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto.class,
                sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto.class,
                sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta.class,
                sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimiento.class,
                sv.edu.ues.occingenieriappi115_2026.salud.entity.ConsultaProcedimientoPaso.class,
                sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento.class
        );

        for (int i = 0; i < DAOS_CONCRETOS.size(); i++) {
            Class<?> entidad = resolverEntidad(DAOS_CONCRETOS.get(i));
            assertEquals(
                    entidadesEsperadas.get(i).getName(),
                    entidad.getName(),
                    DAOS_CONCRETOS.get(i).getSimpleName() + " debe administrar " + entidadesEsperadas.get(i).getSimpleName()
            );
        }
    }

    @Test
    public void testNoLegAcabanLosDaoSinHerencia() {
        // Escenario: la interfaz DAOInterface no debe ser usada directamente
        // por un DAO concreto sin pasar por DefaultDAO.
        // Esperado: ningun DAO concreto implementa DAOInterface directamente.
        DAOS_CONCRETOS.forEach(daoClase -> {
            Class<?>[] interfaces = daoClase.getInterfaces();
            for (Class<?> iface : interfaces) {
                assertTrue(
                        !iface.equals(DAOInterface.class),
                        daoClase.getSimpleName() + " no debe implementar DAOInterface directamente"
                );
            }
        });
    }

    @Test
    public void testTodosEnElMismoPaquete() {
        // Escenario: el proyecto organiza los DAO en el paquete control.
        // Esperado: las 8 clases del DAO concreto viven en control.
        DAOS_CONCRETOS.forEach(daoClase
                -> assertEquals(PAQUETE_RAFIZ,
                        daoClase.getPackageName(),
                        daoClase.getSimpleName() + " debe estar en " + PAQUETE_RAFIZ
                )
        );
    }
}
