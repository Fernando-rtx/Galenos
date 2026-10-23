package sv.edu.ues.occingenieriappi115_2026.salud.rest;

import jakarta.json.bind.annotation.JsonbNillable;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoDocumento;

/** Proyección del catálogo; nunca serializa documentoCollection. */
@JsonbNillable
public class TipoDocumentoDto {
    private final String idTipoDocumento;
    private final String nombre;
    private final String indicaciones;
    private final String expresionRegular;
    private final Boolean activo;

    public TipoDocumentoDto(TipoDocumento source) {
        idTipoDocumento = source.getIdTipoDocumento().toString();
        nombre = source.getNombre();
        indicaciones = source.getIndicaciones();
        expresionRegular = source.getExpresionRegular();
        activo = source.getActivo();
    }

    public String getIdTipoDocumento() { return idTipoDocumento; }
    public String getNombre() { return nombre; }
    public String getIndicaciones() { return indicaciones; }
    public String getExpresionRegular() { return expresionRegular; }
    public Boolean getActivo() { return activo; }
}
