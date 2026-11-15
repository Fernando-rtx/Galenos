-- PROPUESTA NO EJECUTADA. Requiere autorización de estas nueve altas.
-- Alternativa a B: no activa Enfermera ni modifica ningún registro anterior.
-- No ejecutar operacion-b.sql junto con este archivo: comparten identificadores.
-- Esta preparación no resuelve la referencia OrdenExamen -> Examen pendiente.
\set ON_ERROR_STOP on
\ir escenario.psql
BEGIN;
SET LOCAL lock_timeout = '3s';
SET LOCAL statement_timeout = '30s';
LOCK TABLE public.persona, public.documento, public.persona_rol, public.consulta,
  public.consulta_procedimiento, public.consulta_procedimiento_paso
  IN SHARE ROW EXCLUSIVE MODE;
LOCK TABLE public.clinica, public.tipo_documento, public.rol, public.procedimiento,
  public.procedimiento_paso, public.procedimiento_paso_secuencia IN SHARE MODE;
DO $revision$
BEGIN
  IF current_database() <> 'clinica' THEN
    RAISE EXCEPTION 'La base debe ser clinica';
  END IF;
  IF NOT EXISTS (SELECT FROM public.clinica
      WHERE id_clinica='f7c682b3-a58b-486e-a17a-91caea43d67c' AND activo)
    OR NOT EXISTS (SELECT FROM public.procedimiento
      WHERE id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0' AND activo)
    OR NOT EXISTS (SELECT FROM public.tipo_documento
      WHERE id_tipo_documento='9958521f-3234-48b0-8c87-7be2b1cd29de'
        AND activo AND expresion_regular='^[A-Za-z0-9]+$')
    OR (SELECT count(*) FROM public.rol WHERE activo AND id_rol IN
      ('9ff36071-adfb-48f9-bf18-f1ab78295e50','d0d5185e-2033-4b4f-a2e1-3379b3166caa')) <> 2 THEN
    RAISE EXCEPTION 'Clínica, procedimiento, documento o roles no vigentes';
  END IF;
  IF NOT EXISTS (SELECT FROM public.procedimiento_paso
      WHERE id_procedimiento_paso='40eae8a5-455b-437e-9494-061e3e3df408'
        AND id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0'
        AND id_rol='d0d5185e-2033-4b4f-a2e1-3379b3166caa' AND indica_fin IS FALSE)
    OR (SELECT count(*) FROM public.procedimiento_paso p
      WHERE p.id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0'
        AND NOT EXISTS (SELECT FROM public.procedimiento_paso_secuencia s
          WHERE s.id_procedimiento_paso_referencia=p.id_procedimiento_paso)) <> 1
    OR EXISTS (SELECT FROM public.procedimiento_paso_secuencia
      WHERE id_procedimiento_paso_referencia='40eae8a5-455b-437e-9494-061e3e3df408') THEN
    RAISE EXCEPTION 'La plantilla ya no es la raíz única de recepción';
  END IF;
  -- No desplazar a un responsable existente según la regla vigente del Service.
  IF EXISTS (SELECT FROM public.persona_rol
      WHERE id_clinica='f7c682b3-a58b-486e-a17a-91caea43d67c'
        AND id_rol='d0d5185e-2033-4b4f-a2e1-3379b3166caa') THEN
    RAISE EXCEPTION 'Recepción ya tiene responsable; revisar antes de preparar';
  END IF;
  IF EXISTS (SELECT FROM public.persona WHERE id_persona IN
      ('573c9229-2b1d-4875-95a5-9fc1d2baa501','3a9a6d51-d1a3-466b-be5f-d205c7f23e46'))
    OR EXISTS (SELECT FROM public.documento WHERE valor IN ('ACADGT02PAC001','ACADGT02REC001')
      OR id_documento IN ('efa98789-30be-4040-b82b-04477242c997','f690f16a-9bb9-4738-93a9-d849fdadccab'))
    OR EXISTS (SELECT FROM public.persona_rol WHERE id_persona_rol IN
      ('8a812ea0-5572-401c-b306-b263b0ea15f9','d32c3449-65ef-4922-8086-2b6d4af29d54'))
    OR EXISTS (SELECT FROM public.consulta WHERE id_consulta='18d1c9f4-c2f5-452f-88d5-0c43d4110348'
      OR referencia_externa='GT02-HU34-20261115')
    OR EXISTS (SELECT FROM public.consulta_procedimiento
      WHERE id_consulta_procedimiento='126e4e9f-3bfb-4b20-8081-d6690b23ced1')
    OR EXISTS (SELECT FROM public.consulta_procedimiento_paso
      WHERE id_consulta_procedimiento_paso='6c3e2823-3133-4259-83b8-a8bdd0cd5044') THEN
    RAISE EXCEPTION 'Identificadores/documentos ocupados; no sobrescribir ni repetir';
  END IF;
END;
$revision$;
INSERT INTO public.persona(id_persona,nombres,apellidos,fecha_nacimiento,fecha_creacion) VALUES
(:'paciente'::uuid,'Paciente Academico','Galenos','2000-01-01T06:00:00Z',transaction_timestamp()),
(:'recepcion'::uuid,'Recepcion Academica','Galenos','1990-01-01T06:00:00Z',transaction_timestamp());
INSERT INTO public.documento(id_documento,id_persona,id_tipo_documento,valor) VALUES
(:'documento_paciente'::uuid,:'paciente'::uuid,:'tipo_documento'::uuid,:'pasaporte_paciente'),
(:'documento_recepcion'::uuid,:'recepcion'::uuid,:'tipo_documento'::uuid,:'pasaporte_recepcion');
INSERT INTO public.persona_rol(id_persona_rol,id_persona,id_rol,id_clinica,fecha_creacion) VALUES
(:'asignacion_paciente'::uuid,:'paciente'::uuid,:'rol_paciente'::uuid,:'clinica'::uuid,transaction_timestamp()),
(:'asignacion_recepcion'::uuid,:'recepcion'::uuid,:'rol_recepcion'::uuid,:'clinica'::uuid,transaction_timestamp());
INSERT INTO public.consulta(id_consulta,id_persona_rol,fecha_inicio,referencia_externa,observaciones) VALUES
(:'consulta'::uuid,:'asignacion_paciente'::uuid,transaction_timestamp(),:'marca','Preparación académica autorizada');
INSERT INTO public.consulta_procedimiento(id_consulta_procedimiento,id_consulta,id_procedimiento,fecha_inicio,observaciones) VALUES
(:'asociacion'::uuid,:'consulta'::uuid,:'procedimiento'::uuid,transaction_timestamp(),:'marca');
-- Misma raíz y estado inicial usados por ConsultaFlujoService.crearProcedimiento.
-- No se simula una atención ni se fabrican pasos completados o responsables previos.
INSERT INTO public.consulta_procedimiento_paso(id_consulta_procedimiento_paso,id_consulta_procedimiento,id_persona_rol,id_procedimiento_paso,fecha_inicio,estado) VALUES
(:'ejecucion_inicial'::uuid,:'asociacion'::uuid,:'asignacion_recepcion'::uuid,:'plantilla_recepcion'::uuid,transaction_timestamp(),'PENDIENTE');
COMMIT;
