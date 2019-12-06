#!/usr/bin/env bash
. conf.sh
table_data_prefix="/sybiq/project/data"
table_data_file_path=""
mkdir_cmd=$(which mkdir)
table_time=$(date +"%Y/%m/%d")
while read table_name
do
    echo "table name -> ${table_name}"
    for (( hour = 0; hour < 24; hour++ )); do
        if [[ ${hour} -lt 10 ]]; then
            table_data_file_path="${table_data_prefix}/${table_name%_*}/${table_time}/0${hour}"
        else
            table_data_file_path="${table_data_prefix}/${table_name%_*}/${table_time}/${hour}"
        fi
        echo "${table_data_file_path}"
        ${mkdir_cmd} -p ${table_data_file_path}
    done
done < ${CONF_HOME}/table_name.conf
