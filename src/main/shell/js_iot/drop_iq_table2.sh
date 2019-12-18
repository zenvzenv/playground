#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. /sybiq/SYBASE.sh
. /sybiq/IQ.sh
mkdir_cmd=$(which mkdir)
sed_cmd=$(which sed)
IS_ACTIVE="^#"
sybase_iq_db_conn="isql64 -Uasiainfo -PAsiaInfo2019 -Slocaldb -w200"
drop_sql_file=""
while read drop_info
do
    if [[ ${drop_info} =~ ${IS_ACTIVE} ]]; then
        continue
    fi
    table_name=$(echo "${drop_info}" | cut -f1 -d",")
    delay=$(echo "${drop_info}" | cut -f2 -d",")
    drop_table_time=$(date -d "-${delay} day" +"%Y%m%d")
    [[ -d ${SQL_HOME}/drop_table ]] || ${mkdir_cmd} -p ${SQL_HOME}/drop_table
    drop_sql_file_path="${SQL_HOME}/drop_table"
    drop_time=$(date +"%Y%m%d%H%M")
    drop_sql_file="${drop_sql_file_path}/drop_iq_table_${drop_time}.sql"
    echo "-- drop table time -> ${drop_time}" >> ${drop_sql_file}
    final_table_name=$(echo "${table_name}" | ${sed_cmd} "s/YYYYMMDD/${drop_table_time}/g")
    echo "drop table ${final_table_name};" >> ${drop_sql_file}
    echo "go" >> ${drop_sql_file}
done < ${CONF_HOME}/drop_table.conf
${sybase_iq_db_conn} -i ${drop_sql_file}
