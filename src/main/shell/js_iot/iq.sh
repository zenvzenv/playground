#!/usr/bin/env bash
sybase_conn="isql64 -Uasiainfo -PAsiaInfo2019 -Slocaldb -w300"
############cmd############
rm_cmd=$(which rm)
more_cmd=$(which more)
more_cmd=$(which more)
rm_cmd=$(which rm)
############cmd############
showUsage(){
echo "Usage:"
echo "  $0 [sql_file] <-c:true/false> <-s:true/false>"
echo "  $0 [sql_file] -c -s : create result file and show result on terminal"
echo "  $0 [sql_file] -c <true/false> : true -> will create result file ; false -> will not create result file"
echo "  $0 [sql_file] -s <true/false> : true -> will show result on terminal ; false -> will not show result on terminal"
echo "  $0 [sql_file] -c <true/false> -s <true/false>"
echo "  $0 [sql_file] --sql your_pre_edited_sql , your sql should be surrounded by double quotes"
}
judge(){
    echo "$@"
    sql_file="$1"
    shift
    if [[ "-s" == $1 ]]; then
        shift
        if [[ "-c" == $1 ]]; then
            shift
            if [[ "" == $1 || "true" == $1 ]]; then
                ${sybase_conn} -i ${sql_file} -o ${result_file}
                ${more_cmd} ${result_file}
            else
                ${sybase_conn} -i ${sql_file}
            fi
        elif [[ "" == $1 || "true" == $1 ]]; then
            ${sybase_conn} -i ${sql_file}
        else
            ${sybase_conn} -i ${sql_file} > /dev/null
        fi
    elif [[ "-c" == $1 ]]; then
        shift
        if [[ "-s" == $1 ]]; then
            shift
            if [[ "" == $1 || "true" == $1 ]]; then
                ${sybase_conn} -i ${sql_file} -o ${result_file}
                ${more_cmd} ${result_file}
            else
                ${sybase_conn} -i ${sql_file} -o ${result_file}
            fi
        elif [[ "" == $1 || "true" == $1 ]]; then
            ${sybase_conn} -i ${sql_file} -o ${result_file}
        else
            ${sybase_conn} -i ${sql_file} > /dev/null
        fi
    else
        ${sybase_conn} -i ${sql_file}
    fi
}
decideResult(){
    if [[ "--sql" == $1 ]]; then
        shift
        sql=$1
        echo "${sql}" > temp
        echo "go" > temp
        judge temp $@
        ${rm_cmd} temp
    else
        judge $@
    fi
}
checkParam(){
    if [[ $# -lt 1 || "-h" == "$1" || "--help" == "$1" ]]; then
        echo "must enter sql file name!"
        showUsage
        exit
    elif [[ ("--sql" == "$1" && "-c" == "$2") || ("--sql" == "$1" && "-s" == "$2") || ("--sql" == "$1" && $# == 1) ]]; then
        echo "place enter your sql"
        exit
    fi
}
result_file="${sql_file%.*}.txt"
checkParam $@
decideResult $@
