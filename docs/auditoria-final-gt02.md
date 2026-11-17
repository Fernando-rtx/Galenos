# Auditoría final GT02 — 17/11/2026

Evaluación técnica conservadora, no calificación oficial del docente. Base frontend `ebfa0c3`, backend `ce23fd3`, ramas `audit/gt02-final-2026`. Remotos actualizados al comenzar; ningún cambio de compañeros sobrescrito. `scripts/academic/` permanece local y sin seguimiento. La rúbrica utilizada es la transcripción proporcionada por el solicitante; no se verificó otra versión del PDF oficial.

## Matriz después de las correcciones

Los puntos parciales son una estimación interna. En HU1–HU4 se reparte provisionalmente 0.5 por historia; la rúbrica entregada no fija ese reparto. NO VERIFICADO recibe cero puntos demostrados, sin afirmar que la implementación no exista.

| Observable | Máximo | Demostrado | Estado | Evidencia / archivo | Riesgo y acción |
|---|---:|---:|---|---|---|
| SPA | 0.5 | 0.5 | CUMPLE | `galenos-application.js`; Chromium HU2/HU3: navegación, contexto, revocación 401 y logout | Mantener prueba de regresión al cambiar navegación |
| PWA instalable | 0.5 | 0 | NO VERIFICADO | Manifest sin errores, SW activo; CDP devuelve `in-incognito` | Instalar desde perfil normal, cerrar y abrir aplicación instalada |
| PWA online/offline | 0.5 | 0.5 | CUMPLE | `service-worker.js` v34; `offline-shell.mjs`, servidor realmente detenido | Primera visita requiere red; mismo origen y puerto |
| Web Components | 1 | 1 | CUMPLE | Custom Elements de portales, bandeja, atención, historial y gestión; Shadow DOM y eventos nativos | Faces heredado no es parte de la SPA evaluada |
| Modularidad | 0.5 | 0.25 | PARCIAL | Boundary/Control/Entity y transporte; análisis estático de 77 módulos sin ciclos detectados | `ConsultaGestionModel` 634 líneas, aplicación 562, atención 509; revisar con equipo sin refactorización masiva |
| Componentes propios | 1 | 1 | CUMPLE | Frontend sin dependencias de ejecución; CSS y elementos nativos | Mocha/BrowserSync son desarrollo; PrimeFaces pertenece al backend heredado |
| Reactividad | 0.5 | 0.5 | CUMPLE | Fetch, AbortController, async/await, eventos; fixtures HU3/HU4 y respuestas reales | No confundir mock de transición con commit JPA real |
| Cobertura unitaria | 0.5 | 0.25 | PARCIAL | 409 JS, 843 Java; identidad, filtros, PDF, conflictos, exámenes y transiciones | 23 archivos JS sin medición V8; pruebas de JPA con Mockito no prueban commit/rollback real |
| Calidad de pruebas | 0.5 | 0.25 | PARCIAL | TDD Mocha, Chai/Sinon, JUnit Jupiter/Mockito; 20 repeticiones sustituidas por 1 | Fixture `componentes.html` obsoleto falla al buscar HTML de dashboard en index; runner Python antiguo no certificable |
| UI atractiva | 0.5 | 0.5 | CUMPLE | Capturas móviles/tablet/escritorio y fixture de notificaciones | Juicio visual sujeto al evaluador; se inspeccionó captura móvil, mensajes legibles |
| CSS propio | 1 | 1 | CUMPLE | `src/resources/estilos`, estilos de componentes; sin CDN visual | Mantener consistencia al modificar estilos embebidos |
| Responsive | 0.5 | 0.5 | CUMPLE | Chromium 390/768/1440 sin desbordamiento en portales; HU4 sintético móvil | Repetir con historiales largos y Safari real |
| HU1–HU4 | 2 | 1.75 | PARCIAL | HU1/HU2/HU3 navegador+REST real; HU4 DOM sintético, detalle y permisos reales | Falta demostrar commit de parcial/avance/retroceso/final con escritura autorizada y múltiples asignaciones reales |
| Accesibilidad | 0.5 | 0.25 | PARCIAL | Enlace de salto activable por teclado, labels, errores ARIA, avisos status/alert, foco visible | Lector de pantalla y contraste exhaustivo pendientes; Tab inicial no quedó demostrado |
| TOTAL | 10 | **8.25** | **PARCIAL** | Evidencia acotada al estado y recorridos descritos | No equivale a una nota oficial ni certifica 100 % |

## Evidencia y límites

- `npm test`: inicial 428, final 409, sin fallos. Se eliminaron 20 casos equivalentes de configuración/Fetch heredados y se agregó uno explícito de DefaultService: reducción neta 19. Ningún archivo de pruebas eliminado. Se conservan los casos por recurso, contratos pendientes, permisos y HU1–HU4.
- `npm run check`: 116 archivos válidos. `npm run test:transport`: 9 casos HTTP sintéticos pasan. `npm run test:coverage`: pasa; V8 mide 54 archivos y deja 23 sin medir. Antes de simplificar: 3638/4875 líneas de archivos cargados (74.63 %), 926/1078 ramas; no es porcentaje de toda la SPA. Ver informe temporal `scope.json` del comando para cifras del último recorrido.
- `mvn clean test` y `mvn package`: BUILD SUCCESS, 843 pruebas, cero errores/fallos/omisiones. Los tests Java usan JUnit Jupiter 6.1.3 según pom, no JUnit 5; se conservó el motor existente. No se actualizó por estética.
- Ambos repositorios se clonaron desde GitHub en `/private/tmp/gt02-audit-clone-{fe,be}`. `npm ci` y BrowserSync del clon funcionan; Maven package del clon pasa con `-Dliberty.install.dir=/private/tmp/gt02-audit-package-liberty`. Se verificaron los commits clonados. El clon frontend recibió después los dos archivos de la corrección para verificar el 401; no confundir esa fase con el commit publicado.
- Liberty temporal puerto 19084 desplegó el WAR actualizado (`CWWKZ0001I`, `CWWKF0011I`). `scripts/gt02_readonly_runtime.py` fuerza JDBC `default_transaction_read_only=on`; no se ejecutaron migraciones ni escrituras clínicas.
- `test/integration/gt02_clinica_readonly.py`: 7 recorridos de empleado, contexto y revocación real; hashes de 20 tablas iguales. Sesiones POST/DELETE son exclusivamente memoria del backend, no registros clínicos.
- Chromium HU2: validación, paciente rechazado como empleado, doble envío único, PersonaRol ajeno 403, sesión revocada 401, logout, desconexión, HU1 con PDF real. No había múltiples asignaciones para esa persona: prueba manual pendiente.
- Chromium HU3: filtros hoy/pasado/futuro, encabezados de sesión/asignación, consulta propia de paciente, rechazo ajeno y desconexión/reintento. El empleado elegido tenía cero consultas asignadas: no se inventaron tarjetas. El probe REST separado del Doctor responsable devuelve 4 consultas el 15–16/11 y cero el 17/11 y en el rango futuro probado.
- Chromium HU4: fixture sintético comprueba formulario, exámenes, guardado único, avance, conflictos y desconexión. GET real ajeno devuelve 403 sin revelar historial. Una ejecución activa sin plantilla fue detectada; no se modificó.
- UUID de consulta académica de Luis: `e5bd6458-4469-5543-98b0-1a5b595559a3`. SELECT confirma ingreso COMPLETADO, evaluación RETROCEDIDO con nota y orden, nuevo ingreso COMPLETADO y evaluación EN_CURSO con nota y una orden. REST del responsable autorizado devuelve tres pasos históricos y el Hemograma completo `dba100db-f74c-4028-95fe-a4654eb0d26b` dentro de RETROCEDIDO, separado de la orden actual. No se volvió a guardar ni transicionar.
- Chromium `offline-shell.mjs` pasa con servidor estático cerrado antes de la primera recarga: arranque, navegación a empleados y red desconectada. Respuesta clínica sintética nunca entra en Cache Storage. El manifiesto es válido, pero la instalación real quedó pendiente por contexto incógnito de automatización.
- Fixtures `hu3_componentes.html`, `hu4_componentes.html` y `notificaciones.html`: PASS. `componentes.html`: FAIL al buscar `[data-dashboard]` en el nuevo index mínimo; no se usó como evidencia positiva. El runner `webcomponents_pwa.py` depende de ese fixture y requiere actualización antes de presentarse como suite vigente.

Los resúmenes sin tokens ni documentos están en [evidencias](evidencias-auditoria-20261117/hu2.json), [HU3](evidencias-auditoria-20261117/hu3.json), [HU4](evidencias-auditoria-20261117/hu4.json) y [lecturas REST](evidencias-auditoria-20261117/lecturas-rest.json). Capturas de empleados sin pacientes: `evidencias-auditoria-20261117/empleados-{390,768,1440}.png`. Logs completos de esta sesión: `/private/tmp/audit-*.log`; no se versionaron respuestas clínicas, credenciales ni tokens.

## Correcciones justificadas

1. Un 401 desde la bandeja revocaba el contexto, pero la suscripción enviaba al portal de pacientes. `galenos-application.js` ahora dirige explícitamente a `#empleados` después de invalidar. HU3 falló antes por timeout esperando ese destino y pasa después. Cache v34 entrega el módulo actualizado.
2. Tres pruebas de navegador aún utilizaban selectOption en el combobox de empleados. Ahora buscan el nombre del documento recibido del backend, escriben y eligen la opción visible; no inyectan una sesión ni asignación ficticia. HU2 también usa `[data-status]`, ya que un error cambia el rol ARIA a alert.
3. Se consolidó validación heredada repetida en el inventario; permanecen las verificaciones de rutas y de rechazo HTTP por recurso. No se cambió transporte de producción.
4. README y revisión de rúbrica antiguos afirmaban que no había HU1/HU2 ni login. Se actualizó esa orientación. La guía local ya no exige una lista de UUID de roles que el backend actual deriva de JPA.

## Riesgos que pueden detener la evaluación

- Un clon no trae `clinica`, credenciales ni los datos de demostración. Un build correcto no demuestra un sistema listo con datos. `scripts/academic/poblar_20261116.sql` es local, contiene INSERT y no se ejecutó ni se publicó. La reproducción funcional requiere que el equipo proporcione por un canal autorizado esquema oficial y un conjunto sintético revisado; esta auditoría no autoriza importarlo.
- Los casos de fechas fijas envejecieron: hoy es 17/11/2026. No hay registros en todos los rangos ni todos los responsables. Debe elegirse un empleado realmente asignado y buscar las fechas del fixture.
- Una ejecución sin plantilla no puede atenderse válidamente. No se rellenó a ciegas ni se relajaron reglas. Revisar su preparación académica antes de seleccionarla en la evaluación.
- `npm audit` informa 4 vulnerabilidades de desarrollo: follow-redirects alta, glob/minimatch/Mocha moderadas. No hay librerías visuales de ejecución añadidas. Revisar actualización compatible en trabajo específico; no aplicar `audit fix --force` sin comprobar herramientas.
- El código académico verifica documento/asignaciones y conserva hashes de tokens efímeros; no debe confundirse con autenticación de producción ni exponerse fuera de loopback. CORS está limitado a los orígenes declarados; credenciales JDBC fuera de Git.
- No se certifica que todo archivo histórico de documentación/pruebas sea vigente. El alcance detallado y los fallos encontrados se declaran aquí, en vez de inferir éxito por el número de unitarias.

## Para reunir evidencia de 10/10

Instalar y abrir la PWA en perfil normal, completar teclado/lector/contraste en HU1–HU4, corregir/retirar fixtures obsoletos sin perder sus comportamientos útiles, acordar datos sintéticos reproducibles, demostrar múltiples asignaciones y consultas de hoy/pasado/futuro, y autorizar un recorrido clínico controlado con SELECT antes/después para parcial, exámenes, avance, retroceso y finalización. Registrar commit, entorno, capturas y respuesta REST de cada observable. Recalificar después; no anticipar puntos pendientes.
