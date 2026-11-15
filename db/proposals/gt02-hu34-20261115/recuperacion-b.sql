-- PROPUESTA NO EJECUTADA. Reversión EXCLUSIVA del escenario, con autorización propia.
-- Cerrar primero sus sesiones y detener actividad concurrente. No revierte A.
\set ON_ERROR_STOP on
\ir escenario.psql
BEGIN;
SET LOCAL lock_timeout = '3s';
SET LOCAL statement_timeout = '30s';
SELECT set_config('galenos.revertir_b', :'autorizacion_recuperacion_b', true);
SELECT set_config('galenos.recuperar_examen', :'examen_id', true);
LOCK TABLE public.rol,public.persona,public.documento,public.persona_rol,public.consulta,
  public.consulta_procedimiento,public.consulta_procedimiento_paso,public.orden_examen,
  public.examen_resultado,public.procedimiento_paso_examen IN SHARE ROW EXCLUSIVE MODE;
DO $revision$
BEGIN
  IF current_database()<>'clinica' OR current_setting('galenos.revertir_b',true)<>'REVERTIR_B_GT02_HU34_20261115' THEN RAISE EXCEPTION 'Falta autorización específica de reversión B'; END IF;
  IF NOT EXISTS (SELECT FROM public.procedimiento_paso_examen WHERE id_procedimiento_paso_examen='04ff1984-a1bc-4886-8960-5b15bdf570f9'
    AND observaciones='GT02-HU34-20261115' AND activo AND id_procedimiento_paso='b2b4a3ba-317c-4929-bb7b-cd948083d6d4'
    AND id_examen=current_setting('galenos.recuperar_examen')::uuid) THEN RAISE EXCEPTION 'Cambió el permiso creado: revisar antes de retirarlo'; END IF;
  IF (SELECT count(*) FROM public.documento WHERE id_tipo_documento='9958521f-3234-48b0-8c87-7be2b1cd29de' AND (id_documento,id_persona,valor) IN (
    ('efa98789-30be-4040-b82b-04477242c997'::uuid,'573c9229-2b1d-4875-95a5-9fc1d2baa501'::uuid,'ACADGT02PAC001'),
    ('f690f16a-9bb9-4738-93a9-d849fdadccab'::uuid,'3a9a6d51-d1a3-466b-be5f-d205c7f23e46'::uuid,'ACADGT02REC001'),
    ('d5c376bd-4516-4942-a3c0-0dd788d76e3a'::uuid,'a679f51c-c4b3-4b71-a07b-b300377350c2'::uuid,'ACADGT02ENF001'))) <> 3 THEN RAISE EXCEPTION 'Cambió la identidad documental del escenario'; END IF;
  IF NOT EXISTS (SELECT FROM public.consulta WHERE id_consulta='18d1c9f4-c2f5-452f-88d5-0c43d4110348' AND referencia_externa='GT02-HU34-20261115' AND id_persona_rol='8a812ea0-5572-401c-b306-b263b0ea15f9')
    OR NOT EXISTS (SELECT FROM public.consulta_procedimiento WHERE id_consulta_procedimiento='126e4e9f-3bfb-4b20-8081-d6690b23ced1' AND id_consulta='18d1c9f4-c2f5-452f-88d5-0c43d4110348' AND observaciones='GT02-HU34-20261115') THEN RAISE EXCEPTION 'No coincide la identidad del escenario; revisar, no borrar'; END IF;
  IF EXISTS (SELECT FROM public.consulta_procedimiento_paso WHERE id_consulta_procedimiento='126e4e9f-3bfb-4b20-8081-d6690b23ced1'
    AND (id_persona_rol IS NULL OR id_persona_rol NOT IN ('d32c3449-65ef-4922-8086-2b6d4af29d54','b5df9946-9c9a-412c-8d12-95930adc922e')
      OR id_procedimiento_paso IS NULL OR id_procedimiento_paso NOT IN ('40eae8a5-455b-437e-9494-061e3e3df408','b2b4a3ba-317c-4929-bb7b-cd948083d6d4'))) THEN RAISE EXCEPTION 'El caso avanzó fuera del recorrido aprobado; revisar antes de limpiar'; END IF;
  IF EXISTS (SELECT FROM public.consulta_procedimiento WHERE id_consulta='18d1c9f4-c2f5-452f-88d5-0c43d4110348' AND id_consulta_procedimiento<>'126e4e9f-3bfb-4b20-8081-d6690b23ced1')
    OR EXISTS (SELECT FROM public.consulta WHERE id_persona_rol IN ('8a812ea0-5572-401c-b306-b263b0ea15f9','d32c3449-65ef-4922-8086-2b6d4af29d54','b5df9946-9c9a-412c-8d12-95930adc922e') AND id_consulta<>'18d1c9f4-c2f5-452f-88d5-0c43d4110348')
    OR EXISTS (SELECT FROM public.consulta_procedimiento_paso WHERE id_persona_rol IN ('8a812ea0-5572-401c-b306-b263b0ea15f9','d32c3449-65ef-4922-8086-2b6d4af29d54','b5df9946-9c9a-412c-8d12-95930adc922e') AND id_consulta_procedimiento IS DISTINCT FROM '126e4e9f-3bfb-4b20-8081-d6690b23ced1'::uuid)
    OR EXISTS (SELECT FROM public.persona_rol WHERE id_rol='1347c830-406b-4dde-a7a7-42ec246ea832' AND id_persona_rol<>'b5df9946-9c9a-412c-8d12-95930adc922e') THEN RAISE EXCEPTION 'Hay actividad/asignaciones externas: no revertir automáticamente'; END IF;
  IF EXISTS (SELECT FROM public.documento WHERE id_persona IN ('573c9229-2b1d-4875-95a5-9fc1d2baa501','3a9a6d51-d1a3-466b-be5f-d205c7f23e46','a679f51c-c4b3-4b71-a07b-b300377350c2') AND id_documento NOT IN ('efa98789-30be-4040-b82b-04477242c997','f690f16a-9bb9-4738-93a9-d849fdadccab','d5c376bd-4516-4942-a3c0-0dd788d76e3a'))
    OR EXISTS (SELECT FROM public.medio_contacto WHERE id_persona IN ('573c9229-2b1d-4875-95a5-9fc1d2baa501','3a9a6d51-d1a3-466b-be5f-d205c7f23e46','a679f51c-c4b3-4b71-a07b-b300377350c2'))
    OR EXISTS (SELECT FROM public.persona_rol WHERE id_persona IN ('573c9229-2b1d-4875-95a5-9fc1d2baa501','3a9a6d51-d1a3-466b-be5f-d205c7f23e46','a679f51c-c4b3-4b71-a07b-b300377350c2') AND id_persona_rol NOT IN ('8a812ea0-5572-401c-b306-b263b0ea15f9','d32c3449-65ef-4922-8086-2b6d4af29d54','b5df9946-9c9a-412c-8d12-95930adc922e')) THEN RAISE EXCEPTION 'Las personas tienen datos ajenos al plan'; END IF;
  IF EXISTS (SELECT FROM public.orden_examen o JOIN public.consulta_procedimiento_paso p USING(id_consulta_procedimiento_paso)
    JOIN public.procedimiento_paso_examen permiso ON permiso.id_procedimiento_paso_examen='04ff1984-a1bc-4886-8960-5b15bdf570f9'
    WHERE o.id_examen=permiso.id_examen AND p.id_procedimiento_paso=permiso.id_procedimiento_paso
      AND p.id_consulta_procedimiento<>'126e4e9f-3bfb-4b20-8081-d6690b23ced1') THEN RAISE EXCEPTION 'El permiso fue utilizado fuera del escenario'; END IF;
END;
$revision$;
-- Todas las eliminaciones están acotadas por los UUID del escenario; sin CASCADE.
DELETE FROM public.examen_resultado WHERE id_orden_examen IN
 (SELECT o.id_orden_examen FROM public.orden_examen o JOIN public.consulta_procedimiento_paso p USING(id_consulta_procedimiento_paso) WHERE p.id_consulta_procedimiento=:'asociacion'::uuid);
DELETE FROM public.orden_examen WHERE id_consulta_procedimiento_paso IN
 (SELECT id_consulta_procedimiento_paso FROM public.consulta_procedimiento_paso WHERE id_consulta_procedimiento=:'asociacion'::uuid);
DELETE FROM public.consulta_procedimiento_paso WHERE id_consulta_procedimiento=:'asociacion'::uuid;
DELETE FROM public.consulta_procedimiento WHERE id_consulta_procedimiento=:'asociacion'::uuid AND id_consulta=:'consulta'::uuid;
DELETE FROM public.consulta WHERE id_consulta=:'consulta'::uuid AND referencia_externa=:'marca' AND id_persona_rol=:'asignacion_paciente'::uuid;
DELETE FROM public.procedimiento_paso_examen WHERE id_procedimiento_paso_examen=:'permiso_examen'::uuid AND observaciones=:'marca';
DELETE FROM public.persona_rol WHERE id_persona_rol IN (:'asignacion_paciente'::uuid,:'asignacion_recepcion'::uuid,:'asignacion_enfermeria'::uuid)
 AND id_persona IN (:'paciente'::uuid,:'recepcion'::uuid,:'enfermeria'::uuid) AND id_clinica=:'clinica'::uuid;
DELETE FROM public.documento WHERE id_documento IN (:'documento_paciente'::uuid,:'documento_recepcion'::uuid,:'documento_enfermeria'::uuid)
 AND id_persona IN (:'paciente'::uuid,:'recepcion'::uuid,:'enfermeria'::uuid)
 AND valor IN (:'pasaporte_paciente',:'pasaporte_recepcion',:'pasaporte_enfermeria');
DELETE FROM public.persona WHERE id_persona IN (:'paciente'::uuid,:'recepcion'::uuid,:'enfermeria'::uuid) AND apellidos='Galenos';
-- El plan original registra activo=false; los guardas impiden cerrar el rol si otros lo utilizan.
UPDATE public.rol SET activo=false WHERE id_rol=:'rol_enfermeria'::uuid AND activo IS TRUE;
DO $revision$
BEGIN
  IF EXISTS (SELECT FROM public.persona WHERE id_persona IN ('573c9229-2b1d-4875-95a5-9fc1d2baa501','3a9a6d51-d1a3-466b-be5f-d205c7f23e46','a679f51c-c4b3-4b71-a07b-b300377350c2'))
    OR EXISTS (SELECT FROM public.consulta WHERE id_consulta='18d1c9f4-c2f5-452f-88d5-0c43d4110348')
    OR EXISTS (SELECT FROM public.procedimiento_paso_examen WHERE id_procedimiento_paso_examen='04ff1984-a1bc-4886-8960-5b15bdf570f9') THEN RAISE EXCEPTION 'Reversión incompleta: se revierte toda la limpieza'; END IF;
END;
$revision$;
COMMIT;
