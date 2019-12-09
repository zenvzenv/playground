package zhengwei.util.common;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

/**
 * Spark工具类
 *
 * @author zhengwei AKA Awei
 * @since 2019/8/20 19:16
 */
public enum SparkUtils {
    INSTANCE;

    private JavaSparkContext jsc;

    private SparkSession session;

    SparkUtils() {
        session = SparkSession.builder()
                .master("local[*]")
                .appName(System.getProperty("SPARK_APP_NAME") == null ? "SparkTest" : System.getProperty("SPARK_APP_NAME"))
                .config("spark.hadoop.validateOutputSpecs", "false")
                .getOrCreate();
        System.out.println("SparkSession init success");
        SparkContext sparkContext = session.sparkContext();
        jsc = new JavaSparkContext(sparkContext);
        System.out.println("JavaSparkContext init success");
    }

    /**
     * 获取JavaSparkContent
     *
     * @return jsc
     */
    public JavaSparkContext getJsc() {
        //可以覆盖之前输出的文件夹
        return jsc;
    }

    /**
     * 获取SparkSession
     *
     * @return SparkSession
     */
    public SparkSession getSparkSession() {
        return session;
    }
}
