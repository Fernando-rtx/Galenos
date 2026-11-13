-- Paciente ficticia adicional; aplicar después de rubrica_completar.sql.
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
INSERT INTO persona (id_persona,nombres,apellidos,fecha_nacimiento)
VALUES (pg_temp.id(47),'Lucía Gabriela','Flores Sánchez','1997-05-21')
ON CONFLICT (id_persona) DO NOTHING;
INSERT INTO documento (id_documento,id_persona,id_tipo_documento,valor) VALUES
(pg_temp.id(9008),pg_temp.id(47),pg_temp.id(1),'06789012-3'),
(pg_temp.id(9009),pg_temp.id(47),pg_temp.id(2),'B23456789')
ON CONFLICT (id_documento) DO NOTHING;
INSERT INTO medio_contacto (id_medio_contacto,id_persona,id_tipo_medio_contacto,valor) VALUES
(pg_temp.id(12011),pg_temp.id(47),pg_temp.id(11001),'lucia.flores@example.com'),
(pg_temp.id(12012),pg_temp.id(47),pg_temp.id(11002),'76789012')
ON CONFLICT (id_medio_contacto) DO NOTHING;
INSERT INTO persona_rol (id_persona_rol,id_persona,id_rol,id_clinica)
VALUES (pg_temp.id(107),pg_temp.id(47),pg_temp.id(31),pg_temp.id(11))
ON CONFLICT (id_persona_rol) DO NOTHING;
INSERT INTO consulta (id_consulta,fecha_inicio,id_persona_rol,observaciones)
VALUES (pg_temp.id(1053),current_date+time '17:00',pg_temp.id(107),'Consulta de medicina general de Lucía Flores')
ON CONFLICT (id_consulta) DO NOTHING;
INSERT INTO consulta_procedimiento
(id_consulta_procedimiento,id_consulta,id_procedimiento,fecha_inicio,observaciones)
VALUES (pg_temp.id(18053),pg_temp.id(1053),pg_temp.id(5001),current_date+time '17:00','Consulta médica general')
ON CONFLICT (id_consulta_procedimiento) DO NOTHING;
INSERT INTO consulta_procedimiento_paso
(id_consulta_procedimiento_paso,id_consulta_procedimiento,id_persona_rol,id_procedimiento_paso,fecha_inicio,estado)
VALUES (pg_temp.id(19503),pg_temp.id(18053),pg_temp.id(201),pg_temp.id(6001),current_date+time '17:00','PENDIENTE')
ON CONFLICT (id_consulta_procedimiento_paso) DO NOTHING;
COMMIT;
