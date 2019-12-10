#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
scp_cmd=$(which scp)
mkdir_cmd=$(which mkdir)
delay="-4 hour -30 min"
source_dir_time=$(date -d "${delay}" +"%Y/%m/%d/%H")
target_dir_time=$(date -d "${delay}" +"%Y/%m/%d")
echo "${source_dir_time}" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%%H%M").log
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/scp/scp_$(date +"%Y%m%d").log
while read table_name
do
    if [[ x"" != x${table_name} ]]; then
        echo "table name -> ${table_name}"
        echo "${scp_cmd} -r ${SCP_SOURCE_FILE_PATH_PREFIX}/${table_name%_*}/${source_dir_time}/${SCP_FILE_NAME_KEY_WORD} ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/${table_name%_*}/${target_dir_time}"
        expect -c"
            set timeout -1
            spawn ${scp_cmd} -r ${SCP_SOURCE_FILE_PATH_PREFIX}/${table_name%_*}/${source_dir_time}/ ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/${table_name%_*}/${target_dir_time}
            expect {
              \"*yes/no\" { send \"yes\r\"; exp_continue }
              \"*password\" { send \"${SCP_PASSWORD}\n\" }
              }
            expect eof" >> ${LOGS_HOME}/scp/scp_$(date +"%Y%m%d").log
    fi
done < ${CONF_HOME}/table_name.conf
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/scp/scp_$(date +"%Y%m%d").log