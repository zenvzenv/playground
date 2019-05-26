package zhengwei.spark;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import zhengwei.common.SysUtils;

import java.io.Serializable;

/**
 * spark二次排序
 * @author zhengwei AKA Sherlock
 * @since 2019/5/26 9:59
 */
public class SparkSecondarySort {
    public static void main(String[] args) {
        if (SysUtils.isNull(args)) return;
        SparkConf conf=new SparkConf().setMaster("local").setAppName("SparkSecondarySort");
        JavaSparkContext jsc=new JavaSparkContext(conf);
        JavaRDD<String> lines = jsc.textFile(args[0]);
        JavaPairRDD<SecondarySort, String> secondarySortStringJavaPairRDD = lines.mapToPair(line -> {
            String[] split = line.split("[ ]");
            return new Tuple2<>(new SecondarySort(Integer.parseInt(split[0]), Integer.parseInt(split[1])), line);
        });
        secondarySortStringJavaPairRDD.sortByKey(false).foreach(record-> System.out.println(record._2));
    }
}
@Data
@AllArgsConstructor
class SecondarySort implements Serializable,Comparable<SecondarySort> {
    private int first;
    private int secondary;

    @Override
    public int compareTo(SecondarySort o) {
        if (this.first-o.getFirst()==0){
            return this.secondary-o.getSecondary();
        } else {
            return this.first-o.getFirst();
        }
    }
}