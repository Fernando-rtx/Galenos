# GalenoSV GT02: HU1–HU4, perfil académico

## Ámbito y seguridad

En el perfil académico normal, el frontend comparte la sesión efímera del portal: el paciente usa su token `PACIENTE` y el empleado su token `EMPLEADO` junto con `X-Persona-Rol`. El backend deriva la persona de la sesión; los UUID enviados por el navegador no conceden acceso por sí solos. Paciente, clínica, rol y asignación se revalidan en el servidor. `GALENOS_EMPLEADO_ROLES` debe contener los UUID de roles de personal permitidos, nunca el rol Paciente.

El perfil sintético sigue disponible con `GALENOS_CRUD_DEMO=true`, Basic Auth y clínicas/personas limitadas por `GALENOS_CRUD_ACTOR_CLINICS` y `GALENOS_CRUD_ACTOR_PERSONS`. Solo ese perfil usa identidades CRUD sintéticas; requiere la base `galenos_demo_crud` y no debe apuntar a `clinica`.

## Endpoints implementados

- `GET /v2/paciente/clinicas`: devuelve las clínicas de la sesión de paciente (o la identidad sintética del perfil demo).
- `GET /v2/paciente/citas`: filtra la identidad autenticada por clínica, rango de fecha y paginación; devuelve `Total-registros` real.
- `GET /v2/paciente/citas/{id}/ticket`: devuelve PDF y comprueba que la cita pertenezca al paciente autenticado y a la clínica seleccionada.
- `GET /v2/empleado/identidad`: devuelve el contexto del empleado autenticado y sus asignaciones habilitadas.
- `GET /v2/empleado/consultas`: bandeja del empleado autenticado y su asignación/rol; al omitir fechas usa el día en `America/El_Salvador`.
- `GET /v2/atencion/consulta/{id}`: carga contexto de atención solo cuando el paso activo está asignado al empleado/rol/clínica.
- `POST /v2/atencion/registrar`: guarda texto/exámenes y, en la misma transacción, conserva historial y aplica transición validada del grafo.

Los DTO REST no incluyen entidades JPA. Las respuestas clínicas usan `Cache-Control: no-store` mediante `CorsFilter`; el service worker nunca guarda solicitudes REST, respuestas autorizadas ni información clínica.

## Modelo y migración

El esquema SQL académico versionado ya declara `consulta_procedimiento_paso.id_procedimiento_paso`, `consulta_procedimiento_paso.valor` y `orden_examen.id_examen` con claves foráneas. La auditoría encontró el desfase en las entidades JPA, no en el snapshot del esquema. Las entidades ya mapean las columnas. `db/migrations/V2026_11_12_01__consulta_paso_atencion.sql` es idempotente y aditiva para instalaciones cuyo esquema físico aún no tenga esas columnas; conserva nulos y no inventa historial. El preparador actualiza solo una base sintética que tenga el digest anterior conocido, usando las relaciones explícitas del fixture. **No se ejecutó** en la base compartida `clinica` ni en la base de servidores del usuario.

## Regla de asignación conocida

La ejecución actual se determina por los estados `PENDIENTE`/`EN_CURSO`, la persona asignada (`PersonaRol`), la clínica y el rol exigido por `ProcedimientoPaso`. Se rechaza la transición si hay más de un paso activo o si el paso enviado no es el actual. El modelo no contiene política de distribución/cupo para varios empleados del mismo rol. En transiciones, el servicio asigna al primer `PersonaRol` elegible por UUID en esa clínica y rol. Esto vuelve la demo reproducible y comprobable, pero no es una regla permanente de producción: el curso/equipo debe acordar si la asignación es explícita, compartida o por turno.

## Demo local

1. Configura Liberty con PostgreSQL `clinica`, `GALENOS_API_MODE=academic` y los UUID autorizados en `GALENOS_EMPLEADO_ROLES`.
2. Inicia Liberty con `mvn liberty:dev` y el frontend con `npm run dev`.
3. Identifícate en el portal `#pacientes` o `#empleados`. Luego abre `#academico`; HU1 y HU3 usan la sesión activa del portal correspondiente.
4. El perfil sintético aislado sigue disponible para ensayos de HU CRUD y no comparte su base ni sus credenciales con `clinica`.

## Verificación

- Backend: Java 21, `mvn -o test` (en este entorno 777 pruebas pasan con el agente Mockito local).
- Frontend: `npm test`, `npm run check`, `npm run test:transport`.
- El arranque integrado con PostgreSQL aislado requiere Docker activo. En esta ejecución Docker no estaba disponible, así que las pruebas REST reales y navegador contra el backend quedan pendientes de un equipo con ese servicio.
