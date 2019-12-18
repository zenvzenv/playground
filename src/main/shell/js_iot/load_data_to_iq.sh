#!/usr/bin/env bash
showUsage(){
echo "Usage : "
echo "  $0 [statengine|poor] [1h|15m]"
}
if [[ $# -lt 2 ]]; then
    showUsage
    echo "type -> statengine,poor"
    echo "cycle time -> 1h,15m"
    exit
fi
type=$1
cycle_time=$2
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. /sybiq/SYBASE.sh
. /sybiq/IQ.sh
#. conf.sh
sybase_iq_db_conn="isql64 -Uasiainfo -PAsiaInfo2019 -Slocaldb -w200"
###########################################
ls_cmd=$(which ls)
sed_cmd=$(which sed)
mkdir_cmd=$(which mkdir)
cp_cmd=$(which cp)
cat_cmd=$(which cat)
###########################################
delay=""
#全网质差数据
if [[ "poor" == ${type} ]]; then
    DATA_HOME="${DATA_HOME}/fromoracle/poor"
    delay="-9 hour"
#统计引擎数据
elif [[ "statengine" == ${type} ]]; then
    DATA_HOME="${DATA_HOME}/dataimport/cp/statcenter"
    delay="-5 hour"
fi
year=$(date -d "${delay}" +"%Y")
month=$(date -d "${delay}" +"%m")
day=$(date -d "${delay}" +"%d")
hour=$(date -d "${delay}" +"%H")
minute=$(date -d "${delay}" +"%M")
#test
year="2019"
month="12"
day="18"
hour="01"
minute="55"



#导入周期为15分钟
if [[ "15m" == ${cycle_time} ]]; then
    type_file_time=${year}/${month}/${day}/${hour}/${minute}
#导入周期为1小时
elif [[ "1h" == ${cycle_time} ]]; then
    type_file_time=${year}/${month}/${day}/${hour}
fi

#根据输入的周期来确定需要读取的文件
file_pattern="${cycle_time}.load$"
echo "##############################$(date +"%Y%m%d%H%M%S")######################################" >> ${LOGS_HOME}/load_data/load_data_$(date +"%Y%m%d").log
for table_load_conf_file in $(${ls_cmd} ${CONF_HOME}/${type} | egrep "${file_pattern}") ; do
    table_load_conf_file_full_path="${CONF_HOME}/${type}/${table_load_conf_file}"
    echo "${table_load_conf_file_full_path}"
    #${table_load_conf_file%_*}->it_gb_sign_15m...
    load_data_temp_sql_path="${SQL_HOME}/load_data/${table_load_conf_file%_*}"
    if [[ ! -d ${load_data_temp_sql_path} ]]; then
        ${mkdir_cmd} -p ${load_data_temp_sql_path}
    fi
    ${cp_cmd} ${table_load_conf_file_full_path} ${load_data_temp_sql_path}/
    cp_load_file="${load_data_temp_sql_path}/${table_load_conf_file}"
    #replace load_data_path_prefix YYYY MM DD
    ${sed_cmd} -i "s/YYYY/${year}/g" ${cp_load_file}
    ${sed_cmd} -i "s/MM/${month}/g" ${cp_load_file}
    ${sed_cmd} -i "s/DD/${day}/g" ${cp_load_file}
    ${sed_cmd} -i "s/HOUR/${hour}/g" ${cp_load_file}
    #对于15分钟粒度的导入
    if [[ "15m" == ${cycle_time} ]]; then
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
