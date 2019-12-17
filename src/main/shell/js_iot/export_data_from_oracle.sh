#!/usr/bin/env bash
if [[ $# -lt 1 ]]; then
    echo "enter type -> resource or poor"
    echo "export_data_from_oracle [resource|poor]"
    exit
fi
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
#################cmd#################
ls_cmd=$(which ls)
cat_cmd=$(which cat)
scp_cmd=$(which scp)
sed_cmd=$(which sed)
#################cmd#################
#find end with resource.sql or poor.sql or ...
type=$1
spool_file_pattern="${type}.sql$"
delay="-6 hour"
export_time=$(date -d "${delay}" +"%Y/%m/%d/%H/%M")
for table_spool in $(${ls_cmd} ${SQL_HOME} | egrep "${spool_file_pattern}") ; do
    spool_file="${SQL_HOME}/${table_spool}"
    ${sed_cmd} -i "s/YYYYMMDDHH/$(date +"%Y%m%d%H")" ${spool_file}
    export_oracle ${spool_file}
    export_oracle_file="${DATA_HOME}/${table_spool%_*}.txt"
    if [[ -f ${export_oracle_file} ]]; then
        expect -c"
            set timeout -1
            spawn ${scp_cmd} ${export_oracle_file} ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/${type}/${export_oracle_file}
            expect {
              \"*yes/no\" { send \"yes\r\"; exp_continue }
              \"*password\" { send \"${SCP_PASSWORD}\n\" }
              }
            expect eof"
    fi
done
