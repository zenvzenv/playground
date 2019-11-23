#!/usr/bin/env bash
#delay time
delay=30
#delay date
date_delay=$(date -d "-${delay} minute" "+%Y/%m/%d/%H/%M")
#test date
#date_delay=2019/09/25/08/15
local_netflow_path_pre=/export/home/process/netflow/output
hdfs_file_pre=/user/asiainfo/idc/data/source/netflow
netflow_pre=netflowv5
netflow_suffix=source-file-node-1
#year
year=$(echo ${date_delay} | awk -F'/' '{print $1}')
#month
month=$(echo ${date_delay} | awk -F'/' '{print $2}')
#day
day=$(echo ${date_delay} | awk -F'/' '{print $3}')
#hour
hour=$(echo ${date_delay} | awk -F'/' '{print $4}')
#minute
minute=$(echo ${date_delay} | awk -F'/' '{print $5}')
netflow_time=${year}${minute}${day}${hour}${minute}
echo "================================${netflow_pre}.${netflow_time}==========================================="
hdfs_file_full_path=$(echo ${hdfs_file_pre}/${year}/${month}/${day}/${hour}/${minute})
echo "hdfs path==>${hdfs_file_full_path}"
echo "netflow time suffix -> ${netflow_time}"
local_file_pre_path=${local_netflow_path_pre}/${year}/${month}/${day}/${hour}/${minute}
local_file_full_path=${local_file_pre_path}/*
echo "local netflow path==>${local_file_full_path}"
if [[ -d ${local_file_pre_path} ]]; then
    echo "local file directory is exists -> ${local_file_pre_path}"
    hdfs dfs -test -d ${hdfs_file_full_path}
    if [[ $? -ne 0 ]]; then
        echo "directory is not exists!"
        echo "start to create hdfs directory"
        hdfs dfs -mkdir -p ${hdfs_file_full_path}
        echo "create hdfs directory is finish"
    else
        echo "hdfs directory is already exists!"
    fi
    echo "ready to upload ${local_file_full_path} to ${hdfs_file_full_path}"
    echo "${netflow_pre}.${netflow_time} start time $(date +"%Y%m%d%H%M")"
    echo "start to upload ${local_file_full_path} to ${hdfs_file_full_path}"
    hdfs dfs -put ${local_file_full_path} ${hdfs_file_full_path}
    echo "upload ${local_file_full_path} to ${hdfs_file_full_path} is finish!"
    echo "${netflow_pre}.${netflow_time} end time $(date +"%Y%m%d%H%M")"
else
    echo "${local_file_pre_path} local file directory is not exist"
fi
echo "=================================${netflow_pre}.${netflow_time}=========================================="