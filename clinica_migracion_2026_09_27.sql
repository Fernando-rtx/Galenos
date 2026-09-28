-- Migración 2026-09-27: enlaza la ejecución de un paso con su plantilla procedimiento_paso.
BEGIN;

ALTER TABLE public.consulta_procedimiento_paso
    ADD COLUMN IF NOT EXISTS id_procedimiento_paso uuid;

ALTER TABLE public.consulta_procedimiento_paso
    DROP CONSTRAINT IF EXISTS fk_consulta_procedimiento_paso_procedimiento_paso;

ALTER TABLE public.consulta_procedimiento_paso
    ADD CONSTRAINT fk_consulta_procedimiento_paso_procedimiento_paso
    FOREIGN KEY (id_procedimiento_paso) REFERENCES public.procedimiento_paso(id_procedimiento_paso)
    ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS fki_fk_cpp_paso ON public.consulta_procedimiento_paso (id_procedimiento_paso);

COMMIT;
