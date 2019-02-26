create table classement_job (
      id bigint NOT NULL,
      start_date date not null,
      end_date date not null,
      status varchar(50) not null,
      CONSTRAINT classement_job_pkey PRIMARY KEY (id)
);

create table classement_job_trace (
      id bigint NOT NULL,
      message character varying(500),
      classement_job_fk bigint not null references classement_job(id),
      CONSTRAINT classement_job_trace_pkey PRIMARY KEY (id)
);