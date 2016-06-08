#!/bin/bash
echo "BEGINNING TRAINING" >> /home/gpadmin/train.log
/usr/local/greenplum-db/bin/psql -d gemfire -f /home/gpadmin/train.sql -a -L /home/gpadmin/train.out
echo "COMPLETED TRAINING" >> /home/gpadmin/train.log

