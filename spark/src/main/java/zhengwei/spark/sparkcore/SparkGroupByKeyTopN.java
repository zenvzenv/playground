package zhengwei.spark.sparkcore;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import zhengwei.util.common.SysUtils;

import java.util.Iterator;

/**
 * 取出每个分组的Top N
 * 因为在生产环境中,groupByKey之后产生的KVRDD很有可能会数据倾斜，一个key
 * @author zhengwei AKA Sherlock
 * @since 2019/5/26 10:23
 */
public class SparkGroupByKeyTopN {
    public static void main(String[] args) {
        if (SysUtils.isNull(args)) return;
        SparkConf conf=new SparkConf().setAppName("SparkGroupByKeyTopN").setMaster("local");
        JavaSparkContext jsc=new JavaSparkContext(conf);
        JavaRDD<String> lines = jsc.textFile(args[0]);
        JavaPairRDD<String, Integer> clazzScorePairRDD = lines.mapToPair(line -> {
            String[] split = line.split("[ ]");
            return new Tuple2<>(split[0], Integer.parseInt(split[1]));
        });
        clazzScorePairRDD.groupByKey().foreach(record->{
            String clazzName = record._1;
            Iterator<Integer> iterator = record._2.iterator();
            int[] top3=new int[3];
            while (iterator.hasNext()){
                Integer score = iterator.next();
                for (int i=0;i<top3.length;i++){
                    if (SysUtils.isNull(top3[i])){
                        top3[i]=score;
                        break;
                    } else if (score>top3[i]) {
                        for (int j=2;j>i;j--){
                            top3[j]=top3[j-1];
                        }
                        top3[i]=score;
                        break;
                    }
                }
            }
            System.out.println("class name:"+clazzName);
            for (Integer score:top3){
                System.out.println("score:"+score);
            }
        });
    }
}
