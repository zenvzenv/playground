#!/usr/bin/env bash
source ~/.bash_profile
#----------------------------------init start---------------------------------#
last_hour_time=`date -d "-2hour" +%Y/%m/%d/%H/%M`
export last_hour_day_yyyy=`echo $last_hour_time |  awk  -F'/'  '{print  $1}'`
export last_hour_day_mm=`echo $last_hour_time |  awk  -F'/'  '{print  $2}'`
export last_hour_day_m=`echo $last_hour_time |  awk  -F'/'  '{print  $2}' |  sed 's/^0//g'`
export last_hour_day_dd=`echo $last_hour_time |  awk  -F'/'  '{print  $3}'`
export last_hour_day_d=`echo $last_hour_time |  awk  -F'/'  '{print  $3}' |  sed 's/^0//g'`
export last_hour_day_hh=`echo $last_hour_time |  awk  -F'/'  '{print  $4}'`
export last_hour_day_mi=`echo $last_hour_time |  awk  -F'/'  '{print  $5}'`
#-----------------------------------init end----------------------------------#
cd /sybiq/niedm
#---------------------------------config start--------------------------------#
export load_table_config_file=./load_table_config_file.ini
export log_path="load_$last_hour_day_yyyy$last_hour_day_mm$last_hour_day_dd.log"
export sql_file_path="load_table_$last_hour_day_yyyy$last_hour_day_mm$last_hour_day_dd.sql"
export sybaseiq_db_conn="isql64 -Uasiainfo -PAsiaInfo2019 -Slocaldb -w200"
#---------------------------------config end ----------------------------------#

#conf_file check

if [[ -f ${load_table_config_file} ]] ; then
    echo "load_table_config_file exist...,continue."
else
    echo "load_table_config_file file is not exist, exit."
    exit
fi

function load {
sed -i "s/YYYY/$last_hour_day_yyyy/g" $1
sed -i "s/MM/$last_hour_day_mm/g" $1
sed -i "s/DD/$last_hour_day_dd/g" $1
sql=$(cat $1)
echo "load语句：${sql}"
${sybaseiq_db_conn} <<EOF
set temporary option conversion_error='off'
go
${sql}
EOF
}

echo "-- $last_hour_day_yyyy-$last_hour_day_mm-$last_hour_day_dd --" > ${sql_file_path}
echo "-- $last_hour_day_yyyy-$last_hour_day_mm-$last_hour_day_dd --" > ${log_path}

echo "read load_table_config_file..."
config_num=0
file_num=0
while read LINE
do
    if [[ ${LINE} == "#"* ]] ; then
        continue
    fi
    let config_num+=1
    echo "正在读取${load_table_config_file}的第${config_num}条配置信息:$LINE" >> ${log_path}
    # 配置文件内容
    export file_store_root_path=`echo ${LINE} |  awk  -F';'  '{print  $1}'`
    export sql_file=`echo ${LINE} |  awk -F';'  '{print  $2}'`
    #file_store_path=$file_store_root_path"/$last_hour_day_yyyy/$last_hour_day_mm/06/10/00/"
    file_store_path=$file_store_root_path"/$last_hour_day_yyyy/$last_hour_day_mm/$last_hour_day_dd/$last_hour_day_hh/$last_hour_day_mi/"
    echo "读取${file_store_path}路径下的文件，生成load语句文件" >> ${log_path}
    cnt=`ls ${file_store_path} | wc -l`
    if [[ ${cnt} -eq 0 ]]; then
        echo "${file_store_path}路径为空" >> ${log_path}
    else
        for line in `ls ${file_store_path}`
        do
            #if [[ -f $line ]];then
                #ilet file_num+=1
                echo "扫描${file_store_path}路径下文件：$line" >> ${log_path}
                sql_pre=`cat ${sql_file}`
                echo "$sql_pre" >> ${sql_file_path}
                echo "from '$file_store_path$line' escapes off quotes off" >> ${sql_file_path}
                echo "go" >> ${sql_file_path}
            #else
            #    echo "$file_store_path路径下$line不是文件" >> $log_path
            #fi
        done
    fi
done < ${load_table_config_file}
echo "生成load sql文件:${sql_file_path}"
echo "读取${load_table_config_file}完毕,开始执行load语句"

load ${sql_file_path} > ./log/load/sybaseiq_load_`date "+%Y%m%d_%H%M%S"`.log
mv ${sql_file_path} ./log/load/${sql_file_path}_`date "+%Y%m%d_%H%M%S"`
mv ${log_path} ./log/load/${log_path}_`date "+%Y%m%d_%H%M%S"`
echo "load end..."