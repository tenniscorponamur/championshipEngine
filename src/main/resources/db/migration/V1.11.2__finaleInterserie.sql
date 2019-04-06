alter table rencontre add column finale_interserie boolean NOT NULL default false;

update rencontre set finale_interserie = '1' where informations_interserie = 'Finale';

