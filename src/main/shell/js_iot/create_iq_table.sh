#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
year=$(date +"%Y")
month=$(date +"%m")
day=$(date +"%d")
sybase_iq_db_conn="isql64 -Uasiainfo  -PAsiaInfo2019 -Slocaldb"
DATA_PATTERN="(YYYY|MM|DD).sql$"
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
        echo "-- create table time -> $(date +"%Y%m%d%H%M")" > ${SQL_HOME}/create_table/${sybase_iq_table_info%.*}/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.sql
        echo "-- sybase iq table -> ${sybase_iq_table_info}" > ${SQL_HOME}/create_table/${sybase_iq_table_info%.*}/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.sql
        ${cat_cmd} ${SQL_HOME}/${sybase_iq_table_info} >> ${SQL_HOME}/create_table/${sybase_iq_table_info%.*}/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.sql
        ${sed_cmd} -i "s/YYYY/${year}/g" ${SQL_HOME}/create_table/${sybase_iq_table_info%.*}/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.sql
        ${sed_cmd} -i "s/MM/${month}/g" ${SQL_HOME}/create_table/${sybase_iq_table_info%.*}/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.sql
        ${sed_cmd} -i "s/DD/${day}/g" ${SQL_HOME}/create_table/${sybase_iq_table_info%.*}/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.sql
        #exec create table sql
        ${sybase_iq_db_conn} -i ${SQL_HOME}/create_table/${sybase_iq_table_info%.*}/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.sql >> ${LOGS_HOME}/create_table/create_table_${sybase_iq_table_info%.*}_${year}${month}${day}.log
    else
        echo "no no no"
    fi
done < ${CONF_HOME}/sybase_iq_table.conf
