CREATE TABLE MATCH
(
    id bigint NOT NULL,
    ordre int NOT NULL,
    joueurvisites1_fk bigint not null references membre(id),
    joueurvisites2_fk bigint references membre(id),
    joueurvisiteurs1_fk bigint not null references membre(id),
    joueurvisiteurs2_fk bigint references membre(id),
    rencontre_fk bigint not null references rencontre(id),
    CONSTRAINT match_pkey PRIMARY KEY (id)
);

CREATE TABLE SET
(
    id bigint NOT NULL,
    ordre int NOT NULL,
    jeuxvisites int not null,
    jeuxvisiteurs int not null,
    visitesgagnant boolean,
    match_fk bigint not null references match(id),
    CONSTRAINT set_pkey PRIMARY KEY (id)
);
