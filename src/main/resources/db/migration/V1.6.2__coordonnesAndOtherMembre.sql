alter table membre add column date_affiliation_corpo date;
alter table membre add column date_desaffiliation_corpo date;

alter table membre add column responsable_club boolean NOT NULL default false;

alter table membre add column code_postal character varying(255);
alter table membre add column localite character varying(500);
alter table membre add column rue character varying(500);
alter table membre add column rue_numero character varying(255);
alter table membre add column rue_boite character varying(255);

alter table membre add column telephone character varying(255);
alter table membre add column gsm character varying(255);
alter table membre add column mail character varying(255);


