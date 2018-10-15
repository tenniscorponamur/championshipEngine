alter table utilisateur drop column membre_fk;

alter table utilisateur add column membre_fk bigint references membre(id);
