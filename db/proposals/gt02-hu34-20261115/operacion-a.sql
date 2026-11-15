-- PROPUESTA NO EJECUTADA. Solo después de autorización específica A y respaldo verificado.
-- Requiere psql -v autorizacion_a='AUTORIZAR_A_GT02_HU34_20261115'.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL lock_timeout = '3s';
SET LOCAL statement_timeout = '30s';
SELECT set_config('galenos.autorizacion_a', :'autorizacion_a', true);
LOCK TABLE public.orden_examen IN ACCESS EXCLUSIVE MODE;
DO $revision$
BEGIN
  IF current_database() <> 'clinica' OR current_setting('galenos.autorizacion_a',true) <> 'AUTORIZAR_A_GT02_HU34_20261115' THEN
    RAISE EXCEPTION 'Operación A sin confirmación específica o base incorrecta';
  END IF;
  IF (SELECT count(*) FROM pg_tables WHERE schemaname='public') <> 20 THEN RAISE EXCEPTION 'Revisar las20tablas'; END IF;
  IF EXISTS (SELECT FROM information_schema.columns WHERE table_schema='public' AND table_name='orden_examen' AND column_name='id_examen') THEN
    RAISE EXCEPTION 'La columna ya existe: revisar antes de aplicar';
  END IF;
  IF NOT EXISTS (SELECT FROM pg_attribute WHERE attrelid='public.examen'::regclass AND attname='id_examen' AND atttypid='uuid'::regtype AND attnotnull)
     OR NOT EXISTS (SELECT FROM pg_constraint WHERE conrelid='public.examen'::regclass AND contype='p' AND conkey=ARRAY[(SELECT attnum FROM pg_attribute WHERE attrelid='public.examen'::regclass AND attname='id_examen')]) THEN
    RAISE EXCEPTION 'Examen no tiene la PK UUID verificada';
  END IF;
  IF (SELECT count(*) FROM public.orden_examen) <> 1 OR (SELECT count(*) FROM public.examen_resultado) <> 1 THEN
    RAISE EXCEPTION 'Cambió el inventario de órdenes/resultados; revisar respaldo y plan';
  END IF;
  PERFORM set_config('galenos.a_ordenes_antes',(SELECT md5(string_agg(row_to_json(x)::text,'' ORDER BY id_orden_examen)) FROM
    (SELECT id_orden_examen,id_consulta_procedimiento_paso,fecha_creacion,indicaciones FROM public.orden_examen) x),true);
END;
$revision$;
ALTER TABLE public.orden_examen ADD COLUMN id_examen uuid;
ALTER TABLE public.orden_examen ADD CONSTRAINT fk_orden_examen_examen
  FOREIGN KEY (id_examen) REFERENCES public.examen(id_examen) ON UPDATE CASCADE ON DELETE RESTRICT;
DO $revision$
BEGIN
  IF current_setting('galenos.a_ordenes_antes') IS DISTINCT FROM
      (SELECT md5(string_agg(row_to_json(x)::text,'' ORDER BY id_orden_examen)) FROM
       (SELECT id_orden_examen,id_consulta_procedimiento_paso,fecha_creacion,indicaciones FROM public.orden_examen) x)
      OR EXISTS (SELECT FROM public.orden_examen WHERE id_examen IS NOT NULL) THEN
    RAISE EXCEPTION 'No se preservaron exactamente las columnas anteriores';
  END IF;
END;
$revision$;
COMMIT;
