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

    public ProcedimientoPaso crearPaso(UUID idProcedimiento, UUID idPasoOrigen,
            String nombre, UUID idRol, boolean indicaFin, String tipoSecuencia,
            List<UUID> idsExamenes) {
        String nombreNormalizado = nombre == null ? "" : nombre.trim();
        if (nombreNormalizado.length() < 2) {
            throw new IllegalArgumentException("procedimientoPaso.nombreMinimo");
        }
        if (nombreNormalizado.length() > 155) {
            throw new IllegalArgumentException("procedimientoPaso.nombreMaximo");
        }

        Procedimiento procedimiento = idProcedimiento == null
                ? null : procedimientoDAO.buscarPorId(idProcedimiento);
        if (procedimiento == null) {
            throw new IllegalArgumentException("procedimientoPaso.procedimientoNoExiste");
        }
        if (!Boolean.TRUE.equals(procedimiento.getActivo())) {
            throw new IllegalArgumentException("procedimientoPaso.procedimientoInactivo");
        }

        List<ProcedimientoPaso> pasos = pasoDAO.obtenerTodos().stream()
                .filter(paso -> paso != null && paso.getIdProcedimiento() != null
                && idProcedimiento.equals(paso.getIdProcedimiento().getIdProcedimiento()))
                .toList();
        ProcedimientoPaso origen = validarOrigen(idPasoOrigen, idProcedimiento, pasos);

        if (pasos.stream().anyMatch(paso -> paso.getNombre() != null
                && nombreNormalizado.equalsIgnoreCase(paso.getNombre().trim()))) {
            throw new IllegalArgumentException("procedimientoPaso.nombreDuplicado");
        }

        Rol rol = idRol == null ? null : rolDAO.buscarPorId(idRol);
        if (rol == null) {
            throw new IllegalArgumentException("procedimientoPaso.rolNoExiste");
        }
        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new IllegalArgumentException("procedimientoPaso.rolInactivo");
        }
        if (pasos.stream().anyMatch(paso -> paso.getIdRol() != null
                && idRol.equals(paso.getIdRol().getIdRol()))) {
            throw new IllegalArgumentException("procedimientoPaso.rolDuplicado");
        }

        List<UUID> examenesUnicos = idsExamenes == null ? List.of()
                : idsExamenes.stream().filter(id -> id != null).distinct().toList();
        if (idsExamenes != null && examenesUnicos.size() != idsExamenes.size()) {
            throw new IllegalArgumentException("ppe.examenDuplicado");
        }
        List<Examen> examenes = examenesUnicos.stream().map(examenDAO::buscarPorId)
                .toList();
        if (examenes.stream().anyMatch(examen -> examen == null)) {
            throw new IllegalArgumentException("ppe.examenNoExiste");
        }
        if (examenes.stream().anyMatch(examen -> !Boolean.TRUE.equals(examen.getActivo()))) {
            throw new IllegalArgumentException("ppe.examenInactivo");
        }

        String tipoNormalizado = tipoSecuencia == null ? "SIGUIENTE"
                : tipoSecuencia.trim().toUpperCase(Locale.ROOT);
        if (origen != null && !Set.of("SIGUIENTE", "ALTERNATIVA").contains(tipoNormalizado)) {
            throw new IllegalArgumentException("secuencia.tipoInvalido");
        }
        if (origen != null && secuenciaDAO.obtenerTodos().stream()
                .anyMatch(enlace -> enlace != null && enlace.getIdProcedimientoPaso() != null
                && idPasoOrigen.equals(enlace.getIdProcedimientoPaso().getIdProcedimientoPaso())
                && tipoNormalizado.equalsIgnoreCase(enlace.getTipoSecuencia()))) {
            throw new IllegalArgumentException("secuencia.mismoOrden");
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
            throw new IllegalArgumentException("flujo.pasoNoExiste");
        }

        List<ProcedimientoPasoSecuencia> secuencias = secuenciaDAO.obtenerTodos();
        if (secuencias.stream().anyMatch(enlace -> enlace != null
                && enlace.getIdProcedimientoPaso() != null
                && idPaso.equals(enlace.getIdProcedimientoPaso().getIdProcedimientoPaso()))) {
            throw new IllegalArgumentException("flujo.pasoNoEliminable");
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
            throw new IllegalArgumentException("procedimientoPaso.nombreMinimo");
        }
        if (nombreNormalizado.length() > 155) {
            throw new IllegalArgumentException("procedimientoPaso.nombreMaximo");
        }

        ProcedimientoPaso paso = idPaso == null ? null : pasoDAO.buscarPorId(idPaso);
        if (paso == null || paso.getIdProcedimiento() == null) {
            throw new IllegalArgumentException("flujo.pasoNoExiste");
        }
        if (paso.getIdRol() == null || !java.util.Objects.equals(idRol, paso.getIdRol().getIdRol())) {
            throw new IllegalArgumentException("relaciones.noEditables");
        }
        Set<UUID> examenesGuardados = pasoExamenDAO.obtenerTodos().stream()
                .filter(relacion -> relacion != null && relacion.getIdProcedimientoPaso() != null
                && idPaso.equals(relacion.getIdProcedimientoPaso().getIdProcedimientoPaso()))
                .filter(relacion -> relacion.getIdExamen() != null)
                .map(relacion -> relacion.getIdExamen().getIdExamen())
                .collect(java.util.stream.Collectors.toSet());
        Set<UUID> examenesRecibidos = idsExamenes == null ? Set.of() : new java.util.HashSet<>(idsExamenes);
        if (!examenesGuardados.equals(examenesRecibidos)) {
            throw new IllegalArgumentException("relaciones.noEditables");
        }
        UUID idProcedimiento = paso.getIdProcedimiento().getIdProcedimiento();
        List<ProcedimientoPaso> pasos = pasoDAO.obtenerTodos().stream()
                .filter(otro -> otro != null && otro.getIdProcedimiento() != null
                && idProcedimiento.equals(otro.getIdProcedimiento().getIdProcedimiento()))
                .toList();
        if (pasos.stream().filter(otro -> !idPaso.equals(otro.getIdProcedimientoPaso()))
                .anyMatch(otro -> otro.getNombre() != null
                && nombreNormalizado.equalsIgnoreCase(otro.getNombre().trim()))) {
            throw new IllegalArgumentException("procedimientoPaso.nombreDuplicado");
        }

        Rol rol = idRol == null ? null : rolDAO.buscarPorId(idRol);
        if (rol == null) {
            throw new IllegalArgumentException("procedimientoPaso.rolNoExiste");
        }
        if (pasos.stream().filter(otro -> !idPaso.equals(otro.getIdProcedimientoPaso()))
                .anyMatch(otro -> otro.getIdRol() != null
                && idRol.equals(otro.getIdRol().getIdRol()))) {
            throw new IllegalArgumentException("procedimientoPaso.rolDuplicado");
        }

        List<UUID> examenesUnicos = idsExamenes == null ? List.of()
                : idsExamenes.stream().filter(id -> id != null).distinct().toList();
        if (idsExamenes != null && examenesUnicos.size() != idsExamenes.size()) {
            throw new IllegalArgumentException("ppe.examenDuplicado");
        }
        List<Examen> examenes = examenesUnicos.stream().map(examenDAO::buscarPorId).toList();
        if (examenes.stream().anyMatch(examen -> examen == null)) {
            throw new IllegalArgumentException("ppe.examenNoExiste");
        }

        List<ProcedimientoPasoSecuencia> secuencias = secuenciaDAO.obtenerTodos();
        if (indicaFin && secuencias.stream().anyMatch(enlace -> enlace != null
                && enlace.getIdProcedimientoPaso() != null
                && idPaso.equals(enlace.getIdProcedimientoPaso().getIdProcedimientoPaso()))) {
            throw new IllegalArgumentException("flujo.pasoFinalConContinuaciones");
        }

        paso.setNombre(nombreNormalizado);
        paso.setIndicaFin(indicaFin);
        pasoDAO.actualizar(paso);

    }

    private ProcedimientoPaso validarOrigen(UUID idPasoOrigen, UUID idProcedimiento,
            List<ProcedimientoPaso> pasos) {
        if (idPasoOrigen == null) {
            if (!pasos.isEmpty()) {
                throw new IllegalArgumentException("flujo.inicialYaExiste");
            }
            return null;
        }
        ProcedimientoPaso origen = pasoDAO.buscarPorId(idPasoOrigen);
        if (origen == null || origen.getIdProcedimiento() == null
                || !idProcedimiento.equals(origen.getIdProcedimiento().getIdProcedimiento())) {
            throw new IllegalArgumentException("secuencia.pasoOrigenNoExiste");
        }
        if (Boolean.TRUE.equals(origen.getIndicaFin())) {
            throw new IllegalArgumentException("flujo.pasoFinalNoPuedeContinuar");
        }
        return origen;
    }
}
