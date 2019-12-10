#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. conf.sh
delay="-1 day"
rm_date=$(date -d "${delay}" +"%Y/%m/%d/%H")
rm_cmd=$(which rm)
while read table_name
do
    echo "table name -> ${table_name%_*}"
    echo "will rm ${DATA_HOME}/${table_name%_*}/${rm_date}"
    ${rm_cmd} -r ${DATA_HOME}/${table_name%_*}/${rm_date}
done < ${CONF_HOME}/table_name.conf
