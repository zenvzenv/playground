#!/usr/bin/env bash
if [[ $# -lt 1 ]]; then
    echo "type -> statengine , poor"
    exit
fi
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. /sybiq/SYBASE.sh
. /sybiq/IQ.sh
#. conf.sh
type=$1
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
minute=$(date -d "${delay}" +"%M")
#test
year="2019"
month="12"
day="17"
hour="03"
minute="55"
if [[ "poor" == ${type} ]]; then
    type_file_time=${year}/${month}/${day}/${hour}/${minute}
    DATA_HOME="${DATA_HOME}/fromoracle/poor"
elif [[ "statengine" == ${type} ]]; then
    type_file_time=${year}/${month}/${day}/${hour}
    DATA_HOME="${DATA_HOME}/dataimport/cp/statcenter"
fi
file_pattern="(YYYY|MM|DD).conf$"
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
for table_load_conf_file in $(${ls_cmd} ${CONF_HOME}/${type} | egrep "${file_pattern}") ; do
    table_load_conf__file_full_path="${CONF_HOME}/${type}/${table_load_conf_file}"
    echo "${table_load_conf__file_full_path}"
    #${table_load_conf_file%_*}->it_gb_sign_15m...
    if [[ ! -d ${SQL_HOME}/load_data/${table_load_conf_file%_*} ]]; then
        ${mkdir_cmd} -p ${SQL_HOME}/load_data/${table_load_conf_file%_*}
    fi
    ${cp_cmd} ${table_load_conf__file_full_path} ${SQL_HOME}/load_data/${table_load_conf_file%_*}/
    cp_load_file="${SQL_HOME}/load_data/${table_load_conf_file%_*}/${table_load_conf_file}"
    #replace load_data_path_prefix YYYY MM DD
    ${sed_cmd} -i "s/YYYY/${year}/g" ${cp_load_file}
    ${sed_cmd} -i "s/MM/${month}/g" ${cp_load_file}
    ${sed_cmd} -i "s/DD/${day}/g" ${cp_load_file}
    ${sed_cmd} -i "s/HOUR/${hour}/g" ${cp_load_file}
    if [[ "poor" == ${type} ]]; then
        ${sed_cmd} -i "s/MINUTE/${minute}/g" ${cp_load_file}
    fi
    file_name=$(${ls_cmd} ${DATA_HOME}/${type_file_time}/ | grep "${table_load_conf_file%_*}")
    for name in ${file_name} ; do
        ${sed_cmd} -i "s/FILE_NAME/${name}/g" ${cp_load_file}
        echo "the ${DATA_HOME}/${type_file_time}/${name} has $(${cat_cmd} ${DATA_HOME}/${type_file_time}/${name} | wc -l) records" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
        ${sybase_iq_db_conn} -i ${cp_load_file} >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
    done
done
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
