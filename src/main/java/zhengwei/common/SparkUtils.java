package zhengwei.common;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Spark工具类
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/20 19:16
 */
public class SparkUtils {
	public static JavaSparkContext getJsc(String appName) {
		//可以覆盖之前输出的文件夹
		SparkConf conf = new SparkConf().setAppName(appName).setMaster("local[*]").set("spark.hadoop.validateOutputSpecs", "false");
		return new JavaSparkContext(conf);
	}
}
