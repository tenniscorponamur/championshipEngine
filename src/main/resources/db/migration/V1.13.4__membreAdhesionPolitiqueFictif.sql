alter table membre add column adhesion_politique boolean NOT NULL default false;
alter table membre add column fictif boolean NOT NULL default false;

update membre set adhesion_politique=true;


