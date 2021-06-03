create table kpi
(
    id uuid not null primary key,
	timestamp timestamp not null,
	type varchar not null,
    value varchar not null
);