package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;

/**
 * DAO concreto para Examen.
 *
 * <p>La clase existe para permitir persistencia de Examen sin duplicar codigo
 * CRUD.</p>
 */
@Stateless
@LocalBean
public class ExamenDAO extends DefaultDAO<Examen> {

    /** Construye el DAO indicando que administra Examen. */
    public ExamenDAO() {
        super(Examen.class);
    }
}
