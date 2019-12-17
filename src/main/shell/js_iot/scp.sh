#!/usr/bin/env bash
source_dir_time=""
target_dir_time=""
if [[ $# -eq 2 ]]; then
    #test
    source_dir_time=$1
    target_dir_time=$2
else
    #pro
    delay="-4 hour -30 min"
    source_dir_time=$(date -d "${delay}" +"%Y/%m/%d/%H")
target_dir_time=$(date -d "${delay}" +"%Y/%m/%d")
fi
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
scp_cmd=$(which scp)
mkdir_cmd=$(which mkdir)
echo "${source_dir_time}" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%%H%M").log
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/scp/scp_$(date +"%Y%m%d").log
while read table_name
do
    if [[ x"" != x${table_name} ]]; then
        echo "table name -> ${table_name}"
        echo "${scp_cmd} -r ${SCP_SOURCE_FILE_PATH_PREFIX}/${table_name%_*}/${source_dir_time}/${SCP_FILE_NAME_KEY_WORD} ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/${target_dir_time}"
        expect -c"
            set timeout -1
            spawn ${scp_cmd} -r ${SCP_SOURCE_FILE_PATH_PREFIX}/${table_name%_*}/${source_dir_time}/ ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/${target_dir_time}
            expect {
              \"*yes/no\" { send \"yes\r\"; exp_continue }
              \"*password\" { send \"${SCP_PASSWORD}\n\" }
              }
            expect eof" >> ${LOGS_HOME}/scp/scp_$(date +"%Y%m%d").log
    fi
done < ${CONF_HOME}/table_name.conf
#### table_name_1.conf 中配置了 统计引擎输出在 /data/project/data/dataimport/cp/statcenter/YYYY/MM/DD/HH/中的表#####
echo "${scp_cmd} -r ${SCP_SOURCE_FILE_PATH_PREFIX}/${source_dir_time} ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/${target_dir_time}"
expect -c"
    set timeout -1
    spawn ${scp_cmd} -r ${SCP_SOURCE_FILE_PATH_PREFIX}/${source_dir_time} ${SCP_USER}@${SCP_IP}:${SCP_TARGET_FILE_PATH_PREFIX}/${target_dir_time}
    expect {
      \"*yes/no\" { send \"yes\r\"; exp_continue }
      \"*password\" { send \"${SCP_PASSWORD}\n\" }
      }
    expect eof" >> ${LOGS_HOME}/scp/scp_$(date +"%Y%m%d").log
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/scp/scp_$(date +"%Y%m%d").log