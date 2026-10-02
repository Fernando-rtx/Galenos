package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;

/**
 * DAO concreto para ProcedimientoPaso.
 *
 * <p>Hereda CRUD y EntityManager de {@link DefaultDAO}. Sus métodos propios
 * consumen las NamedQueries {@code ProcedimientoPaso.findByIdProcedimiento} y
 * {@code countByIdProcedimiento} para navegar la relación con paginación y
 * conteo sin escribir SQL directo.</p>
 */
@Stateless
@LocalBean
public class ProcedimientoPasoDAO
        extends DefaultDAO<ProcedimientoPaso>
        implements ProcedimientoPasoDAOInterface {

    /** Construye el DAO indicando que administra ProcedimientoPaso. */
    public ProcedimientoPasoDAO() {
        super(ProcedimientoPaso.class);
    }

    /** Contrato compartido: origen → referencia es origen → siguiente. */
    public ProcedimientoPaso obtenerPasoInicial(UUID idProcedimiento) {
        List<ProcedimientoPaso> pasos = findByIdProcedimiento(idProcedimiento, 0, Integer.MAX_VALUE);
        List<ProcedimientoPasoSecuencia> secuencias = getEntityManager().createQuery("""
                SELECT s FROM ProcedimientoPasoSecuencia s
                WHERE s.idProcedimientoPaso.idProcedimiento.idProcedimiento = :id
                """, ProcedimientoPasoSecuencia.class)
                .setParameter("id", idProcedimiento).getResultList();
        return determinarPasoInicial(pasos, secuencias);
    }

    static ProcedimientoPaso determinarPasoInicial(List<ProcedimientoPaso> pasos,
            List<ProcedimientoPasoSecuencia> secuencias) {
        Map<UUID, ProcedimientoPaso> porId = new HashMap<>();
        Map<UUID, Set<UUID>> siguientes = new HashMap<>();
        Map<UUID, Integer> entradas = new HashMap<>();
        for (ProcedimientoPaso paso : pasos) {
            porId.put(paso.getIdProcedimientoPaso(), paso);
            entradas.put(paso.getIdProcedimientoPaso(), 0);
        }
        for (ProcedimientoPasoSecuencia s : secuencias) {
            UUID origen = s.getIdProcedimientoPaso().getIdProcedimientoPaso();
            UUID destino = s.getIdProcedimientoPasoReferencia();
            if (!porId.containsKey(origen) || !porId.containsKey(destino)) {
                throw new FlujoConsultaException("consultaFlujo.plantillaInvalida");
            }
            if (siguientes.computeIfAbsent(origen, key -> new HashSet<>()).add(destino)) {
                entradas.compute(destino, (key, n) -> n + 1);
            }
        }
        List<UUID> raices = entradas.entrySet().stream().filter(e -> e.getValue() == 0)
                .map(Map.Entry::getKey).toList();
        if (raices.size() != 1) {
            throw new FlujoConsultaException("consultaFlujo.plantillaInvalida");
        }
        var pendientes = new ArrayDeque<>(raices);
        int visitados = 0;
        while (!pendientes.isEmpty()) {
            UUID actual = pendientes.remove();
            visitados++;
            for (UUID siguiente : siguientes.getOrDefault(actual, Set.of())) {
                if (entradas.compute(siguiente, (key, n) -> n - 1) == 0) {
                    pendientes.add(siguiente);
                }
            }
        }
        if (visitados != pasos.size()) {
            throw new FlujoConsultaException("consultaFlujo.plantillaInvalida");
        }
        return porId.get(raices.getFirst());
    }

    /**
     * Consulta los pasos asociados a un procedimiento con paginacion.
     *
     * @param idProcedimiento identificador del procedimiento padre.
     * @param first posicion inicial base cero.
     * @param max cantidad maxima de registros.
     * @return lista paginada de pasos de procedimiento.
     * @throws IllegalArgumentException si el ID o la paginación no son válidos
     * @throws IllegalStateException si falla la consulta JPA
     */
    @Override
    public List<ProcedimientoPaso> findByIdProcedimiento(
            UUID idProcedimiento,
            int first,
            int max) {

        if (idProcedimiento == null) {
            throw new IllegalArgumentException(
                    "El identificador del procedimiento no puede ser null"
            );
        }

        if (first < 0) {
            throw new IllegalArgumentException(
                    "El primer registro no puede ser menor que cero"
            );
        }

        if (max <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de registros debe ser mayor que cero"
            );
        }

        try {
            TypedQuery<ProcedimientoPaso> query = getEntityManager()
                    .createNamedQuery(
                            "ProcedimientoPaso.findByIdProcedimiento",
                            ProcedimientoPaso.class
                    );

            query.setParameter("idProcedimiento", idProcedimiento);
            query.setFirstResult(first);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al consultar los pasos por procedimiento",
                    ex
            );
        }
    }

    /**
     * Cuenta los pasos asociados a un procedimiento.
     *
     * @param idProcedimiento identificador del procedimiento padre.
     * @return cantidad de pasos encontrados.
     * @throws IllegalArgumentException si el ID es nulo
     * @throws IllegalStateException si falla el conteo JPA
     */
    @Override
    public long countByIdProcedimiento(UUID idProcedimiento) {
        if (idProcedimiento == null) {
            throw new IllegalArgumentException(
                    "El identificador del procedimiento no puede ser null"
            );
        }

        try {
            TypedQuery<Long> query = getEntityManager()
                    .createNamedQuery(
                            "ProcedimientoPaso.countByIdProcedimiento",
                            Long.class
                    );

            query.setParameter("idProcedimiento", idProcedimiento);

            return query.getSingleResult();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al contar los pasos por procedimiento",
                    ex
            );
        }
    }
}
