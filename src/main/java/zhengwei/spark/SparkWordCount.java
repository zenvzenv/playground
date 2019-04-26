package zhengwei.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019.04.26.11.42
 */
public class SparkWordCount {
	public static void main(String[] args) {
		SparkConf conf=new SparkConf().setAppName("SparkWordCount").setMaster("local[2]");
		JavaSparkContext sc=new JavaSparkContext(conf);
		JavaRDD<String> lines = sc.textFile("src/main/resources/input/word.zw");
		JavaRDD<String[]> map = lines.map(line->line.split(" "));
		System.out.println(map.collect().toString());
		JavaRDD<String> words = lines.flatMap(line->Arrays.asList(line.split(" ")).iterator());
		JavaPairRDD<String, Integer> ones = words.mapToPair(word->new Tuple2<>(word, 1));
		JavaPairRDD<String,Integer> count = ones.reduceByKey((x1,x2)->x1+x2);
		List<Tuple2<String,Integer>> collect = count.collect();
		for (Tuple2<String, Integer> tuple2 : collect) {
			System.out.println(tuple2._1()+"---"+tuple2._2());
		}
	}
}
