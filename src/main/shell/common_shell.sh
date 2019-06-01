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