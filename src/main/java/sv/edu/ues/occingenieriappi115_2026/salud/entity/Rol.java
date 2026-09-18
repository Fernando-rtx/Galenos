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
 * Entidad JPA del catálogo {@code rol}.
 *
 * <p>Su PK es UUID y conserva nombre, estado y observaciones. Las colecciones
 * de {@link PersonaRol} y {@link ProcedimientoPaso} son lados inversos: esas
 * entidades contienen la FK {@code id_rol}.</p>
 */
@Entity
@Table(name = "rol")
@NamedQueries({
    @NamedQuery(name = "Rol.findAll", query = "SELECT rol FROM Rol rol")})
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_rol")
    private UUID idRol;
    @Size(max = 155)
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(mappedBy = "idRol")
    private Collection<PersonaRol> personaRolCollection;
    @OneToMany(mappedBy = "idRol")
    private Collection<ProcedimientoPaso> procedimientoPasoCollection;

    public Rol() {
    }

    public Rol(UUID idRol) {
        this.idRol = idRol;
    }

    public UUID getIdRol() {
        return idRol;
    }

    public void setIdRol(UUID idRol) {
        this.idRol = idRol;
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

    public Collection<PersonaRol> getPersonaRolCollection() {
        return personaRolCollection;
    }

    public void setPersonaRolCollection(Collection<PersonaRol> personaRolCollection) {
        this.personaRolCollection = personaRolCollection;
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
        hash += (idRol != null ? idRol.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Rol)) {
            return false;
        }
        Rol other = (Rol) object;
        if ((this.idRol == null && other.idRol != null) || (this.idRol != null && !this.idRol.equals(other.idRol))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol[ idRol=" + idRol + " ]";
    }
    
}
