#!/usr/bin/env bash
. conf.sh
delay="-1 day"
rm_time=$(date -d "${delay}" +"%Y/%m/%d")
rm_cmd=$(which rm)
local_dir_prefix="/sybiq/project/data"
while read table_name
do
    echo "${rm_cmd} -r ${local_dir_prefix}/${table_name%_*}/${rm_time}"
    ${rm_cmd} -r ${local_dir_prefix}/${table_name%_*}/${rm_time}
done < ${CONF_HOME}/table_name.conf
