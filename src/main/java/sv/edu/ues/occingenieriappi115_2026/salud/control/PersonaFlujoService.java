package sv.edu.ues.occingenieriappi115_2026.salud.control;

import jakarta.ejb.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import sv.edu.ues.occingenieriappi115_2026.salud.entity.*;
import static sv.edu.ues.occingenieriappi115_2026.salud.control.ReglaNegocioException.Motivo.*;

/** Registro de personas y sus documentos, contactos y asignaciones: reglas compartidas. */
@Stateless @LocalBean @TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PersonaFlujoService {
 public static final ZoneId ZONA_CLINICA=ZoneId.of("America/El_Salvador");
 private static final Pattern NOMBRE=Pattern.compile("^[\\p{L}][\\p{L}\\p{M}' -]*$");
 @EJB private PersonaDAO personaDAO;
 @EJB private DocumentoDAO documentoDAO;
 @EJB private MedioContactoDAO contactoDAO;
 @EJB private PersonaRolDAO asignacionDAO;
 @EJB private TipoDocumentoDAO documentoTipoDAO;
 @EJB private TipoMedioContactoDAO contactoTipoDAO;
 @EJB private ClinicaDAO clinicaDAO;
 @EJB private RolDAO rolDAO;
 @EJB private ConsultaDAO consultaDAO;
 @EJB private ConsultaProcedimientoPasoDAO pasoDAO;
 public PersonaFlujoService(){}
 public PersonaFlujoService(PersonaDAO personas,DocumentoDAO documentos){personaDAO=personas;documentoDAO=documentos;}
 public PersonaFlujoService(DocumentoDAO documentos,PersonaDAO personas,TipoDocumentoDAO tipos){documentoDAO=documentos;personaDAO=personas;documentoTipoDAO=tipos;}
 public PersonaFlujoService(MedioContactoDAO contactos,PersonaDAO personas,TipoMedioContactoDAO tipos){contactoDAO=contactos;personaDAO=personas;contactoTipoDAO=tipos;}
 public PersonaFlujoService(PersonaRolDAO asignaciones,PersonaDAO personas,RolDAO roles,ClinicaDAO clinicas){asignacionDAO=asignaciones;personaDAO=personas;rolDAO=roles;clinicaDAO=clinicas;}
 private static String trim(String value){return value==null?null:value.trim();}
 private static ReglaNegocioException invalid(String field,String key){return new ReglaNegocioException(VALIDACION,field,key);}
 public static void validarNombre(String value,String field){
  String text=value==null?"":value.trim();String prefix="persona."+("apellidos".equals(field)?"apellidos":"nombres");
  if(text.isEmpty())throw invalid(field,prefix+"Requeridos");
  if(text.length()<2)throw invalid(field,prefix+"Minimo");
  if(text.length()>255)throw invalid(field,prefix+"Maximo");
  if(!NOMBRE.matcher(text).matches())throw invalid(field,prefix+"Formato");
 }
 public static void validarNacimiento(Date value){
  if(value==null)throw invalid("fechaNacimiento","persona.fechaNacimientoRequerida");
  LocalDate birth=Instant.ofEpochMilli(value.getTime()).atZone(ZONA_CLINICA).toLocalDate();
  if(birth.isBefore(LocalDate.of(1900,1,1)))throw invalid("fechaNacimiento","persona.fechaNacimientoMinima");
  if(birth.isAfter(LocalDate.now(ZONA_CLINICA)))throw invalid("fechaNacimiento","persona.fechaNacimientoFutura");
 }
 public static boolean esDui(TipoDocumento tipo){return tipo!=null&&tipo.getNombre()!=null&&"DUI".equalsIgnoreCase(tipo.getNombre().strip());}
 public static String errorFechaParaDui(Date date,LocalDate hoy){
  if(date==null)return "documento.duiFechaNacimientoInvalida";
  LocalDate birth=Instant.ofEpochMilli(date.getTime()).atZone(ZONA_CLINICA).toLocalDate();
  if(birth.isBefore(LocalDate.of(1900,1,1))||birth.isAfter(hoy))return "documento.duiFechaNacimientoInvalida";
  return Period.between(birth,hoy).getYears()<18?"documento.duiMenorEdad":null;
 }
 public Persona guardarPersona(Persona value,boolean edicion){
  value.setNombres(trim(value.getNombres()));value.setApellidos(trim(value.getApellidos()));
  validarNombre(value.getNombres(),"nombres");validarNombre(value.getApellidos(),"apellidos");validarNacimiento(value.getFechaNacimiento());
  if(edicion&&value.getIdPersona()!=null&&errorFechaParaDui(value.getFechaNacimiento(),LocalDate.now(ZONA_CLINICA))!=null){
   if(documentoDAO.poseeDui(value.getIdPersona()))
    throw invalid("fechaNacimiento","persona.fechaNacimientoIncompatibleDui");
  }
  return edicion?personaDAO.actualizar(value):personaDAO.guardar(value);
 }
 public Documento guardarDocumento(Documento value,boolean edicion,UUID personaOriginal,UUID tipoOriginal){
  UUID persona=value.getIdPersona()==null?null:value.getIdPersona().getIdPersona();
  UUID tipo=value.getIdTipoDocumento()==null?null:value.getIdTipoDocumento().getIdTipoDocumento();
  if(edicion&&!Objects.equals(personaOriginal,persona))throw invalid("persona","relaciones.noEditables");
  if(edicion&&!Objects.equals(tipoOriginal,tipo))throw invalid("tipo","documento.tipoNoEditable");
  if(persona==null)throw invalid(null,"documento.personaRequerida");
  TipoDocumento persisted=tipo==null?null:documentoTipoDAO.buscarPorId(tipo);
  if(persisted==null)throw invalid("tipo","documento.tipoRequerido");
  if(!edicion&&!Boolean.TRUE.equals(persisted.getActivo()))throw invalid("tipo","documento.tipoInactivo");
  value.setIdTipoDocumento(persisted);
  if(esDui(persisted)){
   Persona p=personaDAO.buscarPorId(persona);String error=errorFechaParaDui(p==null?null:p.getFechaNacimiento(),LocalDate.now(ZONA_CLINICA));
   if(error!=null)throw invalid("tipo",error);
  }
  value.setValor(trim(value.getValor()));value.setRutaFisica(trim(value.getRutaFisica()));
  if(value.getValor()==null||value.getValor().isBlank())throw invalid("valor","documento.valorRequerido");
  regex(value.getValor(),persisted.getExpresionRegular(),false,"valor","documento.valorFormato","documento.valorFormato");
  UUID exclude=edicion?value.getIdDocumento():null;
  if(documentoDAO.existeTipoParaPersona(persona,tipo,exclude))throw new ReglaNegocioException(DUPLICADO,"tipo","documento.tipoDuplicadoPersona");
  if(documentoDAO.existeDocumento(tipo,value.getValor(),exclude))throw new ReglaNegocioException(DUPLICADO,"valor","documento.duplicadoGlobal");
  return edicion?documentoDAO.actualizar(value):documentoDAO.guardar(value);
 }
 public MedioContacto guardarContacto(MedioContacto value,boolean edicion,UUID personaOriginal,TipoMedioContacto tipoOriginal){
  UUID persona=value.getIdPersona()==null?null:value.getIdPersona().getIdPersona();
  if(edicion&&(personaOriginal==null||!Objects.equals(personaOriginal,persona)))throw invalid("persona","relaciones.noEditables");
  if(persona==null)throw invalid(null,"medioContacto.personaRequerida");
  UUID tipo=value.getIdTipoMedioContacto()==null?null:value.getIdTipoMedioContacto().getIdTipoMedioContacto();
  if(value.getIdTipoMedioContacto()==null)throw invalid("contactoTipo","medioContacto.tipoRequerido");
  if(edicion){
   MedioContacto old=value.getIdMedioContacto()==null?null:contactoDAO.buscarPorId(value.getIdMedioContacto());
   UUID oldId=old==null||old.getIdTipoMedioContacto()==null?null:old.getIdTipoMedioContacto().getIdTipoMedioContacto();
   if(oldId==null||!Objects.equals(tipo,oldId)||tipoOriginal==null||!Objects.equals(oldId,tipoOriginal.getIdTipoMedioContacto())){
    if(old!=null)value.setIdTipoMedioContacto(tipoOriginal);
    throw invalid("contactoTipo","medioContacto.tipoNoEditable");
   }
  }
  TipoMedioContacto persisted=tipo==null?null:contactoTipoDAO.buscarPorId(tipo);
  if(persisted==null||(!edicion&&!Boolean.TRUE.equals(persisted.getActivo())))throw invalid("contactoTipo","medioContacto.tipoInactivo");
  value.setValor(trim(value.getValor()));
  if(value.getValor()==null||value.getValor().isBlank())throw invalid("contactoValor","medioContacto.valorRequerido");
  value.setIdTipoMedioContacto(persisted);
  regex(value.getValor(),persisted.getExpresionRegular(),true,"contactoValor","medioContacto.regexInvalida","medioContacto.valorFormato");
  if(contactoDAO.existeContacto(persona,tipo,value.getValor(),edicion?value.getIdMedioContacto():null))throw new ReglaNegocioException(DUPLICADO,null,"medioContacto.duplicado");
  return edicion?contactoDAO.actualizar(value):contactoDAO.guardar(value);
 }
 private static void regex(String value,String expression,boolean posix,String field,String malformed,String mismatch){
  if(expression==null||expression.isBlank())return;
  Pattern pattern;
  try{pattern=Pattern.compile(posix?expression.replace("[:space:]","\\s"):expression);}
  catch(PatternSyntaxException cause){throw new ReglaNegocioException(VALIDACION,field,malformed,cause);}
  if(!pattern.matcher(value).matches())throw invalid(field,mismatch);
 }
 public PersonaRol guardarAsignacion(PersonaRol value,boolean edicion,UUID personaOriginal,UUID clinicaOriginal,UUID rolOriginal){
  UUID persona=value.getIdPersona()==null?null:value.getIdPersona().getIdPersona();
  if(edicion&&(personaOriginal==null||!Objects.equals(personaOriginal,persona)))throw invalid("persona","relaciones.noEditables");
  Persona p=persona==null?null:personaDAO.buscarPorId(persona);if(p==null)throw invalid("persona","personaRol.personaRequerida");
  UUID rol=value.getIdRol()==null?null:value.getIdRol().getIdRol();UUID clinic=value.getIdClinica()==null?null:value.getIdClinica().getIdClinica();
  if(rol==null)throw invalid("rol","personaRol.rolRequerido");if(clinic==null)throw invalid("clinica","personaRol.clinicaRequerida");
  if(edicion){
   PersonaRol old=value.getIdPersonaRol()==null?null:asignacionDAO.buscarPorId(value.getIdPersonaRol());
   if(old==null||old.getIdRol()==null||old.getIdClinica()==null||!Objects.equals(rolOriginal,old.getIdRol().getIdRol())
     ||!Objects.equals(clinicaOriginal,old.getIdClinica().getIdClinica())||!Objects.equals(rol,rolOriginal)||!Objects.equals(clinic,clinicaOriginal))
    throw invalid("rol","personaRol.relacionNoEditable");
  }
  Rol r=rolDAO.buscarPorId(rol);if(r==null)throw invalid("rol","personaRol.rolRequerido");
  if(!edicion&&!Boolean.TRUE.equals(r.getActivo()))throw invalid("rol","personaRol.rolInactivo");
  Clinica c=clinicaDAO.buscarPorId(clinic);if(c==null)throw invalid("clinica","personaRol.clinicaNoExiste");
  if(!edicion&&!Boolean.TRUE.equals(c.getActivo()))throw invalid("clinica","personaRol.clinicaInactiva");
  if(!edicion&&asignacionDAO.existeAsignacion(persona,clinic,rol))throw new ReglaNegocioException(DUPLICADO,null,"personaRol.duplicada");
  value.setIdPersona(p);value.setIdRol(r);value.setIdClinica(c);
  return edicion?asignacionDAO.actualizar(value):asignacionDAO.guardar(value);
 }

 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public Persona buscarPersona(UUID id){return personaDAO.buscarPorId(id);}
 private static List<FiltroDAO> filtroPersonas(String nombres,String apellidos){
  List<FiltroDAO> filtros=new ArrayList<>();
  if(nombres!=null&&!nombres.isBlank())filtros.add(new FiltroDAO("nombres",OperadorFiltro.CONTIENE,nombres.trim()));
  if(apellidos!=null&&!apellidos.isBlank())filtros.add(new FiltroDAO("apellidos",OperadorFiltro.CONTIENE,apellidos.trim()));
  return filtros;
 }
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public List<Persona> listarPersonas(int first,int max,String nombres,String apellidos){return personaDAO.obtenerPagina(first,max,filtroPersonas(nombres,apellidos),List.of(new OrdenDAO("idPersona",DireccionOrden.ASCENDENTE)));}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public long contarPersonas(String nombres,String apellidos){return personaDAO.contar(filtroPersonas(nombres,apellidos));}
 private void personaRequerida(UUID id){if(personaDAO.buscarPorId(id)==null)throw new ReglaNegocioException(AUSENTE,null,"personaRol.personaRequerida");}
 private static List<FiltroDAO> porPersona(UUID id){return List.of(new FiltroDAO("idPersona.idPersona",OperadorFiltro.IGUAL,id));}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public List<Documento> documentos(UUID persona,int first,int max){personaRequerida(persona);return documentoDAO.obtenerPagina(first,max,porPersona(persona),List.of(new OrdenDAO("idDocumento",DireccionOrden.ASCENDENTE)));}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public long contarDocumentos(UUID persona){personaRequerida(persona);return documentoDAO.contar(porPersona(persona));}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public Documento buscarDocumento(UUID id){return documentoDAO.buscarPorId(id);}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public List<MedioContacto> contactos(UUID persona,int first,int max){personaRequerida(persona);return contactoDAO.obtenerPagina(first,max,porPersona(persona),List.of(new OrdenDAO("idMedioContacto",DireccionOrden.ASCENDENTE)));}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public long contarContactos(UUID persona){personaRequerida(persona);return contactoDAO.contar(porPersona(persona));}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public MedioContacto buscarContacto(UUID id){return contactoDAO.buscarPorId(id);}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public List<PersonaRol> asignaciones(UUID persona,int first,int max){personaRequerida(persona);return asignacionDAO.obtenerPagina(first,max,porPersona(persona),List.of(new OrdenDAO("idPersonaRol",DireccionOrden.ASCENDENTE)));}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public long contarAsignaciones(UUID persona){personaRequerida(persona);return asignacionDAO.contar(porPersona(persona));}
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 public PersonaRol buscarAsignacion(UUID id){return asignacionDAO.buscarPorId(id);}
 public void eliminarDocumento(UUID id){if(!documentoDAO.eliminar(id))throw new ReglaNegocioException(AUSENTE,null,"documento.errorEliminar");}
 public void eliminarContacto(UUID id){if(!contactoDAO.eliminar(id))throw new ReglaNegocioException(AUSENTE,null,"medioContacto.errorEliminar");}
 public void eliminarAsignacion(UUID id){
  List<FiltroDAO> filtros=List.of(new FiltroDAO("idPersonaRol.idPersonaRol",OperadorFiltro.IGUAL,id));
  if(consultaDAO.contar(filtros)>0||pasoDAO.contar(filtros)>0)throw new ReglaNegocioException(REFERENCIADO,null,"personaRol.errorEliminarEnUso");
  if(!asignacionDAO.eliminar(id))throw new ReglaNegocioException(AUSENTE,null,"personaRol.errorEliminar");
 }
}
