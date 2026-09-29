-- Esquema relacional de GalenosSV. Las PK usan UUID y las relaciones se
-- refuerzan al final mediante FOREIGN KEY; JPA no genera estas tablas.

-- Clínica o sede. PK id_clinica; se relaciona con persona_rol.
CREATE TABLE public.clinica (
    id_clinica uuid NOT NULL,
    nombre character varying(255) NOT NULL,
    activo boolean,
    tipo character varying(20),
    comentarios text
);

-- Consulta clínica vinculada al rol que una persona ejerce en una clínica.
-- PK id_consulta; FK id_persona_rol agregada al final.
CREATE TABLE public.consulta (
    id_consulta uuid NOT NULL,
    fecha_inicio timestamp with time zone DEFAULT now(),
    fecha_fin timestamp with time zone,
    referencia_externa text,
    observaciones text,
    id_persona_rol uuid
);

-- Procedimiento ejecutado dentro de una consulta. PK propia y FK a consulta.
-- id_procedimiento se conserva como UUID según el diseño actual.
CREATE TABLE public.consulta_procedimiento (
    id_consulta_procedimiento uuid NOT NULL,
    id_consulta uuid,
    id_procedimiento uuid,
    fecha_inicio timestamp with time zone,
    fecha_fin timestamp with time zone,
    observaciones text
);

-- Ejecución de un paso: registra responsable, fechas y estado.
-- Sus FKs apuntan a consulta_procedimiento, persona_rol y a la plantilla procedimiento_paso.
CREATE TABLE public.consulta_procedimiento_paso (
    id_consulta_procedimiento_paso uuid NOT NULL,
    id_consulta_procedimiento uuid,
    id_persona_rol uuid,
    fecha_inicio timestamp with time zone DEFAULT now(),
    fecha_fin timestamp with time zone,
    estado character varying(20),
    id_procedimiento_paso uuid
);

-- Documento de una persona clasificado por tipo. PK UUID y dos FKs.
CREATE TABLE public.documento (
    id_documento uuid NOT NULL,
    id_persona uuid,
    id_tipo_documento uuid,
    valor text,
    ruta_fisica text
);

-- Catálogo de exámenes disponibles. Se relaciona con tipos y pasos mediante puentes.
CREATE TABLE public.examen (
    id_examen uuid NOT NULL,
    nombre character varying(255),
    activo boolean DEFAULT true,
    observaciones text
);

-- Resultado e interpretación asociados a una orden de examen.
CREATE TABLE public.examen_resultado (
    id_examen_resultado uuid NOT NULL,
    id_orden_examen uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    resultado text,
    interpretacion text,
    ruta_atestado text
);

-- Entidad puente examen-tipo_examen con fecha y observaciones propias.
CREATE TABLE public.examen_tipo_examen (
    id_examen_tipo_examen uuid NOT NULL,
    id_examen uuid,
    id_tipo_examen uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    observaciones text
);

-- Dato de contacto de una persona, clasificado por tipo de medio.
CREATE TABLE public.medio_contacto (
    id_medio_contacto uuid NOT NULL,
    id_persona uuid,
    id_tipo_medio_contacto uuid,
    valor text,
    fecha_creacion timestamp with time zone DEFAULT now()
);

-- Orden generada durante la ejecución de un paso de procedimiento.
CREATE TABLE public.orden_examen (
    id_orden_examen uuid NOT NULL,
    id_consulta_procedimiento_paso uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    indicaciones text
);

-- Persona base del dominio. Documentos, contactos y roles apuntan a esta PK.
CREATE TABLE public.persona (
    id_persona uuid NOT NULL,
    nombres character varying(255),
    apellidos character varying(255),
    fecha_nacimiento timestamp with time zone,
    fecha_creacion timestamp with time zone DEFAULT now()
);

-- Asociación persona-rol-clínica que contextualiza responsabilidades y consultas.
CREATE TABLE public.persona_rol (
    id_persona_rol uuid NOT NULL,
    id_persona uuid,
    id_rol uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    id_clinica uuid
);

-- Catálogo de procedimientos clínicos; sus pasos se almacenan por separado.
CREATE TABLE public.procedimiento (
    id_procedimiento uuid NOT NULL,
    nombre character varying(155),
    activo boolean,
    observaciones text
);

-- Paso de un procedimiento y rol encargado; indica si finaliza el flujo.
CREATE TABLE public.procedimiento_paso (
    id_procedimiento_paso uuid NOT NULL,
    id_procedimiento uuid,
    nombre character varying(155),
    indica_fin boolean DEFAULT false,
    id_rol uuid
);

-- Puente paso-examen con estado, fecha y observaciones propias.
CREATE TABLE public.procedimiento_paso_examen (
    id_procedimiento_paso_examen uuid NOT NULL,
    id_procedimiento_paso uuid,
    id_examen uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    activo boolean DEFAULT true,
    observaciones text
);

-- Enlace de secuencia entre un paso origen y un UUID de paso de referencia.
CREATE TABLE public.procedimiento_paso_secuencia (
    id_procedimiento_paso_secuencia uuid NOT NULL,
    id_procedimiento_paso uuid,
    id_procedimiento_paso_referencia uuid,
    tipo_secuencia character varying(20)
);

-- Catálogo de roles que una persona puede ejercer y que un paso puede requerir.
CREATE TABLE public.rol (
    id_rol uuid NOT NULL,
    nombre character varying(155),
    activo boolean,
    observaciones text
);

-- Catálogo de tipos documentales y expresión regular descriptiva.
CREATE TABLE public.tipo_documento (
    id_tipo_documento uuid NOT NULL,
    nombre character varying(155),
    indicaciones text,
    expresion_regular text DEFAULT '.'::text,
    activo boolean
);

-- Catálogo de clasificaciones de examen.
CREATE TABLE public.tipo_examen (
    id_tipo_examen uuid NOT NULL,
    nombre character varying,
    activo boolean,
    observaciones text
);

-- Catálogo de tipos de contacto y expresión regular descriptiva.
CREATE TABLE public.tipo_medio_contacto (
    id_tipo_medio_contacto uuid NOT NULL,
    nombre character varying(155),
    indicaciones text,
    expresion_regular text,
    activo boolean
);

-- Claves primarias: cada tabla queda identificada por su UUID.
ALTER TABLE ONLY public.clinica
    ADD CONSTRAINT pk_clinica PRIMARY KEY (id_clinica);

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT pk_consulta PRIMARY KEY (id_consulta);

ALTER TABLE ONLY public.consulta_procedimiento
    ADD CONSTRAINT pk_consulta_procedimiento PRIMARY KEY (id_consulta_procedimiento);

ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT pk_consulta_procedimiento_paso PRIMARY KEY (id_consulta_procedimiento_paso);

ALTER TABLE ONLY public.documento
    ADD CONSTRAINT pk_documento PRIMARY KEY (id_documento);

ALTER TABLE ONLY public.examen
    ADD CONSTRAINT pk_examen PRIMARY KEY (id_examen);

ALTER TABLE ONLY public.examen_resultado
    ADD CONSTRAINT pk_examen_resultado PRIMARY KEY (id_examen_resultado);

ALTER TABLE ONLY public.examen_tipo_examen
    ADD CONSTRAINT pk_examen_tipo_examen PRIMARY KEY (id_examen_tipo_examen);

ALTER TABLE ONLY public.medio_contacto
    ADD CONSTRAINT pk_medio_contacto PRIMARY KEY (id_medio_contacto);

ALTER TABLE ONLY public.orden_examen
    ADD CONSTRAINT pk_orden_examen PRIMARY KEY (id_orden_examen);

ALTER TABLE ONLY public.persona
    ADD CONSTRAINT pk_persona PRIMARY KEY (id_persona);

ALTER TABLE ONLY public.persona_rol
    ADD CONSTRAINT pk_persona_rol PRIMARY KEY (id_persona_rol);

ALTER TABLE ONLY public.procedimiento
    ADD CONSTRAINT pk_procedimiento PRIMARY KEY (id_procedimiento);

ALTER TABLE ONLY public.procedimiento_paso
    ADD CONSTRAINT pk_procedimiento_paso PRIMARY KEY (id_procedimiento_paso);

ALTER TABLE ONLY public.procedimiento_paso_examen
    ADD CONSTRAINT pk_procedimiento_paso_examen PRIMARY KEY (id_procedimiento_paso_examen);

ALTER TABLE ONLY public.procedimiento_paso_secuencia
    ADD CONSTRAINT pk_procedimiento_paso_secuencia PRIMARY KEY (id_procedimiento_paso_secuencia);

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT pk_rol PRIMARY KEY (id_rol);

ALTER TABLE ONLY public.tipo_documento
    ADD CONSTRAINT pk_tipo_documento PRIMARY KEY (id_tipo_documento);

ALTER TABLE ONLY public.tipo_examen
    ADD CONSTRAINT pk_tipo_examen PRIMARY KEY (id_tipo_examen);

ALTER TABLE ONLY public.tipo_medio_contacto
    ADD CONSTRAINT pk_tipo_medio_contacto PRIMARY KEY (id_tipo_medio_contacto);

-- Índice auxiliar y claves foráneas: preservan las relaciones del dominio.
CREATE INDEX fki_fk_persona_rol_clinica ON public.persona_rol USING btree (id_clinica);

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT consulta_persona_rol FOREIGN KEY (id_persona_rol) REFERENCES public.persona_rol(id_persona_rol) ON UPDATE CASCADE ON DELETE RESTRICT NOT VALID;

ALTER TABLE ONLY public.consulta_procedimiento
    ADD CONSTRAINT fk_consulta_procedimiento_consulta FOREIGN KEY (id_consulta) REFERENCES public.consulta(id_consulta) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT fk_consulta_procedimiento_paso_consulta_procedimiento FOREIGN KEY (id_consulta_procedimiento) REFERENCES public.consulta_procedimiento(id_consulta_procedimiento) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT fk_consulta_procedimiento_paso_persona_rol FOREIGN KEY (id_persona_rol) REFERENCES public.persona_rol(id_persona_rol) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT fk_consulta_procedimiento_paso_procedimiento_paso FOREIGN KEY (id_procedimiento_paso) REFERENCES public.procedimiento_paso(id_procedimiento_paso) ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX fki_fk_cpp_paso ON public.consulta_procedimiento_paso USING btree (id_procedimiento_paso);

ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT ck_consulta_procedimiento_paso_estado CHECK (estado IN ('PENDIENTE', 'EN_CURSO', 'COMPLETADO'));

ALTER TABLE ONLY public.documento
    ADD CONSTRAINT fk_documento_persona FOREIGN KEY (id_persona) REFERENCES public.persona(id_persona) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.documento
    ADD CONSTRAINT fk_documento_tipo_documento FOREIGN KEY (id_tipo_documento) REFERENCES public.tipo_documento(id_tipo_documento) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.examen_resultado
    ADD CONSTRAINT fk_examen_resultado_orden_examen FOREIGN KEY (id_orden_examen) REFERENCES public.orden_examen(id_orden_examen) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.examen_tipo_examen
    ADD CONSTRAINT fk_examen_tipo_examen_examen FOREIGN KEY (id_examen) REFERENCES public.examen(id_examen) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.examen_tipo_examen
    ADD CONSTRAINT fk_examen_tipo_examen_tipo_examen FOREIGN KEY (id_tipo_examen) REFERENCES public.tipo_examen(id_tipo_examen) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.medio_contacto
    ADD CONSTRAINT fk_medio_contacto_persona FOREIGN KEY (id_persona) REFERENCES public.persona(id_persona) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.medio_contacto
    ADD CONSTRAINT fk_medio_contacto_tipo_medio_contacto FOREIGN KEY (id_tipo_medio_contacto) REFERENCES public.tipo_medio_contacto(id_tipo_medio_contacto) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.orden_examen
    ADD CONSTRAINT fk_orden_examen_consulta_procedimiento_paso FOREIGN KEY (id_consulta_procedimiento_paso) REFERENCES public.consulta_procedimiento_paso(id_consulta_procedimiento_paso) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.persona_rol
    ADD CONSTRAINT fk_persona_rol_clinica FOREIGN KEY (id_clinica) REFERENCES public.clinica(id_clinica);

ALTER TABLE ONLY public.persona_rol
    ADD CONSTRAINT fk_persona_rol_persona FOREIGN KEY (id_persona) REFERENCES public.persona(id_persona) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.persona_rol
    ADD CONSTRAINT fk_persona_rol_rol FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.procedimiento_paso_examen
    ADD CONSTRAINT fk_procedimiento_paso_examen_examen FOREIGN KEY (id_examen) REFERENCES public.examen(id_examen) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.procedimiento_paso_examen
    ADD CONSTRAINT fk_procedimiento_paso_examen_procedimiento_examen FOREIGN KEY (id_procedimiento_paso) REFERENCES public.procedimiento_paso(id_procedimiento_paso) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.procedimiento_paso
    ADD CONSTRAINT fk_procedimiento_paso_procedimiento FOREIGN KEY (id_procedimiento) REFERENCES public.procedimiento(id_procedimiento) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.procedimiento_paso
    ADD CONSTRAINT fk_procedimiento_paso_rol FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol) ON UPDATE CASCADE ON DELETE RESTRICT NOT VALID;

ALTER TABLE ONLY public.procedimiento_paso_secuencia
    ADD CONSTRAINT fk_procedimiento_paso_secuencia_procedimiento_paso FOREIGN KEY (id_procedimiento_paso) REFERENCES public.procedimiento_paso(id_procedimiento_paso) ON UPDATE CASCADE ON DELETE RESTRICT;
