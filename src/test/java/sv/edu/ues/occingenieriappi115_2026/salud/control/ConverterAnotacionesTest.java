package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Named;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConverterAnotacionesTest {

    private static final List<Class<?>> CONVERTERS = List.of(
            ConsultaConverter.class,
            ConsultaProcedimientoPasoConverter.class,
            ExamenConverter.class,
            OrdenExamenConverter.class,
            TipoExamenConverter.class,
            PersonaRolConverter.class,
            ConsultaProcedimientoConverter.class,
            RolConverter.class,
            ProcedimientoConverter.class,
            ProcedimientoPasoConverter.class
    );

    @TestFactory
    Stream<DynamicTest> cadaConverterEsNamedFacesConverterYImplementaConverter() {
        return CONVERTERS.stream().map(clase -> DynamicTest.dynamicTest(clase.getSimpleName(), () -> {
            assertTrue(Converter.class.isAssignableFrom(clase),
                    clase.getSimpleName() + " debe implementar jakarta.faces.convert.Converter");
            assertNotNull(clase.getAnnotation(Named.class),
                    clase.getSimpleName() + " debe estar anotado con @Named");
            assertNotNull(clase.getAnnotation(FacesConverter.class),
                    clase.getSimpleName() + " debe estar anotado con @FacesConverter");
        }));
    }
}
