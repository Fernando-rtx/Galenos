CREATE TABLE public.clinica (
    id_clinica uuid NOT NULL,
    nombre character varying(255) NOT NULL,
    activo boolean,
    tipo character varying(20),
    comentarios text
);

CREATE TABLE public.consulta (
    id_consulta uuid NOT NULL,
    fecha_inicio timestamp with time zone DEFAULT now(),
    fecha_fin timestamp with time zone,
    referencia_externa text,
    observaciones text,
    id_persona_rol uuid
);

CREATE TABLE public.consulta_procedimiento (
    id_consulta_procedimiento uuid NOT NULL,
    id_consulta uuid,
    id_procedimiento uuid,
    fecha_inicio timestamp with time zone,
    fecha_fin timestamp with time zone,
    observaciones text
);

CREATE TABLE public.consulta_procedimiento_paso (
    id_consulta_procedimiento_paso uuid NOT NULL,
    id_consulta_procedimiento uuid,
    id_persona_rol uuid,
    fecha_inicio timestamp with time zone DEFAULT now(),
    fecha_fin timestamp with time zone,
    estado character varying(20)
);

CREATE TABLE public.documento (
    id_documento uuid NOT NULL,
    id_persona uuid,
    id_tipo_documento uuid,
    valor text,
    ruta_fisica text
);

CREATE TABLE public.examen (
    id_examen uuid NOT NULL,
    nombre character varying(255),
    activo boolean DEFAULT true,
    observaciones text
);

CREATE TABLE public.examen_resultado (
    id_examen_resultado uuid NOT NULL,
    id_orden_examen uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    resultado text,
    interpretacion text,
    ruta_atestado text
);

CREATE TABLE public.examen_tipo_examen (
    id_examen_tipo_examen uuid NOT NULL,
    id_examen uuid,
    id_tipo_examen uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    observaciones text
);

CREATE TABLE public.medio_contacto (
    id_medio_contacto uuid NOT NULL,
    id_persona uuid,
    id_tipo_medio_contacto uuid,
    valor text,
    fecha_creacion timestamp with time zone DEFAULT now()
);

CREATE TABLE public.orden_examen (
    id_orden_examen uuid NOT NULL,
    id_consulta_procedimiento_paso uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    indicaciones text
);

CREATE TABLE public.persona (
    id_persona uuid NOT NULL,
    nombres character varying(255),
    apellidos character varying(255),
    fecha_nacimiento timestamp with time zone,
    fecha_creacion timestamp with time zone DEFAULT now()
);

CREATE TABLE public.persona_rol (
    id_persona_rol uuid NOT NULL,
    id_persona uuid,
    id_rol uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    id_clinica uuid
);

CREATE TABLE public.procedimiento (
    id_procedimiento uuid NOT NULL,
    nombre character varying(155),
    activo boolean,
    observaciones text
);

CREATE TABLE public.procedimiento_paso (
    id_procedimiento_paso uuid NOT NULL,
    id_procedimiento uuid,
    nombre character varying(155),
    indica_fin boolean DEFAULT false,
    id_rol uuid
);

CREATE TABLE public.procedimiento_paso_examen (
    id_procedimiento_paso_examen uuid NOT NULL,
    id_procedimiento_paso uuid,
    id_examen uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    activo boolean DEFAULT true,
    observaciones text
);

CREATE TABLE public.procedimiento_paso_secuencia (
    id_procedimiento_paso_secuencia uuid NOT NULL,
    id_procedimiento_paso uuid,
    id_procedimiento_paso_referencia uuid,
    tipo_secuencia character varying(20)
);

CREATE TABLE public.rol (
    id_rol uuid NOT NULL,
    nombre character varying(155),
    activo boolean,
    observaciones text
);

CREATE TABLE public.tipo_documento (
    id_tipo_documento uuid NOT NULL,
    nombre character varying(155),
    indicaciones text,
    expresion_regular text DEFAULT '.'::text,
    activo boolean
);

CREATE TABLE public.tipo_examen (
    id_tipo_examen uuid NOT NULL,
    nombre character varying,
    activo boolean,
    observaciones text
);

CREATE TABLE public.tipo_medio_contacto (
    id_tipo_medio_contacto uuid NOT NULL,
    nombre character varying(155),
    indicaciones text,
    expresion_regular text,
    activo boolean
);

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

CREATE INDEX fki_fk_persona_rol_clinica ON public.persona_rol USING btree (id_clinica);

ALTER TABLE ONLY public.consulta
    ADD CONSTRAINT consulta_persona_rol FOREIGN KEY (id_persona_rol) REFERENCES public.persona_rol(id_persona_rol) ON UPDATE CASCADE ON DELETE RESTRICT NOT VALID;

ALTER TABLE ONLY public.consulta_procedimiento
    ADD CONSTRAINT fk_consulta_procedimiento_consulta FOREIGN KEY (id_consulta) REFERENCES public.consulta(id_consulta) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT fk_consulta_procedimiento_paso_consulta_procedimiento FOREIGN KEY (id_consulta_procedimiento) REFERENCES public.consulta_procedimiento(id_consulta_procedimiento) ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE ONLY public.consulta_procedimiento_paso
    ADD CONSTRAINT fk_consulta_procedimiento_paso_persona_rol FOREIGN KEY (id_persona_rol) REFERENCES public.persona_rol(id_persona_rol) ON UPDATE CASCADE ON DELETE RESTRICT;

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