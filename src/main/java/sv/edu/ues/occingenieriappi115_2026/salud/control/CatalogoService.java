package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException.Motivo.*;

/** Reglas de los catálogos administrativos; no contiene estado de vista ni HTTP. */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CatalogoService {
    @EJB private TipoDocumentoDAO tipoDocumentoDAO;
    @EJB private TipoMedioContactoDAO tipoMedioContactoDAO;
    @EJB private TipoExamenDAO tipoExamenDAO;
    @EJB private ClinicaDAO clinicaDAO;
    @EJB private RolDAO rolDAO;
    @EJB private PersonaRolDAO personaRolDAO;
    @EJB private ProcedimientoPasoDAO pasoDAO;
    @EJB private ExamenDAO examenDAO;
    @EJB private ExamenTipoExamenDAO examenTipoDAO;
    @EJB private ProcedimientoDAO procedimientoDAO;
    @EJB private ConsultaProcedimientoDAO consultaProcedimientoDAO;

    public CatalogoService() { }
    public CatalogoService(TipoDocumentoDAO dao) { tipoDocumentoDAO = dao; }
    public CatalogoService(TipoMedioContactoDAO dao) { tipoMedioContactoDAO = dao; }
    public CatalogoService(TipoExamenDAO dao) { tipoExamenDAO = dao; }
    public CatalogoService(ClinicaDAO dao, PersonaRolDAO personas) { clinicaDAO = dao; personaRolDAO = personas; }
    public CatalogoService(RolDAO dao, PersonaRolDAO personas, ProcedimientoPasoDAO pasos) {
        rolDAO = dao; personaRolDAO = personas; pasoDAO = pasos;
    }

    public CatalogoService(ExamenDAO dao) { examenDAO = dao; }
    public CatalogoService(ProcedimientoDAO dao, ProcedimientoPasoDAO pasos, ConsultaProcedimientoDAO consultas) {
        procedimientoDAO = dao; pasoDAO = pasos; consultaProcedimientoDAO = consultas;
    }
    public CatalogoService(ExamenTipoExamenDAO relaciones, ExamenDAO examenes, TipoExamenDAO tipos) {
        examenTipoDAO=relaciones;examenDAO=examenes;tipoExamenDAO=tipos;
    }
    public ExamenTipoExamen guardarRelacionExamen(ExamenTipoExamen value, boolean edicion, UUID examenOriginal, UUID tipoOriginal) {
        UUID examen=value.getIdExamen()==null?null:value.getIdExamen().getIdExamen();
        UUID tipo=value.getIdTipoExamen()==null?null:value.getIdTipoExamen().getIdTipoExamen();
        if(examen==null)throw new ReglaNegocioException(VALIDACION,"idExamen","examen.seleccioneExamen");
        var anterior=edicion&&value.getIdExamenTipoExamen()!=null?examenTipoDAO.buscarPorId(value.getIdExamenTipoExamen()):null;
        if(edicion&&(anterior==null||anterior.getIdExamen()==null||!Objects.equals(examenOriginal,examen)||!Objects.equals(examenOriginal,anterior.getIdExamen().getIdExamen())))
            throw new ReglaNegocioException(VALIDACION,"idExamen","relaciones.noEditables");
        if(edicion&&(anterior.getIdTipoExamen()==null||!Objects.equals(tipoOriginal,tipo)||!Objects.equals(tipo,anterior.getIdTipoExamen().getIdTipoExamen()))) {
            value.setIdTipoExamen(anterior.getIdTipoExamen());throw new ReglaNegocioException(VALIDACION,"idTipoExamen","examen.tipoNoEditable");
        }
        var persistido=tipo==null?null:tipoExamenDAO.buscarPorId(tipo);
        if(persistido==null)throw new ReglaNegocioException(VALIDACION,"idTipoExamen","examen.seleccioneTipo");
        value.setIdTipoExamen(persistido);
        if(!edicion&&!Boolean.TRUE.equals(persistido.getActivo()))throw new ReglaNegocioException(VALIDACION,"idTipoExamen","examen.tipoInactivo");
        if(examenTipoDAO.countByIdExamenAndIdTipoExamen(examen,tipo)>(edicion?1:0))throw new ReglaNegocioException(DUPLICADO,"idTipoExamen","examen.tipoDuplicado");
        value.setIdTipoExamen(persistido);value.setObservaciones(normalizar(value.getObservaciones()));
        return edicion?examenTipoDAO.actualizar(value):examenTipoDAO.guardar(value);
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<ExamenTipoExamen> relacionesExamen(UUID id,int first,int max) {return examenTipoDAO.obtenerPagina(first,max,List.of(new FiltroDAO("idExamen.idExamen",OperadorFiltro.IGUAL,id)),List.of(new OrdenDAO("idExamenTipoExamen",DireccionOrden.ASCENDENTE)));}
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarRelacionesExamen(UUID id) {return examenTipoDAO.contar(List.of(new FiltroDAO("idExamen.idExamen",OperadorFiltro.IGUAL,id)));}
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public ExamenTipoExamen buscarRelacionExamen(UUID id) {return examenTipoDAO.buscarPorId(id);}
    public void eliminarRelacionExamen(UUID id) {if(!examenTipoDAO.eliminar(id))throw new ReglaNegocioException(AUSENTE,null,"examen.errorQuitarAsociacion");}

    public void validarNombreExamen(String valor, UUID id) {
        String nombre = nombre(valor, 2, 255, "examen");
        if (examenDAO.obtenerPagina(0, 2, List.of(new FiltroDAO("nombre", OperadorFiltro.IGUAL, nombre)), List.of()).stream()
                .anyMatch(e -> e.getIdExamen() == null || !e.getIdExamen().equals(id))) {
            throw new ReglaNegocioException(DUPLICADO, "nombre", "examen.nombreDuplicado");
        }
    }
    public Examen guardarExamen(Examen datos, boolean edicion) {
        validarNombreExamen(datos.getNombre(), edicion ? datos.getIdExamen() : null);
        datos.setNombre(normalizar(datos.getNombre()));datos.setObservaciones(normalizar(datos.getObservaciones()));
        return edicion ? examenDAO.actualizar(datos) : examenDAO.guardar(datos);
    }
    public Procedimiento guardarProcedimiento(Procedimiento datos, boolean edicion) {
        String nombre = nombre(datos.getNombre(), 2, 155, "procedimiento");
        UUID id = edicion ? datos.getIdProcedimiento() : null;
        if (procedimientoDAO.obtenerTodos().stream().filter(Objects::nonNull)
                .filter(p -> id == null || !id.equals(p.getIdProcedimiento()))
                .anyMatch(p -> nombre.equalsIgnoreCase(normalizar(p.getNombre())))) {
            throw new ReglaNegocioException(DUPLICADO, "nombre", "procedimiento.nombreDuplicado");
        }
        datos.setNombre(nombre);datos.setObservaciones(normalizar(datos.getObservaciones()));
        return edicion ? procedimientoDAO.actualizar(datos) : procedimientoDAO.guardar(datos);
    }
    public void eliminarProcedimiento(UUID id) {
        if (pasoDAO.contar(List.of(new FiltroDAO("idProcedimiento.idProcedimiento", OperadorFiltro.IGUAL, id))) > 0
                || consultaProcedimientoDAO.contar(List.of(new FiltroDAO("idProcedimiento", OperadorFiltro.IGUAL, id))) > 0) {
            throw new ReglaNegocioException(REFERENCIADO, null, "procedimiento.eliminacionBloqueada");
        }
        if (!procedimientoDAO.eliminar(id)) throw new ReglaNegocioException(AUSENTE, null, "procedimiento.errorEliminar");
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Examen> listarExamen(int primero, int tamano, String nombre) {
        return examenDAO.obtenerPagina(primero, tamano, filtroNombre(nombre), List.of(new OrdenDAO("idExamen", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarExamen(String nombre) { return examenDAO.contar(filtroNombre(nombre)); }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Examen buscarExamen(UUID id) { return examenDAO.buscarPorId(id); }
    private static List<FiltroDAO> filtroProcedimiento(String nombre, Boolean activo) {
        var filtros = new java.util.ArrayList<>(filtroNombre(nombre));
        if (activo != null) filtros.add(new FiltroDAO("activo", OperadorFiltro.IGUAL, activo));return filtros;
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Procedimiento> listarProcedimiento(int primero, int tamano, String nombre, Boolean activo) {
        return procedimientoDAO.obtenerPagina(primero, tamano, filtroProcedimiento(nombre, activo), List.of(new OrdenDAO("idProcedimiento", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarProcedimiento(String nombre, Boolean activo) { return procedimientoDAO.contar(filtroProcedimiento(nombre, activo)); }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Procedimiento buscarProcedimiento(UUID id) { return procedimientoDAO.buscarPorId(id); }

    public TipoDocumento guardarTipoDocumento(TipoDocumento datos, boolean edicion) {
        Objects.requireNonNull(datos, "El tipo documental es requerido");
        String nombre = nombre(datos.getNombre(), 1, 155, "tipoDocumento");
        if (tipoDocumentoDAO.existeNombreNormalizado(datos.getNombre(),
                edicion ? datos.getIdTipoDocumento() : null)) {
            throw new ReglaNegocioException(DUPLICADO, "nombre", "tipoDocumento.nombreDuplicado");
        }
        validarRegex(datos.getExpresionRegular(), "tipoDocumento");
        datos.setNombre(nombre);
        return edicion ? tipoDocumentoDAO.actualizar(datos) : tipoDocumentoDAO.guardar(datos);
    }

    public TipoMedioContacto guardarTipoMedioContacto(TipoMedioContacto datos, boolean edicion) {
        Objects.requireNonNull(datos, "El tipo de contacto es requerido");
        String nombre = nombre(datos.getNombre(), 1, 155, "tipoMedioContacto");
        String indicaciones = normalizar(datos.getIndicaciones());
        if (indicaciones == null || indicaciones.isEmpty()) {
            throw new ReglaNegocioException(VALIDACION, "indicaciones", "tipoMedioContacto.indicacionesRequeridas");
        }
        validarRegex(datos.getExpresionRegular(), "tipoMedioContacto");
        datos.setNombre(nombre);
        datos.setIndicaciones(indicaciones);
        return edicion ? tipoMedioContactoDAO.actualizar(datos) : tipoMedioContactoDAO.guardar(datos);
    }

    public Clinica guardarClinica(Clinica datos, boolean edicion) {
        Objects.requireNonNull(datos, "La clinica es requerida");
        String nombre = nombre(datos.getNombre(), 2, 255, "clinica");
        String tipo = normalizar(datos.getTipo());
        if (tipo == null || tipo.isEmpty()) throw new ReglaNegocioException(VALIDACION, "tipo", "clinica.tipoRequerido");
        if (tipo.length() > 20) throw new ReglaNegocioException(VALIDACION, "tipo", "clinica.tipoMaximo");
        UUID id = edicion ? datos.getIdClinica() : null;
        if (clinicaDAO.obtenerTodos().stream().filter(Objects::nonNull)
                .filter(c -> id == null || !id.equals(c.getIdClinica()))
                .anyMatch(c -> nombre.equalsIgnoreCase(normalizar(c.getNombre())))) {
            throw new ReglaNegocioException(DUPLICADO, "nombre", "clinica.nombreDuplicado");
        }
        datos.setNombre(nombre); datos.setTipo(tipo); datos.setComentarios(normalizar(datos.getComentarios()));
        return edicion ? clinicaDAO.actualizar(datos) : clinicaDAO.guardar(datos);
    }

    public Rol guardarRol(Rol datos, boolean edicion) {
        Objects.requireNonNull(datos, "El rol es requerido");
        String nombre = nombre(datos.getNombre(), 2, 155, "rol");
        UUID id = edicion ? datos.getIdRol() : null;
        if (rolDAO.obtenerTodos().stream().filter(Objects::nonNull)
                .filter(r -> id == null || !id.equals(r.getIdRol()))
                .anyMatch(r -> nombre.equalsIgnoreCase(normalizar(r.getNombre())))) {
            throw new ReglaNegocioException(DUPLICADO, "nombre", "rol.nombreDuplicado");
        }
        datos.setNombre(nombre); datos.setObservaciones(normalizar(datos.getObservaciones()));
        return edicion ? rolDAO.actualizar(datos) : rolDAO.guardar(datos);
    }

    public TipoExamen guardarTipoExamen(TipoExamen datos, boolean edicion) {
        Objects.requireNonNull(datos, "El tipo de examen es requerido");
        String nombre = normalizar(datos.getNombre());
        TipoExamen anterior = edicion && datos.getIdTipoExamen() != null
                ? tipoExamenDAO.buscarPorId(datos.getIdTipoExamen()) : null;
        validarNombreTipoExamen(nombre, edicion ? datos.getIdTipoExamen() : null,
                anterior == null ? null : anterior.getNombre());
        datos.setNombre(nombre); datos.setObservaciones(normalizar(datos.getObservaciones()));
        return edicion ? tipoExamenDAO.actualizar(datos) : tipoExamenDAO.guardar(datos);
    }

    public void validarNombreTipoExamen(String valor, UUID idActual, String nombreAnterior) {
        String nombre = normalizar(valor);
        if (nombre == null || nombre.isEmpty()) throw new ReglaNegocioException(VALIDACION, "nombre", "tipoExamen.nombreRequerido");
        if (idActual != null && Objects.equals(nombre, normalizar(nombreAnterior))) return;
        nombre = nombre(nombre, 2, 255, "tipoExamen");
        if (tipoExamenDAO.obtenerPagina(0, 2,
                List.of(new FiltroDAO("nombre", OperadorFiltro.IGUAL, nombre)), List.of()).stream()
                .anyMatch(t -> idActual == null || !idActual.equals(t.getIdTipoExamen()))) {
            throw new ReglaNegocioException(DUPLICADO, "nombre", "tipoExamen.nombreDuplicado");
        }
    }

    public void eliminarClinica(UUID id) {
        Objects.requireNonNull(id, "La clinica es requerida");
        if (personaRolDAO.obtenerTodos().stream().anyMatch(p -> p.getIdClinica() != null && id.equals(p.getIdClinica().getIdClinica()))) {
            throw new ReglaNegocioException(REFERENCIADO, null, "clinica.eliminacionBloqueada");
        }
        if (!clinicaDAO.eliminar(id)) throw new ReglaNegocioException(AUSENTE, null, "clinica.errorEliminar");
    }

    public void eliminarRol(UUID id) {
        Objects.requireNonNull(id, "El rol es requerido");
        if (personaRolDAO.obtenerTodos().stream().anyMatch(p -> p.getIdRol() != null && id.equals(p.getIdRol().getIdRol()))
                || pasoDAO.obtenerTodos().stream().anyMatch(p -> p.getIdRol() != null && id.equals(p.getIdRol().getIdRol()))) {
            throw new ReglaNegocioException(REFERENCIADO, null, "rol.eliminacionBloqueada");
        }
        if (!rolDAO.eliminar(id)) throw new ReglaNegocioException(AUSENTE, null, "rol.errorEliminar");
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<TipoDocumento> listarTipoDocumento(int primero, int tamano, String nombre) {
        return tipoDocumentoDAO.obtenerPagina(primero, tamano, filtroNombre(nombre),
                List.of(new OrdenDAO("idTipoDocumento", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarTipoDocumento(String nombre) { return tipoDocumentoDAO.contar(filtroNombre(nombre)); }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public TipoDocumento buscarTipoDocumento(UUID id) { return tipoDocumentoDAO.buscarPorId(id); }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<TipoMedioContacto> listarTipoMedioContacto(int primero, int tamano, String nombre) {
        return tipoMedioContactoDAO.obtenerPagina(primero, tamano, filtroNombre(nombre),
                List.of(new OrdenDAO("idTipoMedioContacto", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarTipoMedioContacto(String nombre) { return tipoMedioContactoDAO.contar(filtroNombre(nombre)); }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public TipoMedioContacto buscarTipoMedioContacto(UUID id) { return tipoMedioContactoDAO.buscarPorId(id); }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<TipoExamen> listarTipoExamen(int primero, int tamano, String nombre) {
        return tipoExamenDAO.obtenerPagina(primero, tamano, filtroNombre(nombre),
                List.of(new OrdenDAO("idTipoExamen", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarTipoExamen(String nombre) { return tipoExamenDAO.contar(filtroNombre(nombre)); }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public TipoExamen buscarTipoExamen(UUID id) { return tipoExamenDAO.buscarPorId(id); }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Clinica> listarClinica(int primero, int tamano, String nombre) {
        return clinicaDAO.obtenerPagina(primero, tamano, filtroNombre(nombre),
                List.of(new OrdenDAO("idClinica", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarClinica(String nombre) { return clinicaDAO.contar(filtroNombre(nombre)); }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Clinica buscarClinica(UUID id) { return clinicaDAO.buscarPorId(id); }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Rol> listarRol(int primero, int tamano, String nombre) {
        return rolDAO.obtenerPagina(primero, tamano, filtroNombre(nombre),
                List.of(new OrdenDAO("idRol", DireccionOrden.ASCENDENTE)));
    }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long contarRol(String nombre) { return rolDAO.contar(filtroNombre(nombre)); }
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Rol buscarRol(UUID id) { return rolDAO.buscarPorId(id); }

    private static List<FiltroDAO> filtroNombre(String nombre) {
        return nombre == null || nombre.isBlank() ? List.of()
                : List.of(new FiltroDAO("nombre", OperadorFiltro.CONTIENE, nombre.strip()));
    }

    private static String normalizar(String valor) { return valor == null ? null : valor.strip(); }

    private static String nombre(String valor, int minimo, int maximo, String modulo) {
        String nombre = normalizar(valor);
        if (nombre == null || nombre.isEmpty()) throw new ReglaNegocioException(VALIDACION, "nombre", modulo + ".nombreRequerido");
        if (nombre.length() < minimo) throw new ReglaNegocioException(VALIDACION, "nombre", modulo + ".nombreMinimo");
        if (nombre.length() > maximo) throw new ReglaNegocioException(VALIDACION, "nombre", modulo + ".nombreMaximo");
        return nombre;
    }

    private static void validarRegex(String expresion, String modulo) {
        if (expresion != null && !expresion.isBlank()) {
            try { Pattern.compile(expresion); }
            catch (PatternSyntaxException ex) {
                throw new ReglaNegocioException(VALIDACION, "expresionRegular", modulo + ".expresionRegularInvalida", ex);
            }
        }
    }
}
