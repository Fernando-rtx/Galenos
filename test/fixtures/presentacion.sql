-- Datos ficticios con formatos habituales para la presentación, después de gt02.sql.
-- Conserva las relaciones y los casos de prueba de las bases de presentación.
BEGIN;
DO $$ BEGIN
  IF current_database() NOT LIKE 'galenos_rest_test_gt02_presentacion_%' THEN
    RAISE EXCEPTION 'Este archivo requiere una base de presentación GT02';
  END IF;
END $$;

UPDATE tipo_documento SET nombre='DUI', indicaciones='Escribe 8 dígitos, guion y un dígito. Ejemplo: 01234567-8.',
  expresion_regular='^[0-9]{8}-[0-9]$', activo=true
WHERE id_tipo_documento='00000000-0000-4000-8000-000000000001';
UPDATE tipo_documento SET nombre='Pasaporte', indicaciones='Escribe una letra mayúscula y 8 dígitos. Ejemplo: A12345678.',
  expresion_regular='^[A-Z][0-9]{8}$', activo=true
WHERE id_tipo_documento='00000000-0000-4000-8000-000000000002';
INSERT INTO tipo_documento VALUES
('00000000-0000-4000-8000-000000000003','Carné de minoridad','Formato de presentación: año y 4 dígitos. Ejemplo: 2026-0042.','^[0-9]{4}-[0-9]{4}$',true)
ON CONFLICT (id_tipo_documento) DO UPDATE SET nombre=EXCLUDED.nombre, indicaciones=EXCLUDED.indicaciones,
  expresion_regular=EXCLUDED.expresion_regular, activo=EXCLUDED.activo;

UPDATE persona p SET nombres=v.nombres, apellidos=v.apellidos, fecha_nacimiento=v.nacimiento::timestamp
FROM (VALUES
('00000000-0000-4000-8000-000000000041'::uuid,'Ana Gabriela','Martínez López','1998-04-15'),
('00000000-0000-4000-8000-000000000042'::uuid,'Carlos Eduardo','Hernández Pérez','1985-08-22'),
('00000000-0000-4000-8000-000000000043'::uuid,'María Fernanda','García Ramírez','2001-12-09'),
('00000000-0000-4000-8000-000000000044'::uuid,'Diego Andrés','Martínez López','2012-06-18'),
('00000000-0000-4000-8000-000000000045'::uuid,'Sofía Alejandra','Ramírez Torres','1994-03-11'),
('00000000-0000-4000-8000-000000000046'::uuid,'Luis Fernando','Mendoza Castro','1982-09-07')
) AS v(id,nombres,apellidos,nacimiento) WHERE p.id_persona=v.id;

UPDATE documento d SET valor=v.valor, id_tipo_documento=v.tipo::uuid
FROM (VALUES
('00000000-0000-4000-8000-000000009001'::uuid,'02845671-3','00000000-0000-4000-8000-000000000001'),
('00000000-0000-4000-8000-000000009002'::uuid,'05123456-7','00000000-0000-4000-8000-000000000001'),
('00000000-0000-4000-8000-000000009003'::uuid,'03678912-4','00000000-0000-4000-8000-000000000001'),
('00000000-0000-4000-8000-000000009004'::uuid,'A12345678','00000000-0000-4000-8000-000000000002'),
('00000000-0000-4000-8000-000000009005'::uuid,'04789123-5','00000000-0000-4000-8000-000000000001')
) AS v(id,valor,tipo) WHERE d.id_documento=v.id;
INSERT INTO documento VALUES
('00000000-0000-4000-8000-000000009006','00000000-0000-4000-8000-000000000044','00000000-0000-4000-8000-000000000003','2026-0042',NULL)
ON CONFLICT (id_documento) DO UPDATE SET valor=EXCLUDED.valor, id_tipo_documento=EXCLUDED.id_tipo_documento;

UPDATE clinica SET nombre=CASE id_clinica
  WHEN '00000000-0000-4000-8000-000000000011'::uuid THEN 'Clínica Galenos Santa Ana'
  WHEN '00000000-0000-4000-8000-000000000012'::uuid THEN 'Clínica Galenos San Salvador'
  ELSE 'Clínica Galenos Sonsonate' END, comentarios='Consulta externa de medicina general';
UPDATE rol SET observaciones=nombre;
UPDATE tipo_medio_contacto SET nombre='Correo electrónico', indicaciones='Escribe una dirección de correo electrónico';
UPDATE medio_contacto SET valor='ana.martinez@example.com'
WHERE id_medio_contacto='00000000-0000-4000-8000-000000012001';
UPDATE tipo_examen SET nombre='Laboratorio clínico', observaciones='Análisis de muestras de laboratorio';
UPDATE examen SET nombre='Hemograma completo', observaciones='Evaluación de células sanguíneas';
UPDATE examen_tipo_examen SET observaciones='Estudio de hematología';
UPDATE procedimiento SET nombre='Consulta de medicina general', observaciones='Recepción y evaluación médica del paciente';
UPDATE procedimiento_paso SET nombre=CASE WHEN indica_fin THEN 'Evaluación médica' ELSE 'Recepción del paciente' END;
UPDATE procedimiento_paso_examen SET observaciones='Disponible durante la recepción del paciente';
UPDATE consulta_procedimiento SET observaciones='Evaluación de medicina general';
UPDATE orden_examen SET indicaciones='Realizar hemograma completo y presentar el resultado en la consulta';
UPDATE examen_resultado SET resultado='Hemoglobina: 13.5 g/dL; leucocitos: 6800/µL; plaquetas: 250000/µL',
  interpretacion='Resultado de ejemplo para la presentación académica';
COMMIT;
