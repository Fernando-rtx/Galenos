package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import sv.edu.ues.occingenieriappi115_2026.salud.entity.Clinica;

/** Datos mínimos para el selector académico, sin colecciones o comentarios. */
public class ClinicaDto {
    private final String idClinica;
    private final String nombre;

    public ClinicaDto(Clinica source) {
        idClinica = source.getIdClinica().toString();
        nombre = source.getNombre();
    }

    public String getIdClinica() { return idClinica; }
    public String getNombre() { return nombre; }
}
