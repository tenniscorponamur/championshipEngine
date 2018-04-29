CREATE TABLE championnat
  (
      id bigint NOT NULL,
      annee int not null,
      type character varying(50) NOT NULL,
      categorie character varying(50) NOT NULL,
      CONSTRAINT championnat_pkey PRIMARY KEY (id),
      CONSTRAINT championnat_unique UNIQUE (annee,type,categorie)
  );

  CREATE TABLE division
  (
      id bigint NOT NULL,
      numero int not null,
      points_min int not null,
      points_max int not null,
      championnat_fk bigint not null references championnat(id),
      CONSTRAINT division_pkey PRIMARY KEY (id),
      CONSTRAINT division_numero_unique UNIQUE (championnat_fk,numero)
  );