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
        if (clinica == null || rol == null) { return List.of(); }
        return getEntityManager().createQuery("""
                SELECT pr FROM PersonaRol pr JOIN FETCH pr.idPersona p
                JOIN FETCH pr.idRol r JOIN FETCH pr.idClinica c
                WHERE c.idClinica = :clinica AND c.activo = TRUE
                AND r.idRol = :rol AND r.activo = TRUE
                ORDER BY pr.idPersonaRol
                """, PersonaRol.class)
                .setParameter("clinica", clinica).setParameter("rol", rol).getResultList();
    }
}
