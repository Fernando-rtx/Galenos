package sv.edu.ues.occingenieriappi115_2026.salud.boundary;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * Bean de presentación que mantiene el idioma elegido durante la sesión.
 *
 * <p>{@code @Named} permite usar {@code idiomaBean} desde la plantilla JSF y
 * {@code @SessionScoped} conserva la selección al navegar entre páginas. Es
 * {@link Serializable} porque el contenedor puede almacenar la sesión. El bean
 * modifica el {@code Locale} del árbol de componentes; no consulta la base.</p>
 */
@Named
@SessionScoped
public class IdiomaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Map<String, Locale> LOCALES = Map.of(
            "es", Locale.forLanguageTag("es"),
            "en", Locale.ENGLISH,
            "de", Locale.GERMAN,
            "zh_CN", Locale.forLanguageTag("zh-CN")
    );

    private String idioma = "es";

    /**
     * Aplica el idioma seleccionado al UIViewRoot actual.
     */
    public void cambiarIdioma() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context != null && context.getViewRoot() != null) {
            context.getViewRoot().setLocale(getLocale());
        }
    }

    public Locale getLocale() {
        return LOCALES.getOrDefault(idioma, LOCALES.get("es"));
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        if (idioma == null || !LOCALES.containsKey(idioma)) {
            this.idioma = "es";
            return;
        }

        this.idioma = idioma;
    }
}
