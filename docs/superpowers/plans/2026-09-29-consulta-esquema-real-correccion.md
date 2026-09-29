# Plan: corrección al esquema real de Consulta y Exámenes

Fecha: 2026-09-29
Spec: `docs/superpowers/specs/2026-09-29-consulta-esquema-real-correccion-design.md`
Rama: `feature/fernando` (merge directo a `main`, sin PR)

## Fase 1 — Corrección obligatoria del mapeo

1. `ConsultaProcedimientoPaso.java` (entidad): quitar `idProcedimientoPaso` y sus
   anotaciones, getter, setter e imports.
2. `ConsultaProcedimientoPasoModel.java`: quitar `ProcedimientoPasoDAO` y los
   métodos dependientes; simplificar `validarResponsable`.
3. `Consulta.xhtml` tab pasos: quitar columna de plantilla y selector de paso;
   el responsable usa la lista completa `personasRoles`.
4. Actualizar `ConsultaProcedimientoPasoModelTest`.
5. Agregar `SchemaMappingTest`.

## Fase 2 — Restaurar artefactos SQL

6. `clinica_ppi115_2026_08_20.sql`: revertir a `f779e37^`.
7. Borrar `clinica_migracion_2026_09_27.sql`.
8. BD local docker: `DROP COLUMN`/`DROP CONSTRAINT` si estaban aplicados.

## Fase 3 — Limpieza de i18n y docs

9. Quitar claves muertas en los 4 bundles.
10. Actualizar `README.md`.

## Fase 4 — Barrido de botones

11. Consulta (detalle, procedimientos, pasos).
12. Examen, ExamenTipoExamen, TipoExamen.
13. OrdenExamen.
14. ExamenResultado.

## Fase 5 — Verificación

15. `mvn clean test` y `mvn clean package`.
16. BD + Liberty, recorrido HTTP con Playwright, revisión de logs.
17. Commit y merge directo a `main`.
