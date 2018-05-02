CREATE TABLE poule
  (
      id bigint NOT NULL,
      numero int not null,
      division_fk bigint NOT NULL references division(id),
      CONSTRAINT poule_pkey PRIMARY KEY (id),
      CONSTRAINT poule_unique UNIQUE (division_fk,numero)
  );

  CREATE TABLE equipe
  (
      id bigint NOT NULL,
      code_alphabetique varchar(255),
      division_fk bigint NOT NULL references division(id),
      club_fk bigint NOT NULL references club(id),
      capitaine_fk bigint references membre(id),
      poule_fk bigint references poule(id),
      CONSTRAINT equipe_pkey PRIMARY KEY (id)
  );