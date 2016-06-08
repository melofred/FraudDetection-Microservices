#!/bin/bash
echo "BEGINNING PREDICTION" >> /home/gpadmin/predict.log
/usr/local/greenplum-db/bin/psql -d gemfire -f /home/gpadmin/predict.sql -a -L /home/gpadmin/query.out
echo "COMPLETED PREDICTION" >> /home/gpadmin/predict.log

