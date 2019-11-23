#!/usr/bin/env bash
local_path=$2
hdfs_path=$4
filename=$3

HDFS_PUT()
{
    put_localpath=$1
    put_hdfspath=$3
    put_filename=$2
    hdfs dfs -put ${put_localpath}/${put_filename} ${put_hdfspath}/${put_filename}
    #echo "hdfs dfs -put ${put_localpath}/${put_filename} ${put_hdfspath}/${put_filename}"
}

HDFS_GET()
{
    get_localpath=$1
    get_hdfspath=$3
    get_filename=$2
    hdfs dfs -get ${get_hdfspath}/${get_filename} ${get_localpath}/${get_filename}
    #echo "hdfs dfs -get ${get_hdfspath}/${get_filename} ${get_localpath}/${get_filename}"
}

HDFS_DIR_CHECK()
{
    hdfs dfs -test -e $1
    if [[ $? -eq 0 ]] ;then
        echo 'exist'
    else
        echo "hdfs path is not exist"
        echo "start to mkdir hdfs path -> $1"
        hdfs dfs -mkdir -p $1
        #echo "hdfs dfs -mkdir -p $1"
    fi
}

#get:1;put:2
FTP_TYPE=$1
if [[ ${FTP_TYPE} -eq 1 ]];then
    HDFS_GET ${local_path} ${filename} ${hdfs_path}
elif [[ ${FTP_TYPE} -eq 2 ]];then
    HDFS_DIR_CHECK ${hdfs_path}
    HDFS_PUT ${local_path} ${filename} ${hdfs_path}
    echo "put file result -> $?"
fi