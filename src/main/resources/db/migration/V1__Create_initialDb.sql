CREATE SEQUENCE hibernate_sequence start with 2;

CREATE TABLE club
(
    id bigint NOT NULL,
    numero character varying(255) not null,
    nom character varying(255) not null,
    description character varying(500),
    CONSTRAINT club_pkey PRIMARY KEY (id)
);

CREATE TABLE utilisateur
(
    id bigint NOT NULL,
    nom character varying(500) NOT NULL,
    password character varying(500) NOT NULL,
    prenom character varying(500) NOT NULL,
    username character varying(500) NOT NULL,
    CONSTRAINT utilisateur_pkey PRIMARY KEY (id),
    CONSTRAINT username_unique UNIQUE (username)
);

CREATE TABLE membre
(
    id bigint NOT NULL,
    actif boolean NOT NULL,
    date_naissance date,
    genre character varying(10) NOT NULL,
    nom character varying(500) NOT NULL,
    numero character varying(500),
    prenom character varying(500) NOT NULL,
    CONSTRAINT membre_pkey PRIMARY KEY (id),
    CONSTRAINT membre_numero_unique UNIQUE (numero)
);

insert into utilisateur (id,prenom,nom,username,password) values (1,'Fabrice','Calay','fca','$2a$10$Eg98UIrJGchl/G6iO1ZVROIGFMHJcezP6CQayuRLlKzMCss6SMe6G');