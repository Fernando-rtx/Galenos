package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.EJB;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Examen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Procedimiento;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPaso;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoExamen;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.ProcedimientoPasoSecuencia;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.Rol;

/** Crea un paso y sus relaciones dentro de la secuencia del procedimiento. */
@Stateless
@LocalBean
public class ProcedimientoFlujoService {

    @EJB
    private ProcedimientoDAO procedimientoDAO;
    @EJB
    private ProcedimientoPasoDAO pasoDAO;
    @EJB
    private ProcedimientoPasoSecuenciaDAO secuenciaDAO;
    @EJB
    private ProcedimientoPasoExamenDAO pasoExamenDAO;
    @EJB
    private RolDAO rolDAO;
    @EJB
    private ExamenDAO examenDAO;

    public ProcedimientoFlujoService() { }
    public ProcedimientoFlujoService(ProcedimientoPasoExamenDAO relaciones,ProcedimientoPasoDAO pasos,ExamenDAO examenes) {pasoExamenDAO=relaciones;pasoDAO=pasos;examenDAO=examenes;}
    public ProcedimientoFlujoService(ProcedimientoPasoSecuenciaDAO secuencias,ProcedimientoPasoDAO pasos) {secuenciaDAO=secuencias;pasoDAO=pasos;}
    private static ReglaNegocioException regla(String clave) {return new ReglaNegocioException(Set.of("ppe.asociacionDuplicada","secuencia.enlaceDuplicado","secuencia.mismoOrden").contains(clave)?ReglaNegocioException.Motivo.DUPLICADO:ReglaNegocioException.Motivo.VALIDACION,null,clave);}
    public ProcedimientoPasoExamen guardarPasoExamen(ProcedimientoPasoExamen value,boolean edicion,UUID pasoOriginal,UUID examenOriginal,UUID procedimiento) {
        var paso=value.getIdProcedimientoPaso();var examen=value.getIdExamen();
        if(paso==null||paso.getIdProcedimientoPaso()==null)throw regla("ppe.pasoRequerido");
        if(examen==null||examen.getIdExamen()==null)throw regla("ppe.examenRequerido");
        if(edicion&&(!java.util.Objects.equals(pasoOriginal,paso.getIdProcedimientoPaso())||!java.util.Objects.equals(examenOriginal,examen.getIdExamen())))throw regla("relaciones.noEditables");
        if(pasoDAO.buscarPorId(paso.getIdProcedimientoPaso())==null)throw regla("ppe.pasoNoExiste");
        if(procedimiento!=null&&(paso.getIdProcedimiento()==null||!procedimiento.equals(paso.getIdProcedimiento().getIdProcedimiento())))throw regla("ppe.pasoFueraProcedimiento");
        var persistido=examenDAO.buscarPorId(examen.getIdExamen());if(persistido==null)throw regla("ppe.examenNoExiste");
        if(!edicion&&!Boolean.TRUE.equals(persistido.getActivo()))throw regla("ppe.examenInactivo");
        for(var old:pasoExamenDAO.obtenerTodos()) {
            if(value.getIdProcedimientoPasoExamen()!=null&&value.getIdProcedimientoPasoExamen().equals(old.getIdProcedimientoPasoExamen()))continue;
            if(old.getIdProcedimientoPaso()!=null&&old.getIdExamen()!=null&&paso.getIdProcedimientoPaso().equals(old.getIdProcedimientoPaso().getIdProcedimientoPaso())&&examen.getIdExamen().equals(old.getIdExamen().getIdExamen()))throw regla("ppe.asociacionDuplicada");
        }
        value.setObservaciones(value.getObservaciones()==null?null:value.getObservaciones().trim());return edicion?pasoExamenDAO.actualizar(value):pasoExamenDAO.guardar(value);
    }
    public ProcedimientoPasoSecuencia guardarSecuencia(ProcedimientoPasoSecuencia value,boolean edicion,UUID origenOriginal,UUID destinoOriginal,UUID procedimiento) {
        UUID origen=value.getIdProcedimientoPaso()==null?null:value.getIdProcedimientoPaso().getIdProcedimientoPaso();UUID destino=value.getIdProcedimientoPasoReferencia();
        if(edicion&&(!java.util.Objects.equals(origenOriginal,origen)||!java.util.Objects.equals(destinoOriginal,destino)))throw regla("relaciones.noEditables");
        if(origen==null)throw regla("secuencia.pasoOrigenRequerido");var from=pasoDAO.buscarPorId(origen);if(from==null)throw regla("secuencia.pasoOrigenNoExiste");
        if(procedimiento!=null&&(from.getIdProcedimiento()==null||!procedimiento.equals(from.getIdProcedimiento().getIdProcedimiento())))throw regla("secuencia.pasoFueraProcedimiento");
        value.setIdProcedimientoPaso(from);String tipo=value.getTipoSecuencia()==null?null:value.getTipoSecuencia().trim();value.setTipoSecuencia(tipo);
        if(tipo==null||tipo.isEmpty())throw regla("secuencia.tipoRequerido");if(tipo.length()>20)throw regla("secuencia.tipoMaximo");
        if(destino==null)throw regla("secuencia.pasoDestinoRequerido");var to=pasoDAO.buscarPorId(destino);if(to==null)throw regla("secuencia.pasoDestinoNoExiste");
        if(from.getIdProcedimiento()==null||to.getIdProcedimiento()==null||!from.getIdProcedimiento().getIdProcedimiento().equals(to.getIdProcedimiento().getIdProcedimiento()))throw regla("secuencia.pasosDeProcedimientosDistintos");
        var edges=new java.util.HashMap<UUID,List<UUID>>();
        for(var old:secuenciaDAO.obtenerTodos()) {
            if(value.getIdProcedimientoPasoSecuencia()!=null&&value.getIdProcedimientoPasoSecuencia().equals(old.getIdProcedimientoPasoSecuencia()))continue;
            if(old.getIdProcedimientoPaso()==null)continue;UUID source=old.getIdProcedimientoPaso().getIdProcedimientoPaso();
            if(origen.equals(source)) {if(destino.equals(old.getIdProcedimientoPasoReferencia()))throw regla("secuencia.enlaceDuplicado");if(old.getTipoSecuencia()!=null&&tipo.equalsIgnoreCase(old.getTipoSecuencia().trim()))throw regla("secuencia.mismoOrden");}
            if(source!=null&&old.getIdProcedimientoPasoReferencia()!=null)edges.computeIfAbsent(source,k->new java.util.ArrayList<>()).add(old.getIdProcedimientoPasoReferencia());
        }
        var pending=new java.util.ArrayDeque<UUID>();var visited=new java.util.HashSet<UUID>();pending.add(destino);
        while(!pending.isEmpty()){UUID id=pending.remove();if(id.equals(origen))throw regla("secuencia.cicloDetectado");if(visited.add(id))pending.addAll(edges.getOrDefault(id,List.of()));}
        return edicion?secuenciaDAO.actualizar(value):secuenciaDAO.guardar(value);
    }
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public List<ProcedimientoPasoExamen> relacionesPaso(UUID id,int first,int max){return pasoExamenDAO.obtenerPagina(first,max,List.of(new FiltroDAO("idProcedimientoPaso.idProcedimientoPaso",OperadorFiltro.IGUAL,id)),List.of(new OrdenDAO("idProcedimientoPasoExamen",DireccionOrden.ASCENDENTE)));}
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public long contarRelacionesPaso(UUID id){return pasoExamenDAO.contar(List.of(new FiltroDAO("idProcedimientoPaso.idProcedimientoPaso",OperadorFiltro.IGUAL,id)));}
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public ProcedimientoPasoExamen buscarRelacionPaso(UUID id){return pasoExamenDAO.buscarPorId(id);}
    public void eliminarRelacionPaso(UUID id){if(!pasoExamenDAO.eliminar(id))throw regla("ppe.errorQuitar");}
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public List<ProcedimientoPasoSecuencia> secuenciasPaso(UUID id,int first,int max){return secuenciaDAO.obtenerPagina(first,max,List.of(new FiltroDAO("idProcedimientoPaso.idProcedimientoPaso",OperadorFiltro.IGUAL,id)),List.of(new OrdenDAO("idProcedimientoPasoSecuencia",DireccionOrden.ASCENDENTE)));}
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public long contarSecuenciasPaso(UUID id){return secuenciaDAO.contar(List.of(new FiltroDAO("idProcedimientoPaso.idProcedimientoPaso",OperadorFiltro.IGUAL,id)));}
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public ProcedimientoPasoSecuencia buscarSecuencia(UUID id){return secuenciaDAO.buscarPorId(id);}
    public void eliminarSecuencia(UUID id){if(!secuenciaDAO.eliminar(id))throw regla("secuencia.errorQuitar");}

    public ProcedimientoPaso crearPaso(UUID idProcedimiento, UUID idPasoOrigen,
            String nombre, UUID idRol, boolean indicaFin, String tipoSecuencia,
            List<UUID> idsExamenes) {
        String nombreNormalizado = nombre == null ? "" : nombre.trim();
        if (nombreNormalizado.length() < 2) {
            throw new FlujoProcedimientoException("procedimientoPaso.nombreMinimo");
        }
        if (nombreNormalizado.length() > 155) {
            throw new FlujoProcedimientoException("procedimientoPaso.nombreMaximo");
        }

        Procedimiento procedimiento = idProcedimiento == null
                ? null : procedimientoDAO.buscarPorId(idProcedimiento);
        if (procedimiento == null) {
            throw new FlujoProcedimientoException("procedimientoPaso.procedimientoNoExiste");
        }
        if (!Boolean.TRUE.equals(procedimiento.getActivo())) {
            throw new FlujoProcedimientoException("procedimientoPaso.procedimientoInactivo");
        }

        List<ProcedimientoPaso> pasos = pasoDAO.obtenerTodos().stream()
                .filter(paso -> paso != null && paso.getIdProcedimiento() != null
                && idProcedimiento.equals(paso.getIdProcedimiento().getIdProcedimiento()))
                .toList();
        ProcedimientoPaso origen = validarOrigen(idPasoOrigen, idProcedimiento, pasos);

        if (pasos.stream().anyMatch(paso -> paso.getNombre() != null
                && nombreNormalizado.equalsIgnoreCase(paso.getNombre().trim()))) {
            throw new FlujoProcedimientoException("procedimientoPaso.nombreDuplicado");
        }

        Rol rol = idRol == null ? null : rolDAO.buscarPorId(idRol);
        if (rol == null) {
            throw new FlujoProcedimientoException("procedimientoPaso.rolNoExiste");
        }
        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new FlujoProcedimientoException("procedimientoPaso.rolInactivo");
        }
        if (esRolPaciente(rol)) {
            throw new FlujoProcedimientoException("procedimientoPaso.rolPaciente");
        }

        List<UUID> examenesUnicos = idsExamenes == null ? List.of()
                : idsExamenes.stream().filter(id -> id != null).distinct().toList();
        if (idsExamenes != null && examenesUnicos.size() != idsExamenes.size()) {
            throw new FlujoProcedimientoException("ppe.examenDuplicado");
        }
        List<Examen> examenes = examenesUnicos.stream().map(examenDAO::buscarPorId)
                .toList();
        if (examenes.stream().anyMatch(examen -> examen == null)) {
            throw new FlujoProcedimientoException("ppe.examenNoExiste");
        }
        if (examenes.stream().anyMatch(examen -> !Boolean.TRUE.equals(examen.getActivo()))) {
            throw new FlujoProcedimientoException("ppe.examenInactivo");
        }

        String tipoNormalizado = tipoSecuencia == null ? "SIGUIENTE"
                : tipoSecuencia.trim().toUpperCase(Locale.ROOT);
        if (origen != null && !Set.of("SIGUIENTE", "ALTERNATIVA").contains(tipoNormalizado)) {
            throw new FlujoProcedimientoException("secuencia.tipoInvalido");
        }
        if (origen != null && secuenciaDAO.obtenerTodos().stream()
                .anyMatch(enlace -> enlace != null && enlace.getIdProcedimientoPaso() != null
                && idPasoOrigen.equals(enlace.getIdProcedimientoPaso().getIdProcedimientoPaso())
                && tipoNormalizado.equalsIgnoreCase(enlace.getTipoSecuencia()))) {
            throw new FlujoProcedimientoException("secuencia.mismoOrden");
        }

        ProcedimientoPaso nuevo = new ProcedimientoPaso();
        nuevo.setIdProcedimiento(procedimiento);
        nuevo.setIdRol(rol);
        nuevo.setNombre(nombreNormalizado);
        nuevo.setIndicaFin(indicaFin);
        pasoDAO.guardar(nuevo);

        if (origen != null) {
            ProcedimientoPasoSecuencia enlace = new ProcedimientoPasoSecuencia();
            enlace.setIdProcedimientoPaso(origen);
            enlace.setIdProcedimientoPasoReferencia(nuevo.getIdProcedimientoPaso());
            enlace.setTipoSecuencia(tipoNormalizado);
            secuenciaDAO.guardar(enlace);
        }

        for (Examen examen : examenes) {
            ProcedimientoPasoExamen relacion = new ProcedimientoPasoExamen();
            relacion.setIdProcedimientoPaso(nuevo);
            relacion.setIdExamen(examen);
            relacion.setFechaCreacion(new Date());
            relacion.setActivo(Boolean.TRUE);
            pasoExamenDAO.guardar(relacion);
        }
        return nuevo;
    }

    /** Elimina un paso terminal, sus enlaces entrantes y sus exámenes asociados. */
    public void eliminarPaso(UUID idPaso) {
        ProcedimientoPaso paso = idPaso == null ? null : pasoDAO.buscarPorId(idPaso);
        if (paso == null) {
            throw new FlujoProcedimientoException("flujo.pasoNoExiste");
        }

        List<ProcedimientoPasoSecuencia> secuencias = secuenciaDAO.obtenerTodos();
        if (secuencias.stream().anyMatch(enlace -> enlace != null
                && enlace.getIdProcedimientoPaso() != null
                && idPaso.equals(enlace.getIdProcedimientoPaso().getIdProcedimientoPaso()))) {
            throw new FlujoProcedimientoException("flujo.pasoNoEliminable");
        }

        secuencias.stream()
                .filter(enlace -> enlace != null
                && idPaso.equals(enlace.getIdProcedimientoPasoReferencia()))
                .map(ProcedimientoPasoSecuencia::getIdProcedimientoPasoSecuencia)
                .filter(java.util.Objects::nonNull)
                .toList()
                .forEach(secuenciaDAO::eliminar);
        pasoExamenDAO.obtenerTodos().stream()
                .filter(relacion -> relacion != null && relacion.getIdProcedimientoPaso() != null
                && idPaso.equals(relacion.getIdProcedimientoPaso().getIdProcedimientoPaso()))
                .map(ProcedimientoPasoExamen::getIdProcedimientoPasoExamen)
                .filter(java.util.Objects::nonNull)
                .toList()
                .forEach(pasoExamenDAO::eliminar);
        if (!pasoDAO.eliminar(idPaso)) {
            throw new IllegalStateException("No se pudo eliminar el paso del procedimiento.");
        }
    }

    /** Actualiza los datos de un paso sin alterar sus enlaces con otros pasos. */
    public void actualizarPaso(UUID idPaso, String nombre, UUID idRol,
            boolean indicaFin, List<UUID> idsExamenes) {
        String nombreNormalizado = nombre == null ? "" : nombre.trim();
        if (nombreNormalizado.length() < 2) {
            throw new FlujoProcedimientoException("procedimientoPaso.nombreMinimo");
        }
        if (nombreNormalizado.length() > 155) {
            throw new FlujoProcedimientoException("procedimientoPaso.nombreMaximo");
        }

        ProcedimientoPaso paso = idPaso == null ? null : pasoDAO.buscarPorId(idPaso);
        if (paso == null || paso.getIdProcedimiento() == null) {
            throw new FlujoProcedimientoException("flujo.pasoNoExiste");
        }
        if (paso.getIdRol() == null || !java.util.Objects.equals(idRol, paso.getIdRol().getIdRol())) {
            throw new FlujoProcedimientoException("relaciones.noEditables");
        }
        Set<UUID> examenesGuardados = pasoExamenDAO.obtenerTodos().stream()
                .filter(relacion -> relacion != null && relacion.getIdProcedimientoPaso() != null
                && idPaso.equals(relacion.getIdProcedimientoPaso().getIdProcedimientoPaso()))
                .filter(relacion -> relacion.getIdExamen() != null)
                .map(relacion -> relacion.getIdExamen().getIdExamen())
                .collect(java.util.stream.Collectors.toSet());
        Set<UUID> examenesRecibidos = idsExamenes == null ? Set.of() : new java.util.HashSet<>(idsExamenes);
        if (!examenesGuardados.equals(examenesRecibidos)) {
            throw new FlujoProcedimientoException("relaciones.noEditables");
        }
        UUID idProcedimiento = paso.getIdProcedimiento().getIdProcedimiento();
        List<ProcedimientoPaso> pasos = pasoDAO.obtenerTodos().stream()
                .filter(otro -> otro != null && otro.getIdProcedimiento() != null
                && idProcedimiento.equals(otro.getIdProcedimiento().getIdProcedimiento()))
                .toList();
        if (pasos.stream().filter(otro -> !idPaso.equals(otro.getIdProcedimientoPaso()))
                .anyMatch(otro -> otro.getNombre() != null
                && nombreNormalizado.equalsIgnoreCase(otro.getNombre().trim()))) {
            throw new FlujoProcedimientoException("procedimientoPaso.nombreDuplicado");
        }

        Rol rol = idRol == null ? null : rolDAO.buscarPorId(idRol);
        if (rol == null) {
            throw new FlujoProcedimientoException("procedimientoPaso.rolNoExiste");
        }
        if (esRolPaciente(rol)) {
            throw new FlujoProcedimientoException("procedimientoPaso.rolPaciente");
        }

        List<UUID> examenesUnicos = idsExamenes == null ? List.of()
                : idsExamenes.stream().filter(id -> id != null).distinct().toList();
        if (idsExamenes != null && examenesUnicos.size() != idsExamenes.size()) {
            throw new FlujoProcedimientoException("ppe.examenDuplicado");
        }
        List<Examen> examenes = examenesUnicos.stream().map(examenDAO::buscarPorId).toList();
        if (examenes.stream().anyMatch(examen -> examen == null)) {
            throw new FlujoProcedimientoException("ppe.examenNoExiste");
        }

        List<ProcedimientoPasoSecuencia> secuencias = secuenciaDAO.obtenerTodos();
        if (indicaFin && secuencias.stream().anyMatch(enlace -> enlace != null
                && enlace.getIdProcedimientoPaso() != null
                && idPaso.equals(enlace.getIdProcedimientoPaso().getIdProcedimientoPaso()))) {
            throw new FlujoProcedimientoException("flujo.pasoFinalConContinuaciones");
        }

        paso.setNombre(nombreNormalizado);
        paso.setIndicaFin(indicaFin);
        pasoDAO.actualizar(paso);

    }

    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public List<ProcedimientoPaso> pasos(UUID procedimiento, int first, int max) {
        return pasoDAO.obtenerPagina(first,max,List.of(new FiltroDAO("idProcedimiento.idProcedimiento",OperadorFiltro.IGUAL,procedimiento)),List.of(new OrdenDAO("idProcedimientoPaso",DireccionOrden.ASCENDENTE)));
    }
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public long contarPasos(UUID procedimiento) { return pasoDAO.contar(List.of(new FiltroDAO("idProcedimiento.idProcedimiento",OperadorFiltro.IGUAL,procedimiento))); }
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public ProcedimientoPaso buscarPaso(UUID id) { return pasoDAO.buscarPorId(id); }
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
    public List<UUID> examenesPaso(UUID id) {
        List<UUID> ids = new java.util.ArrayList<>();
        int first = 0;
        List<ProcedimientoPasoExamen> pagina;
        do {
            pagina = pasoExamenDAO.obtenerPagina(first,100,List.of(new FiltroDAO("idProcedimientoPaso.idProcedimientoPaso",OperadorFiltro.IGUAL,id)),List.of(new OrdenDAO("idProcedimientoPasoExamen",DireccionOrden.ASCENDENTE)));
            pagina.forEach(p -> ids.add(p.getIdExamen().getIdExamen()));first += pagina.size();
        } while (pagina.size() == 100);
        return List.copyOf(ids);
    }

    private boolean esRolPaciente(Rol rol) {
        return rol != null && rol.getNombre() != null
                && "paciente".equalsIgnoreCase(rol.getNombre().trim());
    }

    private ProcedimientoPaso validarOrigen(UUID idPasoOrigen, UUID idProcedimiento,
            List<ProcedimientoPaso> pasos) {
        if (idPasoOrigen == null) {
            if (!pasos.isEmpty()) {
                throw new FlujoProcedimientoException("flujo.inicialYaExiste");
            }
            return null;
        }
        ProcedimientoPaso origen = pasoDAO.buscarPorId(idPasoOrigen);
        if (origen == null || origen.getIdProcedimiento() == null
                || !idProcedimiento.equals(origen.getIdProcedimiento().getIdProcedimiento())) {
            throw new FlujoProcedimientoException("secuencia.pasoOrigenNoExiste");
        }
        if (Boolean.TRUE.equals(origen.getIndicaFin())) {
            throw new FlujoProcedimientoException("flujo.pasoFinalNoPuedeContinuar");
        }
        return origen;
    }
}
