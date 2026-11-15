-- Solo lecturas. No ejecuta operaciones A/B ni prepara registros.
BEGIN READ ONLY;
SELECT current_database() AS base,(SELECT count(*) FROM pg_tables WHERE schemaname='public') AS tablas;
SELECT column_name,data_type,is_nullable,column_default FROM information_schema.columns
 WHERE table_schema='public' AND table_name='orden_examen' ORDER BY ordinal_position;
SELECT conname,convalidated,pg_get_constraintdef(oid) FROM pg_constraint
 WHERE conrelid IN ('public.orden_examen'::regclass,'public.examen'::regclass) ORDER BY conrelid,conname;
SELECT count(*) AS ordenes FROM public.orden_examen;
SELECT count(*) AS resultados FROM public.examen_resultado;
SELECT count(*) AS colisiones_persona FROM public.persona WHERE id_persona IN
 ('573c9229-2b1d-4875-95a5-9fc1d2baa501','3a9a6d51-d1a3-466b-be5f-d205c7f23e46','a679f51c-c4b3-4b71-a07b-b300377350c2');
SELECT count(*) AS colisiones_documento FROM public.documento WHERE valor IN
 ('ACADGT02PAC001','ACADGT02REC001','ACADGT02ENF001') OR id_documento IN
 ('efa98789-30be-4040-b82b-04477242c997','f690f16a-9bb9-4738-93a9-d849fdadccab','d5c376bd-4516-4942-a3c0-0dd788d76e3a');
SELECT count(*) AS colisiones_asignacion FROM public.persona_rol WHERE id_persona_rol IN
 ('8a812ea0-5572-401c-b306-b263b0ea15f9','d32c3449-65ef-4922-8086-2b6d4af29d54','b5df9946-9c9a-412c-8d12-95930adc922e');
SELECT count(*) AS colisiones_consulta FROM public.consulta
 WHERE id_consulta='18d1c9f4-c2f5-452f-88d5-0c43d4110348' OR referencia_externa='GT02-HU34-20261115';
SELECT count(*) AS colisiones_asociacion FROM public.consulta_procedimiento WHERE id_consulta_procedimiento='126e4e9f-3bfb-4b20-8081-d6690b23ced1';
SELECT count(*) AS colisiones_ejecucion FROM public.consulta_procedimiento_paso WHERE id_consulta_procedimiento_paso='6c3e2823-3133-4259-83b8-a8bdd0cd5044';
SELECT count(*) AS colisiones_permiso FROM public.procedimiento_paso_examen WHERE id_procedimiento_paso_examen='04ff1984-a1bc-4886-8960-5b15bdf570f9';
SELECT id_rol,nombre,activo,(SELECT count(*) FROM public.persona_rol pr WHERE pr.id_rol=r.id_rol) AS asignaciones
 FROM public.rol r WHERE id_rol IN ('d0d5185e-2033-4b4f-a2e1-3379b3166caa','1347c830-406b-4dde-a7a7-42ec246ea832');
SELECT s.id_procedimiento_paso,s.id_procedimiento_paso_referencia,s.tipo_secuencia
 FROM public.procedimiento_paso_secuencia s JOIN public.procedimiento_paso p USING(id_procedimiento_paso)
 WHERE p.id_procedimiento='0957a74a-a727-49b8-9e2a-3f34f304c0b0';
COMMIT;
