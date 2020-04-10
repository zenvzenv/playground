#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. ~/.bash_profile
sed_cmd=$(which sed)
cp_cmd=$(which cp)
mkdir_cmd=$(which mkdir)
delay="+0 year"
year=$(date -d "${delay}" +"%Y")
[[ -d "${SQL_HOME}/create_table/iq_table_create_YYYY" ]] || ${mkdir_cmd} -p "${SQL_HOME}/create_table/iq_table_create_YYYY"
${cp_cmd} "${SQL_HOME}/iq_table_create_YYYY.sql" "${SQL_HOME}/create_table/iq_table_create_YYYY/iq_table_create_${year}.sql"
[[ -f "${SQL_HOME}/create_table/iq_table_create_YYYY/iq_table_create_${year}.sql" ]] && yyyy_file_path="${SQL_HOME}/create_table/iq_table_create_YYYY/iq_table_create_${year}.sql" || exit -255
${sed_cmd} -i "s/YYYY/${year}/g" ${yyyy_file_path}
iq_exec ${yyyy_file_path}