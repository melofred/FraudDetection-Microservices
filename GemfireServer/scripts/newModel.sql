drop table if exists POS_DEVICE cascade;
create table POS_DEVICE (	
	id	bigint ,
	location text,
	merchant_name	text
);

drop table if exists TRANSACTION cascade;
create table TRANSACTION (		
	id bigint,
	device_id	 bigint,
	transaction_value decimal(10,2),
	account_id bigint,
	ts_millis bigint
);

drop table if exists ZIP_CODES cascade;
CREATE TABLE ZIP_CODES 
	(ZIP char(5), LATITUDE double precision, LONGITUDE double precision, 
	CITY varchar, STATE char(2), COUNTY varchar, NAME varchar);

COPY transaction FROM '/tmp/transaction.csv' DELIMITER ',' CSV HEADER;	
COPY zip_codes FROM '/tmp/zip_codes_states.csv' DELIMITER ',' CSV HEADER;
COPY pos_device FROM '/tmp/pos_device.csv' DELIMITER ',' CSV HEADER;






--
select distinct on (t.id) account_id, location, latitude, longitude, transaction_value, ts_millis, device_id, t.id as transaction_id from transaction t INNER JOIN pos_device p ON (t.device_id = p.id) INNER JOIN zip_codes z ON (upper(regexp_replace(p.location, '\\s+County', '')) = upper(z.county || ', ' || z.name) ) where latitude IS NOT NULL and longitude IS NOT NULL and account_id<=30 order by t.id desc LIMIT 100000

select distinct on (id) id as device_id, location, latitude, longitude from pos_device p INNER JOIN (select latitude, longitude, county, name from zip_codes group by county, name, latitude, longitude) z ON (upper(regexp_replace(p.location, '\\s+County', '')) = upper(z.county || ', ' || z.name) ) group by device_id, location, latitude, longitude order by device_id, location