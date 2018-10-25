create table autorisation_rencontre (
      id bigint NOT NULL,
      type character varying(50),
      rencontre_fk bigint not null references rencontre(id),
      membre_fk bigint not null references membre(id),
      CONSTRAINT autorisation_rencontre_pkey PRIMARY KEY (id)
);