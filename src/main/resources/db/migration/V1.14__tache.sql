CREATE TABLE TACHE
(
    id bigint not null,
    date_demande timestamp with time zone not null,
    type_tache character varying(500) NOT NULL,
    demandeur_fk bigint not null references membre(id),

    membre_fk bigint references membre(id),
    code_classement_aft character varying(500),
    points_corpo int,
    desactivation_membre boolean NOT NULL default false,
    reactivation_membre boolean NOT NULL default false,
    commentaires_demande character varying(2000),

    date_traitement timestamp with time zone,
    agent_traitant character varying(500),
    validation_traitement boolean NOT NULL default false,
    refus_traitement boolean NOT NULL default false,
    commentaires_refus character varying(2000),

    mark_as_read boolean NOT NULL default false,
    archived boolean NOT NULL default false,

    CONSTRAINT tache_pkey PRIMARY KEY (id)
);