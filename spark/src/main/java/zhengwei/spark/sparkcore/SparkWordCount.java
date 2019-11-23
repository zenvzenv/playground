package zhengwei.spark.sparkcore;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import zhengwei.util.common.SysUtils;

import java.util.Arrays;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019.04.26.11.42
 */
public class SparkWordCount {
    public static void main(String[] args) {
        if (SysUtils.isNull(args)) {
            System.out.println("请输入要统计的文件路径");
            return;
        }
        SparkConf conf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile(args[0]);
        JavaRDD<String[]> map = lines.map(line -> line.split("[ ]"));
        System.out.println(map.collect().toString());
        JavaRDD<String> words = lines.flatMap(line -> Arrays.asList(line.split("[ ]")).iterator());
        JavaPairRDD<String, Integer> ones = words.mapToPair(word -> new Tuple2<>(word, 1));
        JavaPairRDD<String, Integer> count = ones.reduceByKey(Integer::sum);
        count.foreach(System.out::println);
    }
}
