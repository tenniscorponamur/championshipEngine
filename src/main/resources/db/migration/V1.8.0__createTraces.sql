create table trace (
      id bigint NOT NULL,
      utilisateur character varying(500) NOT NULL,
      type character varying(500),
      foreign_key character varying(500),
      message character varying(500),
      CONSTRAINT trace_pkey PRIMARY KEY (id)

);