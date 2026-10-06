package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersonaXhtmlTest {

    private String pagina() throws IOException {
        return Files.readString(Path.of("src/main/webapp/paginas/Persona.xhtml"));
    }

    @Test
    void calendarioPermiteSeleccionHistoricaYLimitaAlDiaActual() throws IOException {
        String xhtml = pagina();
        int inicio = xhtml.indexOf("<p:datePicker id=\"fechaNacimiento\"");
        int fin = xhtml.indexOf("/>\n", inicio);
        String datePicker = xhtml.substring(inicio, fin);

        assertTrue(datePicker.contains("pattern=\"dd/MM/yyyy\""));
        assertTrue(datePicker.contains("mindate=\"#{personaModel.fechaMinimaNacimiento}\""));
        assertTrue(datePicker.contains("maxdate=\"#{personaModel.fechaMaximaNacimiento}\""));
        assertTrue(datePicker.contains("monthNavigator=\"true\""));
        assertTrue(datePicker.contains("yearNavigator=\"select\""));
        assertTrue(datePicker.contains("yearRange=\"#{personaModel.rangoAniosNacimiento}\""));
        assertTrue(datePicker.contains("readonlyInput=\"true\""));
    }

    @Test
    void guardarSoloOcultaDialogoTrasConfirmacionDePersistencia() throws IOException {
        String xhtml = pagina();
        int accion = xhtml.indexOf("action=\"#{personaModel.guardar()}\"");
        int inicio = xhtml.lastIndexOf("<p:commandButton", accion);
        int fin = xhtml.indexOf("/>", accion);
        String botonGuardar = xhtml.substring(inicio, fin);

        assertTrue(botonGuardar.contains("process=\"@this detallePersona\""));
        assertTrue(botonGuardar.contains("update=\":layoutForm:messages :layoutForm:personaTable :layoutForm:edicionContenido\""));
        assertFalse(botonGuardar.contains("edicionDialog\""));
        assertTrue(botonGuardar.contains("args.personaGuardada === true"));
        assertFalse(botonGuardar.contains("resetValues"));
        assertFalse(botonGuardar.contains("validationFailed"));
    }

    @Test
    void guardadosDeRelacionesActualizanMensajesDentroDelDialogo() throws IOException {
        String xhtml = pagina();

        assertTrue(botonGuardarRelacional(xhtml, "documentoModel.guardar()")
                .contains("update=\"documentosContenido :layoutForm:dialogMessages\""));
        assertTrue(botonGuardarRelacional(xhtml, "medioContactoModel.guardar()")
                .contains("update=\"contactosContenido :layoutForm:dialogMessages\""));
        assertTrue(botonGuardarRelacional(xhtml, "personaRolModel.guardar()")
                .contains("update=\"rolesContenido :layoutForm:dialogMessages\""));
        assertFalse(botonGuardarRelacional(xhtml, "documentoModel.guardar()")
                .contains(":layoutForm:messages"));
        assertFalse(botonGuardarRelacional(xhtml, "medioContactoModel.guardar()")
                .contains(":layoutForm:messages"));
        assertFalse(botonGuardarRelacional(xhtml, "personaRolModel.guardar()")
                .contains(":layoutForm:messages"));
    }

    @Test
    void pestanasMantienenElOrdenSolicitadoYNombreClaro() throws IOException {
        String xhtml = pagina();
        int detalle = xhtml.indexOf("<p:tab title=\"#{msg['persona.detalle']}\"");
        int documentos = xhtml.indexOf("<p:tab title=\"#{msg['persona.documentos']}\"");
        int contactos = xhtml.indexOf("<p:tab title=\"#{msg['persona.contactos']}\"");
        int roles = xhtml.indexOf("<p:tab title=\"#{msg['persona.roles']}\"");

        assertTrue(detalle >= 0 && detalle < documentos && documentos < contactos && contactos < roles);
        assertTrue(Files.readString(Path.of("src/main/resources/i18n/messages_es.properties"))
                .contains("persona.contactos=Medios de contacto"));
    }

    @Test
    void mensajesNoSeRedisplayanEnMasDeUnComponenteYSeLimpianAlCambiarAccion() throws IOException {
        String xhtml = pagina();
        assertEquals(5, xhtml.split("<p:messages\\b", -1).length - 1);
        assertEquals(5, contar(xhtml, "redisplay=\"false\""));
        assertTrue(xhtml.contains("update=\":layoutForm:dialogMessages :layoutForm:messages :layoutForm:personaTabs:detallePersona"
                + " :layoutForm:personaTabs:documentosContenido :layoutForm:personaTabs:contactosContenido :layoutForm:personaTabs:rolesContenido\""));

        int rowDblclick = xhtml.indexOf("<p:ajax event=\"rowDblselect\"");
        int rowFin = xhtml.indexOf("/>", rowDblclick);
        String ajaxEditarPersona = xhtml.substring(rowDblclick, rowFin);
        assertTrue(ajaxEditarPersona.contains("update=\":layoutForm:messages :layoutForm:edicionDialog\""));
        assertTrue(botonGuardarRelacional(xhtml, "personaModel.guardar()")
                .contains("update=\":layoutForm:messages :layoutForm:personaTable :layoutForm:edicionContenido\""));
    }

    private int contar(String texto, String ocurrencia) {
        int cantidad = 0;
        int desde = 0;
        while ((desde = texto.indexOf(ocurrencia, desde)) >= 0) {
            cantidad++;
            desde += ocurrencia.length();
        }
        return cantidad;
    }

    private String botonGuardarRelacional(String xhtml, String accion) {
        int accionInicio = xhtml.indexOf("action=\"#{" + accion + "}\"");
        assertTrue(accionInicio >= 0, "No se encontró la acción " + accion);
        int inicio = xhtml.lastIndexOf("<p:commandButton", accionInicio);
        int fin = xhtml.indexOf("</p:commandButton>", accionInicio);
        if (fin < 0) {
            fin = xhtml.indexOf("/>", accionInicio) + 2;
        } else {
            fin += "</p:commandButton>".length();
        }
        return xhtml.substring(inicio, fin);
    }
}
