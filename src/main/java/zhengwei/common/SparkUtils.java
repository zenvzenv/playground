package zhengwei.common;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.util.Map;
import java.util.Objects;

/**
 * Spark工具类
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/20 19:16
 */
public enum SparkUtils {
    INSTANCE;

    /**
     * 获取JavaSparkContent
     *
     * @param appName 应用名称
     * @return jsc
     */
    public JavaSparkContext getJsc(String appName) {
        //可以覆盖之前输出的文件夹
        SparkConf conf = new SparkConf().setAppName(appName).setMaster("local[*]").set("spark.hadoop.validateOutputSpecs", "false");
        return new JavaSparkContext(conf);
    }

    /**
     * 获取SparkSession
     *
     * @param appName 应用名称
     * @return SparkSession
     */
    public SparkSession getSparkSession(String appName) {
        return this.getSparkSession(appName, null);
    }

    public SparkSession getSparkSession(String appName, Map<String, String> config) {
        SparkSession.Builder sessionBuilder = SparkSession
                .builder()
                .master("local[*]")
                .appName(appName)
                .config("spark.hadoop.validateOutputSpecs", "false");
        if (!Objects.isNull(config)) {
            config.forEach(sessionBuilder::config);
        }
        return sessionBuilder
                .getOrCreate();
    }
}
