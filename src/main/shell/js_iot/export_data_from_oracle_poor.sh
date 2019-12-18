#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. ~/.bash_profile
#########################################
ls_cmd=$(which ls)
cat_cmd=$(which cat)
scp_cmd=$(which scp)
sed_cmd=$(which sed)
cp_cmd=$(which cp)
iconv_cmd=$(which iconv)
mv_cmd=$(which mv)
rm_cmd=$(which rm)
########################################
spool_file_pattern="poor.sql$"

delay="-8 hour"
year=$(date -d"${delay}" +"%Y")
mouth=$(date -d"${delay}" +"%m")
day=$(date -d"${delay}" +"%d")
hour=$(date -d"${delay}" +"%H")
minute=$(date -d"${delay}" +"%M")
cycle_time=$(date -d "${delay}" +"%Y-%m-%d %H:%M:00")

#test
#year="2019"
#mouth="12"
#day="18"
#hour="00"
#minute="15"
#cycle_time="${year}-${mouth}-${day} ${hour}:${minute}:00"
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
    ${sed_cmd} -i "s/CYCLE_TIME/${cycle_time}/g" ${spool_file}
    export_oracle ${spool_file}
    [[ -d "${DATA_HOME}/poor" ]] || mkdir "${DATA_HOME}/poor"
    export_oracle_file="${DATA_HOME}/poor/${table_spool%_*}"
    final_file="${export_oracle_file%_*}_${year}${mouth}${day}${hour}${minute}.txt"
    ${iconv_cmd} -f gb18030 -t utf-8 "${final_file}" -o "${final_file}.U8"
    ${rm_cmd} "${final_file}"
    ${mv_cmd} "${final_file}.U8" "${final_file}"
    echo ${final_file}
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
