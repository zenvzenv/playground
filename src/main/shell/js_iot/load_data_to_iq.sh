#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. /sybiq/SYBASE.sh
. /sybiq/IQ.sh
#. conf.sh
sybase_iq_db_conn="isql64 -Uasiainfo -PAsiaInfo2019 -Slocaldb -w200"
###########################################
ls_cmd=$(which ls)
sed_cmd=$(which sed)
mkdir_cmd=$(which mkdir)
cp_cmd=$(which cp)
cat_cmd=$(which cat)
###########################################
delay="-5 hour"
year=$(date -d "${delay}" +"%Y")
month=$(date -d "${delay}" +"%m")
day=$(date -d "${delay}" +"%d")
hour=$(date -d "${delay}" +"%H")
#test
#year="2019"
#month="12"
#day="06"
#hour="03"
file_pattern="(YYYY|MM|DD).conf$"
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
for table_load_conf_file in $(${ls_cmd} ${CONF_HOME} | egrep "${file_pattern}") ; do
    echo "${CONF_HOME}/${table_load_conf_file}"
    #${table_load_conf_file%_*}->it_gb_sign_15m...
    if [[ ! -d ${SQL_HOME}/load_data/${table_load_conf_file%_*} ]]; then
        ${mkdir_cmd} -p ${SQL_HOME}/load_data/${table_load_conf_file%_*}
    fi
    ${cp_cmd} ${CONF_HOME}/${table_load_conf_file} ${SQL_HOME}/load_data/${table_load_conf_file%_*}/
    cp_load_file="${SQL_HOME}/load_data/${table_load_conf_file%_*}/${table_load_conf_file}"
    #replace YYYY MM DD
    ${sed_cmd} -i "s/YYYY/${year}/g" ${cp_load_file}
    ${sed_cmd} -i "s/MM/${month}/g" ${cp_load_file}
    ${sed_cmd} -i "s/DD/${day}/g" ${cp_load_file}
    ${sed_cmd} -i "s/HOUR/${hour}/g" ${cp_load_file}
    ${sed_cmd} -i "s/TABLE_NAME/${table_load_conf_file%_*}/g" ${cp_load_file}
    echo "${ls_cmd} ${DATA_HOME}/${table_load_conf_file%_*}/${year}/${month}/${day}/${hour}"
    file_name=$(${ls_cmd} ${DATA_HOME}/${table_load_conf_file%_*}/${year}/${month}/${day}/${hour})
    for name in ${file_name} ; do
        ${sed_cmd} -i "s/FILE_NAME/${name}/g" ${cp_load_file}
        echo "the ${DATA_HOME}/${table_load_conf_file%_*}/${year}/${month}/${day}/${hour}/${name} has $(${cat_cmd} ${DATA_HOME}/${table_load_conf_file%_*}/${year}/${month}/${day}/${hour}/${name} | wc -l) records" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
        ${sybase_iq_db_conn} -i ${cp_load_file} >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
    done
done
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
