-- Ocho pacientes adultos ficticios con contactos y dos consultas cada uno.
-- Requiere rubrica_completar.sql. No modifica avances existentes.
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
DO $$
DECLARE p record; dia integer; fecha timestamptz; consulta_id bigint; plantilla bigint;
BEGIN
  FOR p IN SELECT * FROM (VALUES
    (48,'Miguel Ángel','Castro Pérez','1988-02-14','07890123-4','miguel.castro@example.com','78123456',11,5001,11,15),
    (49,'Valeria Isabel','Gómez Reyes','1996-08-03','08901234-5','valeria.gomez@example.com','78234567',12,5002,9,15),
    (50,'Roberto Carlos','Méndez Díaz','1975-11-19','09012345-6','roberto.mendez@example.com','78345678',11,5004,12,16),
    (51,'Daniela Sofía','Vásquez Cruz','1999-06-27','10123456-7','daniela.vasquez@example.com','78456789',12,5006,11,16),
    (52,'Francisco Javier','Morales Rivas','1968-04-09','11234567-8','francisco.morales@example.com','78567890',11,5005,13,17),
    (53,'Patricia Elena','Aguilar Santos','1981-12-16','12345678-9','patricia.aguilar@example.com','78678901',12,5001,12,17),
    (54,'Andrés Felipe','Navarro Molina','1993-09-22','13456789-0','andres.navarro@example.com','78789012',11,5002,14,18),
    (55,'Camila Beatriz','Escobar Fuentes','2002-03-05','14567890-1','camila.escobar@example.com','78890123',12,5006,13,18)
  ) AS v(id,nombres,apellidos,nacimiento,dui,correo,telefono,clinica,procedimiento,hoy,manana)
  LOOP
    INSERT INTO persona (id_persona,nombres,apellidos,fecha_nacimiento)
    VALUES (pg_temp.id(p.id),p.nombres,p.apellidos,p.nacimiento::date)
    ON CONFLICT (id_persona) DO NOTHING;
    INSERT INTO documento (id_documento,id_persona,id_tipo_documento,valor)
    VALUES (pg_temp.id(9100+p.id),pg_temp.id(p.id),pg_temp.id(1),p.dui)
    ON CONFLICT (id_documento) DO NOTHING;
    INSERT INTO medio_contacto (id_medio_contacto,id_persona,id_tipo_medio_contacto,valor) VALUES
    (pg_temp.id(13000+p.id*2),pg_temp.id(p.id),pg_temp.id(11001),p.correo),
    (pg_temp.id(13001+p.id*2),pg_temp.id(p.id),pg_temp.id(11002),p.telefono)
    ON CONFLICT (id_medio_contacto) DO NOTHING;
    INSERT INTO persona_rol (id_persona_rol,id_persona,id_rol,id_clinica)
    VALUES (pg_temp.id(400+p.id),pg_temp.id(p.id),pg_temp.id(31),pg_temp.id(p.clinica))
    ON CONFLICT (id_persona_rol) DO NOTHING;
    FOR dia IN 0..1 LOOP
      consulta_id := 1100+p.id*2+dia;
      IF EXISTS (SELECT 1 FROM consulta WHERE id_consulta=pg_temp.id(consulta_id)) THEN CONTINUE; END IF;
      fecha := (current_date+dia)+make_time(CASE WHEN dia=0 THEN p.hoy ELSE p.manana END,0,0);
      plantilla := 6001+(p.procedimiento-5001)*3;
      INSERT INTO consulta (id_consulta,fecha_inicio,id_persona_rol,observaciones)
      VALUES (pg_temp.id(consulta_id),fecha,pg_temp.id(400+p.id),
        CASE WHEN dia=0 THEN 'Consulta inicial de presentación' ELSE 'Cita adicional de presentación' END);
      INSERT INTO consulta_procedimiento
      (id_consulta_procedimiento,id_consulta,id_procedimiento,fecha_inicio,observaciones)
      VALUES (pg_temp.id(18000+consulta_id),pg_temp.id(consulta_id),pg_temp.id(p.procedimiento),fecha,'Atención programada');
      INSERT INTO consulta_procedimiento_paso
      (id_consulta_procedimiento_paso,id_consulta_procedimiento,id_persona_rol,id_procedimiento_paso,fecha_inicio,estado)
      VALUES (pg_temp.id(20000+consulta_id),pg_temp.id(18000+consulta_id),
        pg_temp.id(CASE WHEN p.clinica=11 THEN 201 ELSE 301 END),pg_temp.id(plantilla),fecha,'PENDIENTE');
    END LOOP;
  END LOOP;
END $$;
COMMIT;
