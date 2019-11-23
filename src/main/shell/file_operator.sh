#!/usr/bin/env bash
#配置文件务必按照要求严格填写参数类型 IP     root密码  端口  本地文件绝对路径   目标地点绝对路径 文件名关键字（可不填，默认为*）
#scp脚本参数：1-->发送到远程机器  2--->从远程机器上获取
starttime=`date +'%Y-%m-%d %H:%M:%S'`
if [[ "$1" != "1" && "$1" != "2" ]];then
echo "scp_operator.sh parameter is error!!!"
exith
fi

home_path=$(pwd)

#配置时间时延 可自行修改
OFFSET_DAY=4
offset_time=$(date -d "${OFFSET_DAY} hours ago" "+%Y%m%d%H%M")
year=${offset_time:0:4}
month=${offset_time:4:2}
day=${offset_time:6:2}
hour=${offset_time:8:2}
minute=${offset_time:10:2}

i=0
cat /data08/home/iot/trans/ip.conf|while read line; do
if [[ ${line:0:1} != "#" ]];then
let i+=1
type=`awk 'BEGIN {split("'"$line"'",arr);print arr[1]}'`
if [[ "$type" == "scp" ]];then
ip=$(awk 'BEGIN {split("'"$line"'",arr);print arr[2]}')
user=$(awk 'BEGIN {split("'"$line"'",arr);print arr[3]}')
passwd=$(awk 'BEGIN {split("'"$line"'",arr);print arr[4]}')
port=$(awk 'BEGIN {split("'"$line"'",arr);print arr[5]}')
local_dir=$(awk 'BEGIN {split("'"$line"'",arr);print arr[6]}')
goal_dir=$(awk 'BEGIN {split("'"$line"'",arr);print arr[7]}')
file_name=$(awk 'BEGIN {split("'"$line"'",arr);print arr[8]}')
echo "正在处理第${i}条任务"
echo "ip->${ip},passwd->$passwd,port->${port},local_dir->${local_dir},goal_dir->${goal_dir}"
if [[ ! -d "${local_dir}/${year}/${month}/${day}/${hour}" ]];then
	echo "start to mkdir ${local_dir}/${year}/${month}/${day}/${hour}"
    mkdir -p ${local_dir}/${year}/${month}/${day}/${hour}
    echo "end to mkdir ${local_dir}/${year}/${month}/${day}/${hour},result is $?"
else
    echo "${local_dir}/${year}/${month}/${day}/${hour} is exsits"
fi
#echo "spawn scp  -r -p$port $local_dir/${file_name} root@$ip:$goal_dir"
#scp发送到远程机器上
if [[ "$1" == "1" ]];then
/usr/bin/expect <<-EOF
#set time 300
spawn bash -c "scp  -r -p${port} ${local_dir}/${year}/${month}/${day}/${hour}/${file_name} ${user}@${ip}:$goal_dir/${year}/${month}/${day}/${hour}"
expect {
"*yes/no" { send "yes\r"; exp_continue }
"*password:" { send "${passwd}\r" }
}
set timeout -1
expect 100%
expect eof
EOF
#scp到远程机器上取文件
elif [[ "$1" == "2" ]];then
/usr/bin/expect <<-EOF
#set time 300
set timeout -1
spawn bash -c "scp  -r -p${port} ${user}@$ip:${goal_dir}/${year}/${month}/${day}/${hour}/${file_name} ${local_dir}/${year}/${month}/${day}/${hour}"
expect {
"*yes/no" { send "yes\r"; exp_continue }
"*password:" { send "${passwd}\r" }
exp_continue
}
#set timeout -1
expect 100%
expect eof
EOF
fi
fi
fi
echo ${local_dir}/${year}/${month}/${day}/${hour}
hdfs_keyword=`echo ${local_dir}|awk -F '/' '{print $NF}'`
echo "hdfs keyword->${hdfs_keyword}"
hdfs_path=/user/bdoc/10/services/hdfs/37/hive/${hdfs_keyword}
echo "hdfs path->${hdfs_path}"
for file in $(ls ${local_dir}/${year}/${month}/${day}/${hour})
do
filename=$(echo ${file} | awk -F '/' '{print $NF}')
echo "local path -> ${local_dir}/${year}/${month}/${day}/${hour}"
echo "file name -> ${file_name}"
echo "hdfs path -> ${hdfs_path}/${year}/${month}/${day}/${hour}"
sh ${home_path}/hdfs_operate.sh 2 ${local_dir}/${year}/${month}/${day}/${hour} ${filename} ${hdfs_path}/${year}/${month}/${day}/${hour}

done
rm -rf ${local_dir}
done
endtime=$(date +'%Y-%m-%d %H:%M:%S')
start_seconds=$(date --date="$starttime" +%s);
end_seconds=$(date --date="$endtime" +%s);
echo "本次运行时间： "$(((end_seconds-start_seconds)))"s"