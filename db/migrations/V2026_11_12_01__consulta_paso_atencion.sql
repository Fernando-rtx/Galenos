-- Required by HU3/HU4: link an execution to its template step and retain notes.
-- Safe for existing records: both columns are nullable; no history is inferred.
ALTER TABLE public.consulta_procedimiento_paso
    ADD COLUMN IF NOT EXISTS id_procedimiento_paso uuid;
ALTER TABLE public.consulta_procedimiento_paso
    ADD COLUMN IF NOT EXISTS valor text;
ALTER TABLE public.orden_examen
    ADD COLUMN IF NOT EXISTS id_examen uuid;
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_consulta_procedimiento_paso_procedimiento_paso'
          AND conrelid = 'public.consulta_procedimiento_paso'::regclass
    ) THEN
        ALTER TABLE public.consulta_procedimiento_paso
            ADD CONSTRAINT fk_consulta_procedimiento_paso_procedimiento_paso
            FOREIGN KEY (id_procedimiento_paso)
            REFERENCES public.procedimiento_paso(id_procedimiento_paso)
            ON UPDATE CASCADE ON DELETE RESTRICT NOT VALID;
    END IF;
END $$;
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_orden_examen_examen'
          AND conrelid = 'public.orden_examen'::regclass
    ) THEN
        ALTER TABLE public.orden_examen
            ADD CONSTRAINT fk_orden_examen_examen
            FOREIGN KEY (id_examen) REFERENCES public.examen(id_examen)
            ON UPDATE CASCADE ON DELETE RESTRICT NOT VALID;
    END IF;
END $$;
