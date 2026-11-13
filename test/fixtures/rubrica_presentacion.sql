-- Datos ficticios: Datos_Prueba_GalenosSV_Rubrica_PPI115.pdf y procedimientos adicionales.
-- REEMPLAZA los datos de la base de presentación. No usar en clinica.
BEGIN;
SET LOCAL TIME ZONE 'America/El_Salvador';
DO $$ BEGIN
  IF current_database() NOT LIKE 'galenos_rest_test_gt02_presentacion_%' THEN
    RAISE EXCEPTION 'Sólo se permite cargar en una base de presentación GT02';
  END IF;
END $$;
CREATE OR REPLACE FUNCTION pg_temp.id(bigint) RETURNS uuid LANGUAGE SQL IMMUTABLE AS $$
  SELECT ('00000000-0000-4000-8000-' || lpad($1::text,12,'0'))::uuid
$$;
TRUNCATE TABLE examen_resultado, orden_examen, consulta_procedimiento_paso,
  consulta_procedimiento, consulta, procedimiento_paso_examen,
  procedimiento_paso_secuencia, procedimiento_paso, procedimiento,
  examen_tipo_examen, examen, tipo_examen, documento, medio_contacto,
  persona_rol, persona, tipo_documento, tipo_medio_contacto, rol, clinica;

INSERT INTO clinica (id_clinica,nombre,activo,tipo,comentarios) VALUES
(pg_temp.id(11),'Clínica Central GalenosSV',true,'Clínica general','Consulta externa'),
(pg_temp.id(12),'Clínica Especializada Santa Ana',true,'Especializada','Atención por especialidades'),
(pg_temp.id(13),'Clínica Antigua',false,'Hospital','Sede inactiva');
INSERT INTO rol (id_rol,nombre,activo,observaciones) VALUES
(pg_temp.id(31),'Paciente',true,'Persona que recibe atención'),
(pg_temp.id(32),'Atención al público',true,'Recepción y registro'),
(pg_temp.id(33),'Doctor',true,'Evaluación médica'),
(pg_temp.id(34),'Enfermera',true,'Preparación del paciente y signos vitales'),
(pg_temp.id(35),'Administrador auxiliar',false,'Rol inactivo para demostración');
INSERT INTO tipo_documento (id_tipo_documento,nombre,indicaciones,expresion_regular,activo) VALUES
(pg_temp.id(1),'DUI','Ocho dígitos, guion y un dígito: 01234567-8.','^[0-9]{8}-[0-9]$',true),
(pg_temp.id(2),'Pasaporte','Una letra mayúscula y ocho dígitos: A12345678.','^[A-Z][0-9]{8}$',true),
(pg_temp.id(3),'Carné antiguo','Tipo inactivo.','^[0-9]{6}$',false);
INSERT INTO tipo_medio_contacto (id_tipo_medio_contacto,nombre,indicaciones,expresion_regular,activo) VALUES
(pg_temp.id(11001),'Correo electrónico','Ejemplo: carlos@gmail.com','^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$',true),
(pg_temp.id(11002),'Teléfono','Ocho dígitos, comienza con 2, 6 o 7.','^[267][0-9]{7}$',true),
(pg_temp.id(11003),'Fax','Tipo inactivo.','^[0-9]{8}$',false);
INSERT INTO tipo_examen (id_tipo_examen,nombre,activo,observaciones) VALUES
(pg_temp.id(4001),'Laboratorio clínico',true,'Análisis de laboratorio'),
(pg_temp.id(4002),'Diagnóstico por imagen',true,'Estudios de imagen'),
(pg_temp.id(4003),'Examen obsoleto',false,'Tipo inactivo');
INSERT INTO examen (id_examen,nombre,activo,observaciones) VALUES
(pg_temp.id(3101),'Hemograma completo',true,'Estudio de hematología'),
(pg_temp.id(3102),'Radiografía de tórax',true,'Estudio de imagen'),
(pg_temp.id(3103),'Examen antiguo',false,'Examen inactivo sin tipo asociado');
INSERT INTO examen_tipo_examen (id_examen_tipo_examen,id_examen,id_tipo_examen) VALUES
(pg_temp.id(4101),pg_temp.id(3101),pg_temp.id(4001)),
(pg_temp.id(4102),pg_temp.id(3102),pg_temp.id(4002));

INSERT INTO persona (id_persona,nombres,apellidos,fecha_nacimiento) VALUES
(pg_temp.id(45),'Carlos','Martínez','1992-03-12'),
(pg_temp.id(42),'María','López','1995-07-24'),
(pg_temp.id(46),'José','Hernández','1983-09-08'),
(pg_temp.id(41),'Ana','Rodríguez','1998-04-15'),
(pg_temp.id(44),'Pedro','Ramírez','1990-11-02');
INSERT INTO persona_rol (id_persona_rol,id_persona,id_rol,id_clinica) VALUES
(pg_temp.id(201),pg_temp.id(45),pg_temp.id(32),pg_temp.id(11)),
(pg_temp.id(205),pg_temp.id(42),pg_temp.id(34),pg_temp.id(11)),
(pg_temp.id(202),pg_temp.id(46),pg_temp.id(33),pg_temp.id(11)),
(pg_temp.id(101),pg_temp.id(41),pg_temp.id(31),pg_temp.id(11));
INSERT INTO documento (id_documento,id_persona,id_tipo_documento,valor) VALUES
(pg_temp.id(9002),pg_temp.id(45),pg_temp.id(1),'01234567-8'),
(pg_temp.id(9004),pg_temp.id(42),pg_temp.id(1),'02345678-9'),
(pg_temp.id(9003),pg_temp.id(46),pg_temp.id(1),'03456789-0'),
(pg_temp.id(9001),pg_temp.id(41),pg_temp.id(1),'04567890-1'),
(pg_temp.id(9006),pg_temp.id(44),pg_temp.id(1),'05678901-2'),
(pg_temp.id(9007),pg_temp.id(41),pg_temp.id(2),'A12345678');
INSERT INTO medio_contacto (id_medio_contacto,id_persona,id_tipo_medio_contacto,valor) VALUES
(pg_temp.id(12001),pg_temp.id(45),pg_temp.id(11001),'carlos@gmail.com'),
(pg_temp.id(12002),pg_temp.id(45),pg_temp.id(11002),'71234567'),
(pg_temp.id(12003),pg_temp.id(42),pg_temp.id(11001),'maria@gmail.com'),
(pg_temp.id(12004),pg_temp.id(42),pg_temp.id(11002),'72345678'),
(pg_temp.id(12005),pg_temp.id(46),pg_temp.id(11001),'jose@gmail.com'),
(pg_temp.id(12006),pg_temp.id(46),pg_temp.id(11002),'73456789'),
(pg_temp.id(12007),pg_temp.id(41),pg_temp.id(11001),'ana@gmail.com'),
(pg_temp.id(12008),pg_temp.id(41),pg_temp.id(11002),'74567890'),
(pg_temp.id(12009),pg_temp.id(44),pg_temp.id(11001),'pedro@gmail.com'),
(pg_temp.id(12010),pg_temp.id(44),pg_temp.id(11002),'75678901');

INSERT INTO procedimiento (id_procedimiento,nombre,activo,observaciones) VALUES
(pg_temp.id(5001),'Consulta médica general',true,'Recepción, signos vitales y evaluación médica'),
(pg_temp.id(5002),'Evaluación de salud',true,'Ingreso, preparación y diagnóstico final'),
(pg_temp.id(5003),'Consulta pediátrica',true,'Flujo de atención pediátrica'),
(pg_temp.id(5004),'Control de hipertensión',true,'Seguimiento en consulta externa'),
(pg_temp.id(5005),'Control de diabetes',true,'Seguimiento en consulta externa'),
(pg_temp.id(5006),'Control preventivo de salud',true,'Consulta preventiva');
INSERT INTO procedimiento_paso (id_procedimiento_paso,id_procedimiento,nombre,indica_fin,id_rol) VALUES
(pg_temp.id(6001),pg_temp.id(5001),'Recepción del paciente',false,pg_temp.id(32)),
(pg_temp.id(6002),pg_temp.id(5001),'Toma de signos vitales',false,pg_temp.id(34)),
(pg_temp.id(6003),pg_temp.id(5001),'Evaluación médica',true,pg_temp.id(33)),
(pg_temp.id(6004),pg_temp.id(5002),'Registro de ingreso',false,pg_temp.id(32)),
(pg_temp.id(6005),pg_temp.id(5002),'Preparación del paciente',false,pg_temp.id(34)),
(pg_temp.id(6006),pg_temp.id(5002),'Diagnóstico final',true,pg_temp.id(33)),
(pg_temp.id(6007),pg_temp.id(5003),'Registro del paciente y acompañante',false,pg_temp.id(32)),
(pg_temp.id(6008),pg_temp.id(5003),'Registro de peso, talla y signos vitales',false,pg_temp.id(34)),
(pg_temp.id(6009),pg_temp.id(5003),'Evaluación pediátrica y cierre',true,pg_temp.id(33)),
(pg_temp.id(6010),pg_temp.id(5004),'Registro de consulta de seguimiento',false,pg_temp.id(32)),
(pg_temp.id(6011),pg_temp.id(5004),'Registro de presión arterial y signos vitales',false,pg_temp.id(34)),
(pg_temp.id(6012),pg_temp.id(5004),'Evaluación médica de seguimiento',true,pg_temp.id(33)),
(pg_temp.id(6013),pg_temp.id(5005),'Recepción para control de diabetes',false,pg_temp.id(32)),
(pg_temp.id(6014),pg_temp.id(5005),'Registro de signos vitales y antecedentes',false,pg_temp.id(34)),
(pg_temp.id(6015),pg_temp.id(5005),'Revisión médica y cierre del control',true,pg_temp.id(33)),
(pg_temp.id(6016),pg_temp.id(5006),'Registro de consulta preventiva',false,pg_temp.id(32)),
(pg_temp.id(6017),pg_temp.id(5006),'Preparación y registro de signos vitales',false,pg_temp.id(34)),
(pg_temp.id(6018),pg_temp.id(5006),'Evaluación preventiva y cierre',true,pg_temp.id(33));
INSERT INTO procedimiento_paso_secuencia (id_procedimiento_paso_secuencia,id_procedimiento_paso,id_procedimiento_paso_referencia,tipo_secuencia)
SELECT pg_temp.id(7000+n),pg_temp.id(6000+n),pg_temp.id(6001+n),'SIGUIENTE'
FROM generate_series(1,18) AS s(n) WHERE n % 3 <> 0;
INSERT INTO procedimiento_paso_examen (id_procedimiento_paso_examen,id_procedimiento_paso,id_examen,activo,observaciones) VALUES
(pg_temp.id(8001),pg_temp.id(6002),pg_temp.id(3101),true,'Disponible según evaluación del responsable'),
(pg_temp.id(8002),pg_temp.id(6003),pg_temp.id(3102),true,'Disponible según evaluación médica'),
(pg_temp.id(8003),pg_temp.id(6006),pg_temp.id(3101),true,'Disponible según evaluación médica');

-- Ana, hoy a las 09:00 en El Salvador; el paso inicial corresponde a Carlos.
INSERT INTO consulta (id_consulta,fecha_inicio,id_persona_rol,observaciones)
VALUES (pg_temp.id(1002),current_date + time '09:00',pg_temp.id(101),'Consulta de demostración de la rúbrica');
INSERT INTO consulta_procedimiento (id_consulta_procedimiento,id_consulta,id_procedimiento,fecha_inicio,observaciones)
VALUES (pg_temp.id(18001),pg_temp.id(1002),pg_temp.id(5001),current_date + time '09:00','Consulta médica general');
INSERT INTO consulta_procedimiento_paso
(id_consulta_procedimiento_paso,id_consulta_procedimiento,id_persona_rol,id_procedimiento_paso,fecha_inicio,estado)
VALUES (pg_temp.id(19001),pg_temp.id(18001),pg_temp.id(201),pg_temp.id(6001),current_date + time '09:00','PENDIENTE');
COMMIT;
