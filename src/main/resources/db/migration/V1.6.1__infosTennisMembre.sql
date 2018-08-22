alter table membre add column numero_aft character varying(255);

alter table membre add column date_affiliation_aft date;

alter table membre add column numero_club_aft character varying(255);

alter table membre add column only_corpo boolean NOT NULL default false;


