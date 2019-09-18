#!/usr/bin/env bash
cd "$(dirname ${0})"
BIN_HOME=$(pwd)
APP_HOME=$(cd ${BIN_HOME}/..; pwd)
SQL_HOME=$(cd ${APP_HOME}/sql; pwd)
LOG_HOME=$(cd ${APP_HOME}/logs; pwd)
echo "BIN_HOME->${BIN_HOME}"
echo "APP_HOME->${APP_HOME}"
echo "SQL_HOME->${SQL_HOME}"
echo "LOG_HOME->${LOG_HOME}"
delay=-1
table_date=$(date -d "${delay} day" +"%Y%m%d")
table_name="tc_netflow_all_${table_date}"
sql="drop table ${table_name}" >> ${SQL_HOME}/delete_es_table_${table_date}.sql
/opt/cloudera/parcels/CDH-6.0.1-1.cdh6.0.1.p0.590678/bin/hive "$@"  --hiveconf mapreduce.job.queuename=root.hive_queue -e "${sql}" >> ${LOG_HOME}/delete_es_hive_table_${table_date}.log 2>&1 &