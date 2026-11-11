package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
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
}
