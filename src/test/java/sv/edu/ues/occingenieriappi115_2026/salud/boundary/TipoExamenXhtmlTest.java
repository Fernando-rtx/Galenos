package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoExamenXhtmlTest {

    private String pagina() throws IOException {
        return Files.readString(Path.of("src/main/webapp/paginas/TipoExamen.xhtml"));
    }

    @Test
    void guardarProcesaCamposYActualizaMensajesListaYContenidoSinReemplazarDialogo() throws IOException {
        String xhtml = pagina();
        int inicio = xhtml.indexOf("<crud:botones-bottom");
        int fin = xhtml.indexOf("/>", inicio);
        String botonGuardar = xhtml.substring(inicio, fin);

        assertTrue(botonGuardar.contains("procesar=\":layoutForm:edicionContenido\""));
        assertTrue(botonGuardar.contains("actualizar=\":layoutForm:messages :layoutForm:tiposExamenTable"
                + " :layoutForm:edicionContenido\""));
        assertTrue(xhtml.contains("id=\"tiposExamenTable\""));
        assertFalse(botonGuardar.contains(":layoutForm:tiposExamenTable :layoutForm:edicionDialog\""));
    }

    @Test
    void mensajesDeCampoSonLocalesYDialogoSoloCierraConConfirmacionDeGuardado() throws IOException {
        String xhtml = pagina();

        assertTrue(xhtml.contains("<p:outputPanel id=\"messages\">"));
        assertTrue(xhtml.contains("id=\"listaMessages\""));
        assertTrue(xhtml.contains("globalOnly=\"true\""));
        assertTrue(xhtml.contains("id=\"dialogMessages\""));
        assertTrue(xhtml.contains("<p:message id=\"nombreMessage\" for=\"nombre\""));
        assertTrue(xhtml.contains("oncompleteGuardar=\"if (args &amp;&amp; args.guardado === true)"
                + " PF('tipoExamenDialog').hide()\""));
        assertFalse(xhtml.contains("if (!args.validationFailed) PF('tipoExamenDialog').hide()"));
    }

    @Test
    void botonNuevoActualizaDialogoConIdsAbsolutosAntesDeAbrirlo() throws IOException {
        String xhtml = pagina();
        int inicio = xhtml.indexOf("<crud:botones-top");
        int fin = xhtml.indexOf("/>", inicio);
        String botonNuevo = xhtml.substring(inicio, fin);

        assertTrue(botonNuevo.contains("actualizar=\":layoutForm:messages :layoutForm:edicionDialog\""));
        assertTrue(botonNuevo.contains("oncompleteNuevo=\"PF('tipoExamenDialog').show()\""));
        assertFalse(botonNuevo.contains("actualizar=\"messages tiposExamenTable edicionDialog\""));
    }

    @Test
    void dobleClicEntregaObjetoSeleccionadoYActualizaPanelSiempreRenderizado() throws IOException {
        String xhtml = pagina();
        int inicio = xhtml.indexOf("<p:ajax\n                                event=\"rowDblselect\"");
        int fin = xhtml.indexOf("/>", inicio);
        String dobleClic = xhtml.substring(inicio, fin);

        assertTrue(xhtml.contains("selectionMode=\"single\""));
        assertTrue(xhtml.contains("selection=\"#{tipoExamenModel.seleccionado}\""));
        assertTrue(dobleClic.contains("listener=\"#{tipoExamenModel.seleccionar}\""));
        assertTrue(dobleClic.contains("process=\"@this\""));
        assertTrue(dobleClic.contains("update=\":layoutForm:edicionContenido :layoutForm:messages\""));
        assertTrue(dobleClic.contains("resetValues=\"true\""));
        assertTrue(dobleClic.contains("oncomplete=\"PF('tipoExamenDialog').show()\""));
        assertTrue(xhtml.contains("<p:outputPanel\n                id=\"edicionContenido\""));
        assertTrue(xhtml.contains("rendered=\"#{tipoExamenModel.seleccionado ne null}\""));
        assertFalse(xhtml.matches("(?s).*\\b\\w+Dialog:\\w+Contenido\\b.*"));
    }
}
