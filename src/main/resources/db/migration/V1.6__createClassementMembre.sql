CREATE TABLE CLASSEMENTAFT
(
    id bigint not null,
    dateclassement date not null,
    codeclassement varchar(255) not null,
    points int not null,
    membre_fk bigint not null references membre(id),
    CONSTRAINT classementaft_pkey PRIMARY KEY (id)
);
CREATE TABLE CLASSEMENTCORPO
(
    id bigint not null,
    dateclassement date not null,
    points int not null,
    membre_fk bigint not null references membre(id),
    CONSTRAINT classementcorpo_pkey PRIMARY KEY (id)
);

alter table membre add column classementaft_fk bigint references classementaft(id);

alter table membre add column classementcorpo_fk bigint references classementcorpo(id);