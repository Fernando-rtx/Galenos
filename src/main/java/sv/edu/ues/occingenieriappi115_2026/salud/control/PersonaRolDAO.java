package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
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

    /** Contrato compartido: ningún paciente de otra clínica entra en Consulta. */
    public List<PersonaRol> buscarPacientesPorClinica(UUID clinica, String criterio, int limite) {
        if (clinica == null || criterio == null || criterio.isBlank() || limite <= 0) {
            return List.of();
        }
        String nombre = Normalizer.normalize(criterio.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        String dui = criterio.replaceAll("\\D", "");
        return getEntityManager().createQuery("""
                SELECT DISTINCT pr FROM PersonaRol pr
                JOIN FETCH pr.idPersona p JOIN FETCH pr.idRol r JOIN FETCH pr.idClinica c
                WHERE c.idClinica = :clinica AND c.activo = TRUE AND r.activo = TRUE
                AND LOWER(TRIM(r.nombre)) = 'paciente'
                AND (FUNCTION('translate', LOWER(CONCAT(CONCAT(p.nombres, ' '), p.apellidos)),
                      'áéíóúüñ', 'aeiouun') LIKE :nombre
                     OR (:dui <> '' AND EXISTS (SELECT d.idDocumento FROM Documento d
                         WHERE d.idPersona = p AND LOWER(TRIM(d.idTipoDocumento.nombre)) = 'dui'
                         AND REPLACE(d.valor, '-', '') LIKE :documento)))
                ORDER BY p.apellidos, p.nombres
                """, PersonaRol.class)
                .setParameter("clinica", clinica).setParameter("nombre", "%" + nombre + "%")
                .setParameter("dui", dui).setParameter("documento", "%" + dui + "%")
                .setMaxResults(limite).getResultList();
    }

    /** Responsable por ID de rol, sin inferir especialidades a partir de texto. */
    public List<PersonaRol> buscarResponsablesPorClinicaYRol(UUID clinica, UUID rol) {
        if (clinica == null || rol == null) {
            return List.of();
        }
        return getEntityManager().createQuery("""
                SELECT pr FROM PersonaRol pr JOIN FETCH pr.idPersona p
                JOIN FETCH pr.idRol r JOIN FETCH pr.idClinica c
                WHERE c.idClinica = :clinica AND c.activo = TRUE
                AND r.idRol = :rol AND r.activo = TRUE
                ORDER BY pr.idPersonaRol
                """, PersonaRol.class)
                .setParameter("clinica", clinica).setParameter("rol", rol).getResultList();
    }

    /**
     * Busca asignaciones vigentes de un rol activo en una clínica activa.
     * Permite reutilizar el filtro sin duplicar lógica de estado y clínica.
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

    /** Comprueba si ya existe la combinación lógica Persona + Clínica + Rol. */
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
