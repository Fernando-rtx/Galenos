package sv.edu.ues.occingenieriappi115_2026.salud.model;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occingenieriappi115_2026.salud.control.ExamenDAO;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExamenModelTest {
    @Test void nuevoInicializaActivo() { ExamenModel m = new ExamenModel(mock(ExamenDAO.class)); m.nuevo(); assertTrue(m.getSeleccionado().getActivo()); assertEquals(ESTADO_CRUD.CREACION, m.getEstado()); }
    @Test void seleccionarEdita() { ExamenModel m = new ExamenModel(mock(ExamenDAO.class)); Examen e = new Examen(); m.seleccionar(e); assertSame(e, m.getSeleccionado()); assertEquals(ESTADO_CRUD.EDICION, m.getEstado()); }
    @Test void guardarCreacionDelegaYDaoAsignaUuid() { ExamenDAO d = mock(ExamenDAO.class); doAnswer(i -> { Examen e=i.getArgument(0); e.setIdExamen(UUID.randomUUID()); return e; }).when(d).guardar(any()); ExamenModel m=new ExamenModel(d); m.nuevo(); m.guardar(); assertNotNull(m.getSeleccionado().getIdExamen()); verify(d).guardar(m.getSeleccionado()); }
    @Test void guardarEdicionUsaResultado() { ExamenDAO d=mock(ExamenDAO.class); Examen a=new Examen(), b=new Examen(); when(d.actualizar(a)).thenReturn(b); ExamenModel m=new ExamenModel(d); m.seleccionar(a); m.guardar(); assertSame(b,m.getSeleccionado()); assertEquals(ESTADO_CRUD.LISTADO,m.getEstado()); }
    @Test void cancelarLimpia() { ExamenModel m=new ExamenModel(mock(ExamenDAO.class)); m.nuevo(); m.cancelar(); assertNull(m.getSeleccionado()); assertEquals(ESTADO_CRUD.LISTADO,m.getEstado()); }
}
