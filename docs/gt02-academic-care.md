# GalenoSV GT02: HU1–HU4, perfil académico

## Ámbito y seguridad

Los endpoints de este flujo solo funcionan con `GALENOS_CRUD_DEMO=true`, Basic Auth del perfil CRUD sintético y clínicas permitidas por `GALENOS_CRUD_ACTOR_CLINICS`. El empleado se vincula al principal técnico con `GALENOS_CRUD_ACTOR_PERSONS`; el documento presentado debe coincidir con la persona de ese principal. Este mecanismo es para la demostración académica, no sustituye el inicio de sesión de una instalación real. El script de preparación de frontend genera una configuración local ignorada por Git; no copiar credenciales a archivos versionados.

## Endpoints implementados

- `GET /v2/paciente/clinicas`: verifica un documento sintético y devuelve clínicas de paciente dentro del alcance demo.
- `GET /v2/paciente/citas`: filtra por identidad, clínica, rango de fecha y paginación; devuelve `Total-registros` real.
- `GET /v2/paciente/citas/{id}/ticket`: devuelve PDF, validando la identidad, clínica y cita.
- `GET /v2/empleado/identidad`: valida documento contra la persona asociada al principal de demostración.
- `GET /v2/empleado/consultas`: bandeja de la persona, clínica, rol, paso activo y fechas; al omitir fechas usa el día en `America/El_Salvador`.
- `GET /v2/atencion/consulta/{id}`: carga contexto de atención solo cuando el paso activo está asignado al empleado/rol/clínica.
- `POST /v2/atencion/registrar`: guarda texto/exámenes y, en la misma transacción, conserva historial y aplica transición validada del grafo.

Los DTO REST no incluyen entidades JPA. Las respuestas clínicas usan `Cache-Control: no-store` mediante `CorsFilter`; el service worker nunca guarda solicitudes REST, respuestas autorizadas ni información clínica.

## Modelo y migración

El esquema SQL académico versionado ya declara `consulta_procedimiento_paso.id_procedimiento_paso`, `consulta_procedimiento_paso.valor` y `orden_examen.id_examen` con claves foráneas. La auditoría encontró el desfase en las entidades JPA, no en el snapshot del esquema. Las entidades ya mapean las columnas. `db/migrations/V2026_11_12_01__consulta_paso_atencion.sql` es idempotente y aditiva para instalaciones cuyo esquema físico aún no tenga esas columnas; conserva nulos y no inventa historial. El preparador actualiza solo una base sintética que tenga el digest anterior conocido, usando las relaciones explícitas del fixture. **No se ejecutó** en la base compartida `clinica` ni en la base de servidores del usuario.

## Regla de asignación conocida

La ejecución actual se determina por los estados `PENDIENTE`/`EN_CURSO`, la persona asignada (`PersonaRol`), la clínica y el rol exigido por `ProcedimientoPaso`. Se rechaza la transición si hay más de un paso activo o si el paso enviado no es el actual. El modelo no contiene política de distribución/cupo para varios empleados del mismo rol. En transiciones, el servicio asigna al primer `PersonaRol` elegible por UUID en esa clínica y rol. Esto vuelve la demo reproducible y comprobable, pero no es una regla permanente de producción: el curso/equipo debe acordar si la asignación es explícita, compartida o por turno.

## Demo local

1. Inicia primero el perfil académico aislado descrito por `scripts/crud_demo.py`; solo usa el contenedor/base sintética identificada por sus etiquetas y digest.
2. Desde GalenoFE, ejecuta `python3 scripts/configure-demo.py [ruta-a-Galenos]`. El archivo `src/js/control/environment.local.js` se crea con permisos privados y está ignorado por Git.
3. Inicia `npm run dev`; abre el origen admitido por CORS (normalmente `http://localhost:3000`).
4. Prueba paciente sintético `ABC`; empleado sintético `DEF` con cuenta `editor-demo` del perfil demo. No expongas credenciales en capturas, commits o documentación.

## Verificación

- Backend: Java 21, `mvn -o test` (en este entorno 777 pruebas pasan con el agente Mockito local).
- Frontend: `npm test`, `npm run check`, `npm run test:transport`.
- El arranque integrado con PostgreSQL aislado requiere Docker activo. En esta ejecución Docker no estaba disponible, así que las pruebas REST reales y navegador contra el backend quedan pendientes de un equipo con ese servicio.
