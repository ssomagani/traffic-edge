
drop view cars_by_sector if exists;
drop stream sector_health if exists;
drop stream traffic if exists;
drop stream control if exists;
drop table sectors if exists;
drop table positions if exists;
drop table pings if exists;


-------- TABLES ###########

create table pings 
	migrate to topic old_pings
(
	id integer not null,
	location DECIMAL not null,
	sector integer not null,
	speed DECIMAL not null,
	ts TIMESTAMP not null,
	constraint PK_PINGS PRIMARY KEY (id, location, speed, ts)
) 	using TTL 10 minutes on column ts;
partition table pings on column id;
CREATE INDEX IDX_PING_TIME ON PINGS(ts) WHERE NOT MIGRATING;

create table positions (
	id integer not null,
	location DECIMAL not null,
	sector integer not null,
	speed DECIMAL not null,
	time TIMESTAMP not null,
	constraint PK_POSITIONS PRIMARY KEY (id)
);
partition table positions on column id;

create table sectors (
	sector integer not null,
	speed_limit decimal not null,
	toll_rate decimal not null,
	accident_speed decimal default 0
);

-------- VIEWS ###########

create view cars_by_sector 
	as 
	select sector, count(id) as traffic 
	from positions group by sector;

-------- STREAMS ###########

create stream traffic 
	export to topic traffic
	partition on column sector
	 (
	sector integer not null,
	traffic integer not null
);

create stream control 
	export to topic control
	partition on column id
	(
		id integer not null,
		speed_diff DECIMAL not null	
);

create stream sector_health
	export to topic accident_stream
	partition on column sector
	(
		sector integer not null,
		traffic integer not null,
		accident tinyint default 0
	);

-------- PROCEDURES ###########

create procedure partition on table pings column id from class com.voltdb.procs.RecordPing;
create procedure from class com.voltdb.procs.CaptureTraffic;
create procedure from class com.voltdb.procs.EvalDriver;
create compound procedure from class com.voltdb.procs.ReceivePing;
create procedure GET_TRAFFIC as select sector, traffic from cars_by_sector;
create procedure from class com.voltdb.procs.SectorHealthCheck;

-------- TASKS ###########

create task SectorHealthCheckTask on schedule every 10 seconds
	procedure SectorHealthCheck
	run on database;
	
