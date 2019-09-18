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
delay=-40
date=$(date -d "${delay} min" +"%Y/%m/%d/%H/%M")
year=$(echo "${date}" | awk -F'/' '{print $1}')
mouth=$(echo "${date}" | awk -F'/' '{print $2}')
day=$(echo "${date}" | awk -F'/' '{print $3}')
hour=$(echo "${date}" | awk -F'/' '{print $4}')
minute=$(echo "${date}" | awk -F'/' '{print $5}')
source_hive_table_name="tc_netflow_all"
target_hive_table_name="tc_netflow_all_es_${year}${mouth}${day}"
fields="cycle_time,dura,source_ip,destination_ip,prtcl_id,source_port,destination_port,
        down_pkts,down_flux,down_flux_speed,up_pkts,up_flux,up_flux_speed,prov_id,opt_id,
        busi_id,data_center_id,router_ip,in_port_num,out_port_num,in_port_encoded,
        out_port_encoded,application_prtcl_name,source_as,dest_as,next_ip,bpp"
sql="insert into table ${target_hive_table_name} select ${fields} from ${source_hive_table_name} where yyyy=${year} and mm=${mouth} and dd=${day} and hh=${hour} and mi=${minute};"
echo "load data sql -> ${sql}" > ${SQL_HOME}/load_data_sql_$(date +"%Y%m%d%H%M").sql
/opt/cloudera/parcels/CDH-6.0.1-1.cdh6.0.1.p0.590678/bin/hive "$@"  --hiveconf mapreduce.job.queuename=root.hive_queue -e "${sql}" >> ${LOG_HOME}/import_into_es_hive_$(date +"%Y%m%d%H%M").log 2>&1 &