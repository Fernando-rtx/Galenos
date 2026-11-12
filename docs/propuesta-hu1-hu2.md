# Decisiones necesarias para HU1, HU2 y el dashboard de empleado

Este documento prepara la siguiente etapa del parcial. No añade endpoints,
permisos, dependencias ni cambios al esquema. El `main` académico expone REST v2
sin sesión y el frontend usa `credentials: omit`; ese contexto no debe usarse
como si identificara a un paciente o empleado.

## Estado existente

- REST v2 tiene consultas paginadas por clínica, búsquedas de PersonaRol por
  nombre/DUI dentro de clínica y DTO administrativos de Persona/Documento.
- La UI contiene CRUD administrativo para Persona, Documento y PersonaRol.
- La respuesta de contexto académico declara `actor: academico-local` y clínicas
  disponibles para el despliegue local.
- `server.xml` académico no habilita `appSecurity`; la SPA no tiene pantalla ni
  servicio de sesión.
- No hay ruta REST para emitir el ticket PDF de una cita.

Estas piezas sirven para el backend y la futura interfaz interna, pero no cumplen
por sí solas una búsqueda pública de citas ni autenticación de empleados.

## HU1: búsqueda de citas del paciente

**Decisión que necesita el ingeniero:** ¿el número de documento es solo un
identificador académico, o se exige un segundo factor para mostrar citas? El
documento y la clínica elegida no demuestran que quien consulta sea su titular.

Antes de programar, acordar:

1. Verificación de titularidad (por ejemplo, código de un solo uso enviado a un
   medio de contacto verificado) y mitigación de enumeración/fuerza bruta.
2. Si una persona puede estar asociada a varias clínicas, cómo descubre sus
   clínicas sin revelar registros ajenos.
3. Qué citas y campos mínimos se devuelven, y el rango máximo de fechas.
4. Cómo se emite y descarga el ticket PDF y cuánto dura su URL o token.

Contrato candidato, sujeto a aprobación: `POST /resources/v2/citas/busqueda`
recibe tipo/número de documento, rango de fechas y, después de verificar
titularidad, una clínica permitida. Devuelve solo fecha/hora, clínica,
procedimientos asociados y un identificador opaco para solicitar el ticket.
`GET /resources/v2/citas/{id}/ticket` entrega `application/pdf` solo si el
token temporal autoriza esa cita. No reutilizar el CRUD `documento` o el endpoint
administrativo de pacientes como acceso público.

## HU2 y HU3: empleados, roles y clínicas

**Decisión que necesita el ingeniero:** qué proveedor/credencial autentica al
empleado y cómo se vincula su sujeto autenticado a PersonaRol. El DNI identifica,
pero no es secreto; no se debe aceptar por sí solo como prueba de identidad.

Una vez acordado el proveedor:

1. Configurar la autenticación en Open Liberty sin alterar el perfil JSF existente.
2. Crear un servicio de sesión en el frontend mediante los Services actuales,
   conservando Boundary–Control–Entity y usando el mecanismo de sesión aprobado.
3. Derivar desde el sujeto autenticado los roles y clínicas del empleado; no
   confiar en UUID de clínica o rol enviados por el selector del navegador.
4. Aplicar esa autorización en Java a cada lectura/escritura y probar usuario,
   rol y clínica ajenos.
5. Usar el dashboard actual y sus tarjetas para presentar la cola autorizada.

La relación PersonaRol existente puede aportar la asignación clínica cuando se
conozca la clave confiable que la liga a la identidad autenticada. No se deben
crear contraseñas, asociaciones ni privilegios ficticios en PostgreSQL.

## Criterios para iniciar implementación

- Decisiones de identidad, titularidad y segundo factor confirmadas por el
  ingeniero/equipo.
- Contratos de entrada/salida y errores aprobados, con paginación y minimización
  de datos.
- Datos sintéticos y entorno de prueba aislado preparados.
- Pruebas Java de autorización por clínica/identidad y pruebas frontend de
  sesión, navegación y errores acordadas.

No requiere cambiar el esquema para redactar esta propuesta. Si las decisiones
requieren persistir identidades o tickets, primero se debe aprobar una propuesta
separada de modelo y seguridad.
