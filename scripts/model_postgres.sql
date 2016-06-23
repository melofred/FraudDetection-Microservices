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

COPY transaction FROM 'transaction.csv' DELIMITER ',' CSV HEADER;	
COPY zip_codes FROM 'zip_codes_states.csv' DELIMITER ',' CSV HEADER;
COPY pos_device FROM 'pos_device.csv' DELIMITER ',' CSV HEADER;

