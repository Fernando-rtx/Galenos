package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import java.util.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Consulta;

/**
 * DAO EJB de {@link Consulta}; hereda CRUD, transacciones y persistencia común de {@link
 * DefaultDAO}.
 */
@Stateless
@LocalBean
public class ConsultaDAO extends DefaultDAO<Consulta> {

  /** Configura el tipo concreto administrado por el DAO genérico. */
  public ConsultaDAO() {
    super(Consulta.class);
  }

  private static final String WHERE =
      " FROM Consulta c WHERE c.idPersonaRol.idPersonaRol IN :asignaciones AND c.fechaInicio >="
          + " :desde AND c.fechaInicio < :hasta";

  public List<Consulta> buscar(List<UUID> ids, Date desde, Date hasta, int first, int max) {
    return getEntityManager()
        .createQuery("SELECT c" + WHERE + " ORDER BY c.fechaInicio, c.idConsulta", Consulta.class)
        .setParameter("asignaciones", ids)
        .setParameter("desde", desde)
        .setParameter("hasta", hasta)
        .setFirstResult(first)
        .setMaxResults(max)
        .getResultList();
  }

  public long contar(List<UUID> ids, Date desde, Date hasta) {
    return getEntityManager()
        .createQuery("SELECT COUNT(c)" + WHERE, Long.class)
        .setParameter("asignaciones", ids)
        .setParameter("desde", desde)
        .setParameter("hasta", hasta)
        .getSingleResult();
  }

  // El fin de la cita puede ser una fecha programada. El cierre clínico se obtiene del recorrido.
  private static final String FINALIZADA =
      "EXISTS (SELECT cp.idConsultaProcedimiento FROM ConsultaProcedimiento cp WHERE cp.idConsulta=c)"
      + " AND NOT EXISTS (SELECT cp.idConsultaProcedimiento FROM ConsultaProcedimiento cp"
      + " WHERE cp.idConsulta=c AND cp.fechaFin IS NULL)"
      + " AND NOT EXISTS (SELECT paso.idConsultaProcedimientoPaso FROM ConsultaProcedimientoPaso paso"
      + " WHERE paso.idConsultaProcedimiento.idConsulta=c AND paso.estado IN ('PENDIENTE','EN_CURSO'))";

  public List<Consulta> buscarPorEstado(UUID clinica, Date desde, Date hasta, UUID paciente,
      boolean finalizadas, int first, int max) {
    return consultaPorEstado("SELECT c", Consulta.class, clinica, desde, hasta, paciente, finalizadas)
        .setFirstResult(first).setMaxResults(max).getResultList();
  }

  public long contarPorEstado(UUID clinica, Date desde, Date hasta, UUID paciente, boolean finalizadas) {
    return consultaPorEstado("SELECT COUNT(c)", Long.class, clinica, desde, hasta, paciente, finalizadas)
        .getSingleResult();
  }

  private <T> TypedQuery<T> consultaPorEstado(String select, Class<T> tipo, UUID clinica,
      Date desde, Date hasta, UUID paciente, boolean finalizadas) {
    String where = " FROM Consulta c WHERE c.idPersonaRol.idClinica.idClinica=:clinica";
    if (desde != null) where += " AND c.fechaInicio>=:desde";
    if (hasta != null) where += " AND c.fechaInicio<:hasta";
    if (paciente != null) where += " AND c.idPersonaRol.idPersonaRol=:paciente";
    where += finalizadas ? " AND (" + FINALIZADA + ")" : " AND NOT (" + FINALIZADA + ")";
    var query = getEntityManager().createQuery(select + where
        + (tipo == Consulta.class ? " ORDER BY c.idConsulta" : ""), tipo)
        .setParameter("clinica", clinica);
    if (desde != null) query.setParameter("desde", desde);
    if (hasta != null) query.setParameter("hasta", hasta);
    if (paciente != null) query.setParameter("paciente", paciente);
    return query;
  }
}
