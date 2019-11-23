package zhengwei.test;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.Test;
import scala.Tuple2;

/**
 * @author zhengwei AKA Awei
 * @since 2019/10/21 17:25
 */
public class SparkTest {
    @Test
    void test(){
        final SparkConf conf = new SparkConf().setAppName("test").setMaster("local[*]");
        new JavaSparkContext(conf).textFile("C:\\Users\\zw305\\Documents\\CRTDownload\\2*.txt")
                .mapToPair(line->{
                    long startIp = Long.parseLong(line.split("[|]")[0]);
                    return new Tuple2<>(startIp,line);
                })
                .sortByKey()
                .map(x->x._2)
                .coalesce(1)
                .saveAsTextFile("C:\\Users\\zw305\\Documents\\CRTDownload\\enter");
    }
}
