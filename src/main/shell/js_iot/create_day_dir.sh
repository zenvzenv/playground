#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
mkdir_cmd=$(which mkdir)
delay="+1 day"
table_time=$(date -d "${delay}" +"%Y/%m/%d")
while read table_name
do
    echo "table name -> ${table_name}"
    ${mkdir_cmd} -p ${DATA_HOME}/${table_name%_*}/${table_time}
done < ${CONF_HOME}/table_name.conf