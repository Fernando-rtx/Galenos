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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;
import java.util.Collection;
import java.util.Date;

/**
 * Entidad JPA de {@code orden_examen}, solicitud de exámenes durante un paso.
 *
 * <p>La PK es UUID; registra fecha e indicaciones. Es dueña de la relación con
 * {@link ConsultaProcedimientoPaso}. La colección de resultados es inversa:
 * cada {@link ExamenResultado} contiene la FK de la orden.</p>
 */
@Entity
@Table(name = "orden_examen")
@NamedQueries({
    @NamedQuery(name = "OrdenExamen.findAll", query = "SELECT ordenExamen FROM OrdenExamen ordenExamen")})
public class OrdenExamen implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_orden_examen")
    private UUID idOrdenExamen;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Size(max = 2147483647)
    @Column(name = "indicaciones")
    private String indicaciones;
    @OneToMany(mappedBy = "idOrdenExamen")
    private Collection<ExamenResultado> examenResultadoCollection;
    @JoinColumn(name = "id_consulta_procedimiento_paso", referencedColumnName = "id_consulta_procedimiento_paso")
    @ManyToOne
    private ConsultaProcedimientoPaso idConsultaProcedimientoPaso;

    public OrdenExamen() {
    }

    public OrdenExamen(UUID idOrdenExamen) {
        this.idOrdenExamen = idOrdenExamen;
    }

    public UUID getIdOrdenExamen() {
        return idOrdenExamen;
    }

    public void setIdOrdenExamen(UUID idOrdenExamen) {
        this.idOrdenExamen = idOrdenExamen;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public Collection<ExamenResultado> getExamenResultadoCollection() {
        return examenResultadoCollection;
    }

    public void setExamenResultadoCollection(Collection<ExamenResultado> examenResultadoCollection) {
        this.examenResultadoCollection = examenResultadoCollection;
    }

    public ConsultaProcedimientoPaso getIdConsultaProcedimientoPaso() {
        return idConsultaProcedimientoPaso;
    }

    public void setIdConsultaProcedimientoPaso(ConsultaProcedimientoPaso idConsultaProcedimientoPaso) {
        this.idConsultaProcedimientoPaso = idConsultaProcedimientoPaso;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOrdenExamen != null ? idOrdenExamen.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrdenExamen)) {
            return false;
        }
        OrdenExamen other = (OrdenExamen) object;
        if ((this.idOrdenExamen == null && other.idOrdenExamen != null) || (this.idOrdenExamen != null && !this.idOrdenExamen.equals(other.idOrdenExamen))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occingenieriappi115_2026.salud.entity.OrdenExamen[ idOrdenExamen=" + idOrdenExamen + " ]";
    }
    
}
