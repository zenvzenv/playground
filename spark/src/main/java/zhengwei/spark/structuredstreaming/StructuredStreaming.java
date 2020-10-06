package zhengwei.spark.structuredstreaming;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;

import java.util.Arrays;

/**
 * @author zhengwei AKA Awei
 * @since 2020/6/9 11:43
 */
public class StructuredStreaming {
    public static void main(String[] args) throws StreamingQueryException {
        SparkSession spark = SparkSession
                .builder()
                .appName("Test")
                .master("local[2]")
                .getOrCreate();
        Dataset<Row> lines = spark
                .readStream()
                .format("socket")
                .option("host", "192.168.1.225")
                .option("port", 9999)
                .load();
//        System.out.println(lines.schema());
        Dataset<String> words = lines
                .as(Encoders.STRING())
                .flatMap((FlatMapFunction<String, String>) x -> {
                    System.out.println("content -> " + x);
                    return Arrays.asList(x.split(" ")).iterator();
                }, Encoders.STRING());
        System.out.println(words.schema());
        Dataset<Row> wordCounts = words.groupBy("value").count();
        StreamingQuery query = wordCounts
                .writeStream()
                .outputMode("complete")
                .format("console")
                .trigger(Trigger.ProcessingTime(0))
                .start();
        query.awaitTermination();
    }
}
