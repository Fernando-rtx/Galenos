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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;
import java.util.Collection;

/**
 * Entidad JPA de {@code procedimiento_paso}, una etapa de un procedimiento.
 *
 * <p>Su UUID es la PK. Es dueña de relaciones muchos-a-uno hacia
 * {@link Procedimiento} y {@link Rol}. Secuencias y exámenes son colecciones
 * inversas cuyas FKs están en las entidades hijas. Las NamedQueries JPQL
 * permiten al {@code ProcedimientoPasoDAO} listar y contar pasos por
 * {@code idProcedimiento}, ordenando por nombre.</p>
 */
@Entity
@Table(name = "procedimiento_paso")
@NamedQueries({
    @NamedQuery(
            name = "ProcedimientoPaso.findAll",
            query = "SELECT p FROM ProcedimientoPaso p"
    ),
    @NamedQuery(
            name = "ProcedimientoPaso.findByIdProcedimiento",
            query = "SELECT p "
            + "FROM ProcedimientoPaso p "
            + "WHERE p.idProcedimiento.idProcedimiento = :idProcedimiento "
            + "ORDER BY p.nombre"
    ),
    @NamedQuery(
            name = "ProcedimientoPaso.countByIdProcedimiento",
            query = "SELECT COUNT(p.idProcedimientoPaso) "
            + "FROM ProcedimientoPaso p "
            + "WHERE p.idProcedimiento.idProcedimiento = :idProcedimiento"
    )})
public class ProcedimientoPaso implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_procedimiento_paso")
    private UUID idProcedimientoPaso;
    @Size(max = 155)
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "indica_fin")
    private Boolean indicaFin;
    @OneToMany(mappedBy = "idProcedimientoPaso")
    private Collection<ProcedimientoPasoSecuencia> procedimientoPasoSecuenciaCollection;
    @OneToMany(mappedBy = "idProcedimientoPaso")
    private Collection<ProcedimientoPasoExamen> procedimientoPasoExamenCollection;
    @JoinColumn(name = "id_procedimiento", referencedColumnName = "id_procedimiento")
    @ManyToOne
    private Procedimiento idProcedimiento;
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    @ManyToOne
    private Rol idRol;

    public ProcedimientoPaso() {
    }

    public ProcedimientoPaso(UUID idProcedimientoPaso) {
        this.idProcedimientoPaso = idProcedimientoPaso;
    }

    public UUID getIdProcedimientoPaso() {
        return idProcedimientoPaso;
    }

    public void setIdProcedimientoPaso(UUID idProcedimientoPaso) {
        this.idProcedimientoPaso = idProcedimientoPaso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getIndicaFin() {
        return indicaFin;
    }

    public void setIndicaFin(Boolean indicaFin) {
        this.indicaFin = indicaFin;
    }

    public Collection<ProcedimientoPasoSecuencia> getProcedimientoPasoSecuenciaCollection() {
        return procedimientoPasoSecuenciaCollection;
    }

    public void setProcedimientoPasoSecuenciaCollection(Collection<ProcedimientoPasoSecuencia> procedimientoPasoSecuenciaCollection) {
        this.procedimientoPasoSecuenciaCollection = procedimientoPasoSecuenciaCollection;
    }

    public Collection<ProcedimientoPasoExamen> getProcedimientoPasoExamenCollection() {
        return procedimientoPasoExamenCollection;
    }

    public void setProcedimientoPasoExamenCollection(Collection<ProcedimientoPasoExamen> procedimientoPasoExamenCollection) {
        this.procedimientoPasoExamenCollection = procedimientoPasoExamenCollection;
    }

    public Procedimiento getIdProcedimiento() {
        return idProcedimiento;
    }

    public void setIdProcedimiento(Procedimiento idProcedimiento) {
        this.idProcedimiento = idProcedimiento;
    }

    public Rol getIdRol() {
        return idRol;
    }

    public void setIdRol(Rol idRol) {
        this.idRol = idRol;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProcedimientoPaso != null ? idProcedimientoPaso.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProcedimientoPaso)) {
            return false;
        }
        ProcedimientoPaso other = (ProcedimientoPaso) object;
        if ((this.idProcedimientoPaso == null && other.idProcedimientoPaso != null) || (this.idProcedimientoPaso != null && !this.idProcedimientoPaso.equals(other.idProcedimientoPaso))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso[ idProcedimientoPaso=" + idProcedimientoPaso + " ]";
    }
    
}
