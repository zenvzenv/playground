package zhengwei.spark.sparksql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * @author zhengwei AKA zenv
 * @since 2020/12/17 11:46
 */
public class SparkSQLTest {
    public static void main(String[] args) {
        final SparkConf conf=new SparkConf();
        conf.setAppName("spark sql test");
        conf.setMaster("local[*]");
        final JavaSparkContext jsc=new JavaSparkContext();

    }
}
