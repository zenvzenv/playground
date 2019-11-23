package zhengwei.spark.sparkstreaming;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.*;

/**
 * Spark Streaming整合Kafka小案例
 * #!/bin/bash
 * cd $(dirname $0)
 * BIN_HOME=$(pwd)
 * LIB_HOME=$(cd ${BIN_HOME}/../lib; pwd)
 * APP_HOME=$(cd ${BIN_HOME}/..;pwd)
 * MAIN_CLASS=com.ai.cac.startup.JavaKafkaDemo
 * echo `pwd`
 * echo "BIN_HOME==>"${BIN_HOME}
 * echo "LIB_HOME==>"${LIB_HOME}
 * echo "APP_HOME==>"${APP_HOME}
 * current_date=`date  "+%Y%m%d%H%M"`
 * #str=`yarn application  -list -appStates  RUNNING | grep "cac-Flux"`
 * #clientname=`ps -ef | grep "ai-cac-cacoss" | grep -v grep`
 * #--keytab /tmp/kafka_client/asiainfostr.keytab –principal asiainfostr@SHHJPT01KDC \
 * /usr/lib/spark/bin/spark-submit \
 * --class ${MAIN_CLASS} \
 * --queue root.bdoc.asiainfo \
 * --master yarn \
 * --deploy-mode client \
 * --executor-memory 1G \
 * --num-executors  1 \
 * --executor-cores 1 \
 * --principal asiainfo@SHHJPT01KDC \
 * --keytab /home/asiainfo/asiainfo.keytab \
 * --conf spark.driver.extraClassPath=${BIN_HOME}/conf \
 * --conf spark.serializer=org.apache.spark.serializer.KryoSerializer \
 * ${LIB_HOME}/ai-cac-cacoss-4.1.0-SNAPSHOT-dependencies.jar yp-tyhj-apollo4200-42242:6667,yp-tyhj-apollo4200-42243:6667,yp-tyhj-apollo4200-42244:6667 r_lte_s1u_http 2>&1
 *
 * @author zhengwei AKA Sherlock
 * @since 2019/7/10 10:19
 */
public class JavaKafkaDemo {
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: JavaDirectKafkaWordCount <brokers> <topics>\n" +
					"  <brokers> is a list of one or more Kafka brokers\n" +
					"  <topics> is a list of one or more kafka topics to consume from\n\n");
			System.exit(1);
		}
		String brokers = args[0];
		String topics = args[1];
		SparkConf sparkConf = new SparkConf().setAppName("JavaDirectKafkaWordCount").setMaster("local[*]");
		JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(2));
		Set<String> topicsSet = new HashSet<>(Arrays.asList(topics.split("[,]")));
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("metadata.broker.list", brokers);
		kafkaParams.put("auto.offset.reset", "latest");
		kafkaParams.put("group.id", "test");
		kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaParams.put("bootstrap.servers", brokers);
		JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(
				jssc,
				LocationStrategies.PreferConsistent(),
				ConsumerStrategies.Subscribe(topicsSet, kafkaParams)
		);
		JavaDStream<String> lines = messages.map(ConsumerRecord::value);
		lines.print();
		jssc.start();
		jssc.awaitTermination();
	}
}
