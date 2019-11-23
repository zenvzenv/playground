#!/usr/bin/env bash
#kafka生产者命令
/usr/lib/kafka/bin/kafka-console-producer.sh --topic  r_lte_s1u_http --broker-list 10.11.142.242:6667,10.11.142.243:6667,10.11.142.244:6667
#kafka消费者命令
/usr/lib/kafka/bin/kafka-console-consumer.sh --bootstrap-server YP-TYHJ-APOLLO4200-42244:6667 --topic r_lte_s1u_http --from-beginning
#查询出的数据进行过滤输出
hdfs dfs -cat hdfs://cmhcluster/user/bdoc/10/services/hdfs/37/analyze/enhance/lte/*/2019/04|awk '{if($1!=0){print $0;}}'
#对查询出的数据进行计算并输出
hdfs dfs -cat /user/asiainfo/ys_data/cmnet/lw_jk/JK_100_2019032620*|awk -F ' ' '{total+=$23;sum+=$24} END{print total/1024/1024/1024,sum/1024/1024/1024}'
#spark提交样例Pi命令
spark-submit --class org.apache.spark.examples.JavaSparkPi --master yarn-client ${SPARK_HOME}/jars/spark-examples_2.11-2.2.0-cdh6.0.1.jar 10
#获取yarn运行日志
yarn log -applicationId xxx
#获取正在运行的application
yarn application -list -appStates RUNNING
#yarn查看队列的资源状态
yarn queue -status root.user.user
#计算时间
date -d "7 day ago" +"%Y/%m/%d/%H/%M"
#对于给定的时间来计算时间
date -d "20190718 2 day ago" +"%Y%m%d%H%M"
#修改文件字符集
iconv -f {转换前字符集} -t {转换后的字符集} inputfile -o outputfile
iconv -f utf-8 -t gb18030 a.txt -o b.txt
#查看最大深度1的文件大小
du -h --max-depth=1
#tar压缩
tar czvf FileName.tar DirName
#tar解压
tar zxvf FileName.tar
hdfs dfs -cat /user/asiainfo/idc/data/source/netflow/2019/10/22/13/50/netflowv5.201910221350.source-file-node-2_025|awk -F'|' '{gsub(/[[:blank:]]*/,"",$5)}'|head -10
hcat /user/asiainfo/idc/rca/process/r2r/trace/2019/10/29/part-00000| \
awk -F':' '{print $2}' |\
awk -F'|' '{
	for(i=1;i<=NF;i++){
		print $i
	}
}' |\
grep -v '*' | awk -F '\^' \
'
BEGIN{
	name=0; port=0; total=0
}
{
	total+=1;
	if($1==$2) port+=1;
	else name+=1;
}
END{
	print "backfill rate="name/total
}
'

hcat /user/asiainfo/idc/rca/process/r2r/trace/2019/10/29/part-00000| \
awk -F':' '{print $2}' |\
awk -F'|' '{
	for(i=1;i<=NF;i++){
		print $i
	}
}' |\
grep -v '*' | awk -F '\^' \
'
{if($1==$2) print $2}
' |\
sort -u > ~/zhengwei/tracepreprocess/data/port.txt

#输出一条记录中按特定字符分割的所有字段
hcat /user/asiainfo/idc/rca/process/r2r/trace/2019/10/29/part-00000| \
awk -F':' '{print $2}' |\
awk -F'|' '{
	for(i=1;i<=NF;i++){
		print $i
	}
}' |\
grep -v '*' | awk -F '\^' \
'
{if($1!=$2) print $2}
' |\
sort -u > ~/zhengwei/tracepreprocess/data/name.txt

hcat hdfs://cmhcluster/user/bdoc/10/services/hdfs/37/hive/it_lac_sign_15m/2019/11/04/00/*|awk -F'\1' '{if($3!=0 && $4!=0) lacci[$3"_"$4]+=1} END{for(i in lacci) {if(lacci[i]>4) printf "lacci->%s,count->%d\n",i,lacci[i]}}'