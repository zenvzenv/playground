package zhengwei.spark.train;

import org.apache.spark.SparkConf;

/**
 * @author zhengwei AKA zenv
 * @since 2020/12/17 19:23
 */
public class Train1 {
    public static void main(String[] args) {
        final SparkConf conf=new SparkConf();
        conf.setMaster("local[*]");
        conf.setAppName("spark sql train1");

    }
}
