package sv.edu.ues.occingenieriappi115_2026.salud.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.TipoMedioContacto;

/**
 * DAO concreto para la entidad TipoMedioContacto.
 *
 * <p>La clase se mantiene pequena porque las operaciones CRUD, busqueda,
 * paginacion y conteo se heredan de {@link DefaultDAO}.</p>
 */
@Stateless
@LocalBean
public class TipoMedioContactoDAO
        extends DefaultDAO<TipoMedioContacto> {

    /**
     * EntityManager asociado a la unidad GalenoPU definida en persistence.xml.
     */
    @PersistenceContext(unitName = "GalenoPU")
    EntityManager em;

    /**
     * Construye el DAO indicando a DefaultDAO la entidad concreta administrada.
     */
    public TipoMedioContactoDAO() {
        super(TipoMedioContacto.class);
    }

    /**
     * Devuelve el EntityManager usado por las operaciones genericas heredadas.
     *
     * @return EntityManager inyectado por el contenedor Jakarta EE.
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
}
