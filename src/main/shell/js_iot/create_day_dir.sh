#!/usr/bin/env bash
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
mkdir_cmd=$(which mkdir)
delay="+1 day"
dir_path_time=$(date -d "${delay}" +"%Y/%m/%d")
new_path=""
#create state engine day dir
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
#create all net poor quality day dir
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
#create special table day dir
special=("it_gb_sign_15m" "it_lac_sign_15m" "it_s1_user_15m" "it_terminal_tac_sign" "it_lacci_user_15m" "it_s1_mme_sign_15m" "it_serip_user_15m" "it_terminal_tac_user")
for tableName in ${special[@]};
do
    special_path="${DATA_HOME}/dataimport/cp/statcenter/${tableName}/${dir_path_time}/"
    for (( hour = 0; hour < 24; hour++ )); do
        if [[ ${hour} -lt 10 ]]; then
    #            echo "${new_path}/0${hour}"
            ${mkdir_cmd} -p "${special_path}/0${hour}"
        else
    #            echo "${new_path}/${hour}"
            ${mkdir_cmd} -p "${special_path}/${hour}"
        fi
    done
done
