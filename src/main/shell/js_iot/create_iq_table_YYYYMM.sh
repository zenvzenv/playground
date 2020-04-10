#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. ~/.bash_profile
sed_cmd=$(which sed)
cp_cmd=$(which cp)
mkdir_cmd=$(which mkdir)
delay="+1 month"
year=$(date -d "${delay}" +"%Y")
month=$(date -d "${delay}" +"%m")
[[ -d "${SQL_HOME}/create_table/iq_table_create_YYYYMM" ]] || ${mkdir_cmd} -p "${SQL_HOME}/create_table/iq_table_create_YYYYMM"
${cp_cmd} "${SQL_HOME}/iq_table_create_YYYYMM.sql" "${SQL_HOME}/create_table/iq_table_create_YYYYMM/iq_table_create_${year}${month}.sql"
[[ -f "${SQL_HOME}/create_table/iq_table_create_YYYYMM/iq_table_create_${year}${month}.sql" ]] && yyyy_mm_file_path="${SQL_HOME}/create_table/iq_table_create_YYYYMM/iq_table_create_${year}${month}.sql" || exit -250
${sed_cmd} -i "s/YYYY/${year}/g" ${yyyy_mm_file_path}
${sed_cmd} -i "s/MM/${month}/g" ${yyyy_mm_file_path}
iq_exec ${yyyy_mm_file_path}