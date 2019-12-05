#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
table_data_prefix="/sybiq/project/data"
table_data_file_path=""
mkdir_cmd=$(which mkdir)
table_time=$(date +"%Y/%m/%d")
while read table_name
do
    echo "table name -> ${table_name}"
    ${mkdir_cmd} -p ${table_data_prefix}/${table_name%_*}/${table_time}
done < ${CONF_HOME}/table_name.conf