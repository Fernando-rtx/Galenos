/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occingenieriappi115_2026.salud.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

/**
 * Entidad JPA de {@code procedimiento_paso_secuencia}, que describe enlaces
 * entre pasos de un procedimiento.
 *
 * <p>Usa UUID como PK. La relación muchos-a-uno enlaza el paso origen mediante
 * una FK; el UUID de referencia y el tipo de secuencia describen el siguiente
 * paso y la naturaleza del enlace según el esquema actual.</p>
 */
@Entity
@Table(name = "procedimiento_paso_secuencia")
@NamedQueries({
    @NamedQuery(name = "ProcedimientoPasoSecuencia.findAll", query = "SELECT procedimientoPasoSecuencia FROM ProcedimientoPasoSecuencia procedimientoPasoSecuencia")})
public class ProcedimientoPasoSecuencia implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_procedimiento_paso_secuencia")
    private UUID idProcedimientoPasoSecuencia;
    @Column(name = "id_procedimiento_paso_referencia")
    private UUID idProcedimientoPasoReferencia;
    @Size(max = 20)
    @Column(name = "tipo_secuencia")
    private String tipoSecuencia;
    @JoinColumn(name = "id_procedimiento_paso", referencedColumnName = "id_procedimiento_paso")
    @ManyToOne
    private ProcedimientoPaso idProcedimientoPaso;

    public ProcedimientoPasoSecuencia() {
    }

    public ProcedimientoPasoSecuencia(UUID idProcedimientoPasoSecuencia) {
        this.idProcedimientoPasoSecuencia = idProcedimientoPasoSecuencia;
    }

    public UUID getIdProcedimientoPasoSecuencia() {
        return idProcedimientoPasoSecuencia;
    }

    public void setIdProcedimientoPasoSecuencia(UUID idProcedimientoPasoSecuencia) {
        this.idProcedimientoPasoSecuencia = idProcedimientoPasoSecuencia;
    }

    public UUID getIdProcedimientoPasoReferencia() {
        return idProcedimientoPasoReferencia;
    }

    public void setIdProcedimientoPasoReferencia(UUID idProcedimientoPasoReferencia) {
        this.idProcedimientoPasoReferencia = idProcedimientoPasoReferencia;
    }

    public String getTipoSecuencia() {
        return tipoSecuencia;
    }

    public void setTipoSecuencia(String tipoSecuencia) {
        this.tipoSecuencia = tipoSecuencia;
    }

    public ProcedimientoPaso getIdProcedimientoPaso() {
        return idProcedimientoPaso;
    }

    public void setIdProcedimientoPaso(ProcedimientoPaso idProcedimientoPaso) {
        this.idProcedimientoPaso = idProcedimientoPaso;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProcedimientoPasoSecuencia != null ? idProcedimientoPasoSecuencia.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProcedimientoPasoSecuencia)) {
            return false;
        }
        ProcedimientoPasoSecuencia other = (ProcedimientoPasoSecuencia) object;
        if ((this.idProcedimientoPasoSecuencia == null && other.idProcedimientoPasoSecuencia != null) || (this.idProcedimientoPasoSecuencia != null && !this.idProcedimientoPasoSecuencia.equals(other.idProcedimientoPasoSecuencia))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia[ idProcedimientoPasoSecuencia=" + idProcedimientoPasoSecuencia + " ]";
    }
    
}
