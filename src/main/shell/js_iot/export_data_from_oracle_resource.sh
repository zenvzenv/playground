#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
ls_cmd=$(which ls)
cat_cmd=$(which cat)
scp_cmd=$(which scp)
iconv_cmd=$(which iconv)
mv_cmd=$(which mv)
rm_cmd=$(which rm)
spool_file_pattern="resource.sql$"
file_list=""
[[ $# -lt 1 ]] && file_list=$(${ls_cmd} "${SQL_HOME}/resource" | egrep "${spool_file_pattern}") || file_list=$@
for table_spool in ${file_list} ; do
    spool_file="${SQL_HOME}/resource/${table_spool}"
    export_oracle ${spool_file}
    [[ -d "${DATA_HOME}/resource" ]] || mkdir "${DATA_HOME}/resource"
    export_oracle_file="${DATA_HOME}/resource/${table_spool%_*}.txt"
    ${iconv_cmd} -f gb18030 -t utf-8 "${export_oracle_file}" -o "${export_oracle_file}.U8"
    ${rm_cmd} "${export_oracle_file}"
    ${mv_cmd} "${export_oracle_file}.U8" "${export_oracle_file}"
    echo ${export_oracle_file}
    if [[ -f ${export_oracle_file} ]]; then
        expect -c"
            set timeout -1
            spawn ${scp_cmd} ${export_oracle_file} ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/resource/
            expect {
              \"*yes/no\" { send \"yes\r\"; exp_continue }
              \"*password\" { send \"${SCP_PASSWORD}\n\" }
              }
            expect eof"
    fi
done
