CREATE TABLE MEMBRE_EQUIPE
(
    id bigint not null,
    equipe_fk bigint not null references equipe(id),
    membre_fk bigint not null references membre(id),
    CONSTRAINT membre_equipe_pkey PRIMARY KEY (id),
    CONSTRAINT membre_equipe_unique UNIQUE (id,equipe_fk,membre_fk)
);