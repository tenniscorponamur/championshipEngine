alter table rencontre add column resultats_encodes boolean NOT NULL default false;

update rencontre set resultats_encodes = '1' where valide = '1';


