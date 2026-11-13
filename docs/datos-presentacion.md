# Datos de presentación

Personas y documentos ficticios para la exposición académica. Los formatos son
descriptivos; no se verifica la autenticidad de los documentos.

| Portal | Persona | Tipo | Documento |
| --- | --- | --- | --- |
| Login (atención al público) | Carlos Martínez | DUI | 01234567-8 |
| Login (enfermera) | María López | DUI | 02345678-9 |
| Login (doctor) | José Hernández | DUI | 03456789-0 |
| Búsqueda de citas | Ana Rodríguez | DUI | 04567890-1 |
| Búsqueda de citas | Ana Rodríguez | Pasaporte | A12345678 |
| Búsqueda de citas | Pedro Ramírez | DUI | 05678901-2 |

La identificación académica funciona sin contraseña. Clínica actual: Clínica
Central GalenosSV. La consulta de Ana se carga para el día de ejecución a las
09:00 de El Salvador; Carlos recibe al paciente, María toma signos vitales y
José realiza la evaluación final. Por petición posterior, Pedro también tiene
rol de paciente; el caso sin rol del PDF ya no se conserva en esta presentación.
Las dos clínicas activas tienen asignaciones para los cinco usuarios.

Para reproducirlos, preparar primero la base GT02 con `scripts/gt02_test_runtime.py`
y cargar `test/fixtures/rubrica_presentacion.sql`. Este archivo REEMPLAZA los
datos de bases `galenos_rest_test_gt02_presentacion_*` dentro de una transacción.
Fuente: `Datos_Prueba_GalenosSV_Rubrica_PPI115.pdf`. Sustituye la presentación
anterior; no cargar después `presentacion.sql` ni `gt02.sql`.

```bash
docker exec -i postgres-db psql -U postgres \
  -d galenos_rest_test_gt02_presentacion_20261113 -v ON_ERROR_STOP=1 \
  < test/fixtures/rubrica_presentacion.sql

docker exec -i postgres-db psql -U postgres \
  -d galenos_rest_test_gt02_presentacion_20261113 -v ON_ERROR_STOP=1 \
  < test/fixtures/rubrica_completar.sql

GT02_DATABASE=galenos_rest_test_gt02_presentacion_20261113 \
GT02_CONTAINER=postgres-db \
GT02_EMPLEADO_ROLES=00000000-0000-4000-8000-000000000032,00000000-0000-4000-8000-000000000033,00000000-0000-4000-8000-000000000034 \
python3 scripts/gt02_test_runtime.py stage
```

Detener el runtime antes de cargar y volver a iniciarlo con `run` después de
`stage` para descartar la caché JPA anterior. La carga restablece los datos;
ejecutarla de nuevo elimina los avances realizados durante la demostración.

Incluye los catálogos activos e inactivos del PDF, cinco personas y seis
procedimientos activos de tres pasos cada uno (18 pasos y 12 transiciones).
Los dos del PDF son Consulta médica general y Evaluación de salud. Los cuatro
adicionales son Consulta pediátrica, Control de hipertensión, Control de diabetes
y Control preventivo de salud. Todos siguen recepción, enfermería y doctor;
sólo el tercer paso finaliza. Los exámenes asociados a los primeros dos siguen
el PDF: hemograma en signos vitales, radiografía en evaluación médica y
hemograma en diagnóstico final. El tipo de clínica especializada se guarda
como `Especializada` porque la columna admite 20 caracteres.

El complemento agrega siete consultas: recepción pendiente, enfermería en curso,
evaluación médica pendiente, controles finalizados y citas para mañana. Incluye
órdenes de examen y resultados relacionados con pasos que permiten esos exámenes.
No asigna la plantilla pediátrica a los pacientes adultos. Al repetir sólo el
complemento se conservan los avances existentes.

## Conexión unificada

La aplicación usa ahora exclusivamente `clinica` en `postgres-db`, mediante el
backend principal en `http://127.0.0.1:9080/GalenosSV/`. El frontend configura
esa misma API en `environment.local.js`. El perfil 19085 está detenido.

Los conjuntos GT02 de este documento quedan como fuentes de prueba; no son la
conexión activa de la aplicación. `scripts/importar_presentacion_clinica.py`
incorpora sus datos a `clinica`, reutilizando identificadores equivalentes por
nombre y conservando los registros existentes. La operación es transaccional y
agrega las columnas faltantes del esquema actual. No elimina registros.
