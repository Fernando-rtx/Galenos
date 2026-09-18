package sv.edu.ues.occingenieriappi115_2026.salud.control;

/**
 * Operadores neutrales usados para convertir filtros de PrimeFaces en
 * predicados de Criteria API: texto, igualdad, comparación y nulidad.
 */
public enum OperadorFiltro {
    CONTIENE,
    INICIA_CON,
    TERMINA_CON,
    IGUAL,
    DISTINTO,
    MENOR_QUE,
    MENOR_O_IGUAL,
    MAYOR_QUE,
    MAYOR_O_IGUAL,
    ES_NULO,
    NO_ES_NULO
}
