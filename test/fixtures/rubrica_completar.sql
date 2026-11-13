-- Complemento de rubrica_presentacion.sql: todas las personas con rol y casos clínicos de demostración.
-- Se aplica sin borrar consultas existentes. Los datos son ficticios.
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

-- Pedro también es paciente. Ambas sedes activas tienen responsables de cada rol.
INSERT INTO persona_rol (id_persona_rol,id_persona,id_rol,id_clinica) VALUES
(pg_temp.id(106),pg_temp.id(44),pg_temp.id(31),pg_temp.id(11)),
(pg_temp.id(301),pg_temp.id(45),pg_temp.id(32),pg_temp.id(12)),
(pg_temp.id(302),pg_temp.id(42),pg_temp.id(34),pg_temp.id(12)),
(pg_temp.id(303),pg_temp.id(46),pg_temp.id(33),pg_temp.id(12)),
(pg_temp.id(304),pg_temp.id(41),pg_temp.id(31),pg_temp.id(12)),
(pg_temp.id(305),pg_temp.id(44),pg_temp.id(31),pg_temp.id(12))
ON CONFLICT (id_persona_rol) DO NOTHING;

-- Casos de consulta externa para adultos; la plantilla pediátrica no se asigna a adultos.
-- Se conservan los avances si el complemento ya se había aplicado.
DO $$
DECLARE c record; j integer; paso bigint; responsable bigint; inicio timestamptz;
BEGIN
  FOR c IN SELECT * FROM (VALUES
    (1,5001,106,0,2,false,11,'Consulta general en enfermería'),
    (2,5002,101,0,3,false,11,'Evaluación de salud en revisión médica'),
    (3,5004,106,-1,3,true,11,'Control de hipertensión finalizado'),
    (4,5005,101,-1,3,true,11,'Control de diabetes finalizado'),
    (5,5006,106,1,1,false,11,'Consulta preventiva programada'),
    (6,5002,304,1,1,false,12,'Evaluación de salud programada en Santa Ana'),
    (7,5001,106,-2,3,true,11,'Consulta general con resultados disponibles')
  ) AS v(n,procedimiento,paciente,dias,paso_actual,finalizada,clinica,motivo)
  LOOP
    IF EXISTS (SELECT 1 FROM consulta WHERE id_consulta=pg_temp.id(1010+c.n)) THEN
      CONTINUE;
    END IF;
    inicio := (current_date+c.dias)+time '10:00';
    INSERT INTO consulta (id_consulta,fecha_inicio,fecha_fin,id_persona_rol,observaciones)
    VALUES (pg_temp.id(1010+c.n),inicio,CASE WHEN c.finalizada THEN inicio+interval '1 hour' END,
      pg_temp.id(c.paciente),c.motivo);
    INSERT INTO consulta_procedimiento
      (id_consulta_procedimiento,id_consulta,id_procedimiento,fecha_inicio,fecha_fin,observaciones)
    VALUES (pg_temp.id(18010+c.n),pg_temp.id(1010+c.n),pg_temp.id(c.procedimiento),inicio,
      CASE WHEN c.finalizada THEN inicio+interval '1 hour' END,c.motivo);
    FOR j IN 1..c.paso_actual LOOP
      paso := 6000+(c.procedimiento-5001)*3+j;
      responsable := CASE WHEN c.clinica=12 THEN 300+j
        WHEN j=1 THEN 201 WHEN j=2 THEN 205 ELSE 202 END;
      INSERT INTO consulta_procedimiento_paso
        (id_consulta_procedimiento_paso,id_consulta_procedimiento,id_persona_rol,
         id_procedimiento_paso,fecha_inicio,fecha_fin,estado,valor)
      VALUES (pg_temp.id(19100+c.n*10+j),pg_temp.id(18010+c.n),pg_temp.id(responsable),
        pg_temp.id(paso),inicio+(j-1)*interval '20 minutes',
        CASE WHEN j<c.paso_actual OR c.finalizada THEN inicio+j*interval '20 minutes' END,
        CASE WHEN j<c.paso_actual OR c.finalizada THEN 'COMPLETADO'
          WHEN j=2 THEN 'EN_CURSO' ELSE 'PENDIENTE' END,
        CASE WHEN j=1 AND (j<c.paso_actual OR c.finalizada)
          THEN 'Identificación y datos de contacto verificados. Ingreso registrado.'
          WHEN j=2 THEN 'Signos vitales registrados en el caso de demostración. Paciente preparado para evaluación.'
          WHEN j=3 AND c.finalizada THEN 'Evaluación registrada. Seguimiento documentado y consulta cerrada.' END);
    END LOOP;
  END LOOP;
END $$;

INSERT INTO orden_examen
  (id_orden_examen,id_consulta_procedimiento_paso,id_examen,fecha_creacion,indicaciones) VALUES
(pg_temp.id(20011),pg_temp.id(19112),pg_temp.id(3101),current_date+time '10:25','Hemograma solicitado para el caso de demostración'),
(pg_temp.id(20012),pg_temp.id(19123),pg_temp.id(3101),current_date+time '10:45','Orden pendiente de resultado para evaluación de salud'),
(pg_temp.id(20013),pg_temp.id(19172),pg_temp.id(3101),current_date-2+time '10:25','Hemograma del caso histórico'),
(pg_temp.id(20014),pg_temp.id(19173),pg_temp.id(3102),current_date-2+time '10:45','Radiografía del caso histórico')
ON CONFLICT (id_orden_examen) DO NOTHING;
INSERT INTO examen_resultado
  (id_examen_resultado,id_orden_examen,fecha_creacion,resultado,interpretacion) VALUES
(pg_temp.id(21011),pg_temp.id(20011),current_date+time '10:35','Hemoglobina: 13.5 g/dL; leucocitos: 6800/µL; plaquetas: 250000/µL.','Resultado ficticio para demostración académica'),
(pg_temp.id(21013),pg_temp.id(20013),current_date-2+time '10:35','Hemoglobina: 14.0 g/dL; leucocitos: 7200/µL; plaquetas: 260000/µL.','Resultado ficticio del caso histórico'),
(pg_temp.id(21014),pg_temp.id(20014),current_date-2+time '10:50','Informe de radiografía de tórax incorporado al caso de demostración.','Informe ficticio para revisión del flujo de resultados')
ON CONFLICT (id_examen_resultado) DO NOTHING;
COMMIT;
