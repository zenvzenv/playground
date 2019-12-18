#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. /sybiq/SYBASE.sh
. /sybiq/IQ.sh
delay="+1 day"
year=$(date -d "${delay}" +"%Y")
month=$(date -d "${delay}" +"%m")
day=$(date -d "${delay}" +"%d")
sybase_iq_db_conn="isql64 -Uasiainfo  -PAsiaInfo2019 -Slocaldb"
DATA_PATTERN=".sql$"
#start with '#' is not effective
IS_EFFECTIVE="^#"
#####################CMD###############################
cat_cmd=$(which cat)
sed_cmd=$(which sed)
mkdir_cmd=$(which mkdir)
#####################CMD###############################
while read sybase_iq_table_info
do
    if [[ ${sybase_iq_table_info} =~ ${IS_EFFECTIVE} ]]; then
        continue
    elif [[ ${sybase_iq_table_info} =~ ${DATA_PATTERN} ]]; then
        if [[ ! -d ${SQL_HOME}/create_table/${sybase_iq_table_info%.*} || ! -d  ${LOGS_HOME}/create_table ]]; then
            ${mkdir_cmd} -p ${SQL_HOME}/create_table/${sybase_iq_table_info%.*}
            ${mkdir_cmd} -p ${LOGS_HOME}/create_table
        fi
        sql_file="${SQL_HOME}/create_table/${sybase_iq_table_info%.*}/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.sql"
        echo "-- create table time -> $(date +"%Y%m%d%H%M")" > ${sql_file}
        echo "-- sybase iq table -> ${sybase_iq_table_info}" > ${sql_file}
        ${cat_cmd} ${SQL_HOME}/${sybase_iq_table_info} >> ${sql_file}
        ${sed_cmd} -i "s/YYYY/${year}/g" ${sql_file}
        ${sed_cmd} -i "s/MM/${month}/g" ${sql_file}
        ${sed_cmd} -i "s/DD/${day}/g" ${sql_file}
        #exec create table sql
        ${sybase_iq_db_conn} -i ${sql_file} >> ${LOGS_HOME}/create_table/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.log
    else
        echo "no no no"
    fi
done < ${CONF_HOME}/sybase_iq_table.conf
