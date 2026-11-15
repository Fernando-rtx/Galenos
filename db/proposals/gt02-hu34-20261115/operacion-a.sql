-- PROPUESTA NO EJECUTADA. Ejecutar solo tras autorización específica del usuario.
-- SQL directo para clinica; sin respaldos, copias ni parámetros de autorización.
BEGIN;
SET LOCAL lock_timeout = '3s';
SET LOCAL statement_timeout = '30s';
DO $revision$
BEGIN
  IF current_database() <> 'clinica' THEN
    RAISE EXCEPTION 'Esta operación corresponde exclusivamente a clinica';
  END IF;
  IF EXISTS (SELECT FROM information_schema.columns WHERE table_schema='public' AND table_name='orden_examen' AND column_name='id_examen') THEN
    RAISE EXCEPTION 'La columna ya existe: revisar antes de aplicar';
  END IF;
  IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_schema='public' AND table_name='examen' AND column_name='id_examen' AND data_type='uuid') THEN
    RAISE EXCEPTION 'examen.id_examen no tiene el tipo UUID esperado';
  END IF;
END;
$revision$;
-- Nullable y sin DEFAULT: las órdenes existentes conservan sus datos y quedan con NULL.
ALTER TABLE public.orden_examen ADD COLUMN id_examen uuid;
-- PostgreSQL comprueba la clave referenciada al crear la FK; si falla, revierte todo.
ALTER TABLE public.orden_examen ADD CONSTRAINT fk_orden_examen_examen
  FOREIGN KEY (id_examen) REFERENCES public.examen(id_examen) ON UPDATE CASCADE ON DELETE RESTRICT;
COMMIT;
