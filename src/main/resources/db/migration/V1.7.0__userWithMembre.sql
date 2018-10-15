alter table utilisateur add column admin boolean NOT NULL default false;

update utilisateur set admin = true;

alter table utilisateur add column membre_fk bigint references classementcorpo(id);
