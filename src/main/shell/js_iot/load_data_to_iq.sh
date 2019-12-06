#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
#. ${BIN_HOME}/conf.sh
. conf.sh
sybase_iq_db_conn="isql64 -Uasiainfo -PAsiaInfo2019 -Slocaldb -w200"
###########################################
ls_cmd=$(which ls)
sed_cmd=$(which sed)
mkdir_cmd=$(which mkdir)
cp_cmd=$(which cp)
###########################################
year=$(date +"%Y")
month=$(date +"%m")
day=$(date +"%d")
hour=$(date +"%H")
file_pattern="(YYYY|MM|DD).conf$"
for table_load_conf_file in $(${ls_cmd} ${CONF_HOME} | egrep "${file_pattern}") ; do
    echo "${CONF_HOME}/${table_load_conf_file}"
    if [[ ! -d ${SQL_HOME}/load_data/${table_load_conf_file%_*} ]]; then
        ${mkdir_cmd} -p ${SQL_HOME}/load_data/${table_load_conf_file%_*}
    fi
    ${cp_cmd} ${CONF_HOME}/${table_load_conf_file} ${SQL_HOME}/load_data/${table_load_conf_file%_*}/
    cp_load_file="${SQL_HOME}/load_data/${table_load_conf_file%_*}/${table_load_conf_file}"
    #replace YYYY MM DD
    ${sed_cmd} -i "s/YYYY/${year}/g" ${cp_load_file}
    ${sed_cmd} -i "s/MM/${month}/g" ${cp_load_file}
    ${sed_cmd} -i "s/DD/${day}/g" ${cp_load_file}
    echo "${ls_cmd} ${DATA_HOME}/${table_load_conf_file%_*}/${year}/${month}/${day}/${hour}"
    file_name=$(${ls_cmd} ${DATA_HOME}/${table_load_conf_file%_*}/${year}/${month}/${day}/${hour})
    for name in ${file_name} ; do
        ${sed_cmd} -i "s/FILE_PATH/${DATA_HOME}/${table_load_conf_file%_*}/${year}${month}/${day}/${hour}/${name}" ${cp_load_file}
        ${sybase_iq_db_conn} -i ${cp_load_file}
    done
done
