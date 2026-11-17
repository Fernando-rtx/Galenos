# Manual final de pruebas GT02

El resultado verificado y sus límites están en [auditoría](auditoria-final-gt02.md). Utilizar únicamente datos sintéticos autorizados de `clinica`. Las acciones de guardado de esta guía son instrucciones para una futura prueba manual autorizada; no se ejecutaron durante la auditoría.

## Arranque

Seguir [preparación](preparacion-entrega.md). PostgreSQL localhost:5432/clinica; configurar `server.env`, ejecutar `./run.sh`; mantener Liberty activo. Frontend: `npm ci`, `npm run dev`, mantener BrowserSync activo, abrir http://localhost:3000. API habitual http://127.0.0.1:9080/GalenosSV/resources/. No usar el puerto 19084 de auditoría para escribir: JDBC está en solo lectura.

## HU1

Abrir Búsqueda de citas. Elegir el tipo y documento académico entregado por el equipo. Elegir una clínica devuelta por el backend y rango con datos. Buscar: tarjetas con fecha/hora/clínica/procedimientos. Descargar ticket: elegir opciones, confirmar PDF generado por Java. Un paciente no necesita una asignación de empleado. Fechas invertidas y documentos inválidos deben indicar el campo incorrecto.

## HU2

Abrir Acceso de empleados. Escribir el nombre del tipo en el combobox, seleccionar la opción y el documento ficticio. Elegir asignación vigente y entrar. El rol y clínica deben coincidir con esa asignación. Intentar paciente sin rol de empleado: rechazo sin dashboard. Cambiar asignación si hay varias y verificar limpieza de información previa. Cerrar sesión y usar Atrás: no debe reaparecer información clínica. Una sesión vencida debe llevar al acceso de empleados.

## HU3

Usar un empleado con consultas asignadas. Por defecto ambas fechas son hoy en America/El_Salvador. Probar pasado, futuro y rango explícito (15–16/11/2026 para el ejemplo existente). Cero resultados en un rango sin datos es válido. Filtrar por documento del paciente según los controles actuales; comprobar información general y árbol de procedimientos, responsables, fechas y notas. Intentar abrir un UUID ajeno: 403, sin exponer detalle. Una consulta que cambia de responsable debe desaparecer de la cola anterior y aparecer en la nueva.

## HU4 — solo con autorización de escritura

Elegir consulta activa con plantilla y responsable válido. Escribir una nota sintética y Guardar avance: toast solo tras confirmación; sigue asignada, sin fecha final y conserva la nota tras volver a abrir. Seleccionar examen permitido y guardar: orden persistida una vez, sin doble envío. Guardar y avanzar: actual COMPLETADO, nuevo PENDIENTE para responsable autorizado. Retroceder cuando la ruta lo permita: ejecución actual RETROCEDIDO, nueva ejecución anterior; las órdenes previas permanecen en su ejecución original. Finalizar solo en paso final: procedimiento con fecha final, fuera de la bandeja activa, historial intacto. Contrastar respuesta REST y SELECT; ninguna de estas transacciones se probó con escritura durante esta auditoría.

Para Luis usar UUID `e5bd6458-4469-5543-98b0-1a5b595559a3`: el Hemograma histórico `dba100db-f74c-4028-95fe-a4654eb0d26b` debe quedar en la evaluación RETROCEDIDO, separado de la orden actual. No mezclar o volver a asignar ese examen para demostrar lectura.

## Notificaciones, permisos y conexión

Mientras guarda: indicador de carga y botones deshabilitados. Doble clic: un envío. Error/409/403/401: aviso claro, sin éxito falso ni pérdida del texto. Errores persistentes se cierran manualmente; éxito temporal después del backend. Para provocar conflictos reales hace falta un caso controlado y autorización; fixtures sintéticos cubren las acciones sin tocar datos. Desconectar red: la interfaz permanece y no permite guardar clínicamente; al volver, reintentar.

## PWA y Safari

Con servidor encendido, abrir mismo origen/puerto y recargar. Inspector: `await navigator.serviceWorker.ready`, controlador no nulo, caché `galenossv-shell-v34`. Instalar desde perfil normal: Safari Añadir al Dock o navegador compatible. Cerrar y abrir instalada. Detener BrowserSync con Control+C y recargar normalmente: interfaz y navegación deben aparecer. Después desconectar la red: aviso offline, sin REST ni PDF recuperados de caché. Revisar Cache Storage: solo recursos públicos, ninguna respuesta clínica, documento, token o configuración local. Reanudar BrowserSync y Liberty para volver a datos. Primera visita requiere red; localhost y 127.0.0.1 son orígenes distintos.

## Accesibilidad y tamaños

390, 768 y 1440 px; ambos portales, bandeja, historial largo y atención. Tab/Shift+Tab, Enter/Escape, enlace de salto y devolución de foco en diálogo. Leer errores, cargando y éxito con VoiceOver, comprobar etiquetas/contraste y que el toast no tape el botón siguiente. Conservar evidencia del lector y la aplicación instalada, pendientes en esta auditoría.
