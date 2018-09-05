alter table championnat add column calendrier_a_rafraichir boolean NOT NULL default false;
alter table championnat add column calendrier_valide boolean NOT NULL default false;
alter table championnat add column cloture boolean NOT NULL default false;
