-- Complemento de rubrica_completar.sql: citas de ambos pacientes hoy en Santa Ana.
BEGIN;
SET LOCAL TIME ZONE 'America/El_Salvador';
DO $$ BEGIN
  IF current_database() NOT LIKE 'galenos_rest_test_gt02_presentacion_%' THEN
    RAISE EXCEPTION 'Sólo se permiten bases de presentación GT02';
  END IF;
END $$;
CREATE OR REPLACE FUNCTION pg_temp.id(bigint) RETURNS uuid LANGUAGE SQL IMMUTABLE AS $$
  SELECT ('00000000-0000-4000-8000-' || lpad($1::text,12,'0'))::uuid
$$;
INSERT INTO consulta (id_consulta,fecha_inicio,id_persona_rol,observaciones) VALUES
(pg_temp.id(1051),current_date+time '15:00',pg_temp.id(304),'Evaluación de salud de Ana en Santa Ana'),
(pg_temp.id(1052),current_date+time '16:00',pg_temp.id(305),'Control preventivo de Pedro en Santa Ana')
ON CONFLICT (id_consulta) DO NOTHING;
INSERT INTO consulta_procedimiento
(id_consulta_procedimiento,id_consulta,id_procedimiento,fecha_inicio,observaciones) VALUES
(pg_temp.id(18051),pg_temp.id(1051),pg_temp.id(5002),current_date+time '15:00','Evaluación de salud'),
(pg_temp.id(18052),pg_temp.id(1052),pg_temp.id(5006),current_date+time '16:00','Control preventivo de salud')
ON CONFLICT (id_consulta_procedimiento) DO NOTHING;
INSERT INTO consulta_procedimiento_paso
(id_consulta_procedimiento_paso,id_consulta_procedimiento,id_persona_rol,id_procedimiento_paso,fecha_inicio,estado) VALUES
(pg_temp.id(19501),pg_temp.id(18051),pg_temp.id(301),pg_temp.id(6004),current_date+time '15:00','PENDIENTE'),
(pg_temp.id(19502),pg_temp.id(18052),pg_temp.id(301),pg_temp.id(6016),current_date+time '16:00','PENDIENTE')
ON CONFLICT (id_consulta_procedimiento_paso) DO NOTHING;
COMMIT;
