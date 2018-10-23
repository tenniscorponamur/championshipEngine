ALTER TABLE membre ADD column password character varying(500);
update membre set password = '$2a$10$1os5Ac5Sby4cCDkskqYHm..pPt.lTnTuoETKPmW2rTCwdy9M5zIcC';
ALTER TABLE membre alter column password set not null;