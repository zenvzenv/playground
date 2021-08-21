package zhengwei.spark.broadcast;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import zhengwei.spark.common.SparkUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhengwei AKA Awei
 * @since 2020/4/21 11:36
 */
public class MyBroadcast {
    public static void main(String[] args) {
        final JavaSparkContext jsc = SparkUtils.INSTANCE.getJsc();
        final List<String> broadcastList = Arrays.asList("1", "2", "3", "4");
        final Broadcast<List<String>> broadcastRDD = jsc.broadcast(broadcastList);
        final JavaRDD<String> parallelizeRDD = jsc.parallelize(Arrays.asList("1", "2", "3", "4"));
        final JavaRDD<Boolean> resultRDD = parallelizeRDD.map(x -> {
            final List<String> broadcastValue = broadcastRDD.value();
            if (broadcastValue.contains(x)) {
                System.out.println(x);
            }
            return broadcastValue.contains((((x))));
        });
        resultRDD.foreach(System.out::print);
    }
}
