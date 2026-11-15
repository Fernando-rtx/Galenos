-- PROPUESTA NO EJECUTADA. Altas académicas mínimas; las atenciones se harán por REST.
-- Requiere autorización B independiente, A aplicada y permiso de examen confirmado.
\set ON_ERROR_STOP on
\ir escenario.psql
BEGIN;
SET LOCAL lock_timeout = '3s';
SET LOCAL statement_timeout = '30s';
SELECT set_config('galenos.autorizacion_b', :'autorizacion_b', true);
-- Sin examen_id/paso_examen_id explícitos, psql falla ANTES de cualquier escritura.
SELECT set_config('galenos.examen_id', :'examen_id', true), set_config('galenos.paso_examen_id', :'paso_examen_id', true);
-- Ventana breve sin escrituras concurrentes sobre las altas y catálogos implicados.
LOCK TABLE public.rol,public.persona,public.documento,public.persona_rol,public.consulta,
  public.consulta_procedimiento,public.consulta_procedimiento_paso,public.procedimiento_paso_examen IN SHARE ROW EXCLUSIVE MODE;
LOCK TABLE public.clinica,public.tipo_documento,public.procedimiento,public.procedimiento_paso,
  public.procedimiento_paso_secuencia,public.examen IN SHARE MODE;
DO $revision$
BEGIN
  IF current_database() <> 'clinica' OR current_setting('galenos.autorizacion_b',true) <> 'AUTORIZAR_B_GT02_HU34_20261115' THEN RAISE EXCEPTION 'Falta autorización B'; END IF;
  IF (SELECT count(*) FROM pg_tables WHERE schemaname='public') <> 20 THEN RAISE EXCEPTION 'Revisar las20tablas'; END IF;
  IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_schema='public' AND table_name='orden_examen' AND column_name='id_examen' AND data_type='uuid' AND is_nullable='YES')
     OR NOT EXISTS (SELECT FROM pg_constraint WHERE conrelid='public.orden_examen'::regclass AND conname='fk_orden_examen_examen' AND confrelid='public.examen'::regclass AND convalidated) THEN RAISE EXCEPTION 'Operación A pendiente o incompatible'; END IF;
  IF NOT EXISTS (SELECT FROM public.clinica WHERE id_clinica='f7c682b3-a58b-486e-a17a-91caea43d67c' AND activo)
    OR NOT EXISTS (SELECT FROM public.procedimiento WHERE id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0' AND activo) THEN RAISE EXCEPTION 'Clínica/procedimiento no vigentes'; END IF;
  IF NOT EXISTS (SELECT FROM public.tipo_documento WHERE id_tipo_documento='9958521f-3234-48b0-8c87-7be2b1cd29de' AND activo AND expresion_regular='^[A-Za-z0-9]+$') THEN RAISE EXCEPTION 'Cambió el formato Pasaporte'; END IF;
  IF (SELECT count(*) FROM public.procedimiento_paso WHERE id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0') <> 3
    OR NOT EXISTS (SELECT FROM public.procedimiento_paso WHERE id_procedimiento_paso='40eae8a5-455b-437e-9494-061e3e3df408' AND id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0' AND id_rol='d0d5185e-2033-4b4f-a2e1-3379b3166caa')
    OR NOT EXISTS (SELECT FROM public.procedimiento_paso WHERE id_procedimiento_paso='b2b4a3ba-317c-4929-bb7b-cd948083d6d4' AND id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0' AND id_rol='1347c830-406b-4dde-a7a7-42ec246ea832')
    OR NOT EXISTS (SELECT FROM public.procedimiento_paso WHERE id_procedimiento_paso='5fec7282-c820-4c0b-b62b-1cdfe9390207' AND id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0' AND id_rol='a2d427c6-15f9-4247-b4b2-8a3d43712273' AND indica_fin)
    OR NOT EXISTS (SELECT FROM public.rol WHERE id_rol='a2d427c6-15f9-4247-b4b2-8a3d43712273' AND activo)
    OR (SELECT count(*) FROM public.procedimiento_paso_secuencia s JOIN public.procedimiento_paso p USING(id_procedimiento_paso) WHERE p.id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0') <> 2
    OR NOT EXISTS (SELECT FROM public.procedimiento_paso_secuencia WHERE id_procedimiento_paso='b2b4a3ba-317c-4929-bb7b-cd948083d6d4' AND id_procedimiento_paso_referencia='5fec7282-c820-4c0b-b62b-1cdfe9390207' AND tipo_secuencia='SIGUIENTE')
    OR EXISTS (SELECT FROM public.procedimiento_paso_secuencia WHERE id_procedimiento_paso_referencia='40eae8a5-455b-437e-9494-061e3e3df408')
    OR NOT EXISTS (SELECT FROM public.procedimiento_paso_secuencia WHERE id_procedimiento_paso='40eae8a5-455b-437e-9494-061e3e3df408' AND id_procedimiento_paso_referencia='b2b4a3ba-317c-4929-bb7b-cd948083d6d4' AND tipo_secuencia='SIGUIENTE') THEN RAISE EXCEPTION 'Cambió el grafo real'; END IF;
  IF NOT EXISTS (SELECT FROM public.rol WHERE id_rol='9ff36071-adfb-48f9-bf18-f1ab78295e50' AND activo)
    OR NOT EXISTS (SELECT FROM public.rol WHERE id_rol='d0d5185e-2033-4b4f-a2e1-3379b3166caa' AND activo)
    OR NOT EXISTS (SELECT FROM public.rol WHERE id_rol='1347c830-406b-4dde-a7a7-42ec246ea832' AND activo IS FALSE)
    OR EXISTS (SELECT FROM public.persona_rol WHERE id_rol='1347c830-406b-4dde-a7a7-42ec246ea832')
    OR EXISTS (SELECT FROM public.persona_rol WHERE id_clinica='f7c682b3-a58b-486e-a17a-91caea43d67c' AND id_rol='d0d5185e-2033-4b4f-a2e1-3379b3166caa') THEN RAISE EXCEPTION 'Cambió la disponibilidad/prioridad de responsables'; END IF;
  IF current_setting('galenos.paso_examen_id')::uuid <> 'b2b4a3ba-317c-4929-bb7b-cd948083d6d4'
    OR NOT EXISTS (SELECT FROM public.examen WHERE id_examen=current_setting('galenos.examen_id')::uuid AND activo)
    OR EXISTS (SELECT FROM public.procedimiento_paso_examen WHERE id_procedimiento_paso=current_setting('galenos.paso_examen_id')::uuid AND id_examen=current_setting('galenos.examen_id')::uuid) THEN RAISE EXCEPTION 'La relación examen/paso requiere confirmación o ya existe'; END IF;
  IF EXISTS (SELECT FROM public.persona WHERE id_persona IN ('573c9229-2b1d-4875-95a5-9fc1d2baa501','3a9a6d51-d1a3-466b-be5f-d205c7f23e46','a679f51c-c4b3-4b71-a07b-b300377350c2'))
    OR EXISTS (SELECT FROM public.documento WHERE valor IN ('ACADGT02PAC001','ACADGT02REC001','ACADGT02ENF001') OR id_documento IN ('efa98789-30be-4040-b82b-04477242c997','f690f16a-9bb9-4738-93a9-d849fdadccab','d5c376bd-4516-4942-a3c0-0dd788d76e3a'))
    OR EXISTS (SELECT FROM public.persona_rol WHERE id_persona_rol IN ('8a812ea0-5572-401c-b306-b263b0ea15f9','d32c3449-65ef-4922-8086-2b6d4af29d54','b5df9946-9c9a-412c-8d12-95930adc922e'))
    OR EXISTS (SELECT FROM public.consulta WHERE id_consulta='18d1c9f4-c2f5-452f-88d5-0c43d4110348' OR referencia_externa='GT02-HU34-20261115')
    OR EXISTS (SELECT FROM public.consulta_procedimiento WHERE id_consulta_procedimiento='126e4e9f-3bfb-4b20-8081-d6690b23ced1')
    OR EXISTS (SELECT FROM public.consulta_procedimiento_paso WHERE id_consulta_procedimiento_paso='6c3e2823-3133-4259-83b8-a8bdd0cd5044')
    OR EXISTS (SELECT FROM public.procedimiento_paso_examen WHERE id_procedimiento_paso_examen='04ff1984-a1bc-4886-8960-5b15bdf570f9') THEN RAISE EXCEPTION 'Colisión o preparación ya realizada; no sobrescribir'; END IF;
END;
$revision$;
-- Única modificación de un registro previo: habilitación EXPLÍCITA del rol Enfermería.
UPDATE public.rol SET activo=true WHERE id_rol=:'rol_enfermeria'::uuid AND activo IS FALSE;
INSERT INTO public.persona(id_persona,nombres,apellidos,fecha_nacimiento,fecha_creacion) VALUES
(:'paciente'::uuid,'Paciente Academico','Galenos','2000-01-01T06:00:00Z',transaction_timestamp()),
(:'recepcion'::uuid,'Recepcion Academica','Galenos','1990-01-01T06:00:00Z',transaction_timestamp()),
(:'enfermeria'::uuid,'Enfermeria Academica','Galenos','1990-01-01T06:00:00Z',transaction_timestamp());
INSERT INTO public.documento(id_documento,id_persona,id_tipo_documento,valor) VALUES
(:'documento_paciente'::uuid,:'paciente'::uuid,:'tipo_documento'::uuid,:'pasaporte_paciente'),
(:'documento_recepcion'::uuid,:'recepcion'::uuid,:'tipo_documento'::uuid,:'pasaporte_recepcion'),
(:'documento_enfermeria'::uuid,:'enfermeria'::uuid,:'tipo_documento'::uuid,:'pasaporte_enfermeria');
INSERT INTO public.persona_rol(id_persona_rol,id_persona,id_rol,id_clinica,fecha_creacion) VALUES
(:'asignacion_paciente'::uuid,:'paciente'::uuid,:'rol_paciente'::uuid,:'clinica'::uuid,transaction_timestamp()),
(:'asignacion_recepcion'::uuid,:'recepcion'::uuid,:'rol_recepcion'::uuid,:'clinica'::uuid,transaction_timestamp()),
(:'asignacion_enfermeria'::uuid,:'enfermeria'::uuid,:'rol_enfermeria'::uuid,:'clinica'::uuid,transaction_timestamp());
INSERT INTO public.procedimiento_paso_examen(id_procedimiento_paso_examen,id_procedimiento_paso,id_examen,activo,observaciones) VALUES
(:'permiso_examen'::uuid,:'paso_examen_id'::uuid,:'examen_id'::uuid,true,:'marca');
INSERT INTO public.consulta(id_consulta,id_persona_rol,fecha_inicio,referencia_externa,observaciones) VALUES
(:'consulta'::uuid,:'asignacion_paciente'::uuid,transaction_timestamp(),:'marca','Consulta académica autorizada; sin datos personales verdaderos');
INSERT INTO public.consulta_procedimiento(id_consulta_procedimiento,id_consulta,id_procedimiento,fecha_inicio,observaciones) VALUES
(:'asociacion'::uuid,:'consulta'::uuid,:'procedimiento'::uuid,transaction_timestamp(),:'marca');
-- Solo estado inicial; NO se inventa una atención completada ni un historial.
INSERT INTO public.consulta_procedimiento_paso(id_consulta_procedimiento_paso,id_consulta_procedimiento,id_persona_rol,id_procedimiento_paso,fecha_inicio,estado) VALUES
(:'ejecucion_inicial'::uuid,:'asociacion'::uuid,:'asignacion_recepcion'::uuid,:'plantilla_recepcion'::uuid,transaction_timestamp(),'PENDIENTE');
DO $revision$
BEGIN
  IF (SELECT count(*) FROM pg_tables WHERE schemaname='public') <> 20 THEN RAISE EXCEPTION 'Cambió el número de tablas'; END IF;
  IF (SELECT count(*) FROM public.persona WHERE id_persona IN ('573c9229-2b1d-4875-95a5-9fc1d2baa501','3a9a6d51-d1a3-466b-be5f-d205c7f23e46','a679f51c-c4b3-4b71-a07b-b300377350c2')) <> 3 THEN RAISE EXCEPTION 'Preparación incompleta'; END IF;
END;
$revision$;
COMMIT;
