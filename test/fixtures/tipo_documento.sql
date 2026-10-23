-- SOLO en una base vacía de pruebas. Catálogo sintético, sin pacientes.
CREATE TABLE tipo_documento (
    id_tipo_documento uuid PRIMARY KEY,
    nombre varchar(155),
    indicaciones text,
    expresion_regular text,
    activo boolean
);
INSERT INTO tipo_documento VALUES
('00000000-0000-4000-8000-000000000001', 'Documento sintético A',
 'Solo pruebas', '^[A-Z]{3}$', true),
('00000000-0000-4000-8000-000000000002', 'Documento sintético B',
 NULL, NULL, false);
