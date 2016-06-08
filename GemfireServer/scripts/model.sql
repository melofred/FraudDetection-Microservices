--
-- $ psql 
-- gpadmin=# CREATE USER pivotal WITH SUPERUSER LOGIN;
-- CREATE ROLE
-- gpadmin=# ALTER ROLE pivotal WITH PASSWORD 'pivotal';
-- ALTER ROLE
-- gpadmin=# CREATE DATABASE gemfire;
-- CREATE DATABASE
-- gpadmin=# GRANT ALL ON DATABASE gemfire TO pivotal;
-- GRANT
-- \q
--
--
--
drop table if exists POS_DEVICE cascade;
create table POS_DEVICE (	
	id	bigint ,
	location text,
	merchant_name	text
) distributed by (id);		
 

drop table if exists TRANSACTION cascade;
create table TRANSACTION (		
	id bigint,
	device_id	 bigint,
	transaction_value decimal(10,2),
	account_id bigint,
	ts_millis bigint
) distributed by (id);


drop table if exists ZIP_CODES cascade;
CREATE TABLE ZIP_CODES 
	(ZIP char(5), LATITUDE double precision, LONGITUDE double precision, 
	CITY varchar, STATE char(2), COUNTY varchar, NAME varchar);
	
COPY zip_codes FROM '/home/gpadmin/zip_codes_states.csv' DELIMITER ',' CSV HEADER;

drop table if exists SUSPECT ;
create table SUSPECT (		
	transaction_id bigint,
	device_id	 bigint,	
	marked_suspect_ts_millis	 bigint,
	reason text,
	marked int
) distributed by (transaction_id);

---

create view suspect_view as (select t.*,marked_suspect_ts_millis,s.reason,s.marked 
from transaction t left join suspect s on t.id=s.transaction_id);


drop table if exists pos_data cascade;
create table pos_data as
with single_codes as (SELECT DISTINCT ON (z.county,z.state) z.latitude, z.longitude, z.county,z.name as state from zip_codes z where z.latitude is not null)
select p.*, z.latitude, z.longitude from pos_device p, single_codes z where 
split_part(p.location, ' ', 1) like z.county and trim(split_part(p.location, ',', 2)) like z.state;


drop view transaction_info;
create view transaction_info as
WITH suspect_device as (select t.device_id as device_id, count(t.id) as suspect_count from transaction t LEFT OUTER JOIN suspect s ON (t.id=s.transaction_id) group by t.device_id),
device_data as ( select d.*, suspect_device.suspect_count from pos_data d left outer join suspect_device on (suspect_device.device_id = d.id) ) ,
home_location as ( select distinct on (t.account_id) t.account_id  as account_id, count(p.location) as location_count,p.location as location from pos_data p, transaction t where p.id=t.device_id group by t.account_id, p.location order by t.account_id,count(p.location) desc  ),
home_latlong as (select h.account_id as account_id, p.latitude as latitude, p.longitude as longitude from home_location h, pos_data p where p.location=h.location),
avg_txn_value as (select account_id, avg(transaction_value) as avg_value from transaction group by account_id),
elapsed_time as (select id,coalesce((ts_millis)-lag(ts_millis) over (PARTITION BY account_id ORDER BY ts_millis asc) ,1) as elapsed_ms from transaction order by account_id,ts_millis desc),
prev_txn as (select t.id,lag(t.id) over (PARTITION BY account_id ORDER BY ts_millis asc) as prev_id, lag(p.longitude) over (PARTITION BY account_id ORDER BY ts_millis asc) as prev_longitude,lag(p.latitude) over (PARTITION BY account_id ORDER BY ts_millis asc) as prev_latitude from transaction t, pos_data p where p.id = t.device_id order by account_id,ts_millis desc)
select t.*,coalesce(s.marked,0) as marked,st_distance_sphere(st_point(h.longitude,h.latitude),st_point(d.longitude,d.latitude))/1000 as DistanceKM,
(t.transaction_value-a.avg_value)/a.avg_value as percentage,a.avg_value,et.elapsed_ms,coalesce(pr.prev_id,t.id) as prev_id,coalesce(st_distance_sphere(st_point(d.longitude,d.latitude),st_point(pr.prev_longitude,pr.prev_latitude))/1000,0) as txn_distance
, coalesce(st_distance_sphere(st_point(d.longitude,d.latitude),st_point(pr.prev_longitude,pr.prev_latitude))/1000 *.621371,0 ) ,(et.elapsed_ms::float /1000/60/60) as mph
from TRANSACTION t left join elapsed_time et on t.id=et.id , home_latlong h, device_data d, avg_txn_value a,suspect_view s,prev_txn pr
where t.account_id = h.account_id and t.device_id = d.id and t.account_id = a.account_id and h.account_id = a.account_id and t.account_id = a.account_id and t.id = s.id and pr.id = t.id and et.elapsed_ms>0 order by t.id;





 




