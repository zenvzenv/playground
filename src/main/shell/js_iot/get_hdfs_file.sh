#!/usr/bin/env bash
if [[ $# -lt 1 ]]; then
    echo "please enter time"
    exit
fi
hdfs_file_path_prefix="hdfs://cmhcluster/user/bdoc/10/services/hdfs/37/hive/"
file_array=(it_gb_sign_15m it_terminal_tac_sign it_terminal_tac_user it_lac_sign_15m it_lacci_user_15m it_s1_mme_sign_15m it_serip_user_15m it_s1_user_15m)
for name in ${file_array[@]} ; do
    if [[ -d ${name} ]]; then
        hdfs dfs -get ${hdfs_file_path_prefix}/${name}/$1/* ${name} &
    else
        mkdir ${name}
        hdfs dfs -get ${hdfs_file_path_prefix}/${name}/$1/* ${name} &
    fi
done
