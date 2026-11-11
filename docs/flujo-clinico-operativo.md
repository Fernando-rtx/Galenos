# Seguimiento operativo de consultas

Base inicial: `0afe6f266606ce04da7374a131088cdc9b52140d`. Integrado con `origin/main` en `22ef3cd9e09eec1ca7fd9367d0009ff572526b0b`.
Rama: `feat/flujo-clinico-operativo`. Este cambio no altera el esquema de `clinica`.

## Estado inicial y cambio

| Función existente | Responsable | REST v2 | Prueba previa | Pendiente y cambio mínimo |
| --- | --- | --- | --- | --- |
| Consultas paginadas por clínica | `ConsultaFlujoService` | `GET consulta` y `consulta/{id}` | `ConsultaGestionResourceTest` | Selección operativa en el frontend |
| Asociación y primer paso | `ConsultaFlujoService.crearProcedimiento` | `POST consulta_procedimiento` | `ConsultaFlujoServiceTest` | Enlazar la ejecución con la plantilla para continuar automáticamente |
| Lectura y edición de pasos | `ConsultaFlujoService.actualizarPaso` | `GET/PUT consulta_procedimiento_paso` | `ConsultaGestionResourceTest` | Serializar y validar cada transición |
| Órdenes y resultados | `ConsultaFlujoService` | `orden_examen`, `examen_resultado` | pruebas REST y de servicio | Vincular una orden al examen concreto si el equipo lo requiere |
| Clínicas activas del entorno académico | `ApiSettings`, `ClinicaDAO` | `clinica_disponible`, `contexto` | `ApiSettingsTest` | Definir identidad profesional persistente para un modo autenticado futuro |

`actualizarPaso` ahora bloquea la fila mediante `PESSIMISTIC_WRITE` dentro de la transacción EJB. Permite `PENDIENTE → EN_CURSO → COMPLETADO`, además de conservar el estado pendiente o en curso al editar fechas. Rechaza saltos, retrocesos y cualquier petición posterior a `COMPLETADO` con HTTP 409. Si la finalización no trae `fechaFin`, el servidor la registra. El bloqueo hace que dos finalizaciones concurrentes del mismo paso se evalúen en serie; la segunda ve el estado completado y falla. La verificación de concurrencia contra PostgreSQL sigue pendiente de una base de pruebas aislada.

La pertenencia de consulta y paso a la clínica indicada se valida en las lecturas y escrituras. El `main` académico actual usa datos ficticios locales en loopback y no autentica ni autoriza por usuario; seleccionar una clínica no representa un permiso personal. El mismo servicio conserva las validaciones usadas por JSF; su formulario recibe un mensaje de negocio si solicita una transición inválida. Completar un paso no completa la consulta ni crea el sucesor.

## Permisos y decisiones pendientes

| Perfil de trabajo deseado | Lectura | Escritura | Administración | Asignación personal |
| --- | --- | --- | --- | --- |
| Recepción | Disponible en modo académico local | Disponible en modo académico local | Sin distinción técnica | No verificable |
| Enfermería | Disponible en modo académico local | Disponible en modo académico local | Sin distinción técnica | No verificable |
| Médico | Disponible en modo académico local | Disponible en modo académico local | Sin distinción técnica | No verificable |
| Administrador | Disponible en modo académico local | Disponible en modo académico local | Sin distinción técnica | No verificable |

Esta matriz refleja el despliegue académico sin login decidido en `main`; los perfiles son escenarios de interfaz y no privilegios. `PersonaRol` representa una persona y función clínica, pero no está vinculada a una cuenta autenticada. Antes de usar datos reales o exponer el servicio fuera de loopback, el equipo tendría que definir autenticación, vínculo cuenta–`PersonaRol`, alcance por clínica y permisos de lectura, escritura y administración. Esta rama no presenta el modo académico como seguridad clínica de producción.

`ConsultaProcedimientoPaso` contiene la asociación ejecutada y `PersonaRol`, pero carece de una FK a `ProcedimientoPaso`. `ProcedimientoPasoSecuencia` conecta definiciones, no ejecuciones. Para avanzar automáticamente haría falta una relación persistente ejecución–plantilla, un criterio clínico para ramas alternativas y reglas para datos obligatorios y órdenes pendientes. Propuesta para revisión: agregar `id_procedimiento_paso` a `consulta_procedimiento_paso`, con FK y estrategia de migración para filas antiguas; no se ejecutó SQL. No se deduce el sucesor por UUID, nombre, fecha o rol.

## Comprobación

`mvn -q -DargLine=-javaagent:/home/rodrigo/.m2/repository/net/bytebuddy/byte-buddy-agent/1.17.7/byte-buddy-agent-1.17.7.jar test`: 776 pruebas, 0 fallos, 0 errores, 0 omitidas. `mvn -q -DskipTests package`: WAR generado. Las pruebas añadidas cubren inicio, finalización, saltos, retrocesos, duplicados y uso del bloqueo JPA. No se hicieron escrituras en `clinica` ni pruebas de persistencia real.
