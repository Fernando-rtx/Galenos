# Verificación estricta GT02 de los commits publicados — 17/11/2026

## Código y fuente evaluados

- Frontend publicado: **`7c48cc528575c911a18933440df13cea044ff4ad`**, https://github.com/Fernando-rtx/GalenoSVFE.
- Backend publicado: **`4406204f04b5020465f16f5c4a2750a58f19c573`**, https://github.com/Fernando-rtx/Galenos.
- Ramas de este informe: `audit/gt02-verificacion-publicada`. No se modifican `main`, código de producción, dependencias, contratos, esquemas ni datos. Se incorpora únicamente un verificador de integración de lectura y este informe/evidencias.
- Fuente oficial: `definicionSegundoParcialPPI115_gt02_2026.pdf`, cuatro páginas. Enunciados HU1–HU4 en páginas2–3; matriz en página4. SHA256 **`f980d8f95167cedd4bac562dba1eaae68f20bd27da738e9c3152f2192eee969a`**. La fuente se contrastó mediante extracción de texto; no se altera ni se redistribuye el PDF.

La rúbrica prescribe revisión secuencial: ante un observable que falle puede detenerse la evaluación. Aquí se continúa con los 14 para diagnóstico, sin afirmar que el docente otorgaría la suma si se detuviera antes.

## Conclusión y puntuación

**No existe evidencia suficiente para certificar 10/10.** Orientación estricta: **8.75/10 incluyendo los 0.5 de instalación comunicada por el usuario**, o **8.25/10 sin aceptar esa confirmación como acta de demostración**. No es una calificación oficial ni una deducción basada en número de pruebas.

La orientación anterior era9.25. Esta revisión detecta una excepción arquitectónica antes no reflejada: el diálogo de descarga está fuera de la raíz y no es un Custom Element. Se reserva0.5 de Web Components por la lectura estricta del requisito. No se trata de una regresión: el código publicado permanece idéntico. El docente podría aceptar el helper como parte de Boundary; aquí se registra el riesgo explícitamente, en lugar de prometer el punto completo.

El usuario ya informó instalación y apertura correctas de la PWA. Esa evidencia se conserva como **manual comunicada por el usuario**. No se confunde con una instalación reproducida en esta ejecución: Chromium automatizado informa `in-incognito`; manifest, SW y prompt sintético no sustituyen la instalación real.

## Matriz secuencial de los 14 observables

FE/BE indican repositorio frontend/backend. CUMPLE se limita al alcance y escenarios señalados; PARCIAL puede significar un hueco de evidencia, no necesariamente una función ausente. Los puntos intermedios son estimación interna. En HU1–HU4 se reparte provisionalmente0.5 por historia; el PDF no define ese reparto.

| Orden / observable | Requisito oficial | Máximo | Orientativo | Evidencia | Estado / riesgo | Acción correctiva mínima | Procedimiento de demostración |
|---|---|---:|---:|---|---|---|---|
| 1. SPA | Navegación fluida, intuitiva y consistente | 0.5 | 0.5 | FE `galenos-application.js`; Chromium navega paciente→empleado→Atrás; tests de sesiones/cancelación | CUMPLE en recorridos probados; no cada ruta/estado | Conservar evidencia de logout y Atrás en el navegador del evaluador | Abrir #pacientes, pasar a #empleados, Atrás; iniciar sesión y cerrar, comprobar ausencia de contexto clínico |
| 2. PWA instalable | Instalación y flujo de instalación correctos | 0.5 | 0.5* | Manifest válido, íconos PNG, SW activo; prompt sintético; confirmación manual del usuario | MANUAL INFORMADO; no acta independiente de instalación para estos SHA; posible primera detención | Adjuntar captura, navegador/versión, origen, commit y apertura instalada | Instalar desde perfil normal; cerrar y abrir instalada; demostrar SW v37 y ventana standalone |
| 3. PWA online/offline | Mantener funcionalidad online/offline | 0.5 | 0.5 | `offline-shell.mjs`: servidor realmente detenido antes de recarga; navegación; lista cerrada de caché | CUMPLE para shell; REST clínico requiere red | Mostrar claramente la diferencia entre interfaz y datos clínicos | Instalar/activar en origen estable, detener servidor y recargar; luego desconectar red y observar aviso y rechazo de REST |
| 4. Web Components | Totalidad de la arquitectura basada en Web Components | 1 | 0.5 | 15 Custom Elements; portales, tarjetas, historial y atención propios; `ticket-options.js` agrega `body > dialog` | PARCIAL bajo interpretación estricta; excepción visual fuera del componente raíz | Encapsular el diálogo de ticket en un Custom Element propio reutilizando su HTML, estilos y comportamiento; justificar el cambio antes de implementarlo | Abrir el diálogo y examinar árbol DOM: hoy su padre es BODY y no está dentro de galenos-application |
| 5. Modularidad | Estructura modular, clara y organizada | 0.5 | 0.5 | Boundary/Control/Entity/Services, transporte común; 78 módulos, sin ciclos estáticos detectados | CUMPLE en análisis estático; no es prueba formal de ausencia de ciclos dinámicos | Ninguna refactorización estética necesaria | Seguir acción Boundary→Control→Service→DefaultService→REST y DAO/JPA en BE |
| 6. Componentes propios | Componentes a medida sin dependencias externas de ejecución | 1 | 1 | package.json sin dependencies; CSS/HTML nativos, sin biblioteca visual/CDN; herramientas solo dev | CUMPLE en frontend; PrimeFaces pertenece al backend heredado | Mantener separación de SPA frente a JSF y herramientas | Inspeccionar package.json, Network y fuentes; no confundir Mocha/BrowserSync con componentes de ejecución |
| 7. Reactividad | Lógica reactiva y asíncrona | 0.5 | 0.5 | Fetch común, Promises/async, cancelación, eventos, carga; fixtures HU3/HU4 y REST real | CUMPLE en escenarios cubiertos; escribir realmente requiere autorización | No sustituir prueba transaccional por mock | Retrasar respuesta en fixture, doble clic, error y cancelación; comprobar actualización sin reload |
| 8. Cobertura unitaria | Cobertura integral mediante pruebas unitarias | 0.5 | 0.25 | 415 JS y843 Java; V8 de59 módulos,19 fuera de medición; navegador separado | PARCIAL: ramas de composición/navegación/portal tienen evidencia de navegador, no equivalencia a unitarias | Identificar ramas de coordinación sin prueba aislada y añadir solo casos con defecto detectable; no imports triviales | Ejecutar test:coverage y revisar scope.json y casos de sesión/cancelación, no solo el porcentaje |
| 9. Calidad de pruebas | Pruebas significativas para calidad y estabilidad | 0.5 | 0.5 | TDD Mocha/Chai/Sinon; JUnit/Mockito; fixtures comprueban XSS, slots, errores, acciones y limpieza | CUMPLE en revisión; tests HTTP sintéticos no demuestran PostgreSQL | Ninguna modificación para subir cantidad/porcentajes | Revisar preparación/acción/aserciones; ejecutar unitarias, transporte e integración por separado |
| 10. UI | Interfaz atractiva, coherente y orientada al usuario | 0.5 | 0.5 | Captura móvil inspeccionada; temas, formularios y notificaciones | CUMPLE en pantallas observadas; atractivo es juicio del evaluador | Recorrer estados largo/vacío/error con el evaluador | Mostrar portales y atención con carga, error persistente y éxito confirmado; cerrar avisos |
| 11. CSS propio | Autoría propia, tipografía, color y espaciado | 1 | 1 | Estilos locales/globales/Shadow DOM; pares de texto y bordes comprobados | CUMPLE en fuentes examinadas; no mide todo píxel o imagen | Mantener variables de tema; no agregar CSS externo | Inspeccionar recursos cargados y estilos computados en ambos temas |
| 12. Responsive | Experiencia uniforme en distintas pantallas | 0.5 | 0.5 | Chromium390/768/1440, ambos temas/portales; atención sintética móvil | CUMPLE en casos probados; historias extremas y zoom requieren observación | Comprobar historial largo y zoom200% sin cambiar diseño de antemano | Recorrer anchos indicados con teclado y sin desbordamiento; verificar acciones accesibles |
| 13. HU1–HU4 | Cumplimiento cabal de los enunciados | 2 | 1.75 | REST real de citas/PDF, identidad, filtros y lectura de historial; HU4 simulado para escrituras | PARCIAL: faltan transacciones autorizadas, múltiples asignaciones y citas reales hoy/futuras; alcance de “generales” por confirmar | Preparar caso sintético autorizado y completar protocolo de persistencia; confirmar campos esperados | Ver tabla HU y manual: comparar UUID, responsables, fechas, notas y órdenes antes/después de cada acción autorizada |
| 14. Accesibilidad | Diseño ergonómico y accesible | 0.5 | 0.25 | Combobox/modal/error/foco/ARIA; contraste de pares; Escape del diálogo real devuelve foco | PARCIAL: VoiceOver y recorrido completo por teclado no reproducidos | Realizar acta manual; corregir solo fallos observados | Cmd+F5, Tab/Shift+Tab, flechas/Enter/Escape, errores, avisos, modal y retorno del foco |
| TOTAL | Suma orientativa, sujeta a revisión secuencial | **10** | **8.75*** | Evidencia tipificada | **NO CERTIFICADO 10/10** | Cerrar excepciones y pendientes | Repetir sobre el commit de entrega |

*Incluye0.5 de PWA comunicado por el usuario, no demostrado por instalación independiente en esta ejecución. Sin esa evidencia la suma reproducida aquí sería8.25. El primer bloqueo posible es el acta de instalación (orden2); el siguiente hallazgo de implementación es la excepción del diálogo (orden4). No se promete que el evaluador continúe después de ellos.

## Hallazgos comprobados y ambigüedades

### H1 — excepción al uso exclusivo de Web Components

`FE/src/js/boundary/ticket-options.js` crea un `<dialog>` y lo agrega con `document.body.append(dialog)`. El diagnóstico Chromium ejecutó el helper real con disparador sintético, sin descargar ni escribir datos. Resultado: padre BODY, `insideApplication=false`, `customElement=false`, título “Descarga tu ticket”, Escape cancela y `focusRestored=true`.

No se cuestionan los controles HTML nativos dentro de un Web Component; la observación es el diálogo autónomo montado fuera de la raíz. No se afirma que esté roto. Bajo una lectura permisiva del patrón Boundary puede aceptarse; bajo el “uso exclusivo y riguroso” es una excepción que debe resolver o aceptar explícitamente el evaluador. Propuesta mínima: un único componente de diálogo reutilizando el comportamiento actual, sin duplicar Services ni cambiar REST. **No se implementó producción**.

Diagnóstico reproducible en consola de una SPA abierta: importar `/js/boundary/ticket-options.js` y llamar `offerTicketDownload()` sin esperar su Promise antes de inspeccionar; observar `document.querySelector('body > dialog.ticket-options')`, comprobar `.closest('galenos-application')` y cerrar con Escape. Es una comprobación sintética del helper, no prueba de HU1 autenticada ni PDF persistido.

### H2 — datos y persistencia no demostrados por escritura

El verificador real encuentra cuatro consultas asignadas al responsable el15–16/11/2026, cero hoy17/11 y cero del18/11 al17/12. Eso demuestra respuestas200/filtrado, **no una consulta futura real**. Existen cero personas con varias asignaciones activas de empleado. Es preparación insuficiente de datos para esos escenarios, no evidencia de un defecto del filtro.

Sigue existiendo la ejecución `e0da3669-f37f-4890-ae80-41d5541d0c18` sin plantilla, en la consulta de desarrollo `a11b2f21-469f-40e6-adcf-6113ba9db065`. Ya había dos plantillas candidatas Inicio/Fin del mismo rol; no se puede elegir por conjetura. No se actualizó esa FK ni se modificaron permisos para atenderla. La preparación de datos debe resolverse con autorización antes de usar ese caso.

`scripts/academic/` permanece local, sin seguimiento. El SQL revisado antes tiene105INSERT y no controla duplicados; no se ejecuta ni publica como inicializador seguro. No se crearon bases, respaldos, migraciones, roles ni permisos.

### H3 — alcance de “generales del paciente”

El DTO `AcademicCareService.Appointment` y el de atención exponen el nombre del paciente y contexto clínico/procedimiento, sin fecha de nacimiento/documento del paciente en ese DTO. El PDF no enumera cuáles “generales” exige. Se registra como **ambigüedad de aceptación**, no como defecto confirmado ni autorización para ampliar contratos. Confirmar con el docente si el nombre/contexto mostrado satisface ese punto; si requiere más campos, acordar el contrato mínimo seguro antes de cambiarlo.

### H4 — documentación anterior es histórica

Los SHA evaluados sí están publicados y se clonaron desde GitHub. Las frases anteriores sobre ramas aún locales se referían a la fase de auditoría previa a publicación; no deben usarse como instrucciones actuales. El contenido evaluado es el de los dos SHA completos de este informe. Las nuevas evidencias y el verificador de lectura se entregan en la rama de verificación, sin modificar main.

## Ejecuciones de esta verificación

| Comando / tipo | Resultado actual | Alcance real |
|---|---|---|
| `npm test` | 415 pasan | Unitarias Mocha/Chai/Sinon, sin PostgreSQL |
| `npm run check` | 124 locales /122 del clon válidos | Sintaxis JS/CJS/MJS; dos configuraciones locales ignoradas no son requisito del clon |
| `npm run test:transport` | 9 pasan | HTTP real a servidor sintético, no persistencia Java |
| `npm run test:browser` | 6 fixtures y comprobaciones SPA/PWA pasan | Chromium sintético, responsive y contraste; no escritura real |
| `node test/browser/offline-shell.mjs` | PASS | Primer control SW, servidor detenido realmente y recarga sin pantalla vacía |
| `npm run test:coverage` | PASS | V8 unitario, separado del navegador |
| `mvn clean test` | 843 pasan | JUnit/Mockito; no prueba de commit/rollback PostgreSQL |
| `mvn package` | BUILD SUCCESS;843 sin errores/omisiones | WAR generado; backend mantiene DAO/JPA |
| Clon FE remoto al SHA publicado | npm ci,415 tests y122 fuentes válidas | Sin archivos ignorados locales |
| Clon BE remoto al SHA publicado | Maven package BUILD SUCCESS | Sin base nueva; bibliotecas empaquetadas a directorio temporal |
| Liberty temporal19084 | Aplicación iniciada, JDBC readonly | localhost:5432/clinica, schema-generation=none |
| `python3 test/integration/gt02_hu_readonly.py` | PASS;20 hashes iguales | Solo GET clínicos/SELECT; POST de identidad solo crea sesión en memoria; sin DELETE ni registro de atención |

El primer intento de Maven del clon se lanzó desde `/private/tmp` y falló por no encontrar POM; se corrigió el directorio y el empaquetado desde el clon pasó. No se atribuye ese error de comando al producto ni se oculta como prueba aprobada.

## Cobertura separada

**Unitaria V8:**59 fuentes medidas;3970/5322 líneas =74.60%,973/1160 ramas =83.88%,318/444 funciones =71.62%, de módulos cargados. No es74.66 ni100% global. Hay19 fuentes fuera de medición unitaria, principalmente Boundary, composición DOM y entrada/configuración. Enumeración y motivos en `estricta-unitaria.json`. No se importaron archivos solo para inflar la medición ni se agregaron casos triviales.

**Navegador:**77 módulos de página medidos mediante V8/Playwright, con funciones y hashes de fuente. El contador indica medición, no ejecución completa de todas las funciones. Sus porcentajes no se combinan con los de unitarias. El Service Worker se evalúa por los tests VM y offline; no se cuenta como módulo de la página. La cobertura del navegador coincide con las fuentes vigentes según hashes.

Los seis fixtures comprueban tarjetas/slots/XSS/selección, montaje/limpieza de páginas, jerarquía/filtros/403 de HU3, acciones de HU4, avisos y accesibilidad. Se conservaron todas las pruebas de producción existentes. El nuevo verificador Python es integración real de solo lectura, no aumenta el contador de unitarias ni el porcentaje V8.

## Contraste de historias de usuario con el PDF

| Historia | Implementado y comprobado | REST real ejecutado ahora | Simulado / evidencia anterior | Requiere autorización o acta |
|---|---|---|---|---|
| HU1 | Tipo/número, clínica propia, rango, tarjetas/procedimientos y descarga Java | Identidad paciente, GET citas y PDF válido `%PDF-`, no-store; documento/bytes solo en memoria | Formularios/componentes de navegador; recorrido Chromium real anterior conservado | Repetir recorrido visible completo ante evaluador con documento ficticio entregado por el equipo |
| HU2 | Identidad académica, contexto/rol/clínica, rechazo de pacientes, sesión y cierre |401 sin sesión;403 paciente como empleado; identidad/asignación propia200 | Logout/revocación/expiración y combobox cubiertos por tests y ejecución real anterior | Demostrar cambio entre varias asignaciones reales cuando existan; no crear asignaciones sin permiso |
| HU3 | Hoy, pasado/futuro, filtros, árbol por procedimiento, responsable/fecha/registro y selección | GET por fechas200:4/0/0;403 de asignaciones ajenas sin exponer historial | Jerarquía/tarjetas/selección y cancelación en fixtures; UI real anterior disponible | Consulta futura/hoy con datos reales de prueba; aclarar “generales” |
| HU4 | Parcial, exámenes permitidos, órdenes, avance/retroceso/finalización y errores | Detalle autorizado200;3 pasos anteriores;1 orden retrocedida y1 actual separadas;403 ajeno | Acciones,409 y bloqueo de duplicados en fixture; transiciones/reglas en Mockito | Probar guardado y commit real, rollback/concurrencia y responsable siguiente con caso sintético autorizado |

**Caso de Luis:** consulta `e5bd6458-4469-5543-98b0-1a5b595559a3`. REST conserva Hemograma completo en la ejecución RETROCEDIDO, separado por UUID de las órdenes actuales. La consulta se limita al fixture de documento académico ACAD. El diagnóstico no volvió a registrar ni avanzar la atención.

La evidencia de filas ya existentes demuestra conservación y recuperación; no demuestra que el guardado de esta ejecución del auditor haya funcionado, porque no se ejecutó un guardado.

## SPA, PWA y caché

Registro `/service-worker.js`, scope `/`, secure context; instalación precarga una lista cerrada, luego skipWaiting; activación elimina solo versiones propias y hace claim. Shell vigente `galenossv-shell-v37`. Se comprobaron dependencias de arranque, iconos y manifest, navegación hash/Atrás, actualización de caché mediante tests y recuperación offline con servidor apagado.

El SW no intercepta métodos distintos de GET, Authorization, otro origen ni URL fuera de la lista; excluye REST, configuración local y PDF. Los GET clínicos reales usan no-store. Cache Storage de la sesión sintética contiene solo recursos públicos; no se inspeccionó ni se afirma el estado de un perfil personal de Safari. LocalStorage del código solo conserva preferencias de tema/idioma, no tokens ni datos clínicos. Desconectado no se debe poder consultar/grabar atención; mostrar interfaz no equivale a dar datos médicos offline.

La prueba de `beforeinstallprompt` usa un evento sintético. El error `in-incognito` del entorno automatizado se conserva en la evidencia; instalar/abrir una PWA real necesita perfil normal y acta. El usuario informó instalación correcta y esa confirmación no se reetiqueta como prueba reproducida por Codex.

## Accesibilidad automatizable y manual

Automático: etiquetas, error asociado/aria-invalid, combobox con opción activa/flechas/Enter, modal con nombre, foco y retorno, role status/alert, avisos temporales/persistentes/cerrables, estados disabled y ausencia de desbordamiento en390/768/1440. Se calcularon ratios de pares del tema y se comprobó que placeholder/borde se aplican realmente en Shadow DOM; texto >=4.5:1 y borde >=3:1 en los pares medidos. El diálogo real conserva foco al cerrarlo con Escape. No equivale a WCAG completa ni a lector de pantalla probado.

Procedimiento VoiceOver detallado en `manual-pruebas-finales.md`: habilitar navegación con teclado de macOS, Cmd+F5, recorrer HU1–HU4 con Tab/Shift+Tab y lector; usar flechas/Enter/Escape en combobox; provocar validación sin enviar datos clínicos; comprobar anuncio de etiqueta/error/estado, modal, foco y retorno. Registrar navegador/versión, commit, resultado y fallo exacto. Atención real y conflictos mediante escritura solo con autorización. Con zoom200% e historial largo comprobar que avisos no tapen acciones. No automatizar una confirmación de VoiceOver sin usarlo.

## Reproducción y qué impide el cumplimiento total

Los clones remotos coincidieron exactamente con ambos SHA publicados. La instalación técnica no necesita configuraciones privadas del autor; sí requiere los prerrequisitos declarados y, para REST, una clinica existente con sus20 tablas, credenciales propias fuera de Git, catálogos, responsables y fixtures sintéticos. El clon por sí solo no suministra esos datos. Ver `preparacion-entrega.md` para comandos exactos.

Evidencias actuales: `docs/evidencias-auditoria-20261117/estricta-{resultados,unitaria,navegador,rest,dialogo,arquitectura,datos}.json` y `estricta-empleados-390.png`. Las evidencias anteriores se conservan. `test/integration/gt02_hu_readonly.py` hace reproducible el probe sin depender de scripts privados de /tmp.

Para aspirar a10/10: resolver o conseguir aceptación explícita de la excepción del diálogo, adjuntar acta de instalación del commit exacto, cubrir con casos unitarios útiles las ramas de coordinación que lo requieran, completar VoiceOver y datos/recorrido HU4 transaccional con autorización. La evaluación secuencial puede detenerse antes de llegar a los escenarios clínicos. No se propone una refactorización masiva ni cambiar contratos por estética.
