# Contratos de integración del parcial — Fernando

Rama: `feature/fernando-parcial`, basada en `origin/main` / `3004b20`.

Estos métodos se añadieron a los DAO existentes para que Consulta consuma una
única interpretación del dominio. No modifican tablas, entidades ni las
pantallas de Persona o de plantillas de Procedimiento.

## PersonaRol — revisión con Itzep

- `buscarPacientesPorClinica(UUID idClinica, String criterio, int limite)`:
  consulta en BD por nombre o DUI; devuelve solo PersonaRol de la clínica activa
  indicada, con persona existente y rol activo cuyo nombre normalizado sea
  Paciente. Devuelve vacío sin clínica o criterio. No usa UUID fijos de datos demo.
- `buscarResponsablesPorClinicaYRol(UUID idClinica, UUID idRol)`:
  devuelve personas de esa clínica activa con el rol exacto activo, ordenadas
  por UUID para una asignación reproducible. Recepción y Enfermera son válidos
  cuando coinciden con el rol requerido; no se infiere que todo paso sea médico.

Las escrituras revalidan los registros persistidos. Ocultar una opción en el
buscador no sustituye esa defensa.

## Paso inicial — revisión con Rodrigo

- `ProcedimientoPasoDAO.obtenerPasoInicial(UUID idProcedimiento)` consulta los
  pasos y secuencias del procedimiento y devuelve la única raíz de un grafo
  acíclico conectado.
- Se conserva la dirección del Model actual: `idProcedimientoPaso` es origen y
  `idProcedimientoPasoReferencia` es siguiente. Los tipos de secuencia no
  convierten esa relación en una FK nueva ni invierten su dirección.
- Cero pasos, varias raíces, ciclos y referencias fuera del conjunto de pasos
  se rechazan con `consultaFlujo.plantillaInvalida`.
- Consulta exige además que la raíz pertenezca al procedimiento seleccionado
  y tenga un rol existente y activo.

No existe una columna `inicial`; no se escoge el primer UUID ni el primer
registro de una lista como sustituto de la raíz.

## Clínica actual y transacción — Fernando

- `ClinicaActualBean` es CDI de sesión, guarda UUID y revisión del contexto.
  Cambiar clínica recarga la vista. Cambios en otra pestaña invalidan los
  formularios abiertos al guardar, incluso si se regresa a la clínica original.
- `ConsultaFlujoService` es EJB `@Stateless` con transacción `REQUIRED`.
  `crearProcedimiento` valida antes de escribir y guarda asociación y paso
  en la misma transacción. La excepción de negocio tiene `rollback=true`.
- El paso inicial tiene estado PENDIENTE, fechaInicio y el PersonaRol responsable;
  fechaFin queda nula. No se almacena una referencia ficticia a la plantilla.
- Actualizar una asociación no crea otro paso. Paciente, procedimiento,
  asociación del paso y responsable se conservan tanto en UI como en servidor.
- El alta manual de pasos no permite eludir la creación automática. La ejecución
  real y el avance de pasos siguen fuera de este parcial.

## Validación de equipo

Revisar los contratos con sus dueños antes de integrar ramas. La revisión
cruzada y el simulacro 1 → 10 requieren a los tres integrantes; las pruebas con
mocks no sustituyen un ensayo real ni demuestran rollback en PostgreSQL.
