package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.MedioContacto;

/** DAO EJB de {@link MedioContacto}; reutiliza la persistencia genérica centralizada. */
@Stateless
@LocalBean
public class MedioContactoDAO extends DefaultDAO<MedioContacto> {

    /** Configura el DAO genérico para medios de contacto. */
    public MedioContactoDAO() {
        super(MedioContacto.class);
    }

    /**
     * Comprueba la existencia de un valor para una persona y tipo, excluyendo
     * opcionalmente el registro que se está editando.
     */
    public boolean existeContacto(UUID idPersona, UUID idTipoMedioContacto,
            String valor, UUID idMedioContactoExcluir) {
        if (idPersona == null || idTipoMedioContacto == null || valor == null) {
            return false;
        }
        List<FiltroDAO> filtros = new java.util.ArrayList<>(List.of(
                new FiltroDAO("idPersona.idPersona", OperadorFiltro.IGUAL, idPersona),
                new FiltroDAO("idTipoMedioContacto.idTipoMedioContacto",
                        OperadorFiltro.IGUAL, idTipoMedioContacto),
                new FiltroDAO("valor", OperadorFiltro.IGUAL, valor)));
        if (idMedioContactoExcluir != null) {
            filtros.add(new FiltroDAO("idMedioContacto", OperadorFiltro.DISTINTO,
                    idMedioContactoExcluir));
        }
        return !obtenerPagina(0, 1,
                filtros, List.of()).isEmpty();
    }
}
