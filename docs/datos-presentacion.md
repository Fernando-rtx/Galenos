# Datos de presentación

Personas y documentos ficticios para la exposición académica. Los formatos son
descriptivos; no se verifica la autenticidad de los documentos.

| Portal | Persona | Tipo | Documento |
| --- | --- | --- | --- |
| Login (recepción) | Sofía Alejandra Ramírez Torres | DUI | 05123456-7 |
| Login (médico) | Luis Fernando Mendoza Castro | DUI | 03678912-4 |
| Búsqueda de citas | Ana Gabriela Martínez López | DUI | 02845671-3 |
| Búsqueda de citas | Carlos Eduardo Hernández Pérez | Pasaporte | A12345678 |
| Búsqueda de citas | Diego Andrés Martínez López | Carné de minoridad | 2026-0042 |

La identificación académica funciona sin contraseña. La clínica de Sonsonate
continúa inactiva para conservar ese caso del flujo.

Para reproducirlos, preparar primero la base GT02 con `scripts/gt02_test_runtime.py`
y cargar `test/fixtures/presentacion.sql` después de `crud_demo.sql` y `gt02.sql`.
El archivo sólo se aplica a bases `galenos_rest_test_gt02_presentacion_*`.

```bash
docker exec -i postgres-db psql -U postgres \
  -d galenos_rest_test_gt02_presentacion_20261113 -v ON_ERROR_STOP=1 \
  < test/fixtures/presentacion.sql
```

La carga actualiza nombres y documentos conservando los identificadores, las
asignaciones y las fechas de las citas. Puede repetirse sin duplicar registros.
