package zhengwei.spark.sparkstreaming;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scala.Tuple2;

import java.util.*;

/**
 * @author zhengwei AKA Awei
 * @since 2019/8/6 9:26
 */
public class SparkStreamingTest {
	private static JavaSparkContext jsc;
	private static JavaStreamingContext jssc;

	@BeforeAll
	static void init() {
		SparkConf conf = new SparkConf().setAppName("SparkStreamingTest").setMaster("local[2]");
		jsc = new JavaSparkContext(conf);
		jssc = new JavaStreamingContext(jsc, Durations.seconds(5));
	}

	@Test
	void sparkStreamingWordCount() throws InterruptedException {
//		JavaReceiverInputDStream<String> lines = jssc.socketTextStream("202.102.120.41", 8888);
		JavaReceiverInputDStream<String> lines = jssc.socketTextStream("10.21.17.121", 8888);
		JavaDStream<String> words = lines.flatMap(line -> Arrays.asList(line.split("[ ]")).iterator());
		JavaPairDStream<String, Integer> pairs = words.mapToPair(word -> new Tuple2<>(word, 1));
		JavaPairDStream<String, Integer> result = pairs.reduceByKey(Integer::sum);
		result.print();
		jssc.start();
		jssc.awaitTermination();
	}

	@Test
	void sparkStreamingKafkaWordCount() throws InterruptedException {
		Collection<String> topic = new HashSet<>();
		topic.add("test");
		Map<String, Object> kafkaParam = new HashMap<>();
		kafkaParam.put("group.id", "test-consumer-group");
		kafkaParam.put("bootstrap.servers", "10.21.17.121:9092");
		kafkaParam.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaParam.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaParam.put("auto.offset.reset", "earliest");
		JavaInputDStream<ConsumerRecord<String, String>> kafkaStream = KafkaUtils.createDirectStream(
				jssc,
				LocationStrategies.PreferConsistent(),
				ConsumerStrategies.Subscribe(topic, kafkaParam)
		);
		JavaDStream<String> lines = kafkaStream.map(ConsumerRecord::value);
		JavaPairDStream<String, Integer> wordAndOne = lines.mapToPair(line -> new Tuple2<>(line.split("[ ]")[0], 1));
		JavaPairDStream<String, Integer> result = wordAndOne.reduceByKey(Integer::sum);
		result.print();
		jssc.start();
		jssc.awaitTermination();
	}
}
