/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occingenieriappi115_2026.salud.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;
import java.util.Collection;

/**
 * Entidad JPA del catálogo {@code procedimiento}.
 *
 * <p>Su UUID es la PK; nombre, estado y observaciones definen el procedimiento.
 * La colección de {@link ProcedimientoPaso} es el lado inverso indicado por
 * {@code mappedBy}; cada paso contiene la FK del procedimiento.</p>
 */
@Entity
@Table(name = "procedimiento")
@NamedQueries({
    @NamedQuery(name = "Procedimiento.findAll", query = "SELECT procedimiento FROM Procedimiento procedimiento")})
public class Procedimiento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_procedimiento")
    private UUID idProcedimiento;
    @Size(max = 155)
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(mappedBy = "idProcedimiento")
    private Collection<ProcedimientoPaso> procedimientoPasoCollection;

    public Procedimiento() {
    }

    public Procedimiento(UUID idProcedimiento) {
        this.idProcedimiento = idProcedimiento;
    }

    public UUID getIdProcedimiento() {
        return idProcedimiento;
    }

    public void setIdProcedimiento(UUID idProcedimiento) {
        this.idProcedimiento = idProcedimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Collection<ProcedimientoPaso> getProcedimientoPasoCollection() {
        return procedimientoPasoCollection;
    }

    public void setProcedimientoPasoCollection(Collection<ProcedimientoPaso> procedimientoPasoCollection) {
        this.procedimientoPasoCollection = procedimientoPasoCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProcedimiento != null ? idProcedimiento.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Procedimiento)) {
            return false;
        }
        Procedimiento other = (Procedimiento) object;
        if ((this.idProcedimiento == null && other.idProcedimiento != null) || (this.idProcedimiento != null && !this.idProcedimiento.equals(other.idProcedimiento))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento[ idProcedimiento=" + idProcedimiento + " ]";
    }
    
}
