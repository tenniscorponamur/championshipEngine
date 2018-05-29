alter table POULE add column allerRetour boolean NOT NULL default false;

CREATE TABLE TERRAIN
(
    id bigint NOT NULL,
    nom character varying(255) NOT NULL,
    description character varying(500),
    adresse character varying(1000),
    CONSTRAINT terrain_pkey PRIMARY KEY (id)
);

alter table CLUB add column terrain_fk bigint references terrain(id);

alter table EQUIPE add column terrain_fk bigint references terrain(id);

create table rencontre(
    id bigint not null,
    dateHeureRencontre date,
    numeroJournee int,
    visites_fk bigint not null references equipe(id),
    visiteurs_fk bigint not null references equipe(id),
    division_fk bigint not null references division(id),
    poule_fk bigint references poule(id),
    terrain_fk bigint references terrain(id),
    CONSTRAINT rencontre_pkey PRIMARY KEY (id)
);