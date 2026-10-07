package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdiomaBeanTest {

    @Test
    void idiomaPorDefectoEsEspanol() {
        IdiomaBean bean = new IdiomaBean();
        assertEquals("es", bean.getIdioma());
        assertEquals(Locale.forLanguageTag("es"), bean.getLocale());
    }

    @Test
    void setIdiomaInglesCambiaLocale() {
        IdiomaBean bean = new IdiomaBean();
        bean.setIdioma("en");
        assertEquals("en", bean.getIdioma());
        assertEquals(Locale.ENGLISH, bean.getLocale());
    }

    @Test
    void setIdiomaAlemanCambiaLocale() {
        IdiomaBean bean = new IdiomaBean();
        bean.setIdioma("de");
        assertEquals("de", bean.getIdioma());
        assertEquals(Locale.GERMAN, bean.getLocale());
    }

    @Test
    void setIdiomaChinoCambiaLocale() {
        IdiomaBean bean = new IdiomaBean();
        bean.setIdioma("zh_CN");
        assertEquals("zh_CN", bean.getIdioma());
        assertEquals(Locale.forLanguageTag("zh-CN"), bean.getLocale());
    }

    @Test
    void setIdiomaNuloVuelveAEspanol() {
        IdiomaBean bean = new IdiomaBean();
        bean.setIdioma("en");
        bean.setIdioma(null);
        assertEquals("es", bean.getIdioma());
    }

    @Test
    void setIdiomaInvalidoVuelveAEspanol() {
        IdiomaBean bean = new IdiomaBean();
        bean.setIdioma("fr");
        assertEquals("es", bean.getIdioma());
    }

}
