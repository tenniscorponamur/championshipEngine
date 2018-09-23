create table horaire_terrain(

      id bigint NOT NULL,
      type_championnat character varying(50) NOT NULL,
      jour_semaine int not null,
      heures int not null,
      minutes int not null,
      terrain_fk bigint NOT NULL references terrain(id),
      CONSTRAINT horaire_terrain_pkey PRIMARY KEY (id)

);