package sv.edu.ues.occingenieriappi115_2026.salud.model;

/** Presets de interfaz para ayudar a configurar la expresión regular del catálogo. */
public enum FormatoRegexSugerido {
    PERSONALIZADO(null),
    DUI_SV("^[0-9]{8}-[0-9]$"),
    TELEFONO_SV("^(2|6|7)[0-9]{7}$"),
    CORREO("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"),
    SOLO_NUMEROS("^[0-9]+$"),
    ALFANUMERICO("^[A-Za-z0-9]+$"),
    RESIDENCIAL_SV("^2[0-9]{7}$");

    private final String expresionRegular;

    FormatoRegexSugerido(String expresionRegular) {
        this.expresionRegular = expresionRegular;
    }

    public String getExpresionRegular() {
        return expresionRegular;
    }

    public String getClave() {
        return "regex.formato." + name();
    }

    public static FormatoRegexSugerido desdeExpresion(String expresion) {
        if (expresion != null) {
            for (FormatoRegexSugerido formato : values()) {
                if (formato.expresionRegular != null && formato.expresionRegular.equals(expresion)) {
                    return formato;
                }
            }
        }
        return PERSONALIZADO;
    }
}
