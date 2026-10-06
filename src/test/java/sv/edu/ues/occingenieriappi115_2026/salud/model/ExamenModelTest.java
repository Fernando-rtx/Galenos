package sv.edu.ues.occingenieriappi115_2026.salud.model;

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExamenModelTest {
    @Test void nombreValidoEsAceptado() {
        ExamenModel modelo = new ExamenModel(mock(ExamenDAO.class));
        assertDoesNotThrow(() -> modelo.validarNombre(null, null, "Hemograma"));
    }

    @Test void nombreNuloEsRechazado() {
        ExamenModel modelo = new ExamenModel(mock(ExamenDAO.class));
        assertThrows(ValidatorException.class,
                () -> modelo.validarNombre(contexto(), null, null));
    }

    @Test void nombreDeUnaLetraEsRechazado() {
        ExamenModel modelo = new ExamenModel(mock(ExamenDAO.class));
        assertThrows(ValidatorException.class,
                () -> modelo.validarNombre(contexto(), null, "A"));
    }

    @Test void nombreDeMasDe255CaracteresEsRechazado() {
        ExamenModel modelo = new ExamenModel(mock(ExamenDAO.class));
        String nombreLargo = "A".repeat(256);
        assertThrows(ValidatorException.class,
                () -> modelo.validarNombre(contexto(), null, nombreLargo));
    }

    @Test void guardarSinExamenSeleccionadoNoLlamaAlDao() {
        ExamenDAO dao = mock(ExamenDAO.class);
        ExamenModel modelo = new ExamenModel(dao);

        modelo.guardar();

        verifyNoInteractions(dao);
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test void guardarRecortaNombreYObservaciones() {
        ExamenDAO dao = mock(ExamenDAO.class);
        ExamenModel modelo = new ExamenModel(dao);
        modelo.nuevo();
        Examen examen = modelo.getSeleccionado();
        examen.setNombre(" Hemograma ");
        examen.setObservaciones(" Control anual ");

        modelo.guardar();

        assertEquals("Hemograma", examen.getNombre());
        assertEquals("Control anual", examen.getObservaciones());
        verify(dao).guardar(examen);
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }

    @Test void errorDelDaoAlGuardarConservaExamenYEstado() {
        ExamenDAO dao = mock(ExamenDAO.class);
        ExamenModel modelo = new ExamenModel(dao);
        modelo.nuevo();
        Examen examen = modelo.getSeleccionado();
        doThrow(new IllegalStateException("Error de persistencia")).when(dao).guardar(examen);

        assertThrows(IllegalStateException.class, modelo::guardar);

        assertSame(examen, modelo.getSeleccionado());
        assertEquals(ESTADO_CRUD.CREACION, modelo.getEstado());
    }

    @Test void nombreDuplicadoEsRechazado() {
        ExamenDAO dao = mock(ExamenDAO.class);
        Examen existente = new Examen(UUID.randomUUID());
        existente.setNombre("Hemograma");
        when(dao.obtenerPagina(anyInt(), anyInt(), anyList(), anyList()))
                .thenReturn(java.util.List.of(existente));
        ExamenModel modelo = new ExamenModel(dao);

        assertThrows(ValidatorException.class,
                () -> modelo.validarNombre(contexto(), null, "Hemograma"));
    }

    private FacesContext contexto() {
        FacesContext contexto = mock(FacesContext.class);
        Application aplicacion = mock(Application.class);
        ResourceBundle mensajes = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"examen.nombreRequerido", "Nombre requerido"},
                    {"examen.nombreMinimo", "Nombre demasiado corto"},
                    {"examen.nombreMaximo", "Nombre demasiado largo"},
                    {"examen.nombreDuplicado", "Nombre duplicado"}
                };
            }
        };
        when(contexto.getApplication()).thenReturn(aplicacion);
        when(aplicacion.getResourceBundle(contexto, "msg")).thenReturn(mensajes);
        return contexto;
    }
    @Test void nuevoInicializaActivo() { ExamenModel m = new ExamenModel(mock(ExamenDAO.class)); m.nuevo(); assertTrue(m.getSeleccionado().getActivo()); assertEquals(ESTADO_CRUD.CREACION, m.getEstado()); }
    @Test void guardaEstadosActivoEInactivoAlCrearOActualizar() {
        ExamenDAO dao = mock(ExamenDAO.class);
        ExamenModel modelo = new ExamenModel(dao);
        modelo.nuevo();
        Examen creado = modelo.getSeleccionado();
        creado.setActivo(false);

        modelo.guardar();

        verify(dao).guardar(argThat(examen -> Boolean.FALSE.equals(examen.getActivo())));
        Examen editado = new Examen(UUID.randomUUID());
        editado.setActivo(true);
        when(dao.actualizar(editado)).thenReturn(editado);
        modelo.seleccionar(editado);
        editado.setActivo(false);

        modelo.guardar();

        verify(dao).actualizar(argThat(examen -> Boolean.FALSE.equals(examen.getActivo())));
        assertEquals(ESTADO_CRUD.LISTADO, modelo.getEstado());
    }
    @Test void seleccionarEdita() { ExamenModel m = new ExamenModel(mock(ExamenDAO.class)); Examen e = new Examen(); m.seleccionar(e); assertSame(e, m.getSeleccionado()); assertEquals(ESTADO_CRUD.EDICION, m.getEstado()); }
    @Test void dobleClicSeleccionaExamenDelEvento() { ExamenModel m = new ExamenModel(mock(ExamenDAO.class)); Examen e = new Examen(); SelectEvent<Examen> evento = mock(SelectEvent.class); when(evento.getObject()).thenReturn(e); m.seleccionarFila(evento); assertSame(e, m.getSeleccionado()); assertEquals(ESTADO_CRUD.EDICION, m.getEstado()); }
    @Test void guardarCreacionDelegaYDaoAsignaUuid() { ExamenDAO d = mock(ExamenDAO.class); doAnswer(i -> { Examen e=i.getArgument(0); e.setIdExamen(UUID.randomUUID()); return e; }).when(d).guardar(any()); ExamenModel m=new ExamenModel(d); m.nuevo(); m.guardar(); assertNotNull(m.getSeleccionado().getIdExamen()); verify(d).guardar(m.getSeleccionado()); }
    @Test void guardarEdicionUsaResultado() { ExamenDAO d=mock(ExamenDAO.class); Examen a=new Examen(), b=new Examen(); when(d.actualizar(a)).thenReturn(b); ExamenModel m=new ExamenModel(d); m.seleccionar(a); m.guardar(); assertSame(b,m.getSeleccionado()); assertEquals(ESTADO_CRUD.LISTADO,m.getEstado()); }
    @Test void cancelarLimpia() { ExamenModel m=new ExamenModel(mock(ExamenDAO.class)); m.nuevo(); m.cancelar(); assertNull(m.getSeleccionado()); assertEquals(ESTADO_CRUD.LISTADO,m.getEstado()); }
}
