#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
mkdir_cmd=$(which mkdir)
delay="+1 day"
dir_path_time=$(date -d "${delay}" +"%Y/%m/%d")
new_path=""
#create state engine day dir
while read table_name
do
    echo "table name -> ${table_name}"
    new_path="${DATA_HOME}/dataimport/cp/statcenter/${dir_path_time}"
    for (( hour = 0; hour < 24; hour++ )); do
        if [[ ${hour} -lt 10 ]]; then
#            echo "${new_path}/0${hour}"
            ${mkdir_cmd} -p "${new_path}/0${hour}"
        else
#            echo "${new_path}/${hour}"
            ${mkdir_cmd} -p "${new_path}/${hour}"
        fi
    done
done < ${CONF_HOME}/table_name.conf
#create all net poor quality day dir
while read poor_quality_table_name
do
    echo "poor quality table -> ${poor_quality_table_name}"
    for (( hour = 0; hour < 24; hour++ )); do
        if [[ ${hour} -lt 10 ]]; then
            new_path="${DATA_HOME}/fromoracle/poor/${dir_path_time}/0${hour}"
            for (( minute = 0; minute < 60; minute+=15 )); do
                if [[ ${minute} -lt 10 ]]; then
#                    echo "${new_path}/0${minute}"
                    ${mkdir_cmd} -p "${new_path}/0${minute}"
                else
#                    echo "${new_path}/${minute}"
                    ${mkdir_cmd} -p "${new_path}/${minute}"
                fi
            done
        else
            new_path="${DATA_HOME}/fromoracle/poor/${dir_path_time}/${hour}"
            for (( minute = 0; minute < 60; minute+=15 )); do
                if [[ ${minute} -lt 10 ]]; then
#                    echo "${new_path}/0${minute}"
                    ${mkdir_cmd} -p "${new_path}/0${minute}"
                else
#                    echo "${new_path}/${minute}"
                    ${mkdir_cmd} -p "${new_path}/${minute}"
                fi
            done
        fi
    done
done < ${CONF_HOME}/poor_quality.conf
