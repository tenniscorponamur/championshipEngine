create table court (

      id bigint NOT NULL,
      code character varying(255) NOT NULL,
      terrain_fk bigint NOT NULL references terrain(id),
      CONSTRAINT court_pkey PRIMARY KEY (id)

);

alter table rencontre add column court_fk bigint references court(id);