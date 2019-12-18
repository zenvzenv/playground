#!/usr/bin/env bash
if [[ $# -lt 1 ]]; then
    echo "enter statengine or poor"
    exit
fi
type=$1
cd $(dirname $0)
BIN_HOME=$(pwd)
. ${BIN_HOME}/conf.sh
. conf.sh
delay="-1 day"
rm_date=""
rm_cmd=$(which rm)
if [[ "statengine" == ${type} ]]; then
    rm_date=$(date -d "${delay}" +"%Y/%m/%d/%H")
    DATA_HOME="${DATA_HOME}/dataimport/cp/statcenter"
elif [[ "poor" == ${type} ]]; then
    rm_date=$(date -d "${delay}" +"%Y/%m/%d/%H/%M")
    DATA_HOME="${DATA_HOME}/fromoracle/poor"
else
    exit
fi
echo "will rm ${DATA_HOME}/${rm_date}"
${rm_cmd} -r "${DATA_HOME}/${rm_date}"
