#!/usr/bin/env bash
. /sybiq/SYBASE.sh
. /sybiq/IQ.sh
sybase_iq_db_conn="isql64 -Uasiainfo -PAsiaInfo2019 -Slocaldb -w200"
until [[ $# -eq 0 ]]
do
    need_drop_table=$1
    echo
    ${sybase_iq_db_conn}
    shift
done