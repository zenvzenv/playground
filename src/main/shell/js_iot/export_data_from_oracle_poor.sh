#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
#########################################
ls_cmd=$(which ls)
cat_cmd=$(which cat)
scp_cmd=$(which scp)
sed_cmd=$(which sed)
cp_cmd=$(which cp)
########################################
spool_file_pattern="poor.sql$"

start_delay="-8 hour"
year=$(date -d"${start_delay}" +"%Y")
mouth=$(date -d"${start_delay}" +"%m")
day=$(date -d"${start_delay}" +"%d")
hour=$(date -d"${start_delay}" +"%H")
minute=$(date -d"${start_delay}" +"%M")

start_time=$(date -d "${start_delay}" +"%Y-%m-%d %H:%M:00")
end_delay="-8 hour +15 minute"
end_time=$(date -d "${end_delay}" +"%Y-%m-%d %H:%M:00")
target_file_path_time="${year}/${mouth}/${day}/${hour}/${minute}"
file_list=""
[[ $# -lt 1 ]] && file_list=$(${ls_cmd} "${SQL_HOME}/poor" | egrep "${spool_file_pattern}") || file_list=$@
for table_spool in ${file_list} ; do
    ${cp_cmd} "${SQL_HOME}/poor/${table_spool}" "${SQL_HOME}/poor/temp/${table_spool}.${year}${mouth}${day}${hour}${minute}"
    spool_file="${SQL_HOME}/poor/temp/${table_spool}.${year}${mouth}${day}${hour}${minute}"
    ${sed_cmd} -i "s/YYYY/${year}/g" ${spool_file}
    ${sed_cmd} -i "s/MM/${mouth}/g" ${spool_file}
    ${sed_cmd} -i "s/DD/${day}/g" ${spool_file}
    ${sed_cmd} -i "s/HH/${hour}/g" ${spool_file}
    ${sed_cmd} -i "s/MIN/${minute}/g" ${spool_file}
    ${sed_cmd} -i "s/START_TIME/${start_time}/g" ${spool_file}
    ${sed_cmd} -i "s/END_TIME/${end_time}/g" ${spool_file}
    export_oracle ${spool_file}
    [[ -d "${DATA_HOME}/poor" ]] || mkdir "${DATA_HOME}/poor"
    export_oracle_file="${DATA_HOME}/poor/${table_spool%_*}"
    final_file="${export_oracle_file%_*}_${year}${mouth}${day}${hour}${minute}.txt"
    echo ${export_oracle_file}
    if [[ -f ${final_file} ]]; then
        expect -c"
            set timeout -1
            spawn ${scp_cmd} ${final_file} ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/poor/${target_file_path_time}
            expect {
              \"*yes/no\" { send \"yes\r\"; exp_continue }
              \"*password\" { send \"${SCP_PASSWORD}\n\" }
              }
            expect eof"
    fi
done
