package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.PersonaRol;

/** DAO EJB de {@link PersonaRol}, asociación entre persona, rol y clínica. */
@Stateless
@LocalBean
public class PersonaRolDAO extends DefaultDAO<PersonaRol> {

    /** Indica a la base genérica qué entidad asociativa administra. */
    public PersonaRolDAO() {
        super(PersonaRol.class);
    }

    /**
     * Busca asignaciones vigentes de un rol activo en una clínica activa. El
     * consumidor puede reutilizar este resultado para resolver las personas
     * habilitadas para acciones clínicas sin duplicar filtros de FK/estado.
     */
    public List<PersonaRol> buscarPorRolYClinica(UUID idRol, UUID idClinica) {
        if (idRol == null || idClinica == null) {
            return List.of();
        }
        return obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idRol.idRol", OperadorFiltro.IGUAL, idRol),
                        new FiltroDAO("idClinica.idClinica", OperadorFiltro.IGUAL, idClinica),
                        new FiltroDAO("idRol.activo", OperadorFiltro.IGUAL, true),
                        new FiltroDAO("idClinica.activo", OperadorFiltro.IGUAL, true)),
                List.of());
    }

    /** Busca las asignaciones activas de una clínica, sin restringir el rol. */
    public List<PersonaRol> buscarPorClinica(UUID idClinica) {
        if (idClinica == null) {
            return List.of();
        }
        return obtenerPagina(0, Integer.MAX_VALUE,
                List.of(new FiltroDAO("idClinica.idClinica", OperadorFiltro.IGUAL, idClinica),
                        new FiltroDAO("idClinica.activo", OperadorFiltro.IGUAL, true),
                        new FiltroDAO("idRol.activo", OperadorFiltro.IGUAL, true)),
                List.of());
    }

    /**
     * Comprueba si ya existe la combinación lógica Persona + Clínica + Rol.
     * La consulta se limita a una fila porque el consumidor solo necesita
     * conocer su existencia.
     */
    public boolean existeAsignacion(UUID idPersona, UUID idClinica, UUID idRol) {
        if (idPersona == null || idClinica == null || idRol == null) {
            return false;
        }
        return !obtenerPagina(0, 1,
                List.of(new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL, idPersona),
                        new FiltroDAO("idClinica.idClinica", OperadorFiltro.IGUAL, idClinica),
                        new FiltroDAO("idRol.idRol", OperadorFiltro.IGUAL, idRol)),
                List.of()).isEmpty();
    }
}
