# Auditoría final GT02 — revisión de correcciones, 17/11/2026

**Antes: 8.25/10. Después: 9.25/10 conservadores.** Estimación técnica, no calificación del docente ni certificación de 10/10. Se conserva la auditoría inicial en el commit backend `582b3a2` y sus evidencias originales; los nuevos archivos `revision-*` distinguen esta comprobación de la anterior.

Se leyó el PDF oficial local `definicionSegundoParcialPPI115_gt02_2026.pdf`: confirma los 14 observables, las historias y la revisión secuencial al clonar el commit entregado. Exige pruebas significativas, sin fijar un porcentaje numérico V8. Se conservan ECMAScript nativo, Web Components, Boundary/Control/Entity, Services y transporte común; backend REST/Services/DAO/JPA sin cambios funcionales.

Ramas: `audit/gt02-final-2026` en ambos repositorios. Bases publicadas FE `ebfa0c3`, BE `ce23fd3`; auditoría anterior FE `7f7df6d`, `42590fa`, BE `582b3a2`. Nuevos commits FE `6be1edc`, `2aae94e`, `a773144`, `5859e14`, `dca5798`; frontend final `7c48cc5` (código validado `dca5798`); consultar `git log` para el commit final de documentación del backend. No hubo merge/push, reset, migraciones ni escrituras clínicas. `scripts/academic/` sigue sin seguimiento.

## Matriz de los 14 observables

CUMPLE significa que existe evidencia del observable en los recorridos descritos, no que se ejecutó cada combinación imaginable. PWA instalable incorpora **verificación manual comunicada por el usuario**: informó haber instalado y abierto GalenosSV. No se presenta como instalación realizada por Codex. En HU1–HU4 se estima 0.5 por historia solo para distribuir los dos puntos; el PDF no prescribe ese reparto.

| Observable | Máximo | Demostrado | Estado | Evidencia actual | Pendiente o límite |
|---|---:|---:|---|---|---|
| SPA | 0.5 | 0.5 | CUMPLE | Chromium: navegación, Atrás, logout, 401, limpieza y cancelación | Repetir Safari instalado en commit final |
| PWA instalable | 0.5 | 0.5 | CUMPLE, informado por usuario | Instalación/apertura manual comunicada; manifest/íconos/SW y prompt sintético comprobados | Adjuntar captura, navegador, versión, URL y commit; abrir instalada con v37 |
| PWA online/offline | 0.5 | 0.5 | CUMPLE | Cold install, control, servidor detenido y recarga; shell v37; REST fuera de caché | La primera visita requiere red; mantener origen/puerto |
| Web Components | 1 | 1 | CUMPLE | Portales, bandeja, atención, historial y gestión en Custom Elements, eventos y Shadow DOM | Faces heredado se distingue de la SPA |
| Modularidad | 0.5 | 0.5 | CUMPLE | Plantilla y composición separadas; relaciones de gestión en método propio; capas conservadas, sin ciclos detectados | No dividir contratos declarativos o componentes solo por longitud |
| Componentes propios | 1 | 1 | CUMPLE | CSS/HTML/ECMAScript propios; sin dependencias de ejecución frontend | Herramientas de prueba y desarrollo se informan aparte |
| Reactividad | 0.5 | 0.5 | CUMPLE | Fetch común, async/await, cancelación, doble envío, errores y actualización sin recarga | Persistencia real se distingue de mocks |
| Cobertura unitaria | 0.5 | 0.25 | PARCIAL | 415 JS y 843 Java; seis pruebas nuevas para cinco módulos de lógica antes sin medir | 19 módulos de presentación/arranque fuera de V8 unitario; no atribuirles cobertura unitaria por medirlos en Chromium |
| Calidad de pruebas | 0.5 | 0.5 | CUMPLE | Mocha TDD/Chai/Sinon; fixtures antiguos corregidos; seis fixtures de navegador pasan | Unitarias no certifican PostgreSQL; mantener separación |
| UI atractiva | 0.5 | 0.5 | CUMPLE | Inspección de captura móvil, ambos temas, carga/éxito/error y cierre de avisos | Valoración visual del evaluador |
| CSS propio | 1 | 1 | CUMPLE | Estilos locales; corrección mínima de bordes sin cambiar distribución | Contraste medido en pares declarados, no certificación de todo píxel |
| Responsive | 0.5 | 0.5 | CUMPLE | 390/768/1440, ambos portales/temas, atención sintética móvil | Historiales extremos y Safari final manual |
| HU1–HU4 | 2 | 1.75 | PARCIAL | HU1/HU2/HU3 con REST real; HU4 sintético, permisos y lectura histórica reales | Guardado/transiciones con escritura PostgreSQL y múltiples asignaciones pendientes de autorización/datos |
| Accesibilidad | 0.5 | 0.25 | PARCIAL | Combobox, etiquetas/errores, modal y foco, avisos ARIA; texto >=4.5:1 y bordes >=3:1 | VoiceOver y recorrido completo por teclado de todos los estados pendientes |
| TOTAL | 10 | **9.25** | **PARCIAL** | Automático + REST real + instalación comunicada por usuario | No afirmar 10/10 |

El aumento de 1 punto proviene de instalación comunicada (+0.5), modularidad revisada/corregida (+0.25) y pruebas obsoletas reparadas (+0.25). Las mejoras de cobertura y accesibilidad se reconocen con evidencia, sin otorgar automáticamente sus puntos pendientes.

## Defectos corregidos y simplificaciones

1. **Fixture `componentes.html`:** buscaba `[data-dashboard]` en `index.html`, que ahora contiene solo el componente raíz. Usa el marcado real de la SPA, sin copiar un HTML desactualizado. `application-markup.js` contiene únicamente presentación estática; `galenos-application.js` conserva montaje, navegación y composición. La extracción reduce unas 289 líneas de plantilla en la clase y permite que prueba y aplicación compartan estructura.
2. **Fixture `page_components.html`:** los componentes de gestión necesitan el contenedor de la aplicación para acceder a tarjetas y detalle. El fixture ahora los monta dentro de ese contenedor; conserva las comprobaciones de acciones y suscripciones.
3. **Defecto real de ciclo de vida:** al retirar `ConsultaGestionModel`, `destroy()` buscaba el ancestro desde un elemento ya desconectado. Fallaba antes de liberar la suscripción. Se guardan las referencias de tarjetas/detalle al montar y se reutilizan al limpiar. El fixture falló antes y ahora comprueba la liberación de las seis suscripciones, sin errores JavaScript.
4. **Renderizado de gestión:** `renderRelationships(state, reception)` separa las relaciones y paginación del resto del formulario dentro de la misma clase. No se crean capas ni nuevos Services. Se conservan la plantilla, contratos, eventos y reglas de autorización.
5. **Runner PWA antiguo:** `webcomponents_pwa.py` queda como entrada breve al único runner vigente `webcomponents-pwa.mjs`; desaparece el recorrido duplicado que esperaba un dashboard anónimo, enlace Login y `<select>` de empleados. Los tres runners reales ya usan el combobox visible. `check` también valida `.mjs`, antes omitidos.
6. **Contraste comprobado:** bordes de campos: antes 1.79:1 en oscuro y 1.59:1 en claro; ahora `--gs-control-border: #71869a` da 4.61:1 y 3.62:1 respectivamente contra el fondo de entrada. Solo cambia ese color de control, no el diseño. Texto/muted/placeholder/botón/error también se comprueban en ambos temas. Se explicita el placeholder dentro del Shadow DOM y se verifican sus estilos computados reales, porque los selectores de la hoja global no atraviesan ese límite. Se entrega caché v37 para distribuir los módulos y estilos actualizados.
7. **Cobertura honesta:** el informe distingue V8 unitario y funciones medidas en Chromium, sin sumar porcentajes ni importar módulos solo para contarlos. Compara hashes de las fuentes con las capturadas para no validar evidencia de código anterior.
8. **Dependencias de desarrollo:** follow-redirects 1.16.1→1.16.3. Overrides limitados al árbol de Mocha para glob 14.0.6 y minimatch 11.0.9; se revisaron sus APIs usadas por Mocha y se probaron descubrimiento TDD, transporte, cobertura y BrowserSync. Son versiones mayores transitivas, no una actualización de las herramientas principales. Mocha 12.0.3, Chai 6.3.0, Sinon 22.1.0 y BrowserSync 3.0.4 se conservan. Node se precisa como >=22.12.0, consistente con Mocha. `npm audit`: cero alertas, también sin dependencias de ejecución.

`DefaultService` continúa centralizando Fetch/validación/errores y no se copió lógica de Cafefe. `api-v2-routes.js` es un contrato declarativo; no necesita dividirse por su longitud. `galenos-academic-app.js` conserva su responsabilidad de Boundary: presentar estado/atención y delegar validación y coordinación en `AcademicCareControl`; se evitó fragmentarlo por estética. Backend: no se movieron DAO, entidades ni reglas a otras capas.

## Pruebas ejecutadas

| Tipo | Resultado | Qué demuestra / qué no |
|---|---|---|
| `npm test` | **415 pasan** (antes 409) | Unitarias sintéticas; no commit PostgreSQL |
| `npm run check` | **124 locales / 122 del clon válidos** | Incluye JS/CJS/MJS; HTML se ejecuta en Chromium |
| `npm run test:transport` | **9 pasan** | HTTP real a servidor sintético, no al backend Java |
| `npm run test:coverage` | **Pasa** | 59 módulos unitarios; 3970/5322 líneas (74.60 %), 973/1160 ramas (83.88 %), 318/444 funciones (71.62 %) de módulos cargados |
| `mvn clean test` | **843 pasan** | Mockito/JUnit Jupiter; no se presentan como transacciones PostgreSQL |
| `mvn package` | **BUILD SUCCESS**, 843 sin errores/omisiones | WAR generado y compatible con Liberty probado |
| `npm run test:browser` | **6 fixtures pasan** + SPA/PWA/contraste/responsive | Chromium sintético y 77 fuentes medidas; cifras separadas de unitarias |
| `node test/browser/offline-shell.mjs` | **PASS**, v37 | Instala/controla y detiene realmente su servidor antes de recargar; sin REST en caché |
| HU2/HU3/HU4 readonly `.mjs` | **PASS**, 0 page errors | Chromium→REST→JPA→clinica; identificación/sesión, PDF, filtros, permisos y SELECT; 20 hashes iguales |
| Probe del historial de Luis | **GET 200 autorizado**, 403 ajeno | Hemograma de RETROCEDIDO separado de orden actual; no nuevo guardado |
| Clon local de commits corregidos sin archivos ignorados | `npm ci` y **415 pasan** | Reproducibilidad de archivos versionados; no publicación de las ramas ni provisión de datos |

Los resúmenes nuevos están en `docs/evidencias-auditoria-20261117/revision-{browser,coverage,hu2,hu3,hu4,historial,resultados,npm-audit,datos}.json`. Capturas públicas: `revision-light-390-empleados.png`, `revision-dark-1440-pacientes.png`. Se conservaron los JSON/capturas de la auditoría inicial. Logs completos de ejecución: `/private/tmp/gt02-final-*.log` y `/private/tmp/final-*.log`, no necesarios como configuración ni subidos con credenciales.

### Pruebas nuevas y eliminaciones

Se añaden seis comportamientos en `gestion-controles.test.js` y `atencion-recepcion.test.js`: búsqueda anterior tardía, editor conservado ante 403, cancelación de opciones, padre correcto/doble envío, alta REST con cabeceras/DTO y conflicto de cita sin falso éxito. No se eliminan casos unitarios en esta revisión. La auditoría anterior ya había reducido 20 repeticiones a una (19 menos). Se elimina la duplicación del recorrido Python, conservando su comando como delegación al runner Node. Los fixtures continúan protegiendo slots, XSS por texto, fecha nula, selección, jerarquía y limpieza.

### Los 23 archivos originalmente fuera de V8 unitario

Todos los nombres son relativos a `src/js/`. No estaban excluidos por una regla de cobertura: Mocha en Node solo mide módulos que carga. Los módulos DOM se prueban en navegador; configuración y entrada no justifican pruebas que solo importen archivos.

| Archivo original | Responsabilidad / necesidad | Situación actual |
|---|---|---|
| `boundary/appearance.js` | Tema, menú y listeners | Medido en Chromium; unitario fuera de alcance DOM |
| `boundary/components/galenos-application.js` | Composición, navegación y sesión | Chromium real/sintético; falta aislar todas las ramas unitarias |
| `boundary/components/galenos-notifications.js` | Avisos, cierre y temporización | Fixture de navegador, además de notificaciones unitarias del emisor |
| `boundary/dashboard.js` | Composición del dashboard | Fixture con servicios sintéticos y REST real separado |
| `boundary/gestion.js` | Composición de pantallas de gestión | Carga medida; no se afirma ejecutar cada CRUD administrativo |
| `boundary/modelos/BusquedaCitasModel.js` | Portal HU1 | Chromium con backend real y PDF |
| `boundary/modelos/CatalogoModel.js` | Catálogos y ciclo de vida | Fixture DOM y limpieza |
| `boundary/modelos/ConsultaGestionModel.js` | Formularios y relaciones | Fixture DOM; defecto de limpieza corregido |
| `boundary/modelos/DashboardModel.js` | Filtros/tarjetas/detalle | Fixture y recorridos reales HU3 |
| `boundary/modelos/InicioSesionModel.js` | Portal HU2 | Chromium con backend real, combobox |
| `boundary/modelos/OrdenModel.js` | Presentación de órdenes | Fixture de montaje; no todos los flujos administrativos |
| `boundary/modelos/PersonaModel.js` | Formulario de personas | Fixture DOM; no todos los CRUD |
| `boundary/modelos/PortalAcademicoModel.js` | Comportamiento común del portal | HU1/HU2 y fixture teclado/ARIA |
| `boundary/modelos/ProcedimientoModel.js` | Editor de plantillas | Fixture de montaje/limpieza; CRUD completo no certificado |
| `boundary/modelos/RelacionModel.js` | Presentación de asociaciones | Fixture DOM; coordinación ahora tiene unitarias |
| `boundary/portal-academico.js` | Coordinación DOM de identificación/citas | Recorridos de navegador; ramas unitarias pendientes |
| `control/AtencionCitaService.js` | Transporte de alta de cita | **Ahora unitario**, conflicto 409 realista |
| `control/AtencionPacienteService.js` | Transporte de alta de paciente | **Ahora unitario**, cabeceras/DTO/ruta/no-store |
| `control/PersonaControl.js` | Búsqueda y edición con cancelación | **Ahora unitario**, respuestas tardías/error |
| `control/ProcedimientoControl.js` | Opciones/editor de pasos | **Ahora unitario**, cancelación |
| `control/RelacionControl.js` | Asociación al padre correcto | **Ahora unitario**, padre/doble envío |
| `control/environment.example.js` | Configuración pública | Probada al arrancar, no prueba trivial de existencia |
| `main.js` | Importar el componente raíz | Arranque real de la SPA, no prueba trivial |

Resultado: 23 originales menos 5 módulos de lógica medidos, más la nueva plantilla estática `boundary/components/application-markup.js` = **19 sin medición unitaria**. Los 19 aparecen en las 77 fuentes del navegador y sus hashes coinciden con el código actual. Eso significa medición, **no ejecución de todas sus funciones ni cobertura integral unitaria**. El Service Worker se comprueba por VM unitario y pruebas offline; no se suma al contador de módulos de la página de Chromium. Ver detalle de funciones medidas en `revision-browser.json` y de alcance en `revision-coverage.json`.

## Datos y flujos reales

La única base usada es localhost:5432/clinica. Liberty de prueba19084 utiliza JDBC con `default_transaction_read_only=on`. Los POST/DELETE de identidad solo crean/revocan sesiones efímeras en memoria. No se ejecutó registrar atención ni ninguna escritura clínica.

**Luis**, consulta `e5bd6458-4469-5543-98b0-1a5b595559a3`: ingreso COMPLETADO, evaluación RETROCEDIDO con nota/orden, nuevo ingreso COMPLETADO, evaluación EN_CURSO con nota/orden. El Doctor autorizado obtiene por REST tres pasos anteriores; Hemograma completo histórico `dba100db-f74c-4028-95fe-a4654eb0d26b` permanece en RETROCEDIDO y `ordenesActuales` contiene otra orden. Las consultas del responsable: cuatro el 15–16/11/2026; cero el 17/11 y cero en el rango futuro comprobado. La cola de otro Doctor probado puede estar vacía legítimamente: no se inventaron tarjetas.

**Ejecución sin plantilla:** `e0da3669-f37f-4890-ae80-41d5541d0c18`, consulta `a11b2f21-469f-40e6-adcf-6113ba9db065`, estado EN_CURSO del procedimiento `DESARROLLO procedimiento flujo 2cb26284`. Hay dos plantillas del mismo rol: Inicio Desarrollo (`b3efc88e-e640-418d-9cbb-cc8fa162ddec`) y Fin Desarrollo (`59c9877a-40a9-4bb5-b10f-3f6a07119897`). No es posible inferir la plantilla solo por el rol. No se asignó ninguna a ciegas ni se cambió el modelo para atenderla. No elegir este residual para la demostración HU4; cualquier reparación de su FK requiere decisión sobre la ejecución correcta y autorización de escritura.

No se encontraron siguientes pasos de las ejecuciones activas sin responsable del rol en la misma clínica en la consulta diagnóstica realizada. Hay **cero personas con múltiples asignaciones de empleado activas**. Para demostrar selección múltiple hace falta preparar una asignación adicional válida mediante un procedimiento autorizado. No se modifican roles/clínicas existentes ni se crean datos automáticamente.

`scripts/academic/poblar_20261116.sql` fue inspeccionado: 113 líneas, 105 INSERT, BEGIN/COMMIT, sin UPDATE/DELETE/ALTER/CREATE y **sin comprobaciones de duplicados**. Depende de UUID/catálogos y fechas locales. No es un inicializador seguro/reproducible de clon limpio; no se publicó ni ejecutó. Los fixtures de navegador sí son sintéticos, reproducibles y no requieren PostgreSQL; eso no los convierte en pruebas de persistencia.

## PWA y comprobaciones manuales pendientes

- El usuario comunica instalación y apertura correctas: se incorpora como evidencia manual de usuario. Chromium comprobó manifest/íconos, activación/control, beforeinstallprompt sintético consumido una sola vez, caché pública y arranque offline. El perfil automatizado puede reportar `in-incognito`: no se oculta ni se presenta como instalación realizada.
- Para el evaluador: captura de aplicación instalada, URL/origen, navegador/versión y commit; abrir instalada con SW v37 después de actualizar, cerrar BrowserSync y recargar. Nunca almacenar respuestas clínicas para aparentar funcionamiento offline.
- VoiceOver: ejecutar el recorrido del manual con controles, combobox, errores, modal, carga/éxito/conflicto; registrar resultado. Se midieron contrastes de los pares declarados y foco/ARIA de escenarios concretos, no todas las combinaciones de contenido ni cada color de imagen.
- HU4 persistente: el manual deja preparada la secuencia de parcial/examen/avance/retroceso/final y SELECT antes/después. Requiere autorización y un caso sintético dedicado; no se ejecutó sobre clinica por esta herramienta.

## Entrega y límites para certificar 10/10

El código versionado instala y prueba desde clon; las ramas corregidas siguen locales. Backend compila/empaqueta y despliega en Liberty. Un clon no proporciona por sí solo la base, catálogos, documentos académicos, fechas ni asignaciones. La preparación de datos autorizada y el esquema oficial son requisitos explícitos en el manual de entrega, no archivos ocultos de la Mac.

Quedan 0.75 puntos conservadoramente sin otorgar: cobertura unitaria integral de toda la presentación/coordinación, recorrido accesible completo con lector y evidencia de escritura HU4/múltiples asignaciones. No se busca cerrarlos con imports triviales, mocks que simulan PostgreSQL o asignaciones inventadas. Ejecutar las comprobaciones manuales indicadas, preparar datos con autorización y volver a calificar sobre el commit de entrega. No se certifica 10/10 mientras esas evidencias estén pendientes.
