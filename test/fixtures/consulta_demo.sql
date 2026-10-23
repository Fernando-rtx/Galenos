-- SOLO en una base galenos_demo_* o galenos_rest_test_* vacía de estas tablas.
-- Todas las personas, clínicas, UUID y fechas son ficticios para integración.
BEGIN;
CREATE TABLE clinica (
    id_clinica uuid PRIMARY KEY,
    nombre varchar(255) NOT NULL,
    activo boolean,
    tipo varchar(20),
    comentarios text
);
CREATE TABLE persona (
    id_persona uuid PRIMARY KEY,
    nombres varchar(255),
    apellidos varchar(255),
    fecha_nacimiento timestamptz,
    fecha_creacion timestamptz
);
CREATE TABLE rol (
    id_rol uuid PRIMARY KEY,
    nombre varchar(155),
    activo boolean,
    observaciones text
);
CREATE TABLE persona_rol (
    id_persona_rol uuid PRIMARY KEY,
    id_persona uuid REFERENCES persona,
    id_rol uuid REFERENCES rol,
    fecha_creacion timestamptz,
    id_clinica uuid REFERENCES clinica
);
CREATE TABLE consulta (
    id_consulta uuid PRIMARY KEY,
    fecha_inicio timestamptz,
    fecha_fin timestamptz,
    referencia_externa text,
    observaciones text,
    id_persona_rol uuid REFERENCES persona_rol
);
INSERT INTO clinica (id_clinica, nombre, activo) VALUES
('00000000-0000-4000-8000-000000000011', 'Clínica ficticia A', true),
('00000000-0000-4000-8000-000000000012', 'Clínica ficticia B', true),
('00000000-0000-4000-8000-000000000013', 'Clínica ficticia inactiva', false);
INSERT INTO rol (id_rol, nombre, activo) VALUES
('00000000-0000-4000-8000-000000000031', 'Rol sintético', true);
INSERT INTO persona (id_persona, nombres, apellidos) VALUES
('00000000-0000-4000-8000-000000000041', 'Persona ficticia', 'Uno'),
('00000000-0000-4000-8000-000000000042', 'Persona ficticia', 'Dos'),
('00000000-0000-4000-8000-000000000043', 'Persona ficticia', 'Tres'),
('00000000-0000-4000-8000-000000000044', NULL, NULL);
INSERT INTO persona_rol (id_persona_rol, id_persona, id_rol, id_clinica) VALUES
('00000000-0000-4000-8000-000000000101', '00000000-0000-4000-8000-000000000041',
 '00000000-0000-4000-8000-000000000031', '00000000-0000-4000-8000-000000000011'),
('00000000-0000-4000-8000-000000000102', '00000000-0000-4000-8000-000000000042',
 '00000000-0000-4000-8000-000000000031', '00000000-0000-4000-8000-000000000012'),
('00000000-0000-4000-8000-000000000103', '00000000-0000-4000-8000-000000000043',
 '00000000-0000-4000-8000-000000000031', '00000000-0000-4000-8000-000000000013'),
('00000000-0000-4000-8000-000000000104', '00000000-0000-4000-8000-000000000044',
 '00000000-0000-4000-8000-000000000031', '00000000-0000-4000-8000-000000000011');
INSERT INTO consulta (id_consulta, fecha_inicio, fecha_fin, id_persona_rol, observaciones, referencia_externa) VALUES
('00000000-0000-4000-8000-000000001001', '2026-10-22T05:59:59Z', NULL,
 '00000000-0000-4000-8000-000000000101', 'Contenido sintético excluido del DTO', 'Referencia sintética excluida'),
('00000000-0000-4000-8000-000000001002', '2026-10-22T06:00:00Z', '2026-10-22T07:00:00Z',
 '00000000-0000-4000-8000-000000000101', NULL, NULL),
('00000000-0000-4000-8000-000000001003', '2026-10-23T05:59:59Z', NULL,
 '00000000-0000-4000-8000-000000000101', NULL, NULL),
('00000000-0000-4000-8000-000000001004', '2026-10-23T06:00:00Z', NULL,
 '00000000-0000-4000-8000-000000000104', NULL, NULL),
('00000000-0000-4000-8000-000000001005', NULL, NULL,
 '00000000-0000-4000-8000-000000000104', NULL, NULL),
('00000000-0000-4000-8000-000000002001', '2026-10-22T06:00:00Z', NULL,
 '00000000-0000-4000-8000-000000000102', NULL, NULL),
('00000000-0000-4000-8000-000000003001', '2026-10-22T06:00:00Z', NULL,
 '00000000-0000-4000-8000-000000000103', NULL, NULL);
COMMIT;
