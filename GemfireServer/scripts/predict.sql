truncate table suspect;
insert into suspect  (select id,device_id,ts_millis,reason from fraud_view where fraud='t' );
