# Seguimiento operativo de consultas

Base de trabajo: `origin/main` en `0afe6f266606ce04da7374a131088cdc9b52140d`.
Rama: `feat/flujo-clinico-operativo`. Este cambio no altera el esquema de `clinica`.

## Estado inicial y cambio

| Función existente | Responsable | REST v2 | Prueba previa | Pendiente y cambio mínimo |
| --- | --- | --- | --- | --- |
| Consultas paginadas por clínica | `ConsultaFlujoService` | `GET consulta` y `consulta/{id}` | `ConsultaGestionResourceTest` | Selección operativa en el frontend |
| Asociación y primer paso | `ConsultaFlujoService.crearProcedimiento` | `POST consulta_procedimiento` | `ConsultaFlujoServiceTest` | Enlazar la ejecución con la plantilla para continuar automáticamente |
| Lectura y edición de pasos | `ConsultaFlujoService.actualizarPaso` | `GET/PUT consulta_procedimiento_paso` | `ConsultaGestionResourceTest` | Serializar y validar cada transición |
| Órdenes y resultados | `ConsultaFlujoService` | `orden_examen`, `examen_resultado` | pruebas REST y de servicio | Vincular una orden al examen concreto si el equipo lo requiere |
| Clínicas autorizadas | `ApiSettings`, filtro REST | `clinica_disponible`, `contexto` | `ApiSettingsTest` | Definir identidad profesional persistente |

`actualizarPaso` ahora bloquea la fila mediante `PESSIMISTIC_WRITE` dentro de la transacción EJB. Permite `PENDIENTE → EN_CURSO → COMPLETADO`, además de conservar el estado pendiente o en curso al editar fechas. Rechaza saltos, retrocesos y cualquier petición posterior a `COMPLETADO` con HTTP 409. Si la finalización no trae `fechaFin`, el servidor la registra. El bloqueo hace que dos finalizaciones concurrentes del mismo paso se evalúen en serie; la segunda ve el estado completado y falla. La verificación de concurrencia contra PostgreSQL sigue pendiente de una base de pruebas aislada.

La clínica se valida en cada lectura y escritura. El filtro REST exige el rol técnico de escritura para PUT. El mismo servicio conserva las validaciones usadas por JSF; su formulario recibe un mensaje de negocio si solicita una transición inválida. Completar un paso no completa la consulta ni crea el sucesor.

## Permisos y decisiones pendientes

| Perfil de trabajo deseado | Lectura de clínica | Escritura clínica | Administración | Asignación personal |
| --- | --- | --- | --- | --- |
| Recepción | Según cuenta y UUID configurados en Liberty | Solo con `galenos-api-write` | No se infiere | No verificable |
| Enfermería | Según cuenta y UUID configurados en Liberty | Solo con `galenos-api-write` | No se infiere | No verificable |
| Médico | Según cuenta y UUID configurados en Liberty | Solo con `galenos-api-write` | No se infiere | No verificable |
| Administrador | Según cuenta y UUID configurados en Liberty | Según rol técnico | `galenos-api-admin` para recursos protegidos | No verificable |

Esta matriz describe las comprobaciones técnicas existentes. Los perfiles de recepción, enfermería y médico son escenarios de interfaz: aún no conceden permisos diferenciados. `PersonaRol` representa una persona y función clínica, pero no está vinculada de forma persistente con la identidad autenticada. El equipo debe acordar y registrar esa relación, quién administra las asignaciones y cómo se revocan. Hasta entonces no se afirma que el actor sea el responsable indicado en el paso. BASIC y el alcance por variables de entorno son mecanismos locales de desarrollo, no una política de seguridad hospitalaria.

`ConsultaProcedimientoPaso` contiene la asociación ejecutada y `PersonaRol`, pero carece de una FK a `ProcedimientoPaso`. `ProcedimientoPasoSecuencia` conecta definiciones, no ejecuciones. Para avanzar automáticamente haría falta una relación persistente ejecución–plantilla, un criterio clínico para ramas alternativas y reglas para datos obligatorios y órdenes pendientes. Propuesta para revisión: agregar `id_procedimiento_paso` a `consulta_procedimiento_paso`, con FK y estrategia de migración para filas antiguas; no se ejecutó SQL. No se deduce el sucesor por UUID, nombre, fecha o rol.

## Comprobación

`mvn -q -DargLine=-javaagent:/home/rodrigo/.m2/repository/net/bytebuddy/byte-buddy-agent/1.17.7/byte-buddy-agent-1.17.7.jar test`: 777 pruebas, 0 fallos, 0 errores, 0 omitidas. `mvn -q -DskipTests package`: WAR generado. Las pruebas añadidas cubren inicio, finalización, saltos, retrocesos, duplicados y uso del bloqueo JPA. No se hicieron escrituras en `clinica` ni pruebas de persistencia real.
